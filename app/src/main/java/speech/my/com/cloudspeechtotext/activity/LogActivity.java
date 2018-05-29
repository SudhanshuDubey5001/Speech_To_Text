package speech.my.com.cloudspeechtotext.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.other.LogHandler;

public class LogActivity extends AppCompatActivity {

    LogHandler log;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        log=new LogHandler(this);

//      setting up title-------------->
        setTitle("Log");

        log.writeLog("[LogActivity started]--------------\n\n");
        TextView logcontent= findViewById(R.id.logFile);
        try {
            logcontent.setText(log.readFromLog().toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        log.writeLog("[Back pressed]\n");
        log.writeLog("FacebookActivity starting....\n");

        Intent in = new Intent(this,FacebookActivity.class);
        startActivity(in);
        this.finish();
    }
}
