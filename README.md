一款基于RxJava+DiskLruCache实现的本地缓存库，支持根据策略自动处理网络数据缓存。<br/>

该库是基于[RxCache](https://github.com/HolenZhou/rxcache)的二次封装。

[toc]

## 简介
RxCache是一个本地缓存功能库，采用Rxjava+DiskLruCache来实现，线程安全内部采用ReadWriteLock机制防止频繁读写缓存造成的异常，可以独立使用，单独用RxCache来存储数据。也可以采用transformer与retrofit网络请求结合，让你的网络库实现网络缓存功能，而且支持适用于不同业务场景的六种缓存模式。

## 历史
### 原库
[RxEasyHttp](https://github.com/zhou-you/RxEasyHttp)中的[缓存模块RxCache](https://github.com/zhou-you/RxEasyHttp/wiki/cache)。

### HolenZhou改动
HolenZhou [RxCache](https://github.com/HolenZhou/rxcache)基于该库做了主要两个改动：
- wrap了一层RealEntity（用来在put的时候记录缓存时间。原库是通过loadCache的时候设置时间，根据文件修改时间来判断缓存是否有效)
- RxCache中添加了一些同步操作的api

### 重构
- 更改RxJava2为RxJava1
- 增加是否对数据进行缓存的条件校验，实现真正的缓存策略
- 设置缓存策略去除枚举类型限制，支持设置自定义缓存策略
- 删除RxCache中没有必要的字段CacheKey和CacheTime
- buildCacheXxx方法自动填充泛型
- 提供CacheSubscriber方便配合缓存使用
- 删除RxCacheProvider
- 修改RxCache中的方法为静态方法，全局使用一个
- 优化删除多余的CacheType子类（更改CacheType为抽象类）等细节修改

## 关键类介绍
### RxCache
缓存核心类

### RequestApi
用于配置网络请求，写入缓存

### CacheSubscriber
配合RxCache便捷的Subscriber

### CacheStrategy
内部提供的6中缓存策略

## API
### 初始化
使用前必须先进行初始化操作。
```
RxCache.initialize(context);

/**
 * 初始化
 *
 * @param cacheDir       缓存目录
 * @param cacheVersion   缓存版本
 * @param maxCacheSize   缓存最大size
 * @param cacheConverter 缓存Converter
 */
public static void initialize(File cacheDir, int cacheVersion, long maxCacheSize, GsonCacheConverter cacheConverter) {
    RxCache.cacheDir = cacheDir;
    RxCache.cacheVersion = cacheVersion;
    RxCache.maxCacheSize = maxCacheSize;
    RxCache.cacheConverter = cacheConverter;
}
```

### 写入与读取缓存
```
RxCache.put("url", "111");
BannerBean bean = new BannerBean();
bean.desc = "享学~";
bean.title = "老板说要加点功能。。。";
RxCache.put("data", bean);

// 移除某缓存
RxCache.remove("url");

// 清除全部缓存
RxCache.clearAsync();
```

### 缓存策略
定义了IStrategy接口，框架内部提供了6中缓存策略，支持自定义。

缓存策略 | 说明
---|---
NO_CACHE | 不使用RxCache进行缓存
ONLY_REMOTE | 只请求网络，但数据依然会被缓存
ONLY_CACHE |  只加载缓存，如离线模式
FIRST_REMOTE | 优先请求网络，网络数据无效后，再加载缓存<br/>（如果缓存也没有，则会响应网络的response or error）
FIRST_CACHE | 优先加载缓存，缓存没有再去请求网络
CACHE_AND_REMOTE | 先加载缓存（成功才会回调缓存response），不管缓存什么结果都会再请求网络。<br/>如果缓存成功，网络请求数据无效，则网络不回调。<br/>如果缓存成功，网络也成功，且网络和缓存数据相同则只有缓存回调，网络不再二次回调，否则会二次回调


### 网络请求
```
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
```
## Thanks
- [RxEasyHttp](https://github.com/zhou-you/RxEasyHttp)
- [HolenZhou rxcache](https://github.com/HolenZhou/rxcache)
