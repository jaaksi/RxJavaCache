package org.jaaksi.rxcache.util;

import java.util.concurrent.Callable;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * Created by fuchaoyang on 2018/7/1.<br/>
 */
public class RxUtil {

  /**
   * 将Callable转换为Observable
   */
  public static <T> Observable<T> deferObservable(final Callable<T> callable) {
    return Observable.defer(new Func0<Observable<T>>() {

      @Override
      public Observable<T> call() {
        T result;
        try {
          result = callable.call();
        } catch (Exception e) {
          return Observable.error(e);
        }
        return Observable.just(result);
      }
    });
  }

  public static <T> Observable<T> createObservable(final Callable<T> callable) {
    return Observable.create(new Observable.OnSubscribe<T>() {
      @Override
      public void call(Subscriber<? super T> subscriber) {
        try {
          T result = callable.call();
          subscriber.onNext(result);
        } catch (Exception e) {
          subscriber.onError(e);
        }
        subscriber.onCompleted();
      }
    });
  }

  public static <T> Observable.Transformer<T, T> io_main() {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> observable) {
        return observable.subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
      }
    };
  }

  public static <T> Observable.Transformer<T, T> subscribeOn_io() {
    return new Observable.Transformer<T, T>() {
      @Override
      public Observable<T> call(Observable<T> observable) {
        return observable
            .subscribeOn(Schedulers.io())
            .unsubscribeOn(Schedulers.io());
      }
    };
  }
}
