package org.jaaksi.rxcachedemo.net;

import org.jaaksi.rxcachedemo.model.ApiResponse;
import org.jaaksi.rxcachedemo.model.BannerBean;

import java.util.List;

import retrofit2.http.GET;
import rx.Observable;

public interface Api {
    @GET("banner/json")
    Observable<ApiResponse<List<BannerBean>>> getBanner();
}
