package org.jaaksi.rxcache.model;

import java.io.Serializable;

/**
 * 实际缓存的类，将传入的data包裹在此类下，用以设置缓存时长等
 */
public class RealEntity<T> implements Serializable {
    /** 缓存有效的时间，以ms为单位 */
    public long duration;

    public T data;

    /** 缓存开始的时间 */
    public long createTime = System.currentTimeMillis();

    public RealEntity(T data, long duration) {
        this.duration = duration;
        this.data = data;
    }
}
