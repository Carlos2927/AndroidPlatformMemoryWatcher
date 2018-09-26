package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.app.Dialog;
import android.os.Handler;

import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;

import java.lang.reflect.Field;

/**
 * 解决android中一些内存泄漏的方法
 */
public class FixAndroidMemoryLeakUtils {

    /**
     * 解决 android.app.Dialog 销毁时由于mListenersHandler导致的内存泄漏问题
     * @param dialog 已经销毁的dialog
     */
    public static void fixDialogMemoryLeakCauseByFiled_mListenersHandler(Dialog dialog){
        Field field_mListenersHandler = JavaReflectUtils.getField(Dialog.class,"mListenersHandler");
         try {
             Handler handler = (Handler) field_mListenersHandler.get(dialog);
             if(handler != null){
                 handler.removeCallbacksAndMessages(null);
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
