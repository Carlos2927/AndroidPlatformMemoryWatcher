package com.carlos2927.java_memory_leak_fixer_android_extension;

import com.carlos2927.java.memoryleakfixer.Watchable;

public abstract class AndroidFrameworkStaticFiledWatcher implements Watchable {

    private long lastWatchTime;
    private int mode;
    private int intervalTime = 500;

    protected boolean checkNeedWatch() {
        if (mode == 0) {
            long now = System.currentTimeMillis();
            if (lastWatchTime == 0 || now - lastWatchTime >= intervalTime || now < lastWatchTime) {
                lastWatchTime = now;
                return true;
            }
        }
        if (mode == 1) {
            mode = -1;
            return true;
        }
        return false;
    }

    /**
     * 一直监控检测
     * @param intervalTime 监控检测的最小时间间隔
     */
    public final void keepWatching(int intervalTime){
        mode = 0;
        this.intervalTime = intervalTime;
    }

    /**
     * 只监控检测一次，在生命周期对象销毁时调用(Activity.onDestroy())
     */
    public final void watchOnlyOnce(){
        mode = 1;
    }



}
