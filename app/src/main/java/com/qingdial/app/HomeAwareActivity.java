package com.qingdial.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/** Reset as the user leaves for Home, before a fast launcher return can resume us. */
public abstract class HomeAwareActivity extends Activity {
    private boolean navigatingToActivity;
    private static final int DIRECT_CALL_PERMISSION=73;
    private String pendingCallNumber;
    @Override protected void onCreate(Bundle state){super.onCreate(state);if(state!=null)pendingCallNumber=state.getString("pending_direct_call");}
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putString("pending_direct_call",pendingCallNumber);}
    void requestDirectCall(String number){
        if(pendingCallNumber!=null)return;
        pendingCallNumber=number;
        requestAppPermissions(new String[]{android.Manifest.permission.CALL_PHONE},DIRECT_CALL_PERMISSION);
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] grants){
        super.onRequestPermissionsResult(code,permissions,grants);
        if(code!=DIRECT_CALL_PERMISSION)return;
        String number=pendingCallNumber;pendingCallNumber=null;
        if(number==null)return;
        if(grants.length>0&&grants[0]==android.content.pm.PackageManager.PERMISSION_GRANTED)SystemDialer.open(this,number);
        else android.widget.Toast.makeText(this,"需要允许电话权限才能直接拨打，可在系统应用权限中开启",android.widget.Toast.LENGTH_LONG).show();
    }
    protected void requestAppPermissions(String[] permissions,int requestCode){navigatingToActivity=true;requestPermissions(permissions,requestCode);}
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
