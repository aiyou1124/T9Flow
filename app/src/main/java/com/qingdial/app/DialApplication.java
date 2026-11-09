package com.qingdial.app;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;

/** Distinguishes returning from the desktop from navigation inside this app. */
public class DialApplication extends Application implements Application.ActivityLifecycleCallbacks {
    private int started;
    private boolean seen;
    private java.lang.ref.WeakReference<MainActivity> main=new java.lang.ref.WeakReference<>(null);
    private final java.util.Set<Activity> secondary=java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private boolean resetting;
    private boolean homeResetPending;
    private int navigationEpoch;
    @Override public void onCreate(){super.onCreate();registerActivityLifecycleCallbacks(this);}
    @Override public void onActivityStarted(Activity activity){
        boolean entering=started++==0;
        boolean restoringSettings=entering&&!seen&&(activity instanceof SettingsActivity||activity instanceof ContactDetailsActivity);seen=true;
        if(restoringSettings){activity.startActivity(new Intent(activity,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP).putExtra("home",true));activity.finish();}
    }
    void resetForDesktop(){
        if(resetting)return;resetting=true;
        try{
            if(!homeResetPending){homeResetPending=true;navigationEpoch++;}
            MainActivity home=main.get();if(home!=null&&!home.isDestroyed())home.showDialHome();
            for(Activity page:new java.util.ArrayList<>(secondary))if(!page.isFinishing()&&!page.isDestroyed())page.finish();
        }finally{resetting=false;}
    }
    int navigationEpoch(){return navigationEpoch;}
    void completeHomeReset(Activity activity){
        if(!homeResetPending)return;
        if(activity instanceof MainActivity){((MainActivity)activity).showDialHome();homeResetPending=false;}
        else if(activity instanceof SettingsActivity||activity instanceof ContactDetailsActivity){if(!activity.isFinishing())activity.finish();}
    }
    @Override public void onActivityStopped(Activity a){
        started=Math.max(0,started-1);
        if(started==0&&!a.isChangingConfigurations()&&a instanceof HomeAwareActivity&&!((HomeAwareActivity)a).isNavigatingToActivity())resetForDesktop();
    }
    @Override public void onActivityCreated(Activity a,Bundle b){if(a instanceof MainActivity)main=new java.lang.ref.WeakReference<>((MainActivity)a);else if(a instanceof HomeAwareActivity)secondary.add(a);}
    @Override public void onActivityResumed(Activity a){}
    @Override public void onActivityPaused(Activity a){}
    @Override public void onActivitySaveInstanceState(Activity a,Bundle b){}
    @Override public void onActivityDestroyed(Activity a){secondary.remove(a);if(main.get()==a)main.clear();}
}
