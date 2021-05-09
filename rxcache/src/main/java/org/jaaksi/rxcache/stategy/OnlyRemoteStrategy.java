package org.jaaksi.rxcache.stategy;

import org.jaaksi.rxcache.model.CacheResult;
import java.lang.reflect.Type;
import rx.Observable;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 只请求网络，但数据依然会被缓存
 */
public /*final*/ class OnlyRemoteStrategy implements IStrategy {
  @Override
  public <T> Observable<CacheResult<T>> execute(String cacheKey, long cacheTime, Observable<CacheResult<T>> source, Type type) {
    return source;
  }
}
