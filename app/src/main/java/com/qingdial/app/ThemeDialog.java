package com.qingdial.app;

import android.app.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;
import java.util.*;

/** Shared theme for all app-owned dialogs. System permission dialogs remain system-owned. */
final class ThemeDialog {
    static final class Action {
        final String label;final int icon;final boolean destructive;final Runnable run;
        Action(String label,int icon,boolean destructive,Runnable run){this.label=label;this.icon=icon;this.destructive=destructive;this.run=run;}
    }
    private static final class Panel {
        final Activity activity;final Dialog dialog;final LinearLayout body;
        Panel(Activity activity,String title,String subtitle,int icon){
            this.activity=activity;dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            ScrollView scroll=new ScrollView(activity);scroll.setFillViewport(false);scroll.setVerticalScrollBarEnabled(false);
            body=Ui.column(activity);body.setPadding(Ui.dp(activity,22),Ui.dp(activity,24),Ui.dp(activity,22),Ui.dp(activity,18));body.setBackground(Ui.bg(0xfff4f7f5,Ui.dp(activity,28)));scroll.addView(body);dialog.setContentView(scroll);
            LinearLayout heading=Ui.row(activity);AppIcon mark=new AppIcon(activity,icon,Ui.GREEN);mark.setBackground(Ui.bg(0xffe3f3e9,Ui.dp(activity,14)));heading.addView(mark,new LinearLayout.LayoutParams(Ui.dp(activity,46),Ui.dp(activity,46)));
            LinearLayout labels=Ui.column(activity);labels.setPadding(Ui.dp(activity,14),0,0,0);TextView name=Ui.text(activity,title,21,Ui.INK);name.setTypeface(null,Typeface.BOLD);labels.addView(name);
            if(subtitle!=null){TextView hint=Ui.text(activity,subtitle,12,0xff727e77);hint.setPadding(0,Ui.dp(activity,5),0,0);labels.addView(hint);}
            heading.addView(labels,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.bottomMargin=Ui.dp(activity,22);body.addView(heading,hp);
        }
        void show(){
            dialog.show();Window window=dialog.getWindow();if(window==null)return;
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams attributes=window.getAttributes();attributes.dimAmount=.32f;attributes.gravity=Gravity.CENTER;window.setAttributes(attributes);
            android.util.DisplayMetrics metrics=activity.getResources().getDisplayMetrics();int width=Math.min(metrics.widthPixels-Ui.dp(activity,32),Ui.dp(activity,420));int limit=(int)(metrics.heightPixels*.82f);
            body.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
            window.setLayout(width,Math.min(limit,body.getMeasuredHeight()));
        }
        void button(String label,boolean primary,Runnable action){
            TextView button=Ui.button(activity,label,primary?Color.WHITE:0xff69766e,()->{dialog.dismiss();if(action!=null)action.run();});button.setTypeface(null,primary?Typeface.BOLD:Typeface.NORMAL);
            button.setBackground(new RippleDrawable(ColorStateList.valueOf(0x18000000),Ui.bg(primary?Ui.GREEN:0xffe9eeeb,Ui.dp(activity,16)),null));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,Ui.dp(activity,50));lp.topMargin=Ui.dp(activity,12);body.addView(button,lp);
        }
    }
    static void menu(Activity activity,String title,String subtitle,int icon,List<Action> actions){
        Panel panel=new Panel(activity,title,subtitle,icon);LinearLayout choices=Ui.column(activity);choices.setBackground(Ui.bg(Color.WHITE,Ui.dp(activity,18)));choices.setClipToOutline(true);panel.body.addView(choices);
        for(Action action:actions){
            if(choices.getChildCount()>0){View divider=new View(activity);divider.setBackgroundColor(0xfff0f3f1);LinearLayout.LayoutParams line=new LinearLayout.LayoutParams(-1,Ui.dp(activity,1));line.leftMargin=Ui.dp(activity,54);line.rightMargin=Ui.dp(activity,16);choices.addView(divider,line);}
            LinearLayout row=Ui.row(activity);row.setPadding(Ui.dp(activity,12),Ui.dp(activity,7),Ui.dp(activity,16),Ui.dp(activity,7));int color=action.destructive?0xffc8584d:Ui.GREEN;
            AppIcon symbol=new AppIcon(activity,action.icon,color);row.addView(symbol,new LinearLayout.LayoutParams(Ui.dp(activity,34),Ui.dp(activity,34)));
            TextView label=Ui.text(activity,action.label,16,action.destructive?color:Ui.INK);label.setPadding(Ui.dp(activity,8),0,0,0);row.addView(label,new LinearLayout.LayoutParams(0,-2,1));
            row.setBackground(new RippleDrawable(ColorStateList.valueOf(0x14087f70),null,Ui.bg(Color.WHITE,0)));row.setOnClickListener(v->{panel.dialog.dismiss();action.run.run();});choices.addView(row,new LinearLayout.LayoutParams(-1,Ui.dp(activity,56)));
        }
        panel.button("取消",false,null);panel.show();
    }
    static void message(Activity activity,String title,String message,int icon,String primary,Runnable action,String secondary,Runnable cancel,boolean cancelable){
        Panel panel=new Panel(activity,title,null,icon);TextView copy=Ui.text(activity,message,15,0xff59665e);copy.setLineSpacing(Ui.dp(activity,4),1);copy.setPadding(0,0,0,Ui.dp(activity,6));panel.body.addView(copy);
        panel.button(primary,true,action);if(secondary!=null)panel.button(secondary,false,cancel);panel.dialog.setCancelable(cancelable);panel.dialog.setCanceledOnTouchOutside(cancelable);panel.show();
    }
}
