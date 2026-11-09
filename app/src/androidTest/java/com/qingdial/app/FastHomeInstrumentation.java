package com.qingdial.app;

import android.accessibilityservice.AccessibilityService;
import android.app.*;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.*;

/** Exercises an existing task, avoiding ACTION_MAIN/onNewIntent resetting it for us. */
public final class FastHomeInstrumentation extends Instrumentation {
    private MainActivity main;
    private int checks;
    @Override public void onCreate(Bundle args){super.onCreate(args);start();}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try{
            main=(MainActivity)startActivitySync(new Intent(getTargetContext(),MainActivity.class).setAction(Intent.ACTION_MAIN).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK));waitForIdleSync();
            for(long delay:new long[]{0,40,120}){
                input();desktopAndReturn("dialer input",delay,null);
                Activity settings=open("设置",SettingsActivity.class);
                desktopAndReturn("settings",delay,settings);
                Activity details=open("查看联系人通话详情",ContactDetailsActivity.class);
                desktopAndReturn("contact details",delay,details);
            }
            result.putString("stream","PASS: "+checks+" Home reset checks before relaunch (0/40/120ms task-return attempts); no calls placed.\n");finish(Activity.RESULT_OK,result);
        }catch(Throwable error){result.putString("stream","FAIL: "+android.util.Log.getStackTraceString(error));finish(Activity.RESULT_CANCELED,result);}
    }
    private void onUi(Runnable action){
        java.util.concurrent.atomic.AtomicReference<Throwable> error=new java.util.concurrent.atomic.AtomicReference<>();
        runOnMainSync(()->{try{action.run();}catch(Throwable failure){error.set(failure);}});
        if(error.get()!=null)throw new AssertionError(error.get());
    }
    private View root(){return main.getWindow().getDecorView();}
    private View description(View v,String wanted){if(wanted.contentEquals(v.getContentDescription()==null?"":v.getContentDescription()))return v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){View child=description(g.getChildAt(i),wanted);if(child!=null)return child;}}return null;}
    private EditText edit(View v){if(v instanceof EditText)return (EditText)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){EditText e=edit(g.getChildAt(i));if(e!=null)return e;}}return null;}
    private void input(){onUi(()->{main.showDialHome();for(String d:new String[]{"9 WXYZ","3 DEF","3 DEF"}){View key=description(root(),d);if(key==null)throw new AssertionError("Key missing");key.performClick();}if(!"933".contentEquals(edit(root()).getText()))throw new AssertionError("Test input missing");});}
    private Activity open(String description,Class<?> activity){
        ActivityMonitor monitor=addMonitor(activity.getName(),null,false);
        onUi(()->{View control=description(root(),description);if(control==null)throw new AssertionError("Control missing: "+description);control.performClick();});
        Activity page=monitor.waitForActivityWithTimeout(4000);removeMonitor(monitor);if(page==null)throw new AssertionError("Page did not open: "+activity.getSimpleName());waitForIdleSync();return page;
    }
    private void desktopAndReturn(String label,long delay,Activity secondary){
        ActivityManager.AppTask task=null;
        for(ActivityManager.AppTask candidate:getTargetContext().getSystemService(ActivityManager.class).getAppTasks())if(candidate.getTaskInfo().taskId==main.getTaskId())task=candidate;
        if(task==null)throw new AssertionError("Own task not found");
        if(!getUiAutomation().performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME))throw new AssertionError("Home action rejected");
        if(delay>0)SystemClock.sleep(delay);
        task.moveToFront(); // Resume the existing task; do not launch MainActivity again.
        waitForIdleSync();SystemClock.sleep(160);waitForIdleSync();
        onUi(()->{
            if(edit(root()).getText().length()!=0)throw new AssertionError(label+" retained input at "+delay+"ms");
            if(edit(root()).getParent() instanceof View && ((View)edit(root()).getParent()).getVisibility()!=View.GONE)throw new AssertionError(label+" retained input row at "+delay+"ms");
            if(secondary!=null&&!secondary.isFinishing()&&!secondary.isDestroyed())throw new AssertionError(label+" remained open at "+delay+"ms");

        });
        // Vendor firmware can ignore moveToFront during its launcher animation.
        // State has already been checked above, before any new launch intent.
        if(!main.hasWindowFocus()){
            try(android.os.ParcelFileDescriptor descriptor=getUiAutomation().executeShellCommand("am start -a android.intent.action.MAIN -n com.qingdial.app/.MainActivity");java.io.InputStream input=new android.os.ParcelFileDescriptor.AutoCloseInputStream(descriptor)){byte[] buffer=new byte[512];while(input.read(buffer)!=-1){}}
            catch(java.io.IOException e){throw new AssertionError(e);}
            waitForIdleSync();SystemClock.sleep(250);
        }
        checks++;
    }
}
