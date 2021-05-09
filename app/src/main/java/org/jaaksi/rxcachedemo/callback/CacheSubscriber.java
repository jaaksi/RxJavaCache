package org.jaaksi.rxcachedemo.callback;

import android.util.Log;

import org.jaaksi.rxcache.exception.NoCacheException;
import org.jaaksi.rxcache.model.CacheResult;
import org.jaaksi.rxcache.util.Utils;

import rx.Subscriber;

/**
 * Created by fuchaoyang on 2019/7/1.<br/>
 *
 * 配合RxCache便捷的Subscriber
 */
public abstract class CacheSubscriber<T> extends Subscriber<CacheResult<T>> {
    private static final String TAG = "CacheSubscriber";

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Log.e(TAG, "onError", e);
        //if (isUnsubscribed()) return;
        onResponse(e instanceof NoCacheException, null);
    }

    public void onNetError() {
        onResponse(false, null);
    }

    public abstract void onResponse(boolean isFromCache, T result);

    @Override
    public void onNext(CacheResult<T> result) {
        //if (isUnsubscribed()) return; // 如果isUnsubscribed，就不会再回调了

        if (result != null) {
            onResponse(result.isFromCache, result.data);
        } else {
            onResponse(false, null);
        }
    }
}