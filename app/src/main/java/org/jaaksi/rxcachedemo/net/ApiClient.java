package org.jaaksi.rxcachedemo.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit sRetrofit;

    static {
        sRetrofit = new Retrofit.Builder().baseUrl("https://www.wanandroid.com/")
            .client(new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).connectTimeout(30, TimeUnit.SECONDS).build())
            .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    }

    /**
     * 创建api Service
     */
    public static <S> S create(Class<S> serviceClass) {
        return sRetrofit.create(serviceClass);
    }
}
