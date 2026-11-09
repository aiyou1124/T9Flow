package com.qingdial.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.telecom.*;
import java.util.*;

public class PhoneService extends InCallService {
    static PhoneService instance;
    static final ArrayList<Call> calls=new ArrayList<>();
    static Runnable listener;
    private final Map<Call,Call.Callback> callbacks=new HashMap<>();
    private static final String CHANNEL="phone_calls";
    @Override public void onCreate(){super.onCreate();instance=this;NotificationChannel channel=new NotificationChannel(CHANNEL,"通话界面",NotificationManager.IMPORTANCE_HIGH);channel.setSound(null,null);channel.setDescription("来电接听及返回通话界面；铃声由系统管理");getSystemService(NotificationManager.class).createNotificationChannel(channel);}
    @Override public void onCallAdded(Call call){super.onCallAdded(call);calls.add(call);Call.Callback cb=new Call.Callback(){@Override public void onStateChanged(Call c,int state){update();}@Override public void onDetailsChanged(Call c,Call.Details d){update();}};callbacks.put(call,cb);call.registerCallback(cb);update();if(call.getState()!=Call.STATE_RINGING)showCall();}
    @Override public void onCallRemoved(Call call){super.onCallRemoved(call);Call.Callback cb=callbacks.remove(call);if(cb!=null)call.unregisterCallback(cb);calls.remove(call);update();}
    @Override public void onBringToForeground(boolean showDialpad){showCall();}
    @Override public void onCallAudioStateChanged(CallAudioState state){super.onCallAudioStateChanged(state);if(listener!=null)listener.run();}
    static Call primary(){for(Call c:calls)if(c.getState()==Call.STATE_RINGING)return c;for(Call c:calls)if(c.getState()==Call.STATE_ACTIVE)return c;for(Call c:calls)if(c.getState()!=Call.STATE_DISCONNECTED)return c;return calls.isEmpty()?null:calls.get(0);}
    static String number(Call c){return c.getDetails().getHandle()==null?"隐藏号码":c.getDetails().getHandle().getSchemeSpecificPart();}
    static String title(Call c){String n=c.getDetails().getCallerDisplayName();return n==null||n.isEmpty()?number(c):n;}
    private PendingIntent open(String action){Intent i=new Intent(this,CallActivity.class).setAction(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);return PendingIntent.getActivity(this,action.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private void update(){
        NotificationManager nm=getSystemService(NotificationManager.class);Call c=primary();
        if(c==null)nm.cancel(11);else{
            boolean ringing=c.getState()==Call.STATE_RINGING;
            Notification.Builder b=new Notification.Builder(this,CHANNEL).setSmallIcon(com.qingdial.app.R.drawable.ic_phone).setContentTitle(title(c)).setContentText(ringing?"来电 · 点击接听":"通话中 · 点击返回").setCategory(Notification.CATEGORY_CALL).setOngoing(true).setContentIntent(open("open")).setVisibility(Notification.VISIBILITY_PRIVATE).setOnlyAlertOnce(true);
            if(ringing){b.setFullScreenIntent(open("open"),true);}
            if(Build.VERSION.SDK_INT>=31){android.app.Person person=new android.app.Person.Builder().setName(title(c)).setImportant(true).build();b.setStyle(ringing?Notification.CallStyle.forIncomingCall(person,open("reject"),open("answer")):Notification.CallStyle.forOngoingCall(person,open("hangup")));}
            else {if(ringing)b.addAction(new Notification.Action.Builder(null,"接听",open("answer")).build());b.addAction(new Notification.Action.Builder(null,ringing?"拒接":"挂断",open(ringing?"reject":"hangup")).build());}
            try{nm.notify(11,b.build());}catch(SecurityException ignored){}
        }
        if(listener!=null)listener.run();
    }
    private void showCall(){try{startActivity(new Intent(this,CallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP));}catch(RuntimeException ignored){/* Notification remains available when background launch is restricted. */}}
    @Override public void onDestroy(){for(var entry:callbacks.entrySet())entry.getKey().unregisterCallback(entry.getValue());callbacks.clear();calls.clear();instance=null;getSystemService(NotificationManager.class).cancel(11);if(listener!=null)listener.run();super.onDestroy();}
}
