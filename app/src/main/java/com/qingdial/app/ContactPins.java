package com.qingdial.app;

import android.app.*;
import android.content.Context;
import org.json.*;
import java.util.*;

/** App-private ordering; does not modify the system address book. */
final class ContactPins {
    static PinOrder read(Context context){
        ArrayList<String> saved=new ArrayList<>();
        try{JSONArray array=new JSONArray(context.getSharedPreferences("contact_pins",0).getString("order","[]"));for(int i=0;i<array.length();i++){Object key=array.opt(i);if(key instanceof String)saved.add((String)key);}}catch(JSONException ignored){}
        return new PinOrder(saved);
    }
    private static void save(Context context,PinOrder order){context.getSharedPreferences("contact_pins",0).edit().putString("order",new JSONArray(order.saved()).toString()).apply();}
    static void actions(Activity activity,String name,String number,Runnable changed){
        if(PinOrder.key(name,number).isEmpty())return;
        PinOrder order=read(activity);int at=order.position(name,number);
        ArrayList<ThemeDialog.Action> actions=new ArrayList<>();
        java.util.function.Consumer<Runnable> apply=change->{change.run();save(activity,order);changed.run();};
        if(at<0)actions.add(new ThemeDialog.Action("置顶联系人",AppIcon.PIN,false,()->apply.accept(()->order.pin(name,number))));
        else{
            if(at>0){actions.add(new ThemeDialog.Action("上移一位",AppIcon.UP,false,()->apply.accept(()->order.move(name,number,at-1))));actions.add(new ThemeDialog.Action("移至首位",AppIcon.TOP,false,()->apply.accept(()->order.move(name,number,0))));}
            if(at<order.saved().size()-1)actions.add(new ThemeDialog.Action("下移一位",AppIcon.DOWN,false,()->apply.accept(()->order.move(name,number,at+1))));
            actions.add(new ThemeDialog.Action("取消置顶",AppIcon.UNPIN,true,()->apply.accept(()->order.unpin(name,number))));
        }
        ThemeDialog.menu(activity,at<0?"联系人置顶":"调整置顶顺序",at<0?"常用联系人，放在更顺手的位置":"当前第 "+(at+1)+" 位 · 排序自动保存在本机",AppIcon.PIN,actions);
    }
}
