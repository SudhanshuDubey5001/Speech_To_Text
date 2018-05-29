package speech.my.com.cloudspeechtotext.other;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefs {

    final public static String TAG= "audio";

    public static SharedPreferences getPrefs(){
        Context c = MyApplication.getContext();
        return c.getSharedPreferences("audioTranscribed",Context.MODE_PRIVATE);
    }

    public static void set(int audiotransribed_number){
        getPrefs().edit().putInt(TAG,audiotransribed_number).apply();
    }
}