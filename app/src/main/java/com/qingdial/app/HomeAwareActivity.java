package com.qingdial.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Reset as the user leaves for Home, before a fast launcher return can resume us. */
public abstract class HomeAwareActivity extends Activity {
    private boolean navigatingToActivity;
    @Override public void startActivityForResult(Intent intent,int requestCode,Bundle options){
        navigatingToActivity=true;
        try{super.startActivityForResult(intent,requestCode,options);}catch(RuntimeException e){navigatingToActivity=false;throw e;}
    }
    @Override protected void onResume(){super.onResume();navigatingToActivity=false;((DialApplication)getApplication()).completeHomeReset(this);}
    @Override protected void onPause(){
        // A quick return may happen before onStop. Clear during pause, not stop.
        if(!navigatingToActivity&&!isFinishing()&&!isChangingConfigurations()){
            if(this instanceof MainActivity)((MainActivity)this).showDialHome();
            ((DialApplication)getApplication()).resetForDesktop();
        }
        super.onPause();
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){navigatingToActivity=false;super.onActivityResult(requestCode,resultCode,data);}
    @Override protected void onUserLeaveHint(){
        super.onUserLeaveHint();
        if(!navigatingToActivity)((DialApplication)getApplication()).resetForDesktop();
    }
    boolean isNavigatingToActivity(){return navigatingToActivity;}
}
