package org.jaaksi.rxcache;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import org.jaaksi.rxcache.converter.GsonCacheConverter;
import org.jaaksi.rxcache.type.CacheType;
import org.jaaksi.rxcache.core.CacheCore;
import org.jaaksi.rxcache.core.LruDiskCache;
import org.jaaksi.rxcache.exception.NoCacheException;
import org.jaaksi.rxcache.model.RealEntity;

import java.io.File;
import java.lang.reflect.Type;

import rx.Observable;
import rx.Subscriber;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 基于https://github.com/HolenZhou/rxcache重构
 *
 * 缓存类，支持普通操作及rx操作<br>
 * 支持设置缓存磁盘大小、缓存key、缓存时间、缓存存储的转换器、缓存目录、缓存Version<br>
 */
public final class RxCache {
    public static final String TAG = "RxCache";

    public static final int NEVER_EXPIRE = -1;                    //缓存过期时间，默认永久缓存

    private static final long MAX_CACHE_SIZE = 50 * 1024 * 1024L; // 50MB

    private static CacheCore cacheCore;                                  //缓存的核心管理类

    private RxCache() {
    }

    public static void initialize(Context context) {
        initialize(new File(context.getExternalCacheDir(), "rxcache"), 1, MAX_CACHE_SIZE, new GsonCacheConverter(new Gson()));
    }

    public static void initialize(File cacheDir) {
        initialize(cacheDir, 1, MAX_CACHE_SIZE, new GsonCacheConverter(new Gson()));
    }

    /**
     * 初始化
     *
     * @param cacheDir       缓存目录
     * @param cacheVersion   缓存版本
     * @param maxCacheSize   缓存最大size
     * @param cacheConverter 缓存Converter
     */
    public static void initialize(File cacheDir, int cacheVersion, long maxCacheSize, GsonCacheConverter cacheConverter) {
        cacheCore = new CacheCore(new LruDiskCache(cacheConverter, cacheDir, cacheVersion, maxCacheSize));
    }

    public static boolean containsKey(String key) {
        return cacheCore.containsKey(key);
    }

    /**
     * 同步获取缓存
     */
    public static <T> T get(String key, Class<T> clazz) {
        return get(key, (Type) clazz);
    }

    /**
     * 同步获取缓存
     */
    public static <T> T get(String key, CacheType<T> cacheType) {
        return get(key, cacheType.getType());
    }

    /**
     * 同步获取缓存
     */
    public static <T> T get(String key, Type type) {
        RealEntity<T> result = cacheCore.load(type, key);
        if (result != null) {
            return result.data;
        }
        return null;
    }

    /**
     * 通过Rx的方式获取缓存，返回一个Observable
     *
     * @param key   缓存key
     * @param clazz 保存的类型
     */
    public static <T> Observable<T> rxGet(String key, Class<T> clazz) {
        return rxGet(key, (Type) clazz);
    }

    public static <T> Observable<T> rxGet(String key, CacheType<T> cacheType) {
        return rxGet(key, cacheType.getType());
    }

    /**
     * 通过Rx的方式获取缓存，返回一个Observable
     *
     * @param type 保存的类型
     * @param key  缓存key
     */
    public static <T> Observable<T> rxGet(final String key, final Type type) {
        // 缓存会空的时候回抛出异常，这里要捕获住
        return RxCache.<T>rxGetInner(key, type).onErrorReturn(new Func1<Throwable, T>() {
            @Override
            public T call(Throwable throwable) {
                return null;
            }
        });
    }

    /**
     * 仅供内部调用，外部调用rxGet方法
     */
    public static <T> Observable<T> rxGetInner(final String key, final Type type) {
        return Observable.create(new SimpleSubscribe<T>() {
            @Override
            T execute() throws Throwable {
                return get(key, type);
            }
        });
    }

    /**
     * 同步保存
     */
    public static <T> boolean put(String key, T value) {
        return put(key, value, NEVER_EXPIRE);
    }

    /**
     * 同步保存
     *
     * @param cacheTime 毫秒ms
     */
    public static <T> boolean put(String key, T value, long cacheTime) {
        if (cacheTime <= 0) {
            cacheTime = NEVER_EXPIRE;
        }
        RealEntity<T> entity = new RealEntity<>(value, cacheTime);
        return cacheCore.save(key, entity);
    }

    /**
     * 通过Rx的方式保存，返回一个Observable
     */
    public static <T> Observable<Boolean> rxPut(final String key, final T value) {
        return rxPut(key, value, NEVER_EXPIRE);
    }

    /**
     * 通过Rx的方式保存，返回一个Observable
     *
     * @param cacheTime ms
     */
    public static <T> Observable<Boolean> rxPut(final String key, final T value, final long cacheTime) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                long time;
                if (cacheTime <= 0) {
                    time = NEVER_EXPIRE;
                } else {
                    time = cacheTime;
                }
                RealEntity<T> entity = new RealEntity<>(value, time);
                return cacheCore.save(key, entity);
            }
        });
    }

    /**
     * 同步删除缓存
     */
    public static boolean remove(final String key) {
        return cacheCore.remove(key);
    }

    /**
     * rx remove
     */
    public static Observable<Boolean> rxRemove(final String key) {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return cacheCore.remove(key);
            }
        });
    }

    /**
     * 异步移除缓存（key）
     */
    public static void removeAsync(String key) {
        rxRemove(key).subscribeOn(Schedulers.io()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "clearCache err!!!");
            }

            @Override
            public void onNext(Boolean aBoolean) {
                Log.i(TAG, "clearCache success!!!");
            }
        });
    }

    /**
     * 清空缓存
     */
    public static boolean clear() {
        return cacheCore.clear();
    }

    public static Observable<Boolean> rxClear() {
        return Observable.create(new SimpleSubscribe<Boolean>() {
            @Override
            Boolean execute() throws Throwable {
                return cacheCore.clear();
            }
        });
    }

    /**
     * 异步清空缓存
     */
    public static void clearAsync() {
        rxClear().subscribeOn(Schedulers.io()).subscribe(new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Log.i(TAG, "clearCache err!!!");
            }

            @Override
            public void onNext(Boolean aBoolean) {
                Log.i(TAG, "clearCache success!!!");
            }
        });
    }

    private static abstract class SimpleSubscribe<T> implements Observable.OnSubscribe<T> {
        @Override
        public final void call(Subscriber<? super T> subscriber) {
            try {
                T data = execute();
                if (!subscriber.isUnsubscribed()) {
                    // 不会发射null数据，所以onNext回调可以不用判空
                    if (data instanceof RealEntity) { // get的时候对象是RealEntity，如果为null也会走下面的
                        if (((RealEntity) data).data != null) {
                            subscriber.onNext(data);
                        } else {
                            subscriber.onError(new NoCacheException());
                        }
                    } else {
                        if (data != null) {
                            subscriber.onNext(data);
                        } else {
                            subscriber.onError(new NoCacheException());
                        }
                    }
                }
            } catch (Throwable e) { // 如果发生异常，通知订阅者
                Exceptions.throwIfFatal(e); // 抛出致命异常
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onError(e); // 如果操作发生异常，也会正常抛出异常
                }
                return;
            }

            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        }

        abstract T execute() throws Throwable;
    }
}
