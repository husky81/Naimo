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
public class ListDataHandler_Word extends SQLiteOpenHelper {
    public String DATABASE_NAME;
    private static final int DATABASE_VERSION = 12;
    private static final String TABLE_NAME = "Words";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_TEXT1 = "Text1"; //이름
    private static final String COLUMN_TEXT2 = "Text2"; //주요뜻
    private static final String COLUMN_TEXT3 = "Text3"; //메모
    private static final String COLUMN_TEXT4 = "Text4"; //발음
    private static final String COLUMN_TEXT5 = "Text5";
    private static final String COLUMN_NUM_CORRECT = "Int1";
    private static final String COLUMN_NUM_WRONG = "Int2";
    private static final String COLUMN_FAMILIARITY = "Int3";
    private static final String COLUMN_INT4 = "Int4";
    private static final String COLUMN_HAS_IMAGE = "Int5"; //이미지가 있으면 1, 없으면 0
    private static final String COLUMN_TIME_GENERATE = "Long1";
    private static final String COLUMN_TIME_MODIFY = "Long2";
    private static final String COLUMN_TIME_EXPOSE = "Long3";
    private static String sortingColumn;
    private static boolean sortDescending;
    private static String searchingWord;
    private static int lastInsertedID;

    ListDataHandler_Word(Context context, String DataBaseName) {
        super(context, DataBaseName, null, DATABASE_VERSION);
        DATABASE_NAME=DataBaseName;
    }
    @Override  // Creating Tables
    public void onCreate(SQLiteDatabase db) {
        StringBuilder builder = new StringBuilder();
        builder.append("CREATE TABLE " + TABLE_NAME + "(");
        builder.append(COLUMN_ID + " INTEGER PRIMARY KEY" + ",");
        builder.append(COLUMN_TEXT1 + " TEXT" + ",");
        builder.append(COLUMN_TEXT2 + " TEXT" + ",");
        builder.append(COLUMN_TEXT3 + " TEXT" + ",");
        builder.append(COLUMN_TEXT4 + " TEXT" + ",");
        builder.append(COLUMN_TEXT5 + " TEXT" + ",");
        builder.append(COLUMN_NUM_CORRECT + " INTEGER" + ",");
        builder.append(COLUMN_NUM_WRONG + " INTEGER" + ",");
        builder.append(COLUMN_FAMILIARITY + " INTEGER" + ",");
        builder.append(COLUMN_INT4 + " INTEGER" + ",");
        builder.append(COLUMN_HAS_IMAGE + " INTEGER" + ",");
        builder.append(COLUMN_TIME_GENERATE + " LONG" + ",");
        builder.append(COLUMN_TIME_MODIFY + " LONG" + ",");
        builder.append(COLUMN_TIME_EXPOSE + " LONG" + ")");  //주의: 마지막은 , 가 아니라 )로 끝나야 함.
        String s = builder.toString();
        db.execSQL(s);

        //정렬 초기값
        sortingColumn=COLUMN_ID;
        sortDescending=false;
        searchingWord="";
    }
    @Override // Upgrading database
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        //db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);

        String clearDBQuery = "DELETE FROM "+TABLE_NAME;
        db.execSQL(clearDBQuery);

        // Create tables again
        onCreate(db);
    }
    public int addContent(Content_Word content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT1, content.getText1());
        values.put(COLUMN_TEXT2, content.getText2());
        values.put(COLUMN_TEXT3, content.getText3());
        values.put(COLUMN_TEXT4, content.getText4());
        values.put(COLUMN_NUM_CORRECT, content.getNumCorrect());
        values.put(COLUMN_NUM_WRONG, content.getNumWrong());
        values.put(COLUMN_FAMILIARITY, content.getFamiliarity());
        values.put(COLUMN_HAS_IMAGE, content.getHasImage());
        values.put(COLUMN_TIME_GENERATE, content.getTimeSave());
        values.put(COLUMN_TIME_MODIFY, content.getTimeModify());
        values.put(COLUMN_TIME_EXPOSE, content.getTimeExpose());
        lastInsertedID = (int) db.insert(TABLE_NAME, null, values);
        db.close();
        return lastInsertedID;
    }
    private String ModifyEmptyContent(String value){
        if(value.length()==0) {value=" ";}
        return value;
    }
    public Content_Word getContent(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] selectQuery = {COLUMN_ID, COLUMN_TEXT1, COLUMN_TEXT2, COLUMN_TEXT3, COLUMN_TEXT4, COLUMN_NUM_CORRECT, COLUMN_NUM_WRONG, COLUMN_FAMILIARITY, COLUMN_HAS_IMAGE, COLUMN_TIME_GENERATE, COLUMN_TIME_MODIFY, COLUMN_TIME_EXPOSE};
        String strID = String.valueOf(id);
        Cursor cursor = db.query(TABLE_NAME, selectQuery, COLUMN_ID + "=?", new String[] {strID}, null, null, null, null);
        if (cursor != null) cursor.moveToFirst();

        Content_Word cb = new Content_Word();

//        int a1 = cursor.getColumnIndex(COLUMN_ID);
//        int a2 = cursor.getColumnIndex(COLUMN_TEXT1);
//        int a3 = cursor.getColumnIndex(COLUMN_TEXT2);
//        int a4 = cursor.getColumnIndex(COLUMN_TEXT3);
//        int a5 = cursor.getColumnIndex(COLUMN_NUM_CORRECT);
//        int a6 = cursor.getColumnIndex(COLUMN_NUM_WRONG);
//        int a7 = cursor.getColumnIndex(COLUMN_FAMILIARITY);
//        int a8 = cursor.getColumnIndex(COLUMN_TIME_GENERATE);

        try{
            cb.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }

        cb.setText1(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT1)));
        cb.setText2(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT2)));
        cb.setText3(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT3)));
        cb.setText4(cursor.getString(cursor.getColumnIndex(COLUMN_TEXT4)));
        cb.setNumCorrect(cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_CORRECT)));
        cb.setNumWrong(cursor.getInt(cursor.getColumnIndex(COLUMN_NUM_WRONG)));
        cb.setFamiliarity(cursor.getInt(cursor.getColumnIndex(COLUMN_FAMILIARITY)));
        cb.setHasImage(cursor.getInt(cursor.getColumnIndex(COLUMN_HAS_IMAGE)));
        cb.setTimeSave(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_GENERATE)));
        cb.setTimeModify(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_MODIFY)));
        cb.setTimeExpose(cursor.getLong(cursor.getColumnIndex(COLUMN_TIME_EXPOSE)));
        cursor.close();
        db.close();
        return cb;
    }
    public List<Content_Word> getAllContents() {
        List<Content_Word> contentList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        if(searchingWord!=null && searchingWord.length()>0){
            //selectQuery += " WHERE " + COLUMN_TEXT1 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " WHERE " + COLUMN_TEXT1 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " OR " + COLUMN_TEXT2 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " OR " + COLUMN_TEXT3 + " LIKE '%"+searchingWord+"%'";
//            cursor = db.query(TABLE_NAME, new String[] { TABLE_COLUMN_ID, TABLE_COLUMN_ONE, TABLE_COLUMN_TWO },
//                    TABLE_COLUMN_ONE + " LIKE ? AND " + TABLE_COLUMN_TWO + " LIKE ?",
//                    new String[] {"%" + dan + "%", "%" + vrijeme + "%"},
//                    null, null, null, null);
        }

        //Sorting
        selectQuery += " Order By " + sortingColumn;
        if(sortDescending) selectQuery=selectQuery+" DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int CI_id = cursor.getColumnIndex(COLUMN_ID);
            int CI_text1 = cursor.getColumnIndex(COLUMN_TEXT1);
            int CI_text2 = cursor.getColumnIndex(COLUMN_TEXT2);
            int CI_text3 = cursor.getColumnIndex(COLUMN_TEXT3);
            int CI_text4 = cursor.getColumnIndex(COLUMN_TEXT4);
            int CI_nCorr = cursor.getColumnIndex(COLUMN_NUM_CORRECT);
            int CI_nWorn = cursor.getColumnIndex(COLUMN_NUM_WRONG);
            int CI_Fami = cursor.getColumnIndex(COLUMN_FAMILIARITY);
            int CI_HasImage = cursor.getColumnIndex(COLUMN_HAS_IMAGE);
            int CI_TimeSave = cursor.getColumnIndex(COLUMN_TIME_GENERATE);
            int CI_TimeModify = cursor.getColumnIndex(COLUMN_TIME_MODIFY);
            int CI_TimeExpose = cursor.getColumnIndex(COLUMN_TIME_EXPOSE);
            do {
                Content_Word content = new Content_Word();
                content.setID(Integer.parseInt(cursor.getString(CI_id)));
                content.setText1(cursor.getString(CI_text1));
                content.setText2(cursor.getString(CI_text2));
                content.setText3(cursor.getString(CI_text3));
                content.setText4(cursor.getString(CI_text4));
                content.setNumCorrect(cursor.getInt(CI_nCorr));
                content.setNumWrong(cursor.getInt(CI_nWorn));
                content.setFamiliarity(cursor.getInt(CI_Fami));
                content.setHasImage(cursor.getInt(CI_HasImage));
                content.setTimeSave(cursor.getLong(CI_TimeSave));
                content.setTimeModify(cursor.getLong(CI_TimeModify));
                content.setTimeExpose(cursor.getLong(CI_TimeExpose));
                contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contentList;
    }
    public List<Content_Word> getQuizContents() {
        List<Content_Word> contentList = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        if(searchingWord!=null && searchingWord.length()>0){
            //selectQuery += " WHERE " + COLUMN_TEXT1 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " WHERE " + COLUMN_TEXT1 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " OR " + COLUMN_TEXT2 + " LIKE '%"+searchingWord+"%'";
            selectQuery += " OR " + COLUMN_TEXT3 + " LIKE '%"+searchingWord+"%'";
        }

        //Sorting
        selectQuery += " Order By " + sortingColumn;
        if(sortDescending) selectQuery=selectQuery+" DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            int CI_id = cursor.getColumnIndex(COLUMN_ID);
            int CI_text1 = cursor.getColumnIndex(COLUMN_TEXT1);
            int CI_text2 = cursor.getColumnIndex(COLUMN_TEXT2);
            int CI_text3 = cursor.getColumnIndex(COLUMN_TEXT3);
            int CI_text4 = cursor.getColumnIndex(COLUMN_TEXT4);
            int CI_nCorr = cursor.getColumnIndex(COLUMN_NUM_CORRECT);
            int CI_nWorn = cursor.getColumnIndex(COLUMN_NUM_WRONG);
            int CI_Fami = cursor.getColumnIndex(COLUMN_FAMILIARITY);
            int CI_HasImage = cursor.getColumnIndex(COLUMN_HAS_IMAGE);
            int CI_TimeSave = cursor.getColumnIndex(COLUMN_TIME_GENERATE);
            int CI_TimeModify = cursor.getColumnIndex(COLUMN_TIME_MODIFY);
            int CI_TimeExpose = cursor.getColumnIndex(COLUMN_TIME_EXPOSE);

            String text1;
            String text2;
            int hasImage;
            int numQuizItem ;
            do {
                Content_Word content = new Content_Word();

                numQuizItem = 0;
                content.setID(Integer.parseInt(cursor.getString(CI_id)));
                text1 = cursor.getString(CI_text1);
                content.setText1(text1);
                text2 = cursor.getString(CI_text2);
                content.setText2(text2);
                content.setText3(cursor.getString(CI_text3));
                content.setText4(cursor.getString(CI_text4));
                content.setNumCorrect(cursor.getInt(CI_nCorr));
                content.setNumWrong(cursor.getInt(CI_nWorn));
                content.setFamiliarity(cursor.getInt(CI_Fami));
                hasImage = cursor.getInt(CI_HasImage);
                content.setHasImage(hasImage);
                content.setTimeSave(cursor.getLong(CI_TimeSave));
                content.setTimeModify(cursor.getLong(CI_TimeModify));
                content.setTimeExpose(cursor.getLong(CI_TimeExpose));

                if(text1.length()>0) numQuizItem += 1;
                if(text2.length()>0) numQuizItem += 1;
                if(hasImage == 1) numQuizItem += 1;

                if(numQuizItem > 1) contentList.add(content);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return contentList;
    }
    public int updateContent(Content_Word content) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TEXT1, content.getText1());
        values.put(COLUMN_TEXT2, content.getText2());
        values.put(COLUMN_TEXT3, content.getText3());
        values.put(COLUMN_TEXT4, content.getText4());
        values.put(COLUMN_NUM_CORRECT, content.getNumCorrect());
        values.put(COLUMN_NUM_WRONG, content.getNumWrong());
        values.put(COLUMN_FAMILIARITY, content.getFamiliarity());
        values.put(COLUMN_HAS_IMAGE, content.getHasImage_int());
        values.put(COLUMN_TIME_GENERATE, content.getTimeSave());
        values.put(COLUMN_TIME_MODIFY, content.getTimeModify());
        values.put(COLUMN_TIME_EXPOSE, content.getTimeExpose());
        int result = db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[] { String.valueOf(content.getID()) });
        db.close();
        return result;
    }
    public void deleteContent(Content_Word content) {
        if(content.getHasImage()){
            Manager_PreviewImage.deletePreviewImage(content.getID());
        }
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?", new String[] { String.valueOf(content.getID()) });
        db.close();
    }
    public int getCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }
    public void setSortingColumn(int colSorting){
        String requestedSortingColumn;
        switch (colSorting){
            case 0:
                requestedSortingColumn = COLUMN_ID;
                break;
            case 1:
                requestedSortingColumn=COLUMN_TEXT1;
                break;
            case 2:
                requestedSortingColumn=COLUMN_TEXT2;
                break;
            case 3:
                requestedSortingColumn=COLUMN_TEXT3;
                break;
            case 4:
                requestedSortingColumn=COLUMN_FAMILIARITY;
                break;
            case 5:
                requestedSortingColumn=COLUMN_HAS_IMAGE;
                break;
            case 6:
                requestedSortingColumn=COLUMN_NUM_CORRECT;
                break;
            case 7:
                requestedSortingColumn=COLUMN_NUM_WRONG;
                break;
            case 8:
                requestedSortingColumn= COLUMN_TIME_GENERATE;
                break;
            case 9:
                requestedSortingColumn= COLUMN_TIME_MODIFY;
                break;
            case 10:
                requestedSortingColumn= COLUMN_TIME_EXPOSE;
                break;
            default:
                requestedSortingColumn=COLUMN_FAMILIARITY;
                break;
        }
        if(requestedSortingColumn.equals(sortingColumn)){
            sortDescending=!sortDescending;
        }else{
            if(requestedSortingColumn.equals(COLUMN_TIME_EXPOSE)) sortDescending = true;
        }
        sortingColumn=requestedSortingColumn;
    }
    public void setSortDescending(boolean sortDescending) {
        ListDataHandler_Word.sortDescending = sortDescending;
    }
    public boolean isSortDescending() {
        return sortDescending;
    }
    public String getSortingColumn(){
        return sortingColumn;
    }
    public void setSearchingWord(String searchWord){
        searchingWord = searchWord;
    }
    public String getSearchingWord(){
        return searchingWord;
    }
}

class Content_Word {
    private int id;
    private String text1;
    private String text2;
    private String text3;
    private String text4;
    private int numCorrect;
    private int numWrong;
    private int Familiarity;
    private boolean hasImage;
    private long TimeSave;
    private long TimeModify;
    private long TimeExpose;

    Content_Word(){
        this.numCorrect = 0;
        this.numWrong = 0;
        this.Familiarity = 50;
        this.hasImage=false;
        this.TimeSave = System.currentTimeMillis();
        this.TimeModify = System.currentTimeMillis();
        this.TimeExpose = System.currentTimeMillis();
    }
    Content_Word(String text1, String text2){
        this.text1 = text1;
        this.text2 = text2;
        this.numCorrect = 0;
        this.numWrong = 0;
        this.Familiarity = 50;
        this.hasImage=false;
        this.TimeSave = System.currentTimeMillis();
        this.TimeModify = System.currentTimeMillis();
        this.TimeExpose = System.currentTimeMillis();
    }
    Content_Word(String text1, String text2, String text3, String text4){
        this.text1 = text1;
        this.text2 = text2;
        this.text3 = text3;
        this.text4 = text4;
        this.numCorrect = 0;
        this.numWrong = 0;
        this.Familiarity = 50;
        this.hasImage=false;
        this.TimeSave = System.currentTimeMillis();
        this.TimeModify = System.currentTimeMillis();
        this.TimeExpose = System.currentTimeMillis();
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
    public String getText3(){
        return this.text3;
    }
    public void setText3(String text){
        this.text3 = text;
    }
    public String getText4(){
        return this.text4;
    }
    public void setText4(String text){
        this.text4 = text;
    }
    public int getNumCorrect(){
        return this.numCorrect;
    }
    public void setNumCorrect(int numCorrect){
        this.numCorrect = numCorrect;
    }
    public int getNumWrong(){
        return this.numWrong;
    }
    public void setNumWrong(int numWrong){this.numWrong = numWrong;}
    public int getFamiliarity(){ return this.Familiarity; }
    public void setFamiliarity(int Familiarity){this.Familiarity = Familiarity;}
    public boolean getHasImage(){ return this.hasImage; }
    public void setHasImage(boolean hasImage){this.hasImage = hasImage;}
    public int getHasImage_int(){
        if(this.hasImage){
            return 1;
        }else{
            return 0;
        }
    }
    public void setHasImage(int hasImage){
        if(hasImage==0){
            this.hasImage = false;
        }else{
            this.hasImage=true;
        }
    }
    public long getTimeSave(){ return this.TimeSave; }
    public void setTimeSave(long dateTime){this.TimeSave = dateTime;}
    public long getTimeModify(){ return this.TimeModify; }
    public void setTimeModify(long dateTime){this.TimeModify = dateTime;}
    public void setTimeModifyNow(){this.TimeModify = System.currentTimeMillis();}
    public long getTimeExpose(){ return this.TimeExpose; }
    public void setTimeExpose(long dateTime){this.TimeExpose = dateTime;}
    public void setTimeExposeNow(){this.TimeExpose = System.currentTimeMillis();}
}