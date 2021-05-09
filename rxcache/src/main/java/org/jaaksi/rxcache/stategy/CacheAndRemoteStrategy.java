package org.jaaksi.rxcache.stategy;

import com.google.gson.Gson;

import java.lang.reflect.Type;

import org.jaaksi.rxcache.model.CacheResult;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 先加载缓存（成功才会回调缓存response），不管缓存什么结果都会再请求网络。<br/>
 * 如果缓存成功，网络请求数据无效，则网络不回调<br/>
 * 如果缓存成功，网络也成功，且网络和缓存数据相同则只有缓存回调，网络不再二次回调，否则会二次回调<br/>
 */
public class CacheAndRemoteStrategy extends BaseStrategy {
    private final Gson gson = new Gson();

    @Override
    public <T> Observable<CacheResult<T>> execute(String cacheKey, long cacheTime, Observable<CacheResult<T>> source, Type type) {
        final Observable<CacheResult<T>> cache = loadCache(cacheKey, type, true);

        // 如果有缓存，网络失败，则不回调网络
        return cache.concatWith(source.flatMap(new Func1<CacheResult<T>, Observable<CacheResult<T>>>() {
            @Override
            public Observable<CacheResult<T>> call(CacheResult<T> result) {
                if (result.cacheable) {
                    return Observable.just(result);
                }
                // 如果网络数据是无效的，缓存也是无效的，则抛出网络的结果
                return cache.switchIfEmpty(Observable.just(result));
            }
        })).onErrorResumeNext(new Func1<Throwable, Observable<? extends CacheResult<T>>>() {
            @Override
            public Observable<? extends CacheResult<T>> call(final Throwable throwable) {
                return cache.switchIfEmpty(Observable.create(new Observable.OnSubscribe<CacheResult<T>>() {
                    @Override
                    public void call(Subscriber<? super CacheResult<T>> subscriber) {
                        subscriber.onError(throwable);
                    }
                }));
            }
        }).distinct(new Func1<CacheResult<T>, String>() {
            @Override
            public String call(CacheResult<T> result) {
                if (result.data == null) return null;
                if (!result.isFromCache && !result.cacheable) { // 如果网络数据是无效的
                    return null;
                }

                return toJson(result.data);
            }
        });
    }

   protected String toJson(Object object){
       return gson.toJson(object).hashCode() + "";
   }
}
