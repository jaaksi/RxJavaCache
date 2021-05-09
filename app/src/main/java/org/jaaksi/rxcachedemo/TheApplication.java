package org.jaaksi.rxcachedemo;

import android.app.Application;

import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.File;

import org.jaaksi.rxcache.RxCache;

public class TheApplication extends Application {
    private RefWatcher refWatcher;

    private static TheApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        RxCache.initialize(this);
        refWatcher = setupLeakCanary();
    }

    private RefWatcher setupLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return RefWatcher.DISABLED;
        }
        return LeakCanary.install(this);
    }

    public static TheApplication getInstance() {
        return instance;
    }

    public static RefWatcher getRefWatcher() {
        return getInstance().refWatcher;
    }
}
