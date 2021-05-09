package org.jaaksi.rxcache.core;

import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

import org.jaaksi.rxcache.converter.ICacheConverter;
import org.jaaksi.rxcache.model.RealEntity;
import org.jaaksi.rxcache.util.Utils;

/**
 * 磁盘缓存实现类
 */
public class LruDiskCache extends BaseCache {
    private ICacheConverter mDiskConverter;

    private DiskLruCache mDiskLruCache;

    private File diskDir;

    private int appVersion;

    private long diskMaxSize;

    public LruDiskCache(ICacheConverter diskConverter, File diskDir, int appVersion, long diskMaxSize) {
        this.mDiskConverter = Utils.checkNotNull(diskConverter, "cacheConverter ==null");
        this.diskDir = diskDir;
        this.appVersion = appVersion;
        this.diskMaxSize = diskMaxSize;
    }

    private DiskLruCache getDiskLruCache() {
        if (mDiskLruCache == null) {
            try {
                mDiskLruCache = DiskLruCache.open(diskDir, appVersion, 1, diskMaxSize);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mDiskLruCache;
    }

    @Override
    protected <T> RealEntity<T> doLoad(Type type, String key) {
        if (getDiskLruCache() == null) {
            return null;
        }
        try {
            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit == null) {
                return null;
            }

            InputStream source = edit.newInputStream(0);
            RealEntity<T> value;
            if (source != null) {
                value = mDiskConverter.load(source, type);
                Utils.close(source);
                edit.commit();
                return value;
            }
            edit.abort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected <T> boolean doSave(String key, T value) {
        if (getDiskLruCache() == null) {
            return false;
        }
        try {
            DiskLruCache.Editor edit = mDiskLruCache.edit(key);
            if (edit == null) {
                return false;
            }
            OutputStream sink = edit.newOutputStream(0);
            if (sink != null) {
                boolean result = mDiskConverter.writer(sink, value);
                Utils.close(sink);
                edit.commit();
                return result;
            }
            edit.abort();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean doContainsKey(String key) {
        if (getDiskLruCache() == null) {
            return false;
        }
        try {
            return mDiskLruCache.get(key) != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean doRemove(String key) {
        if (getDiskLruCache() == null) {
            return false;
        }
        try {
            return mDiskLruCache.remove(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean doClear() {
        if (getDiskLruCache() == null) {
            return false;
        }

        boolean statu = false;
        try {
            mDiskLruCache.delete();
            mDiskLruCache = null;
            statu = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return statu;
    }
}
