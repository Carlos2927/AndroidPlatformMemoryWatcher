package com.carlos2927.java_memory_leak_fixer_android_extension;

import android.app.Dialog;
import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;

import com.carlos2927.java.memoryleakfixer.JavaReflectUtils;

import java.lang.reflect.Field;

/**
 * 解决android中一些内存泄漏的方法
 */
public class FixAndroidMemoryLeakUtils {
    private static class MyMessageQueueIdleHandler implements MessageQueue.IdleHandler{
        private Handler handler;
        private boolean isKeeping;
        private long flushMessageDelayedTime;
        public MyMessageQueueIdleHandler(Handler handler,boolean isKeeping,long flushMessageDelayedTime){
            this.handler = handler;
            this.isKeeping = isKeeping;
            this.flushMessageDelayedTime = flushMessageDelayedTime;
        }

        @Override
        public boolean queueIdle() {
            handler.sendMessageDelayed(handler.obtainMessage(), flushMessageDelayedTime);
            return isKeeping;
        }
    }

    private static MyMessageQueueIdleHandler dialogMessageQueueIdleHandler = null;

    /**
     * 解决 android.app.Dialog 销毁时由于mListenersHandler导致的内存泄漏问题
     * 也可以直接在Application.onCreate()中调用FixAndroidMemoryLeakUtils.flushStackLocalLeaks(Looper.getMainLooper(),true,600)即可，可以解决所有在
     * 主线程中创建的Dialog等由于Handle中的Looper.loop()引发的内存泄漏问题
     * @param dialog 已经销毁的dialog
     */
    public static void fixDialogMemoryLeakCauseByFiled_mListenersHandler(Dialog dialog){
        Field field_mListenersHandler = JavaReflectUtils.getField(Dialog.class,"mListenersHandler");
         try {
             Handler handler = (Handler) field_mListenersHandler.get(dialog);
             if(handler != null){
                 final Handler newHandler = new Handler(handler.getLooper());
                 newHandler.post(new Runnable() {
                     @Override
                     public void run() {
                         if(dialogMessageQueueIdleHandler == null){
                             dialogMessageQueueIdleHandler = new MyMessageQueueIdleHandler(newHandler,true,600);
                             Looper.myQueue().addIdleHandler(dialogMessageQueueIdleHandler);
                         }
                     }
                 });
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解决Handle中的Looper.loop()阻塞导致的最后一个Message没有被vm释放导致的内存泄漏的bug
     * for (;;) {
     *     Message msg = queue.next(); // might block    The last Message in here will be leaked when block
     *     if (msg == null) {
     *         return;
     *     }
     *     msg.target.dispatchMessage(msg);
     *     msg.recycleUnchecked();
     * }
     * @see <a>https://github.com/hehonghui/android-tech-frontier/blob/master/issue-25/%E4%B8%80%E4%B8%AA%E5%86%85%E5%AD%98%E6%B3%84%E6%BC%8F%E5%BC%95%E5%8F%91%E7%9A%84%E8%A1%80%E6%A1%88-Square.md<a/>
     * @param looper 消息循环器
     * @param keeping flushMessage是否只发送一次
     * @param flushMessageDelayedTime flushMessage延迟时间
     */
    public static void flushStackLocalLeaks(Looper looper,final boolean keeping,final long flushMessageDelayedTime) {
        final Handler handler = new Handler(looper);
        handler.post(new Runnable() {
            @Override public void run() {
                Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
                    @Override public boolean queueIdle() {
                        handler.sendMessageDelayed(handler.obtainMessage(), flushMessageDelayedTime);
                        return keeping;
                    }
                });
            }
        });
    }

    public static void flushStackLocalLeaks(Looper looper){
        flushStackLocalLeaks(looper,true,500);
    }

    public static void flushStackLocalLeaks(Looper looper,boolean keeping){
        flushStackLocalLeaks(looper,keeping,500);
    }

}
