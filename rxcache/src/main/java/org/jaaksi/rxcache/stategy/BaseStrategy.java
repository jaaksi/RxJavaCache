package org.jaaksi.rxcache.stategy;

import org.jaaksi.rxcache.RxCache;
import org.jaaksi.rxcache.model.CacheResult;

import java.lang.reflect.Type;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 缓存策略的base抽象类，提供 load data from cache and remote
 */
public abstract class BaseStrategy implements IStrategy {
    /**
     * 根据不同的策略，设置是否需要 onErrorResumeNext Observable.empty()该抛异常的还是要onError
     *
     * @param emptyOnError 是否需要 onErrorResumeNext Observable.empty()
     */
    <T> Observable<CacheResult<T>> loadCache(String cacheKey, Type type, final boolean emptyOnError) {
        Observable<CacheResult<T>> observable =
            RxCache.<T>rxGetInner(cacheKey, type).flatMap(new Func1<T, Observable<CacheResult<T>>>() {
                @Override
                public Observable<CacheResult<T>> call(T t) {
                    return Observable.just(new CacheResult<>(true, t));
                }
            });
        if (emptyOnError) {
            observable = observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends CacheResult<T>>>() {
                @Override
                public Observable<? extends CacheResult<T>> call(Throwable throwable) {
                    return Observable.empty();
                }
            });
        }

        return observable;
    }
}
