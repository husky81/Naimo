package com.real.bckim.naimo2000;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bckim on 2018-03-02.
 * http://www.androidhive.info/2011/11/android-sqlite-database-tutorial/
 */
public class ListDataHandler_Book extends SQLiteOpenHelper {
    public String DATABASE_NAME;
    private static final int DATABASE_VERSION = 2;
    private static final String TABLE_NAME = "Books";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TEXT1 = "Text1";
    private static final String COLUMN_TEXT2 = "Text2";
    private static final String COLUMN_TEXT3 = "Text3";
    private static final String COLUMN_TEXT4 = "Text4";
    private static final String COLUMN_TEXT5 = "Text5";
    private static final String COLUMN_NUMITEM = "Int1";
    private static final String COLUMN_INT2 = "Int2";
    private static final String COLUMN_INT3 = "Int3";
    private static final String COLUMN_QUIZROW = "Int4";
    private static final String COLUMN_QUIZCOL = "Int5";
    private static final String COLUMN_DATETIME = "DateTime";

    public ListDataHandler_Book(Context context, String DataBaseName) {
        super(context, DataBaseName, null, DATABASE_VERSION);
        DATABASE_NAME=DataBaseName;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE " + TABLE_NAME + "(");
        builder.append(COLUMN_ID + " INTEGER PRIMARY KEY,");
        builder.append(COLUMN_TEXT1 + " TEXT,");
        builder.append(COLUMN_TEXT2 + " TEXT,");
        builder.append(COLUMN_TEXT3 + " TEXT,");
        builder.append(COLUMN_TEXT4 + " TEXT,");
        builder.append(COLUMN_TEXT5 + " TEXT,");
        builder.append(COLUMN_NUMITEM + " INTEGER,");
        builder.append(COLUMN_INT2 + " INTEGER,");
        builder.append(COLUMN_INT3 + " INTEGER,");
        builder.append(COLUMN_QUIZROW + " INTEGER,");
        builder.append(COLUMN_QUIZCOL + " INTEGER,");
        builder.append(COLUMN_DATETIME + " LONG)");
        String s = builder.toString();
        db.execSQL(s);
    }
    @Override // Upgrading database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        // Create tables again
        onCreate(db);
    }
    public long addContent(Content_Book content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT1, content.getText1());
        values.put(COLUMN_TEXT2, content.getText2());
        values.put(COLUMN_NUMITEM, content.getNumItem());
        values.put(COLUMN_QUIZROW, content.getQuizSizeRow());
        values.put(COLUMN_QUIZCOL, content.getQuizSizeCol());
        values.put(COLUMN_DATETIME, System.currentTimeMillis());
        // Inserting Row
        long id = db.insert(TABLE_NAME, null, values);
        db.close(); // Closing database connection
        return id;
    }
    private String ModifyEmptyContent(String value){
        if(value.length()==0) {value=" ";}
        return value;
    }
    public Content_Book getContent(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectQuery = {COLUMN_ID, COLUMN_TEXT1, COLUMN_TEXT2, COLUMN_NUMITEM, COLUMN_QUIZROW, COLUMN_QUIZCOL, COLUMN_DATETIME};
        String strID = String.valueOf(id);
        Cursor cursor = db.query(TABLE_NAME, selectQuery, COLUMN_ID + "=?", new String[] {strID}, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();

        Content_Book cb = new Content_Book();
        cb.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        cb.setText1(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT1)));
        cb.setText2(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT2)));
        cb.setNumItem(cursor.getInt(cursor.getColumnIndex(COLUMN_NUMITEM)));
        cb.setQuizSizeRow(cursor.getInt(cursor.getColumnIndex(COLUMN_QUIZROW)));
        cb.setQuizSizeCol(cursor.getInt(cursor.getColumnIndex(COLUMN_QUIZCOL)));
        cb.setDateTime(cursor.getLong(cursor.getColumnIndex(COLUMN_DATETIME)));

        cursor.close();
        return cb;
    }
    public List<Content_Book> getAllContents() {
        List<Content_Book> contentList = new ArrayList<>();
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int CI_id = cursor.getColumnIndex(COLUMN_ID);
            int CI_text1 = cursor.getColumnIndex(COLUMN_TEXT1);
            int CI_text2 = cursor.getColumnIndex(COLUMN_TEXT2);
            int CI_int1 = cursor.getColumnIndex(COLUMN_NUMITEM);
            int CI_qsRow = cursor.getColumnIndex(COLUMN_QUIZROW);
            int CI_qsCol = cursor.getColumnIndex(COLUMN_QUIZCOL);
            int CI_Time = cursor.getColumnIndex(COLUMN_QUIZCOL);
            do {
                Content_Book content = new Content_Book();
                content.setID(cursor.getInt(CI_id));
                content.setText1(cursor.getString(CI_text1));
                content.setText2(cursor.getString(CI_text2));
                content.setNumItem(cursor.getInt(CI_int1));
                content.setQuizSizeRow(cursor.getInt(CI_qsRow));
                content.setQuizSizeCol(cursor.getInt(CI_qsCol));
                content.setDateTime(cursor.getLong(CI_Time));
                // Adding content to list
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return contentList;
    }
    public int updateContent(Content_Book content) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DATETIME, System.currentTimeMillis());
        values.put(COLUMN_TEXT1, content.getText1());
        values.put(COLUMN_TEXT2, content.getText2());
        values.put(COLUMN_NUMITEM, content.getNumItem());
        values.put(COLUMN_QUIZROW, content.getQuizSizeRow());
        values.put(COLUMN_QUIZCOL, content.getQuizSizeCol());
        values.put(COLUMN_DATETIME, System.currentTimeMillis());
        // updating row
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(content.getID()) });
    }
    public void deleteContent(Content_Book content) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[] { String.valueOf(content.getID()) });
        db.close();
    }
    public int getContentsCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }
}

class Content_Book {
    private int id;
    private String text1;
    private String text2;
    private int numItem;
    private int quizSizeRow;
    private int quizSizeCol;
    private long DateTime;

    Content_Book(){
        this.numItem = 0;
        this.quizSizeRow=3;
        this.quizSizeCol=2;
        this.DateTime = System.currentTimeMillis();
    }
    Content_Book(String text1, String text2){
        this.text1 = text1;
        this.text2 = text2;
        this.numItem = 0;
        this.quizSizeRow=3;
        this.quizSizeCol=2;
        this.DateTime = System.currentTimeMillis();
    }
    Content_Book(String text1, String text2, int row, int col){
        this.text1 = text1;
        this.text2 = text2;
        this.numItem = 0;
        this.quizSizeRow=row;
        this.quizSizeCol=col;
        this.DateTime = System.currentTimeMillis();
    }
    public int getID(){
        return this.id;
    }
    public void setID(int id){
        this.id = id;
    }
    public String getText1(){
        return this.text1;
    }
    public void setText1(String text){
        this.text1 = text;
    }
    public String getText2(){
        return this.text2;
    }
    public void setText2(String text){
        this.text2 = text;
    }
    public int getNumItem(){
        return this.numItem;
    }
    public void setNumItem(int numItem){
        this.numItem = numItem;
    }
    public int getQuizSizeRow(){
        return this.quizSizeRow;
    }
    public void setQuizSizeRow(int quizSizeRow){ this.quizSizeRow = quizSizeRow; }
    public int getQuizSizeCol(){
        return this.quizSizeCol;
    }
    public void setQuizSizeCol(int quizSizeCol){
        this.quizSizeCol = quizSizeCol;
    }
    public long getDateTime(){ return this.DateTime; }
    public void setDateTime(long dateTime){this.DateTime = dateTime;}

}
