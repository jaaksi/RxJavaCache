package org.jaaksi.rxcache.func;

import org.jaaksi.rxcache.model.CacheResult;
import rx.functions.Func1;

/**
 * 缓存结果转换
 */
public class CacheResultFunc<T> implements Func1<CacheResult<T>, T> {

  @Override
  public T call(CacheResult<T> result) {
    return result.data;
  }
}
