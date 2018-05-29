package speech.my.com.cloudspeechtotext.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class OutputSQLDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OutputDatabase";
    private static final int DATABASE_VERSION = 1;
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Architecture.TABLE_NAME + " (" +
                    Architecture._ID + " INTEGER PRIMARY KEY," +
                    Architecture.COL_EMAIl + " TEXT," +
                    Architecture.COL_PASS + " TEXT)";

    public OutputSQLDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
