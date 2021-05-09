package org.jaaksi.rxcache.core;

import android.util.Log;

import org.jaaksi.rxcache.RxCache;
import org.jaaksi.rxcache.model.RealEntity;
import org.jaaksi.rxcache.util.Utils;

import java.lang.reflect.Type;

/**
 * <p>描述：缓存核心管理类</p>
 * <p>
 * 1.采用LruDiskCache<br>
 * 2.对Key进行MD5加密<br>
 * <p>
 * <p>
 */
public class CacheCore {
    private static final String TAG = RxCache.TAG;

    private LruDiskCache disk;

    public CacheCore(LruDiskCache disk) {
        this.disk = Utils.checkNotNull(disk, "disk==null");
    }

    /**
     * 读取
     */
    public synchronized <T> RealEntity<T> load(Type type, String key) {
        String cacheKey = encryptKey(key);
        Log.d(TAG, String.format("loadCache  key=%s|%s", key, cacheKey));
        if (disk != null) {
            RealEntity<T> result = disk.load(type, cacheKey);
            if (result != null) {
                if (result.duration == -1 || result.createTime + result.duration > System.currentTimeMillis()) {
                    // 未过期
                    return result;
                }

                // 已过期
                disk.remove(cacheKey);
            }
        }

        return null;
    }

    /**
     * 保存
     */
    public synchronized <T> boolean save(String key, T value) {
        String cacheKey = encryptKey(key);
        Log.d(TAG, String.format("saveCache  key=%s|%s", key, cacheKey));
        return disk.save(cacheKey, value);
    }

    /**
     * 是否包含
     */
    public synchronized boolean containsKey(String key) {
        String cacheKey = encryptKey(key);
        if (disk != null) {
            if (disk.containsKey(cacheKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 删除缓存
     */
    public synchronized boolean remove(String key) {
        String cacheKey = encryptKey(key);
        Log.d(TAG, "removeCache  key=" + cacheKey);
        if (disk != null) {
            return disk.remove(cacheKey);
        }
        return true;
    }

    /**
     * 清空缓存
     */
    public synchronized boolean clear() {
        if (disk != null) {
            return disk.clear();
        }
        return false;
    }

    // encryptKey
    private String encryptKey(String key) {
        return Utils.md5(key);
    }
}
