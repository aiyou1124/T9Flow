package com.qingdial.app;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.telecom.TelecomManager;
import android.view.*;
import android.widget.*;

/** Theme-matched settings; changes are persisted as soon as a switch changes. */
public class SettingsActivity extends HomeAwareActivity {
    private LinearLayout root,body;
    private int navigationEpoch;
    private final int subtle=0xfff4f7f5;
    @Override public void onCreate(Bundle state){super.onCreate(state);navigationEpoch=((DialApplication)getApplication()).navigationEpoch();}
    @Override public void onResume(){super.onResume();build();}
    private void build(){
        root=Ui.column(this);root.setBackgroundColor(subtle);root.setFocusableInTouchMode(true);setContentView(root);Ui.edge(this,root);
        LinearLayout toolbar=Ui.row(this);
        TextView back=Ui.button(this,"‹",Ui.INK,this::finish);back.setTextSize(34);back.setContentDescription("返回");back.setBackgroundColor(subtle);toolbar.addView(back,new LinearLayout.LayoutParams(Ui.dp(this,42),Ui.dp(this,54)));
        TextView title=Ui.text(this,"设置",25,Ui.INK);title.setTypeface(null,Typeface.BOLD);toolbar.addView(title);root.addView(toolbar);
        ScrollView scroll=new ScrollView(this);scroll.setVerticalScrollBarEnabled(false);body=Ui.column(this);body.setPadding(0,Ui.dp(this,12),0,Ui.dp(this,20));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        LinearLayout brand=Ui.row(this);brand.setPadding(Ui.dp(this,16),Ui.dp(this,18),Ui.dp(this,16),Ui.dp(this,18));brand.setBackground(Ui.bg(0xffffffff,Ui.dp(this,22)));
        ImageView icon=new ImageView(this);icon.setImageResource(R.mipmap.ic_launcher);brand.addView(icon,new LinearLayout.LayoutParams(Ui.dp(this,56),Ui.dp(this,56)));
        LinearLayout text=Ui.column(this);text.setPadding(Ui.dp(this,14),0,0,0);TextView name=Ui.text(this,"轻拨",23,Ui.INK);name.setTypeface(null,Typeface.BOLD);text.addView(name);text.addView(Ui.text(this,"中文 T9 · 简单联系",12,Ui.MUTED));brand.addView(text,new LinearLayout.LayoutParams(0,-2,1));
        TextView version=Ui.text(this,versionName(),12,Ui.GREEN);brand.addView(version);body.addView(brand);
        boolean defaultPhone=getPackageName().equals(getSystemService(TelecomManager.class).getDefaultDialerPackage());
        boolean contacts=checkSelfPermission(Manifest.permission.READ_CONTACTS)==PackageManager.PERMISSION_GRANTED;
        boolean logs=checkSelfPermission(Manifest.permission.READ_CALL_LOG)==PackageManager.PERMISSION_GRANTED;
        section("电话服务");LinearLayout service=card();
        row(service,AppIcon.PHONE,"默认电话与权限",defaultPhone&&contacts&&logs?"已启用 · 通讯录与通话记录可用":"完成设置，启用完整拨号体验",defaultPhone&&contacts&&logs?"已启用":"去设置",()->result("setup",0));
        section("使用体验");LinearLayout experience=card();
        toggle(experience,AppIcon.RECENTS,"按键音","短促拨号音，静音模式下不播放","sound",true);
        toggle(experience,AppIcon.KEYPAD,"触感反馈","按下数字时轻振，跟随系统触感设置","haptic",true);
        toggle(experience,AppIcon.PHONE,"双卡拨号按钮","开启后，两张可用 SIM 卡分别显示拨号键","dual_sim_buttons",false);
        section("快捷入口");LinearLayout shortcuts=card();
        row(shortcuts,AppIcon.CONTACTS,"通讯录","查看本机联系人",null,()->result("tab",1));
        row(shortcuts,AppIcon.RECENTS,"最近通话","查看和回拨最近的电话",null,()->result("tab",2));
        row(shortcuts,AppIcon.KEYPAD,"拨号键盘","返回首页，快速查找并拨号",null,()->result("tab",0));
        section("系统设置");LinearLayout system=card();
        row(system,AppIcon.SETTINGS,"应用权限","管理通讯录、电话、通知等授权",null,()->open(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:"+getPackageName()))));
        row(system,AppIcon.PHONE,"来电全屏显示","允许锁屏时显示接听界面",null,()->{if(Build.VERSION.SDK_INT>=34)open(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT,Uri.parse("package:"+getPackageName())));else Toast.makeText(this,"当前系统自动管理来电全屏显示",Toast.LENGTH_SHORT).show();});
        row(system,AppIcon.SETTINGS,"关于轻拨","本机搜索 · 不上传通讯录",null,()->new AlertDialog.Builder(this).setTitle("轻拨 "+versionName()).setMessage("中文姓名按从左到右顺序进行 T9 匹配，橙色标出命中的字。\n\n通讯录及通话记录缓存仅保存在本机应用目录，不上传。系统权限由你授权，可随时在系统设置撤销。\n\n声音采用标准 DTMF，触感反馈跟随系统设置。").setPositiveButton("知道了",null).show());
        TextView footer=Ui.text(this,"本机处理，安心联系",12,Ui.MUTED);footer.setGravity(Gravity.CENTER);footer.setPadding(0,Ui.dp(this,24),0,0);body.addView(footer);root.requestFocus();
    }
    private void section(String title){TextView t=Ui.text(this,title,12,Ui.MUTED);t.setPadding(Ui.dp(this,14),Ui.dp(this,22),0,Ui.dp(this,8));body.addView(t);}
    private LinearLayout card(){LinearLayout c=Ui.column(this);c.setBackground(Ui.bg(0xffffffff,Ui.dp(this,18)));c.setClipToOutline(true);body.addView(c);return c;}
    private LinearLayout base(LinearLayout card,int icon,String title,String subtitle){
        if(card.getChildCount()>0){View line=new View(this);line.setBackgroundColor(0xfff0f2f1);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(this,1));lp.leftMargin=Ui.dp(this,58);card.addView(line,lp);}
        LinearLayout row=Ui.row(this);row.setPadding(Ui.dp(this,14),Ui.dp(this,13),Ui.dp(this,14),Ui.dp(this,13));
        AppIcon badge=new AppIcon(this,icon,Ui.GREEN);badge.setBackground(Ui.bg(0xffedf8f0,Ui.dp(this,10)));row.addView(badge,new LinearLayout.LayoutParams(Ui.dp(this,34),Ui.dp(this,34)));
        LinearLayout labels=Ui.column(this);labels.setPadding(Ui.dp(this,12),0,Ui.dp(this,8),0);TextView t=Ui.text(this,title,16,Ui.INK);labels.addView(t);TextView sub=Ui.text(this,subtitle,11,Ui.MUTED);sub.setPadding(0,Ui.dp(this,4),0,0);labels.addView(sub);row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));card.addView(row);return row;
    }
    private void row(LinearLayout card,int icon,String title,String subtitle,String state,Runnable action){LinearLayout r=base(card,icon,title,subtitle);if(state!=null){TextView v=Ui.text(this,state,11,Ui.GREEN);r.addView(v);}r.addView(new AppIcon(this,AppIcon.CHEVRON,0xffb6bfba),new LinearLayout.LayoutParams(Ui.dp(this,22),Ui.dp(this,28)));r.setBackground(new android.graphics.drawable.RippleDrawable(ColorStateList.valueOf(0x15000000),null,Ui.bg(0xffffffff,0)));r.setOnClickListener(v->action.run());}
    private void toggle(LinearLayout card,int icon,String title,String subtitle,String key,boolean defaultValue){LinearLayout r=base(card,icon,title,subtitle);Switch sw=new Switch(this);sw.setContentDescription(title);sw.setChecked(getSharedPreferences("settings",0).getBoolean(key,defaultValue));sw.setThumbTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{Ui.GREEN,0xffb7c0bb}));sw.setTrackTintList(new ColorStateList(new int[][]{new int[]{android.R.attr.state_checked},new int[]{}},new int[]{0xffb6e5c5,0xffe5e9e6}));sw.setMinHeight(Ui.dp(this,48));sw.setOnCheckedChangeListener((v,on)->getSharedPreferences("settings",0).edit().putBoolean(key,on).apply());r.addView(sw);r.setOnClickListener(v->sw.setChecked(!sw.isChecked()));}
    private void result(String action,int tab){setResult(RESULT_OK,new Intent().putExtra("settings_action",action).putExtra("tab",tab).putExtra("navigation_epoch",navigationEpoch));finish();}
    private String versionName(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(PackageManager.NameNotFoundException e){return "";}}
    private void open(Intent intent){try{startActivity(intent);}catch(ActivityNotFoundException e){Toast.makeText(this,"请在系统设置中管理此权限",Toast.LENGTH_SHORT).show();}}
}
