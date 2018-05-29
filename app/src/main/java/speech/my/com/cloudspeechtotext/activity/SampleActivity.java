package speech.my.com.cloudspeechtotext.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import speech.my.com.cloudspeechtotext.R;
import speech.my.com.cloudspeechtotext.other.LogHandler;

public class SampleActivity extends ListActivity {

    public static final String RES_ID = "index";
    int[] resId = {R.raw.sample1,
            R.raw.sample2,
            R.raw.sample3,
            R.raw.sample4,
            R.raw.sample5,
            R.raw.sample6,
            R.raw.sample7,
            R.raw.sample8,
            R.raw.sample9,
            R.raw.sample10};

    LogHandler log;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log = new LogHandler(this);

        log.writeLog("SampleActivity started------------------>\n");
        String[] samples = {
                "Sample 1",
                "Sample 2",
                "Sample 3",
                "Sample 4",
                "Sample 5",
                "Sample 6",
                "Sample 7",
                "Sample 8",
                "Sample 9",
                "Sample 10"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_sample, R.id.labelOfSample, samples);
        this.setListAdapter(adapter);

        ListView list = getListView();
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                log.writeLog("Sample number selected: " + position + 1 + "\n");
                Intent in = new Intent(SampleActivity.this, FacebookActivity.class);
                in.putExtra(RES_ID, resId[position]);
                startActivity(in);
                SampleActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        log.writeLog("[Back Pressed]\n");
        log.writeLog("FacebookActivity starting....\n");

        super.onBackPressed();
        Intent in = new Intent(this, FacebookActivity.class);
        startActivity(in);
        this.finish();
    }
}
