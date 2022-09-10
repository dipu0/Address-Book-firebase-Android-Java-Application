package com.chowdhuryelab.addressbook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Address_Book.db";

    public static final String TABLE_NAME = "Address_Book_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "NAME";
    public static final String COL_3 = "PHN1";
    public static final String COL_4 = "PHN2";
    public static final String COL_5 = "EMAIL";
    public static final String COL_6 = "ADDRESS";
    public static final String COL_7 = "IMG";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "NAME TEXT," +
                "PHN1 INTEGER," +
                "PHN2 INTEGER,"+
                "EMAIL TEXT,"+
                "ADDRESS TEXT,"+
                "IMG BLOB NOT NULL)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public boolean insertData(String name, String phn1, String phn2, String email, String address, byte[] img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, phn1);
        contentValues.put(COL_4, phn2);
        contentValues.put(COL_5, email);
        contentValues.put(COL_6, address);
        contentValues.put(COL_7, img);
        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Cursor readData(String id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res = db.rawQuery("Select * from "+ TABLE_NAME +" where ID=" + id + "", null);
        return res;
    }

    public boolean updateData(String id,String name, String phn1, String phn2, String email, String address, byte[] img) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_1, id);
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, phn1);
        contentValues.put(COL_4, phn2);
        contentValues.put(COL_5, email);
        contentValues.put(COL_6, address);
        contentValues.put(COL_7, img);
        db.update(TABLE_NAME, contentValues, "ID = ?", new String[]{id});
        return true;
    }

    public int deleteData(String id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NAME, COL_1+ "= '" + id + "';", null);
    }
    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME+" order by "+COL_2, null);
        return res;
    }


    public void delete_all() {
        SQLiteDatabase myDb = this.getWritableDatabase();
        myDb.execSQL("delete from " + TABLE_NAME);
        myDb.close();
    }
}




