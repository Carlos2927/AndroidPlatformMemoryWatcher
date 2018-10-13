package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.os.Build;
import android.support.annotation.Keep;
import android.view.accessibility.AccessibilityNodeInfo;

import com.carlos2927.java.memoryleakfixer.Watchable;

import java.util.HashMap;
import java.util.Map;

/**
 * android平台内存泄漏监控，主要提供由Android平台系统类静态持有直接或者检检引用context导致内存泄漏的处理方案
 */
@Keep
public class AndroidPlatformMemoryWatcher {
    static final String TAG = "AndroidMemoryWatcher";
    @Keep
    private static final Map<Class,Watchable> AndroidPlatformCache = new HashMap<>();

    public static void addWatcher(Class cls,Watchable watchable){
        AndroidPlatformCache.put(cls,watchable);
    }

    public static void removeWatcher(Class cls){
        AndroidPlatformCache.remove(cls);
    }

    static {
        if(Build.VERSION.SDK_INT >= AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo.CompatAndroidSDK){
            addWatcher(AccessibilityNodeInfo.class,new AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo());
        }
    }
}
