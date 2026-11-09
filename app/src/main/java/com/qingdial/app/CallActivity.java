package com.qingdial.app;

import android.app.*;
import android.content.*;
import android.os.*;
import android.telecom.*;
import android.telecom.VideoProfile;
import android.view.*;
import android.widget.*;
import java.util.*;

public class CallActivity extends Activity {
    private LinearLayout root;
    private TextView status;
    private Call selected;
    private KeyFeedback feedback;
    private PowerManager.WakeLock proximity;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable changed=()->runOnUiThread(this::render);
    private final Runnable ticker=new Runnable(){public void run(){if(selected!=null&&status!=null)status.setText(state(selected));handler.postDelayed(this,1000);}};
    @Override public void onCreate(Bundle b){super.onCreate(b);feedback=new KeyFeedback(this);getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);setShowWhenLocked(true);setTurnScreenOn(true);root=Ui.column(this);root.setBackgroundColor(Ui.BG);setContentView(root);Ui.edge(this,root);handle(getIntent());}
    @Override public void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handle(i);render();}
    @Override public void onResume(){super.onResume();PowerManager pm=getSystemService(PowerManager.class);if(proximity==null&&pm.isWakeLockLevelSupported(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK))proximity=pm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,"qingdial:proximity");PhoneService.listener=changed;render();handler.post(ticker);}
    @Override public void onPause(){super.onPause();if(PhoneService.listener==changed)PhoneService.listener=null;handler.removeCallbacks(ticker);if(proximity!=null&&proximity.isHeld())proximity.release();}
    @Override public void onDestroy(){super.onDestroy();handler.removeCallbacksAndMessages(null);feedback.close();}
    private void handle(Intent i){Call c=PhoneService.primary();if(c==null)return;String action=i.getAction();if("answer".equals(action)&&c.getState()==Call.STATE_RINGING)c.answer(VideoProfile.STATE_AUDIO_ONLY);else if("reject".equals(action)&&c.getState()==Call.STATE_RINGING){c.reject(false,null);finish();}else if("hangup".equals(action))c.disconnect();i.setAction("open");}
    private String state(Call c){switch(c.getState()){case Call.STATE_RINGING:return "来电";case Call.STATE_DIALING:return "正在拨号…";case Call.STATE_CONNECTING:return "正在连接…";case Call.STATE_SELECT_PHONE_ACCOUNT:return "请选择 SIM 卡";case Call.STATE_HOLDING:return "已保持";case Call.STATE_DISCONNECTED:return "通话已结束";case Call.STATE_DISCONNECTING:return "正在挂断…";case Call.STATE_ACTIVE:long seconds=Math.max(0,(System.currentTimeMillis()-c.getDetails().getConnectTimeMillis())/1000);return String.format(Locale.CHINA,"%02d:%02d",seconds/60,seconds%60);default:return "正在连接…";}}
    private void render(){
        if(isFinishing())return;
        if(selected==null||!PhoneService.calls.contains(selected)||selected.getState()==Call.STATE_DISCONNECTED)selected=PhoneService.primary();
        Call primary=PhoneService.primary();if(primary!=null&&primary.getState()==Call.STATE_RINGING)selected=primary;
        if(selected==null){finish();return;}
        root.removeAllViews();Call c=selected;
        if(proximity!=null){PhoneService svc=PhoneService.instance;CallAudioState audio=svc==null?null:svc.getCallAudioState();boolean near=(c.getState()==Call.STATE_ACTIVE||c.getState()==Call.STATE_DIALING)&&audio!=null&&audio.getRoute()==CallAudioState.ROUTE_EARPIECE;if(near&&!proximity.isHeld())proximity.acquire(3*60*60*1000L);else if(!near&&proximity.isHeld())proximity.release();}
        TextView brand=Ui.text(this,"轻拨 · 通话",14,Ui.GREEN);root.addView(brand,new LinearLayout.LayoutParams(-1,Ui.dp(this,48)));
        TextView name=Ui.text(this,PhoneService.title(c),32,Ui.INK);name.setGravity(Gravity.CENTER);root.addView(name,new LinearLayout.LayoutParams(-1,Ui.dp(this,70)));
        status=Ui.text(this,state(c),18,Ui.MUTED);status.setGravity(Gravity.CENTER);root.addView(status,new LinearLayout.LayoutParams(-1,Ui.dp(this,44)));
        if(PhoneService.calls.size()>1)for(Call other:new ArrayList<>(PhoneService.calls)){if(other!=c)root.addView(Ui.button(this,PhoneService.title(other)+" · "+state(other)+" · 切换",Ui.GREEN,()->{selected=other;render();}));}
        if(c.getState()==Call.STATE_SELECT_PHONE_ACCOUNT){List<PhoneAccountHandle> accounts=availableAccounts();if(accounts!=null)for(PhoneAccountHandle account:accounts){PhoneAccount pa=getSystemService(TelecomManager.class).getPhoneAccount(account);root.addView(Ui.button(this,pa==null?"SIM 卡":String.valueOf(pa.getLabel()),Ui.GREEN,()->c.phoneAccountSelected(account,false)));}}
        ScrollView controls=new ScrollView(this);LinearLayout inner=Ui.column(this);controls.addView(inner);root.addView(controls,new LinearLayout.LayoutParams(-1,0,1));
        if(c.getState()==Call.STATE_RINGING){inner.addView(Ui.button(this,"接听",Ui.GREEN,()->c.answer(VideoProfile.STATE_AUDIO_ONLY)));inner.addView(Ui.button(this,"拒接",0xffce4747,()->c.reject(false,null)));}
        else {
            PhoneService service=PhoneService.instance;CallAudioState audio=service==null?null:service.getCallAudioState();
            LinearLayout row=Ui.row(this);row.addView(Ui.button(this,audio!=null&&audio.isMuted()?"取消静音":"静音",Ui.GREEN,()->{if(service!=null)service.setMuted(audio==null||!audio.isMuted());}),new LinearLayout.LayoutParams(0,-2,1));
            row.addView(Ui.button(this,"音频输出",Ui.GREEN,this::audioRoutes),new LinearLayout.LayoutParams(0,-2,1));
            if(c.getDetails().can(Call.Details.CAPABILITY_HOLD))row.addView(Ui.button(this,c.getState()==Call.STATE_HOLDING?"恢复":"保持",Ui.GREEN,()->{if(c.getState()==Call.STATE_HOLDING)c.unhold();else c.hold();}),new LinearLayout.LayoutParams(0,-2,1));inner.addView(row);
            TextView hint=Ui.text(this,"通话键盘",13,Ui.MUTED);hint.setGravity(Gravity.CENTER);inner.addView(hint);
            for(String chars:new String[]{"123","456","789","*0#"}){LinearLayout r=Ui.row(this);for(char key:chars.toCharArray()){TextView keyView=Ui.button(this,String.valueOf(key),Ui.INK,()->{});keyView.setTextSize(25);keyView.setOnClickListener(v->{feedback.play(v,key);c.playDtmfTone(key);handler.postDelayed(c::stopDtmfTone,150);});r.addView(keyView,new LinearLayout.LayoutParams(0,Ui.dp(this,58),1));}inner.addView(r);}
            TextView hang=Ui.button(this,"挂断",0xffffffff,c::disconnect);hang.setBackground(Ui.bg(0xffcc4848,Ui.dp(this,28)));inner.addView(hang,new LinearLayout.LayoutParams(-1,Ui.dp(this,56)));
        }
    }
    private List<PhoneAccountHandle> availableAccounts(){try{return getSystemService(TelecomManager.class).getCallCapablePhoneAccounts();}catch(SecurityException e){return Collections.emptyList();}}
    private void audioRoutes(){PhoneService s=PhoneService.instance;if(s==null)return;CallAudioState a=s.getCallAudioState();if(a==null)return;int[] routes={CallAudioState.ROUTE_EARPIECE,CallAudioState.ROUTE_SPEAKER,CallAudioState.ROUTE_BLUETOOTH,CallAudioState.ROUTE_WIRED_HEADSET};String[] names={"听筒","扬声器","蓝牙","有线耳机"};ArrayList<String> labels=new ArrayList<>();ArrayList<Integer> values=new ArrayList<>();for(int i=0;i<routes.length;i++)if((a.getSupportedRouteMask()&routes[i])!=0){labels.add(names[i]);values.add(routes[i]);}new AlertDialog.Builder(this).setTitle("音频输出").setItems(labels.toArray(new String[0]),(d,w)->s.setAudioRoute(values.get(w))).show();}
}
