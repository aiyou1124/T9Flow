package com.qingdial.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.RippleDrawable;
import android.view.View;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/** A circular key with separate type scales for the digit and alphabet. */
final class DialKey extends View {
    private final char digit;
    private final String letters;
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface digitFont=Typeface.create("sans-serif-light",Typeface.NORMAL);
    private final Typeface letterFont=Typeface.create("sans-serif-medium",Typeface.NORMAL);
    private final Runnable longPress=()->{if(isPressed())performLongClick();};
    DialKey(Context context,char digit,String letters){
        super(context);this.digit=digit;this.letters=letters;
        setBackground(new RippleDrawable(ColorStateList.valueOf(0x15000000),Ui.bg(0xffffffff,0),Ui.bg(0xffffffff,0)));
        setContentDescription(digit+" "+letters);setFocusable(true);
    }
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        float density=getResources().getDisplayMetrics().density;
        float scale=Math.min(getWidth(),getHeight())/(76f*density);
        paint.setColor(Ui.INK);paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(digitFont);
        paint.setTextSize((digit=='*'?41:34)*density*scale);
        float baseline=getHeight()/2f+4*density*scale;
        if(digit=='*')baseline+=5*density*scale;
        if(digit=='#')baseline+=4*density*scale;
        canvas.drawText(String.valueOf(digit),getWidth()/2f,baseline,paint);
        if(!letters.isEmpty()){
            paint.setTypeface(letterFont);
            paint.setColor(0xff777777);
            paint.setTextSize((digit=='0'?14:9)*density*scale);
            canvas.drawText(letters,getWidth()/2f,getHeight()/2f+21*density*scale,paint);
        }
    }
    @Override public boolean onTouchEvent(MotionEvent e){
        if(!isEnabled())return false;
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                drawableHotspotChanged(e.getX(),e.getY());
                setPressed(true);
                performClick();
                if(isLongClickable())postDelayed(longPress,ViewConfiguration.getLongPressTimeout());
                return true;
            case MotionEvent.ACTION_MOVE:
                int slop=ViewConfiguration.get(getContext()).getScaledTouchSlop();
                if(e.getX() < -slop || e.getY() < -slop || e.getX()>getWidth()+slop || e.getY()>getHeight()+slop){setPressed(false);removeCallbacks(longPress);}
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);removeCallbacks(longPress);return true;
            default:return true;
        }
    }
    @Override protected void onDetachedFromWindow(){removeCallbacks(longPress);super.onDetachedFromWindow();}
    @Override public boolean performClick(){super.performClick();return true;}
}
