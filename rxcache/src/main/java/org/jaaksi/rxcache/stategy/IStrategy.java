package org.jaaksi.rxcache.stategy;

import org.jaaksi.rxcache.model.CacheResult;
import java.lang.reflect.Type;
import rx.Observable;

/**
 * 缓存策略的接口
 */
public interface IStrategy {

  /**
   * 根据缓存策略处理，返回对应的Observable<CacheResult<T>>
   *  @param cacheKey 缓存的key
   * @param cacheTime 缓存时间
   * @param source 网络请求对象
   */
  <T> Observable<CacheResult<T>> execute(String cacheKey, long cacheTime, Observable<CacheResult<T>> source, Type type);
}
