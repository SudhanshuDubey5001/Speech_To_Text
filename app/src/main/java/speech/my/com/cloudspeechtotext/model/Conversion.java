package speech.my.com.cloudspeechtotext.model;

import android.annotation.SuppressLint;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.services.speech.v1beta1.Speech;
import com.google.api.services.speech.v1beta1.SpeechRequestInitializer;
import com.google.api.services.speech.v1beta1.model.RecognitionAudio;
import com.google.api.services.speech.v1beta1.model.RecognitionConfig;
import com.google.api.services.speech.v1beta1.model.SpeechRecognitionResult;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeRequest;
import com.google.api.services.speech.v1beta1.model.SyncRecognizeResponse;

import java.io.IOException;

import speech.my.com.cloudspeechtotext.activity.FacebookActivity;
import speech.my.com.cloudspeechtotext.other.LogHandler;
import speech.my.com.cloudspeechtotext.other.SharedPrefs;

public class Conversion extends AsyncTask{

    private String transcribedTEXT;
    private String base64Audio;
    private final static String GCP_KEY = "AIzaSyASbrM_C5TAmv1OiBK8yL5fnEKGlHDmtf8";
    @SuppressLint("StaticFieldLeak")
    private FacebookActivity fb;
    LogHandler log;

    //  to get the audio file and activity object
    // (activity object: required for triggering function when upload is complete)--------->
    public void setter(String base64Audio, FacebookActivity fb) {
        this.base64Audio = base64Audio;
        this.fb =fb;
    }

    @Override
    protected Object doInBackground(Object... ob) {
        try {

            log=new LogHandler(fb);
//          setting up speech Service object------------>
            Speech speechService = new Speech.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(),
                    null
            ).setSpeechRequestInitializer(
                    new SpeechRequestInitializer(GCP_KEY))
                    .build();

//          1. Getting RecognitionConfig object---------------->
            RecognitionConfig config = new RecognitionConfig();
            config.setLanguageCode("en-US");

//          2. Getting recognitionAudio object----------------->
            RecognitionAudio recognitionAudio = new RecognitionAudio();
            recognitionAudio.setContent(base64Audio);

//          Setting up recognition audio and config from 1 and 2---------------->
            SyncRecognizeRequest request = new SyncRecognizeRequest();
            request.setConfig(config);
            request.setAudio(recognitionAudio);

//          Generate response--------------->
            log.writeLog("Conversion process started..................\n");
            SyncRecognizeResponse response = speechService.speech()
                    .syncrecognize(request)
                    .execute();

//          Extract transcript----------------->
            SpeechRecognitionResult result = response.getResults().get(0);
            transcribedTEXT = result.getAlternatives().get(0).getTranscript();
            log.writeLog("Conversion Complete!!!\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//  Triggering the function to setup the text field in login fields-------------->
    @Override
    protected void onPostExecute(Object ob) {
        super.onPostExecute(ob);
        log.writeLog("Sending transcribed audio back to activity..\n");
        fb.sendData(transcribedTEXT);

//      updating audio transcribed count------------------------->
        int value= SharedPrefs.getPrefs().getInt(SharedPrefs.TAG,0);
        value++;
        SharedPrefs.set(value);
    }
}
