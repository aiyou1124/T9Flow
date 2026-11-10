package com.qingdial.app;

import android.app.Activity;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.content.*;
import android.net.Uri;
import android.telecom.TelecomManager;
import android.widget.Toast;

/** Places calls through system Telecom; the system phone owns the call UI. */
final class SystemDialer {
    static Intent intent(Context context,String number){
        String value=number==null?"":number.replace(" ","").replace("-","");
        Intent intent=new Intent(Intent.ACTION_DIAL,Uri.fromParts("tel",value,null)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        TelecomManager telecom=context.getSystemService(TelecomManager.class);
        if(telecom!=null){
            String target=telecom.getDefaultDialerPackage();
            if(target==null||target.equals(context.getPackageName()))target=telecom.getSystemDialerPackage();
            if(target!=null&&!target.equals(context.getPackageName()))intent.setPackage(target);
        }
        return intent;
    }
    static void open(Activity activity,String number){
        if(number!=null&&!number.matches("[+0-9*#(),; .-]+")){Toast.makeText(activity,"没有可用的电话号码",Toast.LENGTH_SHORT).show();return;}
        if(number!=null){
            String value=number.replace(" ","").replace("-","").replace(".","");
            if(value.isEmpty()){Toast.makeText(activity,"请输入电话号码",Toast.LENGTH_SHORT).show();return;}
            if(activity.checkSelfPermission(Manifest.permission.CALL_PHONE)!=PackageManager.PERMISSION_GRANTED){
                if(activity instanceof HomeAwareActivity)((HomeAwareActivity)activity).requestDirectCall(value);
                return;
            }
            TelecomManager telecom=activity.getSystemService(TelecomManager.class);
            if(telecom==null){Toast.makeText(activity,"此设备不支持电话通话",Toast.LENGTH_SHORT).show();return;}
            try{
                telecom.placeCall(Uri.fromParts("tel",value,null),new Bundle());
                if(activity instanceof MainActivity)((MainActivity)activity).showDialHome();
            }catch(SecurityException denied){Toast.makeText(activity,"无法拨打，请在系统设置允许电话权限",Toast.LENGTH_LONG).show();}
            catch(RuntimeException unavailable){Toast.makeText(activity,"拨打失败，请检查系统电话和 SIM 卡设置",Toast.LENGTH_LONG).show();}
            return;
        }
        Intent intent=intent(activity,null);
        if(activity instanceof MainActivity)((MainActivity)activity).showDialHome();
        try{activity.startActivity(intent);}catch(ActivityNotFoundException e){
            try{intent.setPackage(null);activity.startActivity(intent);}catch(ActivityNotFoundException|SecurityException unavailable){Toast.makeText(activity,"未找到系统电话应用",Toast.LENGTH_SHORT).show();}
        }catch(SecurityException denied){Toast.makeText(activity,"无法打开系统电话，请检查系统默认应用设置",Toast.LENGTH_SHORT).show();}
    }
}
