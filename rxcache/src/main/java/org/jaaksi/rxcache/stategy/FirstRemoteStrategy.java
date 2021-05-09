package org.jaaksi.rxcache.stategy;

import org.jaaksi.rxcache.model.CacheResult;
import java.lang.reflect.Type;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 优先请求网络，网络数据无效后，再加载缓存（如果缓存也没有，则会响应网络的response or error）
 */
public final class FirstRemoteStrategy extends BaseStrategy {
  @Override
  public <T> Observable<CacheResult<T>> execute(String cacheKey, long cacheTime, Observable<CacheResult<T>> source, final Type type) {

    final Observable<CacheResult<T>> cache = loadCache(cacheKey, type, true);
    return source.flatMap(new Func1<CacheResult<T>, Observable<CacheResult<T>>>() {
      @Override
      public Observable<CacheResult<T>> call(CacheResult<T> result) {
        if (result.cacheable) { // 如果数据有效则正常处理
          return Observable.just(result);
        }
        // 如果网络数据是无效的，缓存也是无效的，则抛出网络的结果
        return cache.switchIfEmpty(Observable.just(result));
      }
    })
        // 如果网络请求过程中发生了异常，则取缓存，如果没有缓存，则把网络中的异常抛出去
        .onErrorResumeNext(new Func1<Throwable, Observable<? extends CacheResult<T>>>() {
          @Override
          public Observable<? extends CacheResult<T>> call(final Throwable throwable) {
            return cache.switchIfEmpty(
                Observable.create(new Observable.OnSubscribe<CacheResult<T>>() {
                  @Override
                  public void call(Subscriber<? super CacheResult<T>> subscriber) {
                    subscriber.onError(throwable);
                  }
                }));
          }
        });
  }
}
