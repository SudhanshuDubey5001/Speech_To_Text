package speech.my.com.cloudspeechtotext.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.database.Architecture;
import speech.my.com.cloudspeechtotext.database.OutputCursor;
import speech.my.com.cloudspeechtotext.database.OutputSQLDatabase;
import speech.my.com.cloudspeechtotext.other.LogHandler;
import speech.my.com.cloudspeechtotext.other.Output;
import speech.my.com.cloudspeechtotext.other.SharedPrefs;

public class DashboardActivity extends AppCompatActivity {

    LogHandler log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        log=new LogHandler(this);
        log.writeLog("[DashboardActivity started]--------------\n\n");

//      setting up title------------>
        setTitle("Dashboard");

//      setting up Output database to fetch the output counts--------->
        OutputSQLDatabase database = new OutputSQLDatabase(this);
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = db.query(Architecture.TABLE_NAME, null, null, null, null, null, null);
        OutputCursor result = new OutputCursor(cursor);
        int outputGenerated=result.getCount();
        Log.d("my",""+outputGenerated);
        db.close();

//      setting up screen------------------------------------------------>
        TextView audioTranscribed=findViewById(R.id.audioTranscribed);
        audioTranscribed.setText(String.valueOf(SharedPrefs.getPrefs().getInt(SharedPrefs.TAG,0)));
        TextView outputCount=findViewById(R.id.gen_output);
        outputCount.setText(String.valueOf(outputGenerated));

//      ok button------------------------------------------>
        Button okButton= findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(DashboardActivity.this,FacebookActivity.class);
                startActivity(in);
                DashboardActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        log.writeLog("[Back pressed]\n");
        log.writeLog("FacebookActivity starting....\n");

        super.onBackPressed();
        Intent in = new Intent(this,FacebookActivity.class);
        startActivity(in);
        this.finish();
    }
}
