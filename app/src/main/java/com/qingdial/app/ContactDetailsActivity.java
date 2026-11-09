package com.qingdial.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import android.provider.CallLog;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

/** The arrow opens this page; no call is placed until the explicit dial button is pressed. */
public class ContactDetailsActivity extends HomeAwareActivity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final Handler handler=new Handler(Looper.getMainLooper());
    private LinearLayout content;
    private String name,number;
    private static class Record{int type;long date,seconds;Record(int t,long d,long s){type=t;date=d;seconds=s;}}
    @Override public void onCreate(Bundle state){
        super.onCreate(state);name=getIntent().getStringExtra("name");number=getIntent().getStringExtra("number");if(name==null||name.isEmpty())name=number==null?"未知号码":number;
        LinearLayout root=Ui.column(this);root.setBackgroundColor(0xfff4f7f5);root.setFocusableInTouchMode(true);setContentView(root);Ui.edge(this,root);
        LinearLayout toolbar=Ui.row(this);TextView back=Ui.button(this,"‹",Ui.INK,this::finish);back.setTextSize(34);back.setContentDescription("返回");back.setBackgroundColor(0xfff4f7f5);toolbar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,54)));toolbar.addView(Ui.text(this,"联系人详情",23,Ui.INK));root.addView(toolbar);
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);content=Ui.column(this);content.setPadding(0,Ui.dp(this,16),0,Ui.dp(this,20));scroll.addView(content);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout identity=Ui.column(this);identity.setPadding(Ui.dp(this,20),Ui.dp(this,20),Ui.dp(this,20),Ui.dp(this,20));identity.setBackground(Ui.bg(0xffffffff,Ui.dp(this,20)));
        TextView title=Ui.text(this,name,26,0xff299e69);title.setTypeface(null,android.graphics.Typeface.BOLD);identity.addView(title);TextView phone=Ui.text(this,number==null?"隐藏号码":number,18,Ui.MUTED);phone.setTextIsSelectable(true);phone.setPadding(0,Ui.dp(this,6),0,Ui.dp(this,18));identity.addView(phone);
        TextView dial=Ui.button(this,"拨打此号码",0xffffffff,this::dial);dial.setBackground(Ui.bg(Ui.GREEN,Ui.dp(this,23)));identity.addView(dial,new LinearLayout.LayoutParams(-1,Ui.dp(this,46)));content.addView(identity);
        TextView loading=Ui.text(this,"正在读取此号码的通话记录…",13,Ui.MUTED);loading.setPadding(Ui.dp(this,8),Ui.dp(this,22),0,0);content.addView(loading);root.requestFocus();load(loading);
    }
    private static String canonical(String number){if(number==null)return "";String d=SearchIndex.digits(number);if(d.length()==13&&d.startsWith("861"))return d.substring(2);if(d.length()==15&&d.startsWith("00861"))return d.substring(4);return d;}
    private void load(TextView loading){
        if(checkSelfPermission(Manifest.permission.READ_CALL_LOG)!=PackageManager.PERMISSION_GRANTED){loading.setText("允许读取通话记录后，可查看此号码的详情");content.addView(Ui.button(this,"管理通话记录权限",Ui.GREEN,()->startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())))));return;}
        worker.execute(()->{
            ArrayList<Record> records=new ArrayList<>();long incoming=0,outgoing=0;int incomingCount=0,outgoingCount=0,missedCount=0,total=0;String error=null,target=canonical(number);
            try(Cursor cursor=getContentResolver().query(CallLog.Calls.CONTENT_URI,new String[]{CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE,CallLog.Calls.DURATION},null,null,CallLog.Calls.DATE+" DESC")){
                if(cursor!=null&&!target.isEmpty())while(cursor.moveToNext()){
                    if(Thread.currentThread().isInterrupted())return;
                    if(!target.equals(canonical(cursor.getString(0))))continue;
                    int type=cursor.getInt(1);long seconds=Math.max(0,cursor.getLong(3));total++;
                    if(type==CallLog.Calls.INCOMING_TYPE){incoming+=seconds;incomingCount++;}else if(type==CallLog.Calls.OUTGOING_TYPE){outgoing+=seconds;outgoingCount++;}else if(type==CallLog.Calls.MISSED_TYPE)missedCount++;
                    if(records.size()<200)records.add(new Record(type,cursor.getLong(2),seconds));
                }
            }catch(RuntimeException e){error="暂时无法读取记录，请检查系统通话记录权限";}
            final long inSeconds=incoming,outSeconds=outgoing;final int inCount=incomingCount,outCount=outgoingCount,missed=missedCount,all=total;String failure=error;
            handler.post(()->{if(isDestroyed())return;content.removeView(loading);if(failure!=null){loading.setText(failure);content.addView(loading);return;}show(records,inSeconds,outSeconds,inCount,outCount,missed,all);});
        });
    }
    private String duration(long seconds){return String.format(Locale.CHINA,"%d分%02d秒",seconds/60,seconds%60);}
    private void show(List<Record> records,long incoming,long outgoing,int incomingCount,int outgoingCount,int missed,int total){
        TextView caption=Ui.text(this,"此号码的通话统计",13,Ui.MUTED);caption.setPadding(Ui.dp(this,8),Ui.dp(this,24),0,Ui.dp(this,10));content.addView(caption);
        LinearLayout stats=Ui.row(this);stat(stats,"呼入 · "+incomingCount+"次",duration(incoming),0xff299e69,0xffe7f5ed);stat(stats,"呼出 · "+outgoingCount+"次",duration(outgoing),0xff3977d5,0xffeaf1fc);content.addView(stats,new LinearLayout.LayoutParams(-1,Ui.dp(this,86)));
        TextView history=Ui.text(this,"通话记录 · 共 "+total+" 条 · 未接 "+missed+" 次"+(total>200?"\n展示最近 200 条":""),13,Ui.MUTED);history.setPadding(Ui.dp(this,8),Ui.dp(this,22),0,Ui.dp(this,10));content.addView(history);
        if(records.isEmpty()){content.addView(Ui.text(this,"此号码暂无通话记录",15,Ui.MUTED));return;}
        LinearLayout list=Ui.column(this);list.setBackground(Ui.bg(0xffffffff,Ui.dp(this,18)));content.addView(list);
        for(Record record:records){
            boolean out=record.type==CallLog.Calls.OUTGOING_TYPE,miss=record.type==CallLog.Calls.MISSED_TYPE;
            int color=out?0xff3977d5:miss?0xffd34b4b:record.type==CallLog.Calls.INCOMING_TYPE?0xff299e69:Ui.MUTED;
            String type=out?"呼出":miss?"未接":record.type==CallLog.Calls.REJECTED_TYPE?"已拒接":record.type==CallLog.Calls.BLOCKED_TYPE?"已拦截":"呼入";
            LinearLayout row=Ui.row(this);row.setPadding(Ui.dp(this,16),Ui.dp(this,13),Ui.dp(this,16),Ui.dp(this,13));LinearLayout labels=Ui.column(this);labels.addView(Ui.text(this,type,16,color));labels.addView(Ui.text(this,new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.CHINA).format(new Date(record.date)),12,Ui.MUTED));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));row.addView(Ui.text(this,record.seconds>0?duration(record.seconds):"未接通",13,color));list.addView(row);
        }
    }
    private void stat(LinearLayout parent,String label,String value,int color,int background){LinearLayout card=Ui.column(this);card.setPadding(Ui.dp(this,14),Ui.dp(this,12),Ui.dp(this,8),Ui.dp(this,10));card.setBackground(Ui.bg(background,Ui.dp(this,16)));card.addView(Ui.text(this,label,12,color));TextView v=Ui.text(this,value,19,color);v.setTypeface(null,android.graphics.Typeface.BOLD);card.addView(v);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(Ui.dp(this,3),0,Ui.dp(this,3),0);parent.addView(card,lp);}
    private void dial(){if(number==null||!number.matches("[+0-9*#(),; .-]+")){Toast.makeText(this,"没有可拨打的号码",Toast.LENGTH_SHORT).show();return;}if(checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){Toast.makeText(this,"请在应用设置中允许电话权限",Toast.LENGTH_SHORT).show();return;}try{getSystemService(TelecomManager.class).placeCall(Uri.fromParts("tel",number.replace(" ","").replace("-",""),null),new Bundle());}catch(SecurityException|IllegalArgumentException e){Toast.makeText(this,"拨号失败，请检查系统权限和 SIM 卡",Toast.LENGTH_SHORT).show();}}
    @Override public void onDestroy(){super.onDestroy();handler.removeCallbacksAndMessages(null);worker.shutdownNow();}
}
