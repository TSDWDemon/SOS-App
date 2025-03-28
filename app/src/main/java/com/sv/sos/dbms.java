package com.sv.sos;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class dbms extends SQLiteOpenHelper {
    private static final String TABLE_NAME = "emergency_contacts";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE_NUMBER = "phone_number";
    private static final String DATABASE_NAME = "sos_database";
    private static final int DATABASE_VERSION = 1;

    public dbms(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_NAME + " TEXT UNIQUE, " + COLUMN_PHONE_NUMBER + " TEXT)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS emergency_contacts");
            onCreate(db);
        }
    }

    public Boolean insertData(String name, String phone_number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, name);
        contentValues.put(COLUMN_PHONE_NUMBER, phone_number);
        long result = db.insert(TABLE_NAME, null, contentValues);
        return result != -1;
    }

    public Boolean updatedata(String name, String phone_number) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("phone_number", phone_number);
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + " = ?", new String[]{name});
        if (cursor.getCount() > 0) {
          long result = db.update(TABLE_NAME, contentValues, COLUMN_NAME + "=?", new String[]{name});
           return result != -1;
        } else {
            return false;
        }
    }


    public Boolean deletedata(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_NAME + " = ?", new String[]{name});
        if (cursor.getCount() > 0) {
            long result = db.delete(TABLE_NAME, COLUMN_NAME + "=?", new String[]{name});
            return result != -1;
        }
        return false;

    }

    public Cursor getdata() {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        return cursor;

    }
}
