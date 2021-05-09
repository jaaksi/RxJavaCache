package org.jaaksi.rxcachedemo;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jaaksi.rxcache.RequestApi;
import org.jaaksi.rxcache.RxCache;
import org.jaaksi.rxcache.stategy.CacheStrategy;
import org.jaaksi.rxcache.type.CacheType;
import org.jaaksi.rxcachedemo.callback.CacheSubscriber;
import org.jaaksi.rxcachedemo.model.ApiResponse;
import org.jaaksi.rxcachedemo.model.BannerBean;
import org.jaaksi.rxcachedemo.net.Api;
import org.jaaksi.rxcachedemo.net.ApiClient;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_add).setOnClickListener(view -> {
            RxCache.put("url", "111");
            BannerBean bean = new BannerBean();
            bean.desc = "享学~";
            bean.title = "老板说要加点功能。。。";
            RxCache.put("data", bean);
        });

        findViewById(R.id.btn_get).setOnClickListener(view -> {
            RxCache.rxGet("url", String.class).subscribe(new Action1<String>() {
                @Override
                public void call(String s) {
                    Toast.makeText(MainActivity.this, String.format("url=%s", s), Toast.LENGTH_SHORT).show();
                }
            });//rxGet内部应该消化，没有缓存的时候，返回数据null即可
            RxCache.rxGet("data", BannerBean.class).subscribe(new Action1<BannerBean>() {
                @Override
                public void call(BannerBean bean) {
                    Toast.makeText(MainActivity.this, bean.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        });
        findViewById(R.id.btn_clear).setOnClickListener(view -> {
            RxCache.clearAsync();
        });
        findViewById(R.id.btn_request).setOnClickListener(view -> {
            request();
        });
    }

    private void request() {
        RequestApi.api(ApiClient.create(Api.class).getBanner())
            .cacheKey("banner2")
            .cacheStrategy(CacheStrategy.CACHE_AND_REMOTE)
            .cacheable(data -> data.hasData())
            .buildCacheWithCacheResult(new CacheType<ApiResponse<List<BannerBean>>>() {})
            .subscribe(new CacheSubscriber<ApiResponse<List<BannerBean>>>() {
                @Override
                public void onResponse(boolean isFromCache, ApiResponse<List<BannerBean>> result) {
                    ((TextView) findViewById(R.id.textview)).setText(new Gson().toJson(result.data));
                    Toast.makeText(MainActivity.this, "来自" + (isFromCache ? "缓存" : "网络"), Toast.LENGTH_SHORT).show();
                }
            });
    }
}
