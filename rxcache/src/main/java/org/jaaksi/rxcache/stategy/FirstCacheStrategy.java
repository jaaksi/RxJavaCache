package org.jaaksi.rxcache.stategy;

import org.jaaksi.rxcache.model.CacheResult;

import java.lang.reflect.Type;

import rx.Observable;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 先加载缓存，缓存不存在，再请求网络
 */
public final class FirstCacheStrategy extends BaseStrategy {
    @Override
    public <T> Observable<CacheResult<T>> execute(String cacheKey, long cacheTime, Observable<CacheResult<T>> source, Type type) {
        return this.<T>loadCache(cacheKey, type, true).switchIfEmpty(source);
    }
}
