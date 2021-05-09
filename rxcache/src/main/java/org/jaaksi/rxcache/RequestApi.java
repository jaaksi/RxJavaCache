package org.jaaksi.rxcache;

import java.lang.reflect.Type;

import org.jaaksi.rxcache.func.CacheResultFunc;
import org.jaaksi.rxcache.model.CacheResult;
import org.jaaksi.rxcache.stategy.CacheStrategy;
import org.jaaksi.rxcache.stategy.IStrategy;
import org.jaaksi.rxcache.stategy.NoStrategy;
import org.jaaksi.rxcache.type.CacheType;
import org.jaaksi.rxcache.util.RxUtil;

import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by fuchaoyang on 2019/7/1.
 *
 * 用于发送网络请求
 */
public class RequestApi<T> {
    private long cacheTime = RxCache.NEVER_EXPIRE;

    private String cacheKey;

    private ICacheable<T> cacheable;

    private IStrategy cacheStrategy = CacheStrategy.CACHE_AND_REMOTE;

    private final Observable<T> api;

    private RequestApi(Observable<T> api) {
        this.api = api;
    }

    public static <T> RequestApi<T> api(Observable<T> api) {
        return new RequestApi<>(api);
    }

    /**
     * 设置缓存key
     */
    public RequestApi<T> cacheKey(String cacheKey) {
        this.cacheKey = cacheKey;
        return this;
    }

    /**
     * 校验response的有效性，数据有效才进行缓存
     */
    public RequestApi<T> cacheable(ICacheable<T> cacheable) {
        this.cacheable = cacheable;
        return this;
    }

    /**
     * 设置缓存时间
     */
    public RequestApi<T> cacheTime(long cacheTime) {
        if (cacheTime <= 0) cacheTime = RxCache.NEVER_EXPIRE;
        this.cacheTime = cacheTime;
        return this;
    }

    /**
     * 设置缓存策略
     */
    public RequestApi<T> cacheStrategy(IStrategy iStrategy) {
        this.cacheStrategy = iStrategy;
        return this;
    }

//    /**
//     * 不建议使用这个方法，不会自动填充泛型
//     * use {@link #buildCache(CacheType)} instead
//     */
//    @Deprecated
//    public Observable<T> buildCache(Type type) {
//        return doBuildCache(type);
//    }

    /**
     * 构建Observable
     *
     * 建议统一使用{@link #buildCacheWithCacheResult(CacheType)}
     */
    public Observable<T> buildCache(CacheType<T> cacheType) {
        return doBuildCache(cacheType.getType());
    }

//    /**
//     * 不建议使用这个方法，不会自动填充泛型
//     * use {@link #buildCacheWithCacheResult(CacheType)} (CacheType)} instead
//     */
//    @Deprecated
//    public Observable<CacheResult<T>> buildCacheWithCacheResult(Type type) {
//        return doBuildCacheWithCacheResult(type);
//    }

    /**
     * 构建Observable & wrap CacheResult
     */
    public Observable<CacheResult<T>> buildCacheWithCacheResult(CacheType<T> cacheType) {
        return doBuildCacheWithCacheResult(cacheType.getType());
    }

    private Observable.Transformer<CacheResult<T>, CacheResult<T>> transformer(final Type type) {
        return new Observable.Transformer<CacheResult<T>, CacheResult<T>>() {
            @Override
            public Observable<CacheResult<T>> call(Observable<CacheResult<T>> observable) {
                return cacheStrategy.execute(cacheKey, cacheTime, observable, type);
            }
        };
    }

    private Observable<CacheResult<T>> doBuildCacheWithCacheResult(Type type) {
        return api.compose(RxUtil.<T>io_main()).compose(checkRemote()).compose(transformer(type));
    }

    private Observable<T> doBuildCache(Type type) {
        return doBuildCacheWithCacheResult(type).map(new CacheResultFunc<T>());
    }

    // 校验网络请求数据，如果数据有效写入缓存
    private Observable.Transformer<T, CacheResult<T>> checkRemote() {
        return new Observable.Transformer<T, CacheResult<T>>() {
            @Override
            public Observable<CacheResult<T>> call(Observable<T> observable) {
                return observable.map(new Func1<T, CacheResult<T>>() {
                    @Override
                    public CacheResult<T> call(T t) {
                        CacheResult<T> result = new CacheResult<T>(false, t);
                        if (!(cacheStrategy instanceof NoStrategy)) { // 如果缓存策略是不缓存
                            boolean canCache = false;
                            if (cacheable == null) {
                                canCache = t != null;
                            } else if (cacheable.cacheable(t)) {
                                canCache = true;
                            }

                            if (canCache) {
                                result.cacheable = true;
                                writeCache(t);
                            }
                        }

                        return result;
                    }
                });
            }
        };
    }

    private void writeCache(T t) {
        RxCache.rxPut(cacheKey, t).retry(1) // 写入失败重试一次
            .subscribeOn(Schedulers.io()).subscribe();
    }

    public interface ICacheable<T> {
        /**
         * @param data data
         * @return 是否缓存该条数据
         */
        boolean cacheable(T data);
    }
}
