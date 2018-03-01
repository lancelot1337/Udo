package com.ironsource.eventsmodule;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.ironsource.sdk.utils.Constants.ErrorCodes;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

public class DataBaseEventsStorage extends SQLiteOpenHelper implements IEventsStorageHelper {
    private static final String COMMA_SEP = ",";
    private static final String TYPE_INTEGER = " INTEGER";
    private static final String TYPE_TEXT = " TEXT";
    private static DataBaseEventsStorage mInstance;
    private final String SQL_CREATE_ENTRIES = "CREATE TABLE events (_id INTEGER PRIMARY KEY,eventid INTEGER,timestamp INTEGER,type TEXT,data TEXT )";
    private final String SQL_DELETE_TABLE = "DROP TABLE IF EXISTS events";

    static abstract class EventEntry implements BaseColumns {
        public static final String COLUMN_NAME_DATA = "data";
        public static final String COLUMN_NAME_EVENT_ID = "eventid";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final int NUMBER_OF_COLUMNS = 4;
        public static final String TABLE_NAME = "events";

        EventEntry() {
        }
    }

    public DataBaseEventsStorage(Context context, String databaseName, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    public static synchronized DataBaseEventsStorage getInstance(Context context, String databaseName, int databaseVersion) {
        DataBaseEventsStorage dataBaseEventsStorage;
        synchronized (DataBaseEventsStorage.class) {
            if (mInstance == null) {
                mInstance = new DataBaseEventsStorage(context, databaseName, databaseVersion);
            }
            dataBaseEventsStorage = mInstance;
        }
        return dataBaseEventsStorage;
    }

    public synchronized void saveEvents(List<EventData> events, String type) {
        if (events != null) {
            if (!events.isEmpty()) {
                SQLiteDatabase db = getWritableDatabase();
                try {
                    for (EventData toInsert : events) {
                        ContentValues values = getContentValuesForEvent(toInsert, type);
                        if (!(db == null || values == null)) {
                            db.insert(EventEntry.TABLE_NAME, null, values);
                        }
                    }
                    if (db != null) {
                        if (db.isOpen()) {
                            db.close();
                        }
                    }
                } catch (Exception e) {
                    if (db != null) {
                        if (db.isOpen()) {
                            db.close();
                        }
                    }
                } catch (Throwable th) {
                    if (db != null && db.isOpen()) {
                        db.close();
                    }
                }
            }
        }
    }

    public synchronized ArrayList<EventData> loadEvents(String type) {
        ArrayList<EventData> events;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        events = new ArrayList();
        try {
            String[] whereArgs = new String[]{type};
            cursor = db.query(EventEntry.TABLE_NAME, null, "type = ?", whereArgs, null, null, "timestamp ASC");
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    events.add(new EventData(cursor.getInt(cursor.getColumnIndex(EventEntry.COLUMN_NAME_EVENT_ID)), cursor.getLong(cursor.getColumnIndex(EventEntry.COLUMN_NAME_TIMESTAMP)), new JSONObject(cursor.getString(cursor.getColumnIndex(EventEntry.COLUMN_NAME_DATA)))));
                    cursor.moveToNext();
                }
                cursor.close();
            }
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return events;
    }

    public synchronized void clearEvents(String type) {
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.delete(EventEntry.TABLE_NAME, "type = ?", new String[]{type});
            if (db != null) {
                if (db.isOpen()) {
                    db.close();
                }
            }
        } catch (Exception e) {
            if (db != null) {
                if (db.isOpen()) {
                    db.close();
                }
            }
        } catch (Throwable th) {
            if (db != null) {
                if (db.isOpen()) {
                    db.close();
                }
            }
        }
    }

    private ContentValues getContentValuesForEvent(EventData event, String type) {
        if (event == null) {
            return null;
        }
        ContentValues values = new ContentValues(4);
        values.put(EventEntry.COLUMN_NAME_EVENT_ID, Integer.valueOf(event.getEventId()));
        values.put(EventEntry.COLUMN_NAME_TIMESTAMP, Long.valueOf(event.getTimeStamp()));
        values.put(EventEntry.COLUMN_NAME_TYPE, type);
        values.put(EventEntry.COLUMN_NAME_DATA, event.getAdditionalData());
        return values;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE events (_id INTEGER PRIMARY KEY,eventid INTEGER,timestamp INTEGER,type TEXT,data TEXT )");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS events");
        onCreate(db);
    }

    public synchronized long getLatestEventTimestamp(String type) {
        long timeStamp;
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = null;
        timeStamp = System.currentTimeMillis();
        try {
            String[] whereArgs = new String[]{type};
            cursor = db.query(EventEntry.TABLE_NAME, null, "type = ?", whereArgs, null, null, "timestamp DESC", ErrorCodes.FOLDER_NOT_EXIST_CODE);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                timeStamp = cursor.getLong(cursor.getColumnIndex(EventEntry.COLUMN_NAME_TIMESTAMP));
                cursor.close();
            }
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return timeStamp;
    }
}
