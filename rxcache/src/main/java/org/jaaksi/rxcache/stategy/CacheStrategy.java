package org.jaaksi.rxcache.stategy;

/**
 * Created by fuchaoyang on 2019/7/8.<br/>
 *
 * 网络请求缓存策略常量
 */
public class CacheStrategy {

  /**
   * 不使用RxCache
   **/
  public static final IStrategy NO_CACHE = new NoStrategy();

  /**
   * 只请求网络，但数据依然会被缓存
   */
  public static final IStrategy ONLY_REMOTE = new OnlyRemoteStrategy();

  /**
   * 只加载缓存
   */
  public static final IStrategy ONLY_CACHE = new OnlyCacheStrategy();

  /**
   * 优先请求网络，网络数据无效后，再加载缓存（如果缓存也没有，则会响应网络的response or error）
   */
  public static final IStrategy FIRST_REMOTE = new FirstRemoteStrategy();

  /**
   * 优先加载缓存，缓存没有再去请求网络
   */
  public static final IStrategy FIRST_CACHE = new FirstCacheStrategy();

  /**
   * 先加载缓存（成功才会回调缓存response），不管缓存什么结果都会再请求网络。
   * 如果缓存成功，网络请求数据无效，则网络不回调
   * 如果缓存成功，网络也成功，且网络和缓存数据相同则只有缓存回调，网络不再二次回调，否则会二次回调
   */
  public static final IStrategy CACHE_AND_REMOTE = new CacheAndRemoteStrategy();
}
