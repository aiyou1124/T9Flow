package com.qingdial.app;
import android.content.*;
import android.media.*;
import android.os.*;
import android.view.View;

/** Standard short DTMF; no Apple recordings or bundled audio assets. */
final class KeyFeedback {
    private final Context context;
    private ToneGenerator tone;
    private final HandlerThread audioThread=new HandlerThread("key-audio");
    private final Handler audioHandler;
    private final AudioManager audio;
    private final SharedPreferences prefs;
    KeyFeedback(Context c){context=c;prefs=c.getSharedPreferences("settings",0);audio=c.getSystemService(AudioManager.class);audioThread.start();audioHandler=new Handler(audioThread.getLooper());audioHandler.post(()->{try{tone=new ToneGenerator(AudioManager.STREAM_DTMF,55);}catch(RuntimeException ignored){}});}
    void play(View v,char key){
        if(prefs.getBoolean("haptic",true)) v.performHapticFeedback(android.view.HapticFeedbackConstants.KEYBOARD_TAP);
        audioHandler.post(()->{if(tone!=null && prefs.getBoolean("sound",true) && audio.getRingerMode()==AudioManager.RINGER_MODE_NORMAL){
            int t=key=='*'?ToneGenerator.TONE_DTMF_S:key=='#'?ToneGenerator.TONE_DTMF_P:key-'0';
            tone.startTone(t,55);
        }});
    }
    void close(){audioHandler.removeCallbacksAndMessages(null);audioHandler.post(()->{if(tone!=null){tone.release();tone=null;}audioThread.quitSafely();});}
}
