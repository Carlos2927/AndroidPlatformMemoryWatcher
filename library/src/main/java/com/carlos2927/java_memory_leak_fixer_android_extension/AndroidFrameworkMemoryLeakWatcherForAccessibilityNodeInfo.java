package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.app.Activity;
import android.os.Build;
import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;

import com.carlos2927.java.memoryleakfixer.InnerClassHelper;
import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;
import com.carlos2927.java.memoryleakfixer.Watchable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决由android.view.accessibility.AccessibilityNodeInfo#sPool静态持有间接引用Context的变量引发的内存泄漏
 */
public class AndroidFrameworkMemoryLeakWatcherForAccessibilityNodeInfo implements Watchable {
    Class cls;
    Class cls_Editor$UndoInputFilter;
    Class cls_Editor;
    Class cls_TextView$ChangeWatcher;
    List<AccessibilityNodeInfo> accessibilityNodeInfoList = new ArrayList<>();
    private CharSequence getOriginalText(AccessibilityNodeInfo accessibilityNodeInfo){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Method method = JavaReflectUtils.getMethod(AccessibilityNodeInfo.class,"getOriginalText");
            if(method != null){
                try {
                    return (CharSequence) method.invoke(accessibilityNodeInfo);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            Field field = JavaReflectUtils.getField(AccessibilityNodeInfo.class,"mOriginalText");
            if(field != null){
                try {
                    return (CharSequence) field.get(accessibilityNodeInfo);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
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
                        try {
                            Object[] mSpans = (Object[]) JavaReflectUtils.getField(SpannableStringBuilder.class,"mSpans").get(spannableStringBuilder);
                            if(cls_TextView$ChangeWatcher == null){
                                cls_TextView$ChangeWatcher = Class.forName("android.widget.TextView$ChangeWatcher");
                            }
                            for(Object obj:mSpans){
                                if(cls_TextView$ChangeWatcher.isInstance(obj)){
                                    List<Field> fieldList = InnerClassHelper.getSyntheticFields(cls_TextView$ChangeWatcher);
                                    if(fieldList != null){
                                        for(Field field:fieldList){
                                            field.setAccessible(true);
                                            if(TextView.class.isInstance(field.get(obj))){
                                                TextView textView = (TextView)field.get(obj);
                                                activity = InnerClassHelper.getActivityFromContext(textView.getContext());
                                                break;
                                            }
                                        }
                                        if(activity != null){
                                            break;
                                        }
                                    }
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }

                    if(activity != null){
                        return InnerClassHelper.isActivityDestroyed(activity);
                    }

                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void watch() {
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
