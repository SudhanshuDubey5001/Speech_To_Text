package speech.my.com.cloudspeechtotext.activity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.adapter.OutputAdapter;
import speech.my.com.cloudspeechtotext.database.Architecture;
import speech.my.com.cloudspeechtotext.database.OutputCursor;
import speech.my.com.cloudspeechtotext.database.OutputSQLDatabase;
import speech.my.com.cloudspeechtotext.other.LogHandler;
import speech.my.com.cloudspeechtotext.other.Output;

public class OutputActivity extends AppCompatActivity {

    ArrayList<Output> outputArrayList=new ArrayList<>();
    RecyclerView outputView;
    LogHandler log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_output);
        log= new LogHandler(this);

//      setting title---------------->
        setTitle("Outputs");

        log.writeLog("[OutputActivity started]--------------\n\n");

//      setting up Output database to fetch the outputs--------->
        OutputSQLDatabase database = new OutputSQLDatabase(this);
        SQLiteDatabase db=database.getReadableDatabase();
        Cursor cursor = db.query(Architecture.TABLE_NAME,null,null,null,null,null,null);
        OutputCursor result=new OutputCursor(cursor);

//      extracting all the outputs from table and populating the outputArray----------->
        while(result.moveToNext()){
            Output o = new Output();
//          Extracting....
            o.output_number=result.getOutputIndex();
            o.output_email=result.getOutput_Email();
            o.output_pass=result.getOutput_Pass();

//          Populating...
            outputArrayList.add(o);
        }

//      setting up recycler view------------------------>
        outputView= findViewById(R.id.outputView);
        outputView.setLayoutManager(new LinearLayoutManager(this));

//      sending outputArray to adapter for display--------------->
        OutputAdapter adapter = new OutputAdapter();
        adapter.outputElements(outputArrayList,this);
        outputView.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        log.writeLog("[Back Pressed]\n");
        log.writeLog("FacebookActivity starting....\n");

        super.onBackPressed();
        Intent in = new Intent(this,FacebookActivity.class);
        startActivity(in);
        this.finish();
    }
}
