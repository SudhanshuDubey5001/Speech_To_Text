package speech.my.com.cloudspeechtotext.database;

import android.database.Cursor;
import android.database.CursorWrapper;

public class OutputCursor extends CursorWrapper {

    public OutputCursor(Cursor cursor) {
        super(cursor);
    }

    public int getOutputIndex() {
        return getInt(getColumnIndex(Architecture._ID));
    }

    public String getOutput_Email() {
        return getString(getColumnIndex(Architecture.COL_EMAIl));
    }

    public String getOutput_Pass() {
        return getString(getColumnIndex(Architecture.COL_PASS));
    }
}
