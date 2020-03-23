package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.app.Activity;
import android.os.Build;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.carlos2927.java.memoryleakfixer.InnerClassHelper;
import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决由android.view.accessibility.AccessibilityNodeInfo#sPool静态持有间接引用Context的变量引发的内存泄漏
 */
public class AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo extends AndroidFrameworkStaticFiledWatcher {
    public static final int CompatAndroidSDK = Build.VERSION_CODES.JELLY_BEAN;
    Class cls;
    Class cls_Editor$UndoInputFilter;
    Class cls_Editor;
    Class cls_TextView$ChangeWatcher;
    List<AccessibilityNodeInfo> accessibilityNodeInfoList = new ArrayList<>();
    private AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo(){

    }
    private static AndroidFrameworkStaticFiledWatcher Instance;
    public static AndroidFrameworkStaticFiledWatcher getInstance(){
        synchronized (AndroidFrameworkStaticFiledWatcher.class){
            if(Instance == null){
                Instance = new AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo();
            }
        }
        return Instance;
    }
    private CharSequence getOriginalText(AccessibilityNodeInfo accessibilityNodeInfo){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Field field = JavaReflectUtils.getField(AccessibilityNodeInfo.class,"mOriginalText");
            if(field != null){
                try {
                    return (CharSequence) field.get(accessibilityNodeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Method method = JavaReflectUtils.getMethod(AccessibilityNodeInfo.class,"getOriginalText");
            if(method != null){
                try {
                    return (CharSequence) method.invoke(accessibilityNodeInfo);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
//            accessibilityNodeInfo.getText();
            watchOnlyOnce(); // 在高版本系统中获取不到mOriginalText时就不再监测了
        }else {
            return accessibilityNodeInfo.getText();
        }
        return null;
    }

    boolean isNeedRelease(CharSequence charSequence){
        if(cls == null){
            try {
                cls = Class.forName("android.text.method.ReplacementTransformationMethod$SpannedReplacementCharSequence");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if(cls.isInstance(charSequence)){
            try {
                Spanned spanned = (Spanned) JavaReflectUtils.getField(cls,"mSpanned").get(charSequence);
                if(spanned instanceof SpannableStringBuilder){
                    Activity activity = null;
                    SpannableStringBuilder spannableStringBuilder = (SpannableStringBuilder) spanned;
                    InputFilter[] filters = spannableStringBuilder.getFilters();
                    if(filters != null){
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
                            if(cls_Editor$UndoInputFilter == null){
                                try {
                                    cls_Editor$UndoInputFilter = Class.forName("android.widget.Editor$UndoInputFilter");
                                    for(InputFilter inputFilter:filters){
                                        if(cls_Editor$UndoInputFilter.isInstance(inputFilter)){
                                            try {
                                                Object mEditor = JavaReflectUtils.getField(cls_Editor$UndoInputFilter,"mEditor").get(inputFilter);
                                                if(mEditor != null){
                                                    if(cls_Editor == null){
                                                        cls_Editor = Class.forName("android.widget.Editor");
                                                    }
                                                    TextView textView = (TextView) JavaReflectUtils.getField(cls_Editor,"mTextView").get(mEditor);
                                                    if(textView != null){
                                                        activity = InnerClassHelper.getActivityFromContext(textView.getContext());
                                                        break;
                                                    }
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }else{
                            // Can we should compat ICE_CREAM_SANDWICH(14) and ICE_CREAM_SANDWICH_MR1(15) now?
                        }
                    }
                    if(activity == null){
                        activity = checkSpannableStringBuilder_mSpans(spannableStringBuilder);

                    }
                    if(activity != null){
                        boolean isActivityDestroyed = InnerClassHelper.isActivityDestroyed(activity);
                        if(isActivityDestroyed){
                            Log.i(AndroidPlatformMemoryWatcher.TAG,String.format("AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo  %s is destroyed,The AccessibilityNodeInfo that related this activity will invoke setText(null)",activity.toString()));
                        }
                        return isActivityDestroyed;
                    }

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }else if(SpannableStringBuilder.class.isInstance(charSequence)){
            Activity activity = checkSpannableStringBuilder_mSpans((SpannableStringBuilder) charSequence);
            if(activity != null){
                boolean isActivityDestroyed = InnerClassHelper.isActivityDestroyed(activity);
                if(isActivityDestroyed){
                    Log.i(AndroidPlatformMemoryWatcher.TAG,String.format("AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo  %s is destroyed,The AccessibilityNodeInfo that related this activity will invoke setText(null)",activity.toString()));
                }
                return isActivityDestroyed;
            }
        }
        return false;
    }

    public Activity checkSpannableStringBuilder_mSpans(SpannableStringBuilder spannableStringBuilder){
        try {
            Object[] mSpans = (Object[]) JavaReflectUtils.getField(SpannableStringBuilder.class,"mSpans").get(spannableStringBuilder);
            if(cls_TextView$ChangeWatcher == null){
                cls_TextView$ChangeWatcher = Class.forName("android.widget.TextView$ChangeWatcher");
            }
            Activity activity = null;
            for(Object obj:mSpans){
                if(cls_TextView$ChangeWatcher.isInstance(obj)){
                    List<Field> fieldList = InnerClassHelper.getSyntheticFields(cls_TextView$ChangeWatcher);
                    if(fieldList != null){
                        for(Field field:fieldList){
                            field.setAccessible(true);
                            Object target = field.get(obj);
                            if(TextView.class.isInstance(target)){
                                TextView textView = (TextView)target;
                                activity = InnerClassHelper.getActivityFromContext(textView.getContext());
                                break;
                            }
                        }
                        if(activity != null){
                            return activity;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void watch() {
        if(!checkNeedWatch()){
            return;
        }
        for(int i = 0;i<50;i++){
            AccessibilityNodeInfo accessibilityNodeInfo = AccessibilityNodeInfo.obtain();
            CharSequence charSequence = getOriginalText(accessibilityNodeInfo);
            if(charSequence == null){
                break;
            }
            if(isNeedRelease(charSequence)){
                accessibilityNodeInfo.setText(null);
            }
            accessibilityNodeInfoList.add(accessibilityNodeInfo);
        }
        for(AccessibilityNodeInfo accessibilityNodeInfo:accessibilityNodeInfoList){
            accessibilityNodeInfo.recycle();
        }
        accessibilityNodeInfoList.clear();

    }
}
