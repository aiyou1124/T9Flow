package com.qingdial.app;

import android.Manifest;
import android.app.*;
import android.app.role.RoleManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.*;
import android.icu.text.Transliterator;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.telecom.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends HomeAwareActivity {
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final ExecutorService searchWorker=Executors.newSingleThreadExecutor();
    private int searchGeneration;
    private Runnable pendingSearch;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private List<SearchIndex> contacts=new ArrayList<>();
    private List<Recent> recents=new ArrayList<>();
    private LinearLayout root,results,keypad,nav,inputRow,callActions;
    private boolean textEntryMode;
    private TextView banner,count,title;
    private View backspace;
    private EditText search;
    private KeyFeedback feedback;
    private int tab=0, generation=0;
    private boolean loaded=false;
    private boolean cacheLoaded,cacheContactsAllowed,cacheLogsAllowed,foreground,dataDirty;
    private long cacheSavedAt;
    private static final long CACHE_FRESH_MS=5*60*1000L;
    private final Runnable changedData=this::reload;
    private static class Snapshot implements java.io.Serializable {
        private static final long serialVersionUID=1L;
        ArrayList<SearchIndex> contacts;ArrayList<Recent> recents;long savedAt;boolean contactsAllowed,logsAllowed;
    }
    private String dataError="";
    private final ContentObserver observer=new ContentObserver(handler){@Override public void onChange(boolean self){dataDirty=true;if(foreground){handler.removeCallbacks(changedData);handler.postDelayed(changedData,300);}}};
    private static class Recent implements java.io.Serializable {private static final long serialVersionUID=1L;String name,number;int type;long date,duration;Recent(String n,String p,int t,long d,long s){name=n;number=p;type=t;date=d;duration=s;}}
    @Override public void onCreate(Bundle b){super.onCreate(b);feedback=new KeyFeedback(this);build();readIntent(getIntent());render();if(b==null)handler.post(this::onboarding);}
    @Override public void onSaveInstanceState(Bundle b){super.onSaveInstanceState(b);b.putInt("tab",tab);b.putString("query",search.getText().toString());}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);readIntent(i);}
    private void readIntent(Intent i){if(i.getBooleanExtra("home",false)||i.getData()==null)showDialHome();if(i.getData()!=null && "tel".equals(i.getData().getScheme())){tab=0;search.setText(i.getData().getSchemeSpecificPart());render();}}
    @Override protected void onResume(){super.onResume();foreground=true;getContentResolver().unregisterContentObserver(observer);ensureData();renderCallButtons();try{getContentResolver().registerContentObserver(ContactsContract.Contacts.CONTENT_URI,true,observer);if(has(Manifest.permission.READ_CALL_LOG))getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI,true,observer);}catch(SecurityException ignored){}status();}
    @Override protected void onPause(){super.onPause();foreground=false;handler.removeCallbacks(changedData);}
    @Override protected void onDestroy(){super.onDestroy();getContentResolver().unregisterContentObserver(observer);generation++;handler.removeCallbacksAndMessages(null);worker.shutdownNow();searchWorker.shutdownNow();feedback.close();}
    private boolean has(String p){
        if(checkSelfPermission(p)!=PackageManager.PERMISSION_GRANTED)return false;
        String op=AppOpsManager.permissionToOp(p);
        if(op==null)return true;
        int mode=getSystemService(AppOpsManager.class).unsafeCheckOpNoThrow(op,android.os.Process.myUid(),getPackageName());
        return mode!=AppOpsManager.MODE_IGNORED&&mode!=AppOpsManager.MODE_ERRORED;
    }
    private boolean isDefault(){return getPackageName().equals(getSystemService(TelecomManager.class).getDefaultDialerPackage());}
    private void build(){
        root=Ui.column(this);root.setBackgroundColor(Ui.BG);root.setFocusableInTouchMode(true);setContentView(root);Ui.edge(this,root);
        LinearLayout header=Ui.row(this);
        title=Ui.text(this,"电话",25,Ui.INK);title.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));
        header.addView(title,new LinearLayout.LayoutParams(0,Ui.dp(this,52),1));
        AppIcon settingsIcon=new AppIcon(this,AppIcon.SETTINGS,Ui.INK);settingsIcon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);settingsIcon.setContentDescription("设置");settingsIcon.setBackground(ripple(0x00000000,24));settingsIcon.setOnClickListener(v->settings());header.addView(settingsIcon,new LinearLayout.LayoutParams(Ui.dp(this,44),Ui.dp(this,44)));root.addView(header);
        banner=Ui.button(this,"",Ui.GREEN,this::setup);banner.setTextSize(13);root.addView(banner);
        inputRow=Ui.row(this);inputRow.setVisibility(View.GONE);inputRow.setBackgroundColor(0xffffffff);inputRow.setPadding(Ui.dp(this,8),0,Ui.dp(this,8),0);View inputSpacer=new View(this);inputRow.addView(inputSpacer,new LinearLayout.LayoutParams(Ui.dp(this,48),1));
        search=new EditText(this);search.setSingleLine(true);search.setTextSize(32);search.setTextColor(0xff27bb4f);search.setGravity(Gravity.CENTER);search.setHintTextColor(Ui.MUTED);search.setHint("输入姓名或拼音");search.setShowSoftInputOnFocus(false);search.setPadding(0,0,Ui.dp(this,8),0);search.setBackgroundColor(android.graphics.Color.TRANSPARENT);inputRow.addView(search,new LinearLayout.LayoutParams(0,Ui.dp(this,60),1));
        backspace=new AppIcon(this,AppIcon.BACKSPACE,Ui.MUTED);backspace.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);backspace.setContentDescription("退格，长按清空");backspace.setBackground(ripple(0x00000000,0));backspace.setOnClickListener(v->{int a=Math.max(0,search.getSelectionStart()),b=Math.max(a,search.getSelectionEnd());if(a!=b)search.getText().delete(a,b);else if(a>0)search.getText().delete(a-1,a);});backspace.setOnLongClickListener(v->{textEntryMode=false;search.setText("");search.clearFocus();return true;});inputRow.addView(backspace,new LinearLayout.LayoutParams(Ui.dp(this,48),Ui.dp(this,48)));
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int a,int c,int f){}public void onTextChanged(CharSequence s,int a,int b,int c){inputRow.setVisibility(s.length()==0&&!textEntryMode?View.GONE:View.VISIBLE);renderResults();}public void afterTextChanged(Editable e){}});
        count=Ui.text(this,"",12,Ui.MUTED);root.addView(count,new LinearLayout.LayoutParams(-1,Ui.dp(this,28)));
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setVerticalScrollBarEnabled(false);results=Ui.column(this);scroll.addView(results);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(inputRow,new LinearLayout.LayoutParams(-1,Ui.dp(this,60)));
        keypad=Ui.column(this);keypad.setBackgroundColor(0xffffffff);
        String[] letters={"+","","ABC","DEF","GHI","JKL","MNO","PQRS","TUV","WXYZ"};
        int rowHeight=Ui.dp(this,64);
        for(String keys:new String[]{"123","456","789","*0#"}){
            LinearLayout r=Ui.row(this);
            for(char key:keys.toCharArray()){
                DialKey v=new DialKey(this,key,Character.isDigit(key)?letters[key-'0']:"");
                r.addView(v,new LinearLayout.LayoutParams(0,rowHeight,1));
                v.setOnClickListener(w->{feedback.play(w,key);textEntryMode=false;search.setShowSoftInputOnFocus(false);int a=Math.max(0,search.getSelectionStart()),b=Math.max(a,search.getSelectionEnd());search.getText().replace(a,b,String.valueOf(key));});
                if(key=='0')v.setOnLongClickListener(w->{int a=Math.max(0,search.getSelectionStart());if(a>0&&search.getText().charAt(a-1)=='0')search.getText().replace(a-1,a,"+");else search.getText().insert(a,"+");return true;});
            }
            keypad.addView(r,new LinearLayout.LayoutParams(-1,rowHeight));
        }
        callActions=Ui.row(this);callActions.setGravity(Gravity.CENTER);keypad.addView(callActions,new LinearLayout.LayoutParams(-1,Ui.dp(this,68)));root.addView(keypad);renderCallButtons();
        View divider=new View(this);divider.setBackgroundColor(0xffeeeeef);LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(-1,Ui.dp(this,1));line.topMargin=Ui.dp(this,8);divider.setVisibility(View.GONE);root.addView(divider,line);
        nav=Ui.row(this);nav.setVisibility(View.GONE);root.addView(nav,new LinearLayout.LayoutParams(-1,Ui.dp(this,64)));renderNav();
    }
    private android.graphics.drawable.Drawable ripple(int color,int radius){return new android.graphics.drawable.RippleDrawable(android.content.res.ColorStateList.valueOf(0x20000000),Ui.bg(color,Ui.dp(this,radius)),Ui.bg(0xffffffff,Ui.dp(this,radius)));}
    private void renderNav(){
        nav.removeAllViews();String[] names={"拨号键盘","通讯录","最近通话"};
        for(int i=0;i<3;i++){
            final int t=i;int color=tab==i?Ui.GREEN:Ui.MUTED;
            LinearLayout item=Ui.column(this);item.setGravity(Gravity.CENTER);item.setBackground(ripple(0x00000000,12));item.setContentDescription(names[i]);item.setOnClickListener(v->{tab=t;textEntryMode=false;search.setText("");((android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(search.getWindowToken(),0);search.clearFocus();render();});
            item.addView(new AppIcon(this,i,color),new LinearLayout.LayoutParams(Ui.dp(this,37),Ui.dp(this,37)));
            TextView label=Ui.text(this,names[i],10,color);label.setGravity(Gravity.CENTER);item.addView(label);nav.addView(item,new LinearLayout.LayoutParams(0,-1,1));
        }
    }
    private void render(){title.setText(tab==0?"电话":tab==1?"通讯录":"最近通话");keypad.setVisibility(tab==0?View.VISIBLE:View.GONE);renderNav();renderResults();}
    private void status(){boolean ready=isDefault()&&has(Manifest.permission.READ_CONTACTS)&&has(Manifest.permission.CALL_PHONE)&&has(Manifest.permission.READ_CALL_LOG);banner.setText(ready?"✓ 默认电话应用 · 本机搜索":"启用轻拨 · 设置默认电话与权限");banner.setVisibility(ready?View.GONE:View.VISIBLE);}
    private void onboarding(){
        if(isFinishing()||getSharedPreferences("settings",0).getBoolean("onboarding_seen",false))return;
        new AlertDialog.Builder(this).setTitle("欢迎使用轻拨")
            .setMessage("几步设置，让拨号更顺手。\n\n① 设为默认电话，接听和拨打电话\n② 允许通讯录，支持中文与拼音快搜\n③ 允许通话记录和通知，查看最近通话、显示来电\n\n你同意系统授权后，轻拨会自动读取并显示本机数据，不会上传。")
            .setCancelable(false)
            .setPositiveButton("开始使用",(d,w)->{getSharedPreferences("settings",0).edit().putBoolean("onboarding_seen",true).apply();setup();})
            .setNegativeButton("稍后设置",(d,w)->getSharedPreferences("settings",0).edit().putBoolean("onboarding_seen",true).apply()).show();
    }
    private void setup(){
        if(!isDefault()){
            try{if(Build.VERSION.SDK_INT>=29){RoleManager rm=getSystemService(RoleManager.class);if(rm.isRoleAvailable(RoleManager.ROLE_DIALER)){startActivityForResult(rm.createRequestRoleIntent(RoleManager.ROLE_DIALER),20);return;}}
            else {startActivityForResult(new Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER).putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME,getPackageName()),20);return;}}
            catch(ActivityNotFoundException e){toast("此设备不支持设置默认电话应用");}
        }permissions();
    }
    private void permissions(){ArrayList<String> ps=new ArrayList<>();for(String p:new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.CALL_PHONE,Manifest.permission.READ_PHONE_STATE})if(!has(p))ps.add(p);if(isDefault()&&!has(Manifest.permission.READ_CALL_LOG))ps.add(Manifest.permission.READ_CALL_LOG);if(Build.VERSION.SDK_INT>=33&&!has(Manifest.permission.POST_NOTIFICATIONS))ps.add(Manifest.permission.POST_NOTIFICATIONS);if(!ps.isEmpty()){boolean restricted=false;for(String p:ps)if(checkSelfPermission(p)==PackageManager.PERMISSION_GRANTED&&!has(p))restricted=true;if(restricted){toast("请在系统设置中允许通讯录和通话记录访问");startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName())));}else requestPermissions(ps.toArray(new String[0]),21);}else{status();reload();}}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==20){status();permissions();}else if(r==31&&c==RESULT_OK&&d!=null&&d.getIntExtra("navigation_epoch",-1)==((DialApplication)getApplication()).navigationEpoch()){if("setup".equals(d.getStringExtra("settings_action")))setup();else{tab=d.getIntExtra("tab",0);textEntryMode=false;search.setText("");render();}}}
    @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);status();reload();}
    private void settings(){startActivityForResult(new Intent(this,SettingsActivity.class),31);}
    void showDialHome(){if(search==null)return;tab=0;textEntryMode=false;search.setText("");search.clearFocus();render();}
    private void renderCallButtons(){
        if(callActions==null)return;callActions.removeAllViews();
        List<PhoneAccountHandle> accounts=new ArrayList<>();
        if(getSharedPreferences("settings",0).getBoolean("dual_sim_buttons",false)&&has(Manifest.permission.READ_PHONE_STATE)){
            try{TelecomManager tm=getSystemService(TelecomManager.class);for(PhoneAccountHandle account:tm.getCallCapablePhoneAccounts()){PhoneAccount pa=tm.getPhoneAccount(account);if(pa!=null&&pa.hasCapabilities(PhoneAccount.CAPABILITY_SIM_SUBSCRIPTION))accounts.add(account);}}catch(SecurityException ignored){}
        }
        if(accounts.size()>=2){Map<PhoneAccountHandle,Integer> slots=new HashMap<>();for(int i=0;i<accounts.size();i++)slots.put(accounts.get(i),simNumber(accounts.get(i),i+1));accounts.sort((a,b)->Integer.compare(slots.get(a),slots.get(b)));for(int i=0;i<2;i++){PhoneAccountHandle account=accounts.get(i);PhoneAccount pa=getSystemService(TelecomManager.class).getPhoneAccount(account);String label=pa==null?"拨号":String.valueOf(pa.getLabel());dialButton(label,account,slots.get(account));}}
        else dialButton("拨号",null,0);
    }
    private int simNumber(PhoneAccountHandle account,int fallback){
        if(Build.VERSION.SDK_INT>=30)try{android.telephony.TelephonyManager phone=getSystemService(android.telephony.TelephonyManager.class).createForPhoneAccountHandle(account);if(phone!=null){android.telephony.SubscriptionInfo info=getSystemService(android.telephony.SubscriptionManager.class).getActiveSubscriptionInfo(phone.getSubscriptionId());if(info!=null&&info.getSimSlotIndex()>=0)return info.getSimSlotIndex()+1;}}catch(SecurityException ignored){}
        return fallback;
    }
    private void dialButton(String label,PhoneAccountHandle account,int simNumber){
        boolean dual=simNumber>0;
        LinearLayout button=Ui.row(this);button.setGravity(Gravity.CENTER);button.setBackground(ripple(0xff28b45c,25));button.setContentDescription(label+"，拨打第一条匹配联系人");button.setOnClickListener(v->callTyped(account));
        android.widget.FrameLayout symbol=new android.widget.FrameLayout(this);symbol.addView(new AppIcon(this,AppIcon.PHONE,0xffffffff),new android.widget.FrameLayout.LayoutParams(Ui.dp(this,30),Ui.dp(this,30),Gravity.LEFT|Gravity.CENTER_VERTICAL));
        if(dual){TextView badge=Ui.text(this,String.valueOf(simNumber),8,0xffffffff);badge.setTypeface(null,android.graphics.Typeface.BOLD);badge.setGravity(Gravity.CENTER);badge.setIncludeFontPadding(false);android.widget.FrameLayout.LayoutParams bp=new android.widget.FrameLayout.LayoutParams(Ui.dp(this,10),Ui.dp(this,14),Gravity.TOP|Gravity.RIGHT);bp.topMargin=Ui.dp(this,3);bp.rightMargin=Ui.dp(this,3);symbol.addView(badge,bp);button.setContentDescription("卡"+simNumber+"，"+label+"，拨打第一条匹配联系人");}
        button.addView(symbol,new LinearLayout.LayoutParams(Ui.dp(this,dual?26:30),Ui.dp(this,32)));
        TextView text=Ui.text(this,label,dual?13:16,0xffffffff);text.setSingleLine(true);text.setEllipsize(android.text.TextUtils.TruncateAt.END);text.setMaxWidth(Ui.dp(this,100));text.setPadding(Ui.dp(this,3),0,Ui.dp(this,7),0);button.addView(text);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(dual?0:Ui.dp(this,156),Ui.dp(this,46),dual?1:0);if(dual)lp.setMargins(Ui.dp(this,6),0,Ui.dp(this,6),0);callActions.addView(button,lp);
    }
    private void ensureData(){
        boolean contactsAllowed=has(Manifest.permission.READ_CONTACTS),logsAllowed=has(Manifest.permission.READ_CALL_LOG);
        if(!contactsAllowed)contacts=new ArrayList<>();if(!logsAllowed)recents=new ArrayList<>();
        if(cacheLoaded){if(dataDirty||contactsAllowed!=cacheContactsAllowed||logsAllowed!=cacheLogsAllowed||System.currentTimeMillis()-cacheSavedAt>CACHE_FRESH_MS)reload();else renderResults();return;}
        cacheLoaded=true;final int token=++generation;
        worker.execute(()->{
            Snapshot cached=null;
            try(java.io.ObjectInputStream input=new java.io.ObjectInputStream(new android.util.AtomicFile(new java.io.File(getFilesDir(),"local-cache-v1.bin")).openRead())){Object value=input.readObject();if(value instanceof Snapshot)cached=(Snapshot)value;}catch(java.io.IOException|ClassNotFoundException|RuntimeException ignored){}
            Snapshot result=cached;handler.post(()->{
                if(isDestroyed()||token!=generation)return;
                boolean readContacts=has(Manifest.permission.READ_CONTACTS),readLogs=has(Manifest.permission.READ_CALL_LOG);
                if(result!=null){contacts=readContacts?result.contacts:new ArrayList<>();recents=readLogs?result.recents:new ArrayList<>();cacheSavedAt=result.savedAt;cacheContactsAllowed=result.contactsAllowed;cacheLogsAllowed=result.logsAllowed;loaded=true;renderResults();}
                if(result==null||readContacts!=cacheContactsAllowed||readLogs!=cacheLogsAllowed||System.currentTimeMillis()-cacheSavedAt>CACHE_FRESH_MS)reload();
            });
        });
    }
    private void saveCache(ArrayList<SearchIndex> cs,ArrayList<Recent> rs,boolean contactsAllowed,boolean logsAllowed,long savedAt){
        Snapshot snapshot=new Snapshot();snapshot.contacts=cs;snapshot.recents=rs;snapshot.savedAt=savedAt;snapshot.contactsAllowed=contactsAllowed;snapshot.logsAllowed=logsAllowed;
        android.util.AtomicFile file=new android.util.AtomicFile(new java.io.File(getFilesDir(),"local-cache-v1.bin"));java.io.FileOutputStream stream=null;
        try{stream=file.startWrite();java.io.ObjectOutputStream output=new java.io.ObjectOutputStream(stream);output.writeObject(snapshot);output.flush();file.finishWrite(stream);}catch(java.io.IOException|RuntimeException ignored){if(stream!=null)file.failWrite(stream);}
    }
    private void reload(){dataDirty=false;int token=++generation;boolean readContacts=has(Manifest.permission.READ_CONTACTS),readLogs=has(Manifest.permission.READ_CALL_LOG);worker.execute(()->{
        ArrayList<SearchIndex> cs=new ArrayList<>();ArrayList<Recent> rs=new ArrayList<>();String error="";
        try{if(readContacts){Transliterator tr=Transliterator.getInstance("Han-Latin; Latin-ASCII; Lower");Set<String> seen=new HashSet<>();try(Cursor c=getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,new String[]{"display_name","data1"},null,null,"display_name COLLATE LOCALIZED ASC")){if(c!=null)while(c.moveToNext()){String name=c.getString(0),number=c.getString(1);if(number!=null&&seen.add(name+"|"+SearchIndex.digits(number)))cs.add(new SearchIndex(name,number,tr::transliterate));}}}
        if(readLogs)try(Cursor c=getContentResolver().query(CallLog.Calls.CONTENT_URI,new String[]{CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE,CallLog.Calls.DURATION},null,null,CallLog.Calls.DATE+" DESC")){if(c!=null)while(c.moveToNext()&&rs.size()<500)rs.add(new Recent(c.getString(0),c.getString(1),c.getInt(2),c.getLong(3),c.getLong(4)));}}
        catch(RuntimeException e){android.util.Log.e("QingDialData","Local data loading failed",e);error="数据读取失败，请检查权限后重试";}
        long savedAt=System.currentTimeMillis();if(error.isEmpty())saveCache(cs,rs,readContacts,readLogs,savedAt);String resultError=error;handler.post(()->{if(isDestroyed()||token!=generation)return;contacts=cs;recents=rs;cacheLoaded=true;cacheSavedAt=savedAt;cacheContactsAllowed=readContacts;cacheLogsAllowed=readLogs;dataError=resultError;loaded=true;status();renderResults();});
    });}
    private void renderResults(){if(results==null)return;results.setVisibility(View.VISIBLE);searchGeneration++;if(pendingSearch!=null)handler.removeCallbacks(pendingSearch);String q=search.getText().toString().trim();if(tab==2||q.isEmpty()||!dataError.isEmpty()||!has(Manifest.permission.READ_CONTACTS))results.removeAllViews();count.setVisibility(tab==0?View.GONE:View.VISIBLE);
        if(!dataError.isEmpty()){empty(dataError);return;}
        if(tab==2){renderHistory(q);return;}
        if(!has(Manifest.permission.READ_CONTACTS)){count.setText("中文 · 全拼 · 首字母 · T9");empty("允许通讯录权限后，即可搜索联系人");return;}
        
        searchContacts(q);
    }
    private void renderHistory(String query){
        count.setText("最近 500 条记录 · 点击回拨");
        if(!has(Manifest.permission.READ_CALL_LOG)){empty("设置为默认电话并允许读取通话记录");return;}
        long incomingSeconds=0,outgoingSeconds=0;int incomingCount=0,outgoingCount=0;
        for(Recent r:recents){if(r.type==CallLog.Calls.INCOMING_TYPE){incomingSeconds+=Math.max(0,r.duration);incomingCount++;}else if(r.type==CallLog.Calls.OUTGOING_TYPE){outgoingSeconds+=Math.max(0,r.duration);outgoingCount++;}}
        LinearLayout summary=Ui.row(this);
        historyTotal(summary,"呼入",incomingCount,incomingSeconds,0xff299e69,0xffedf8f1);
        historyTotal(summary,"呼出",outgoingCount,outgoingSeconds,0xff3977d5,0xffeef4fd);
        LinearLayout.LayoutParams summaryLayout=new LinearLayout.LayoutParams(-1,Ui.dp(this,88));summaryLayout.bottomMargin=Ui.dp(this,10);results.addView(summary,summaryLayout);
        int shown=0;
        for(Recent r:recents){
            if(!query.isEmpty()&&!(r.number!=null&&r.number.contains(query))&&!(r.name!=null&&r.name.contains(query)))continue;
            String name=r.name==null||r.name.isEmpty()?r.number:r.name;
            boolean out=r.type==CallLog.Calls.OUTGOING_TYPE,missed=r.type==CallLog.Calls.MISSED_TYPE;
            String direction=out?"呼出":missed?"未接":r.type==CallLog.Calls.REJECTED_TYPE?"已拒接":r.type==CallLog.Calls.BLOCKED_TYPE?"已拦截":"呼入";
            int color=out?0xff3977d5:missed?0xffd34b4b:r.type==CallLog.Calls.INCOMING_TYPE?0xff299e69:Ui.MUTED;
            String date=new SimpleDateFormat("MM-dd HH:mm",Locale.CHINA).format(new Date(r.date));
            String duration=r.duration>0?" · "+durationText(r.duration):"";
            String detail=direction+" · "+date+duration;
            TextView title=addPerson(name,r.number,detail,missed);title.setTextColor(color);
            LinearLayout labels=(LinearLayout)title.getParent();TextView secondary=(TextView)labels.getChildAt(1);
            android.text.SpannableString styled=new android.text.SpannableString(detail);styled.setSpan(new android.text.style.ForegroundColorSpan(color),0,direction.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if(r.duration>0)styled.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),detail.length()-durationText(r.duration).length(),detail.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            secondary.setText(styled);shown++;
        }
        if(shown==0)results.addView(Ui.text(this,loaded?"暂无通话记录":"正在读取…",14,Ui.MUTED));
    }
    private String durationText(long seconds){return String.format(Locale.CHINA,"%d分%02d秒",seconds/60,seconds%60);}
    private void historyTotal(LinearLayout parent,String label,int count,long seconds,int color,int background){
        LinearLayout card=Ui.column(this);card.setPadding(Ui.dp(this,14),Ui.dp(this,12),Ui.dp(this,10),Ui.dp(this,10));card.setBackground(Ui.bg(background,Ui.dp(this,15)));
        card.addView(Ui.text(this,label+" · "+count+"次",12,color));TextView total=Ui.text(this,durationText(seconds),20,color);total.setTypeface(null,android.graphics.Typeface.BOLD);card.addView(total);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(Ui.dp(this,4),0,Ui.dp(this,4),0);parent.addView(card,lp);
    }
    private void searchContacts(String q){
        results.setVisibility(View.INVISIBLE);
        final int token=searchGeneration;
        final List<SearchIndex> snapshot=contacts;
        pendingSearch=()->searchWorker.execute(()->{
            SearchIndex.Query query=new SearchIndex.Query(q);
            ArrayList<SearchIndex> matches=new ArrayList<>();
            Map<SearchIndex,Integer> scores=new IdentityHashMap<>();
            for(SearchIndex contact:snapshot){
                if(Thread.currentThread().isInterrupted())return;
                int score=contact.score(query);
                if(score>0){matches.add(contact);scores.put(contact,score);}
            }
            matches.sort((a,b)->Integer.compare(scores.get(b),scores.get(a)));
            handler.post(()->{
                if(isDestroyed()||token!=searchGeneration)return;
                results.setVisibility(View.VISIBLE);results.removeAllViews();count.setText(matches.size()+" 个匹配 · 点击联系人拨号");
                for(SearchIndex contact:matches.subList(0,Math.min(100,matches.size()))){
                    TextView label=addPerson(contact.name,contact.number,contact.number,false);
                    android.text.SpannableString highlighted=new android.text.SpannableString(contact.name);
                    for(int at:contact.matchedNamePositions(q)){
                        int end=at+Character.charCount(contact.name.codePointAt(at));
                        highlighted.setSpan(new android.text.style.ForegroundColorSpan(0xffd65a12),at,end,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        highlighted.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),at,end,android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                    label.setText(highlighted);
                }
                if(matches.isEmpty())empty(loaded?"未找到联系人，可直接拨打输入的号码":"正在读取…");
                else if(matches.size()>100)results.addView(Ui.text(this,"显示前 100 个结果，请继续输入缩小范围",12,Ui.MUTED));
            });
        });
        handler.postDelayed(pendingSearch,65);
    }

    private void empty(String message){TextView v=Ui.text(this,message,13,Ui.MUTED);v.setGravity(Gravity.CENTER);v.setPadding(0,Ui.dp(this,14),0,Ui.dp(this,14));results.addView(v,new LinearLayout.LayoutParams(-1,-1));}
    private TextView addPerson(String name,String number,String detail,boolean missed){
        LinearLayout row=Ui.row(this);row.setPadding(Ui.dp(this,8),Ui.dp(this,10),Ui.dp(this,8),Ui.dp(this,10));row.setBackground(ripple(0xffffffff,0));
        LinearLayout labels=Ui.column(this);
        TextView nameView=Ui.text(this,name==null||name.isEmpty()?"未知号码":name,20,missed?0xffcc4e4e:0xff4bab7d);nameView.setSingleLine(true);nameView.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(nameView);
        TextView secondary=Ui.text(this,detail==null?"隐藏号码":detail,14,0xff777777);secondary.setSingleLine(true);secondary.setEllipsize(android.text.TextUtils.TruncateAt.END);labels.addView(secondary);
        row.addView(labels,new LinearLayout.LayoutParams(0,Ui.dp(this,50),1));
        AppIcon arrow=new AppIcon(this,AppIcon.CHEVRON,0xffa6a6a6);arrow.setBackground(Ui.bg(0xfff3f3f3,Ui.dp(this,18)));arrow.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);arrow.setContentDescription("查看联系人通话详情");arrow.setOnClickListener(v->startActivity(new Intent(this,ContactDetailsActivity.class).putExtra("name",name).putExtra("number",number)));row.addView(arrow,new LinearLayout.LayoutParams(Ui.dp(this,36),Ui.dp(this,40)));
        row.setContentDescription((name==null?"未知号码":name)+"，"+detail+"，拨打");row.setOnClickListener(v->place(number));results.addView(row,new LinearLayout.LayoutParams(-1,Ui.dp(this,76)));return nameView;
    }
    private void callTyped(PhoneAccountHandle account){
        String query=search.getText().toString().trim();
        if(query.isEmpty()){toast("请先输入姓名或号码");return;}
        SearchIndex first=SearchIndex.firstMatch(contacts,query);
        if(first!=null){place(first.number,account);return;}
        if(query.matches("[+0-9*#(),; .-]+"))place(query,account);
        else toast("没有匹配的联系人");
    }
    private void place(String number){place(number,null);}
    private void place(String number,PhoneAccountHandle account){if(number==null||!number.matches("[+0-9*#(),; .-]+")){toast("该记录没有可拨打的号码");return;}if(!isDefault()||!has(Manifest.permission.CALL_PHONE)){toast("请先设置默认电话并允许拨号权限");setup();return;}try{Bundle extras=new Bundle();if(account!=null)extras.putParcelable(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE,account);getSystemService(TelecomManager.class).placeCall(Uri.fromParts("tel",number.replace(" ","").replace("-",""),null),extras);}catch(SecurityException|IllegalArgumentException e){toast("拨号失败，请检查电话权限、SIM 卡与系统设置");}}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
