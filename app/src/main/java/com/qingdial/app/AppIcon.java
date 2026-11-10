package com.qingdial.app;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Small, consistent vector-style icons rendered at any display density. */
final class AppIcon extends View {
    static final int KEYPAD=0, CONTACTS=1, RECENTS=2, PHONE=3, BACKSPACE=4, SETTINGS=5, SEARCH=6, CHEVRON=7, PIN=8, UP=9, DOWN=10, TOP=11, UNPIN=12;
    private final int kind,color;
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    AppIcon(Context c,int kind,int color){super(c);this.kind=kind;this.color=color;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){
        super.onDraw(c);float size=Math.min(getWidth(),getHeight())*.57f;
        c.save();c.translate((getWidth()-size)/2,(getHeight()-size)/2);c.scale(size/24,size/24);
        p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
        if(kind==KEYPAD){p.setStyle(Paint.Style.FILL);for(int y=4;y<=20;y+=8)for(int x=4;x<=20;x+=8)c.drawCircle(x,y,2,p);}
        else if(kind==CONTACTS){c.drawCircle(12,7,4,p);c.drawArc(4,13,20,27,180,180,false,p);}
        else if(kind==RECENTS){c.drawCircle(12,12,9,p);c.drawLine(12,6,12,12,p);c.drawLine(12,12,16,14,p);}
        else if(kind==PIN||kind==UNPIN){Path pin=new Path();pin.moveTo(8,3);pin.lineTo(16,3);pin.lineTo(15,9);pin.lineTo(18,13);pin.lineTo(6,13);pin.lineTo(9,9);pin.close();c.drawPath(pin,p);c.drawLine(12,13,12,21,p);if(kind==UNPIN)c.drawLine(3,3,21,21,p);}
        else if(kind==UP||kind==DOWN||kind==TOP){if(kind==DOWN){c.rotate(180,12,12);}c.drawLine(12,20,12,5,p);c.drawLine(6,11,12,5,p);c.drawLine(18,11,12,5,p);if(kind==TOP)c.drawLine(5,2,19,2,p);}
        else if(kind==SEARCH){c.drawCircle(10,10,6,p);c.drawLine(14.5f,14.5f,21,21,p);}
        else if(kind==BACKSPACE){Path path=new Path();path.moveTo(9,5);path.lineTo(22,5);path.lineTo(22,19);path.lineTo(9,19);path.lineTo(2,12);path.close();p.setStyle(Paint.Style.FILL);c.drawPath(path,p);p.setColor(0xffffffff);p.setStyle(Paint.Style.STROKE);c.drawLine(12,9,17,15,p);c.drawLine(17,9,12,15,p);}
        else if(kind==CHEVRON){c.drawLine(9,6,15,12,p);c.drawLine(15,12,9,18,p);}
        else if(kind==SETTINGS){c.drawCircle(12,12,4,p);for(int j=0;j<8;j++){c.save();c.rotate(j*45,12,12);c.drawLine(12,2,12,4,p);c.restore();}c.drawCircle(12,12,8,p);}
        else {p.setStyle(Paint.Style.FILL);Path path=new Path();path.moveTo(5,2);path.cubicTo(2,2,1,5,2,8);path.cubicTo(4,15,9,20,16,22);path.cubicTo(19,23,22,22,22,19);path.lineTo(22,16);path.lineTo(16,13);path.lineTo(13,16);path.cubicTo(10,14,8,12,7,9);path.lineTo(10,6);path.lineTo(7,2);path.close();c.drawPath(path,p);}
        c.restore();
    }
}
