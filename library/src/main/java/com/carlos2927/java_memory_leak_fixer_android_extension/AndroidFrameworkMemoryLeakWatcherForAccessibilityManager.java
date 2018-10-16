package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.os.Build;
import android.os.Handler;
import android.util.ArrayMap;
import android.view.accessibility.AccessibilityManager;

import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;
import com.carlos2927.java.memoryleakfixer.Watchable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  解决android.view.accessibility.AccessibilityManager.mAccessibilityStateChangeListeners引发的内存泄漏问题
 */
public class AndroidFrameworkMemoryLeakWatcherForAccessibilityManager implements Watchable {
    AccessibilityManager accessibilityManager;
    Field field_mAccessibilityStateChangeListeners;
    List<AccessibilityManager.AccessibilityStateChangeListener> tempList = new ArrayList<>();
    private boolean isAccessibilityStateChangeListenerMemoryLeak(AccessibilityManager.AccessibilityStateChangeListener accessibilityStateChangeListener){
        //https://www.jianshu.com/p/a798408bfb6f
        return false;
    }
    @Override
    public void watch() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ){
            return;
        }
        if(accessibilityManager == null){
            try {
                accessibilityManager = (AccessibilityManager) JavaReflectUtils.getField(AccessibilityManager.class,"sInstance").get(null);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if(accessibilityManager != null){
            field_mAccessibilityStateChangeListeners = JavaReflectUtils.getField(AccessibilityManager.class,"mAccessibilityStateChangeListeners");
            if(field_mAccessibilityStateChangeListeners != null){
                Object  mAccessibilityStateChangeListeners = null;
                try {
                    mAccessibilityStateChangeListeners = field_mAccessibilityStateChangeListeners.get(accessibilityManager);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if(mAccessibilityStateChangeListeners instanceof CopyOnWriteArrayList){
                    CopyOnWriteArrayList<AccessibilityManager.AccessibilityStateChangeListener> list = (CopyOnWriteArrayList) mAccessibilityStateChangeListeners;
                    for(AccessibilityManager.AccessibilityStateChangeListener accessibilityStateChangeListener:list){
                        if(isAccessibilityStateChangeListenerMemoryLeak(accessibilityStateChangeListener)){
                            tempList.add(accessibilityStateChangeListener);
                        }
                    }
                    if(tempList.size()>0){
                        list.removeAll(tempList);
                        tempList.clear();
                    }
                }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if(mAccessibilityStateChangeListeners instanceof ArrayMap){
                        ArrayMap arrayMap = (ArrayMap) mAccessibilityStateChangeListeners;
                        Set<AccessibilityManager.AccessibilityStateChangeListener> keySet =  arrayMap.keySet();
                        for(AccessibilityManager.AccessibilityStateChangeListener key:keySet){
                            if(isAccessibilityStateChangeListenerMemoryLeak(key)){
                                tempList.add(key);
                            }
                        }
                        if(tempList.size()>0){
                            arrayMap.removeAll(tempList);
                            tempList.clear();
                        }
                    }
                }
            }
        }
    }
}
