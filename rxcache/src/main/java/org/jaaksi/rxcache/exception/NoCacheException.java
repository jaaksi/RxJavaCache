package org.jaaksi.rxcache.exception;

/**
 * 没有找到对应缓存
 */
public class NoCacheException extends RuntimeException {

  public NoCacheException() {
    super("cache is null");
  }
}
