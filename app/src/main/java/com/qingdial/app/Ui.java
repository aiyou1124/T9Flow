package com.qingdial.app;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.*;

final class Ui {
    static final int BG=Color.WHITE, INK=Color.rgb(28,28,30), MUTED=Color.rgb(142,142,147), GREEN=Color.rgb(38,179,91);
    static int dp(Context c,float n){return (int)(n*c.getResources().getDisplayMetrics().density+.5f);}
    static GradientDrawable bg(int color,int radius){GradientDrawable b=new GradientDrawable();b.setColor(color);b.setCornerRadius(radius);return b;}
    static TextView text(Context c,String s,int size,int color){TextView v=new TextView(c);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    static LinearLayout column(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    static LinearLayout row(Context c){LinearLayout v=new LinearLayout(c);v.setGravity(Gravity.CENTER_VERTICAL);return v;}
    static TextView button(Context c,String label,int color,Runnable action){TextView v=text(c,label,16,color);v.setGravity(Gravity.CENTER);v.setMinHeight(dp(c,48));v.setPadding(dp(c,12),dp(c,8),dp(c,12),dp(c,8));v.setBackground(new RippleDrawable(ColorStateList.valueOf(0x22087f70),bg(Color.WHITE,dp(c,20)),null));v.setOnClickListener(w->action.run());return v;}
    static void edge(Activity a,View root){a.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(a,20)+insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop()+dp(a,8),dp(a,20)+insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom()+dp(a,8));return insets;});root.requestApplyInsets();}
}
