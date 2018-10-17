package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;

import com.carlos2927.java.memoryleakfixer.InnerClassHelper;
import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;
import com.carlos2927.java_memory_leak_fixer_android_extension.AndroidFrameworkStaticFiledWatcher;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *  解决android.view.accessibility.AccessibilityManager.mAccessibilityStateChangeListeners,mHighTextContrastStateChangeListeners引发的内存泄漏问题
 */
public class AndroidFrameworkMemoryLeakWatcherForAccessibilityManager extends AndroidFrameworkStaticFiledWatcher {
    final Field NO_Field = Byte.class.getFields()[0];
    AccessibilityManager accessibilityManager;
    List tempList = new ArrayList<>();
    Field field_contentViewCore_mContainerView = NO_Field;
    Class cls_contentViewCore = Object.class;
    Activity NoFindActivity = new Activity();
    Class cls_ViewRootImpl  = Object.class;
    Class cls_ViewRootImpl$AccessibilityInteractionConnectionManager = null;
    Field field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0;
    Class cls_ViewRootImpl$HighContrastTextManager = Object.class;
    Field field_ViewRootImpl$HighContrastTextManager_this$0;
    Field field_ViewRootImpl_mContext;
    Field field_mHighTextContrastStateChangeListeners = NO_Field;
    private AndroidFrameworkMemoryLeakWatcherForAccessibilityManager(){

    }
    private static AndroidFrameworkStaticFiledWatcher Instance;
    public static AndroidFrameworkStaticFiledWatcher getInstance(){
        synchronized (AndroidFrameworkStaticFiledWatcher.class){
            if(Instance == null){
                Instance = new AndroidFrameworkMemoryLeakWatcherForAccessibilityManager();
            }
        }
        return Instance;
    }

    private boolean isHighTextContrastChangeListenerMemoryLeak(Object highTextContrastChangeListener ){
        Activity activity = NoFindActivity;
        if(View.class.isInstance(highTextContrastChangeListener)){
            activity = InnerClassHelper.getActivityFromContext(((View)highTextContrastChangeListener).getContext());
        }
        if(activity == NoFindActivity && cls_ViewRootImpl$HighContrastTextManager  == Object.class){
            try {
                if(cls_ViewRootImpl  == Object.class){
                    cls_ViewRootImpl = Class.forName("android.view.ViewRootImpl");
                }
            } catch (Exception e) {
                e.printStackTrace();
                cls_ViewRootImpl = null;
            }
            if(cls_ViewRootImpl != null){
                try {
                    cls_ViewRootImpl$HighContrastTextManager = Class.forName("android.view.ViewRootImpl$HighContrastTextManager");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(cls_ViewRootImpl$HighContrastTextManager != null){
                if(field_ViewRootImpl$HighContrastTextManager_this$0 == null){
                    List<Field> fields = InnerClassHelper.getSyntheticFields(cls_ViewRootImpl$HighContrastTextManager);
                    for(Field f:fields){
                        if(f.getType() == cls_ViewRootImpl){
                            field_ViewRootImpl$HighContrastTextManager_this$0 = f;
                            field_ViewRootImpl$HighContrastTextManager_this$0.setAccessible(true);
                            break;
                        }
                    }
                }
            }
            if(cls_ViewRootImpl != null && field_ViewRootImpl_mContext == null){
                field_ViewRootImpl_mContext = JavaReflectUtils.getField(cls_ViewRootImpl,"mContext");
            }
        }
        if(field_ViewRootImpl$HighContrastTextManager_this$0 != null && cls_ViewRootImpl$HighContrastTextManager.isInstance(highTextContrastChangeListener)){
            try {
                Object viewRootImpl = field_ViewRootImpl$HighContrastTextManager_this$0.get(highTextContrastChangeListener);
                if(field_ViewRootImpl_mContext != null){
                    Context context = (Context) field_ViewRootImpl_mContext.get(viewRootImpl);
                    activity = InnerClassHelper.getActivityFromContext(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(activity != NoFindActivity && activity != null){
            return InnerClassHelper.isActivityDestroyed(activity);
        }
        return false;
    }
    private boolean isAccessibilityStateChangeListenerMemoryLeak(AccessibilityManager.AccessibilityStateChangeListener accessibilityStateChangeListener){
        Activity activity = NoFindActivity;
        if(View.class.isInstance(accessibilityStateChangeListener)){
            activity = InnerClassHelper.getActivityFromContext(((View)accessibilityStateChangeListener).getContext());
        }

        if(activity == NoFindActivity && cls_contentViewCore == Object.class){
            //https://www.jianshu.com/p/a798408bfb6f
            try {
                cls_contentViewCore = Class.forName("org.chromium.content.browser.ContentViewCore");
            } catch (Exception e) {
                e.printStackTrace();
                cls_contentViewCore = null;
            }
            if(cls_contentViewCore != null){
                if(field_contentViewCore_mContainerView != null && !ViewGroup.class.isAssignableFrom(field_contentViewCore_mContainerView.getType()) ){
                    field_contentViewCore_mContainerView = JavaReflectUtils.getField(cls_contentViewCore,"mContainerView");
                }
            }
        }
        if(field_contentViewCore_mContainerView != null && cls_contentViewCore != null){
            if(cls_contentViewCore.isInstance(accessibilityStateChangeListener)){
                try {
                    ViewGroup mContainerView = (ViewGroup) field_contentViewCore_mContainerView.get(accessibilityStateChangeListener);
                    activity = InnerClassHelper.getActivityFromContext(mContainerView.getContext());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        if(activity == NoFindActivity && cls_ViewRootImpl  == Object.class){
            try {
                cls_ViewRootImpl = Class.forName("android.view.ViewRootImpl");
            } catch (Exception e) {
                e.printStackTrace();
                cls_ViewRootImpl = null;
            }
            if(cls_ViewRootImpl != null){
                try {
                    cls_ViewRootImpl$AccessibilityInteractionConnectionManager = Class.forName("android.view.ViewRootImpl$AccessibilityInteractionConnectionManager");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(cls_ViewRootImpl$AccessibilityInteractionConnectionManager != null){
                if(field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0 == null){
                    List<Field> fields = InnerClassHelper.getSyntheticFields(cls_ViewRootImpl$AccessibilityInteractionConnectionManager);
                    for(Field f:fields){
                        if(f.getType() == cls_ViewRootImpl){
                            field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0 = f;
                            field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0.setAccessible(true);
                            break;
                        }
                    }
                }
            }
            if(cls_ViewRootImpl != null){
                field_ViewRootImpl_mContext = JavaReflectUtils.getField(cls_ViewRootImpl,"mContext");
            }
        }
        if(field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0 != null && cls_ViewRootImpl$AccessibilityInteractionConnectionManager.isInstance(accessibilityStateChangeListener)){
            try {
                Object viewRootImpl = field_ViewRootImpl$AccessibilityInteractionConnectionManager_this$0.get(accessibilityStateChangeListener);
                if(field_ViewRootImpl_mContext != null){
                    Context context = (Context) field_ViewRootImpl_mContext.get(viewRootImpl);
                    activity = InnerClassHelper.getActivityFromContext(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(activity != NoFindActivity && activity != null){
            return InnerClassHelper.isActivityDestroyed(activity);
        }
        return false;
    }

    private void watch_mAccessibilityStateChangeListeners(){
        Field field_mAccessibilityStateChangeListeners = JavaReflectUtils.getField(AccessibilityManager.class,"mAccessibilityStateChangeListeners");
        if(field_mAccessibilityStateChangeListeners != null){
            Object mAccessibilityStateChangeListeners = null;
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



    private void watch_mHighTextContrastStateChangeListeners(){
        if(field_mHighTextContrastStateChangeListeners == NO_Field){
            field_mHighTextContrastStateChangeListeners = JavaReflectUtils.getField(AccessibilityManager.class,"");
        }
        if(field_mHighTextContrastStateChangeListeners != null){
            Object mHighTextContrastStateChangeListeners = null;
            try {
                mHighTextContrastStateChangeListeners = field_mHighTextContrastStateChangeListeners.get(accessibilityManager);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            if(mHighTextContrastStateChangeListeners instanceof CopyOnWriteArrayList){
                CopyOnWriteArrayList list = (CopyOnWriteArrayList) mHighTextContrastStateChangeListeners;
                for(Object accessibilityStateChangeListener:list){
                    if(isHighTextContrastChangeListenerMemoryLeak(accessibilityStateChangeListener)){
                        tempList.add(accessibilityStateChangeListener);
                    }
                }
                if(tempList.size()>0){
                    list.removeAll(tempList);
                    tempList.clear();
                }
            }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if(mHighTextContrastStateChangeListeners instanceof ArrayMap){
                    ArrayMap arrayMap = (ArrayMap) mHighTextContrastStateChangeListeners;
                    Set keySet =  arrayMap.keySet();
                    for(Object key:keySet){
                        if(isHighTextContrastChangeListenerMemoryLeak(key)){
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

    @Override
    public void watch() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1 ){
            return;
        }
        if(!checkNeedWatch()){
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
            // fix mAccessibilityStateChangeListeners memory leak
            watch_mAccessibilityStateChangeListeners();
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                // fix mHighTextContrastStateChangeListeners memory leak
                watch_mHighTextContrastStateChangeListeners();
            }
        }
    }
}
