package speech.my.com.cloudspeechtotext.other;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogHandler {

    private static final String FILE_NAME = "Speech_log.txt";
    Activity app;

    public LogHandler(Activity app) {
        this.app = app;
    }

    //  reading from log-------------------------------------------------------->
    public StringBuilder readFromLog() throws IOException {
        File file = new File(app.getFilesDir(), FILE_NAME);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder content = new StringBuilder();

        while ((line = reader.readLine()) != null) {
            content.append(line);
            content.append("\n");
        }
        return content;
    }

    //  Wrting into log--------------------------------------------------------->
    public void writeLog(String content) {
//      Data and time-->
        SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy 'at' hh:mm:ss");
        String main_content = df.format(Calendar.getInstance().getTime()) + ": " + content;

        BufferedWriter bf;
        try {
            File file = new File(app.getFilesDir(), FILE_NAME);
            FileWriter writer= new FileWriter(file,true);
            bf=new BufferedWriter(writer);
            bf.write(main_content);
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
