package com.real.bckim.naimo2000;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;

import static com.real.bckim.naimo2000.MainActivity.NaimoDataExportPath;
import static com.real.bckim.naimo2000.MainActivity.REQ_CODE_SELECT_IMAGE;

public class WordListActivity extends AppCompatActivity {
    static String bookName;
    static int bookID;
    static ListDataHandler_Book db_book;
    public static Content_Book content_Book;

    public static final String pre_Word_DB_Name  = "WLDB5_";
    static String Word_DB_Name;
    public static ListDataHandler_Word db_word;
    static List<Content_Word> contents;
    static CustomAdapter_Word customAdapter_word;

    public static int numWord;
    static int[] IDs;
    static int quizArraySizeRow;
    static int quizArraySizeCol;

    public static String FileName_NaimoDataList = "NaimoDataList";
    int LongClickSelectedItem;
    int SelectedItem;
    int selectedID;
    private AdView mAdView;
    static AlertDialog dialog_word_edit;
    String selectedImagePathName;
    static Bitmap image_bitmap; //메모리 절약을 위해 비트맵은 Activity마다 한개만 쓰면 좋을 듯.
    final static int EmptyImageIconResource= R.drawable.raindrop3; //노트 기본아이콘.
    int REQUEST_CODE_IMPORT_PICTURE_FOLDER = 102;
    int REQUEST_CODE_GET_WORD = 105;
    int REQ_CODE_FAMILIARITY = 103;
    final int REQ_ADD_CONTACT_PICTURES = 104;

    final static String REQUEST_IMAGE_FILE_NAME = "getImageFileName2342";
    static boolean zip_xls_ImportComplete;
    static String importZipFile;
    String importXlsFile;

    static ListView wordList;
    static int listViewPosition_index;
    static int listViewPosition_top;

    static String txtPronunciation;
    static String txtMeanings;

    WordEditingDialog wordEditingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_list2);
        adSetting();
        ToolbarFloatingButtonSetting();

        //Data base setting
        Intent intent = getIntent();
        bookName = intent.getStringExtra("bookName");
        setTitle(bookName);
        bookID = intent.getIntExtra("bookID",0);
        db_book = MainActivity.db_book;
        content_Book = db_book.getContent(bookID);

        Word_DB_Name = pre_Word_DB_Name + bookID;
        db_word = new ListDataHandler_Word(this, Word_DB_Name);

        numWord = db_word.getCount();

        Manager_PreviewImage.setDataBaseName(Word_DB_Name);
        wordList = findViewById(R.id.wordList);

        quizArraySizeRow = intent.getIntExtra("QuizArraySize_Row",4);
        quizArraySizeCol = intent.getIntExtra("QuizArraySize_Col",3);

        db_word.setSearchingWord("");

        if(numWord==0){requestImportCall();}

        ReadWordDataBase();
        customAdapter_word = new CustomAdapter_Word(this,contents);
        DisplayList();

        ListViewClickEventSetting();
        ActivityResultsPreperation();
    }
    private void requestImportCall(){
        Intent intent = getIntent();

        importZipFile = intent.getStringExtra("importZipFile");
        if(importZipFile !=null && !zip_xls_ImportComplete){
            if(!importZipFile.equals("")){
                new LoadSampleBook_zipFile(this,importZipFile).execute();
            }else {
                importXlsFile = intent.getStringExtra("importXlsFile");
                if(importXlsFile!=""){
                    //new LoadSampleBook_xlsFile(this).execute();
                    new LoadDaumXlsFile(WordListActivity.this,importXlsFile).execute();
                }
            }
        }

        String iCP = intent.getStringExtra("importContactPictures");
        if(iCP!=null && iCP.equals("true")){
            getContactPermissionAndImportContactPictures();
        }

        String iPF = intent.getStringExtra("importPictureFolder");
        if(iPF!=null && iPF.equals("true")){
            Toast.makeText(this,"그림파일이 있는 폴더를 선택하세요.",Toast.LENGTH_LONG).show();
            getPicturesFolderByUser();
        }

        if(bookName.equals(getResources().getString(R.string.exKeyString01))){
            ImportNaimoZipResourceFile(R.raw.ex1_elementry);
        }else if(bookName.equals(getResources().getString(R.string.exKeyString02))){
            ImportNaimoZipResourceFile(R.raw.ex2_midium);
        }else if(bookName.equals(getResources().getString(R.string.exKeyString03))){
            ImportNaimoZipResourceFile(R.raw.ex3_high);
        }else if(bookName.equals(getResources().getString(R.string.exKeyString04))){
            ImportNaimoZipResourceFile(R.raw.ex4_toeic);
        }else if(bookName.equals(getResources().getString(R.string.exKeyString05))){
            ImportNaimoZipResourceFile(R.raw.ex5_conversation);
        }else if(bookName.equals(getResources().getString(R.string.exKeyString06))){
            ImportNaimoZipResourceFile(R.raw.ex6_presidents_rok);
        }else if(bookName.equals("인프라안전연구본부")){
            //ImportNaimoZipResourceFile(R.raw.ex_hide_infra_safety);
        }
    }
    private void ImportNaimoZipResourceFile(int exResource){
        InputStream databaseInputStream = getResources().openRawResource(exResource);
        String pathName = NaimoDataExportPath + "sample_element.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        new ImportNaimoZipFile(this,pathName).execute();
    }
    private void ActivityResultsPreperation(){
        Intent data = new Intent();
        data.putExtra("bookID",bookID);
        data.putExtra("numItem",db_word.getCount());
        data.putExtra("QuizArraySize_Row",quizArraySizeRow);
        data.putExtra("QuizArraySize_Col",quizArraySizeCol);
        setResult(MainActivity.RLT_CODE_NUM_ITEM,data);
    }
    private void AddWord(){
        selectedID = db_word.addContent(new Content_Word("", ""));
        Content_Word cw = db_word.getContent(selectedID);
        wordEditingDialog = new WordEditingDialog(this,db_word,cw);
        wordEditingDialog.addEditEndListener(new WordEditingDialog.EditEndedListener(){
            public void editEnded() {
                wordEditingDialog.removeEditEndListener(this);
                ReadWordDataBase();
                DisplayList();
            }
        });
        wordEditingDialog.showDialog();
    }
    private int AddWord(String text1, String text2){
        return db_word.addContent(new Content_Word(text1, text2));
    }
    private int AddWord(String ImagePathName, String text1, String text2){
        selectedID = AddWord(text1,text2);
        Manager_PreviewImage.setPreviewImage(ImagePathName, selectedID);
        setHasImage(selectedID,true);
        return selectedID;
    }
    private void AddWord_old(String ImagePathName, String text1, String text2){
        if(text1.length() != 0){
            AddWord(text1,text2);
            ReadWordDataBase();
            int lastItemNumber = IDs.length-1;
            Manager_PreviewImage.setPreviewImage(ImagePathName,IDs[lastItemNumber]);
            setHasImage(IDs[lastItemNumber],true);
        }
    }
    public static void setHasImage(int id,boolean hasImage){
        Content_Word cw = db_word.getContent(id);
        cw.setHasImage(hasImage);
        db_word.updateContent(cw);
    }
    public void EditWord(int position){
        selectedID = IDs[position];
        Content_Word cw = db_word.getContent(selectedID);
        wordEditingDialog = new WordEditingDialog(this,db_word,cw);
        wordEditingDialog.addEditEndListener(new WordEditingDialog.EditEndedListener(){
            public void editEnded() {
                wordEditingDialog.removeEditEndListener(this);
                ReadWordDataBase();
                DisplayList();
            }
        });
        wordEditingDialog.showDialog();
    }
    private void SaveWordEditDialogInfoToDataBase(EditText et1, EditText et2, EditText et3, EditText et4){
        Content_Word cn = db_word.getContent(IDs[SelectedItem]);
        String text1 = et1.getText().toString();
        String text2 = et2.getText().toString();
        String text3 = et3.getText().toString();
        String text4;
        if(txtPronunciation==""){
            text4 = et4.getText().toString();
        }else{
            text4 = txtPronunciation;
        }
        if(text1.length() != 0){
            cn.setText1(text1);
            if(!text2.equals(getResources().getString(R.string.loading))) cn.setText2(text2);
            cn.setText3(text3);
            if(!text4.equals(getResources().getString(R.string.loading))) cn.setText4(text4);
            db_word.updateContent(cn);
        }
    }
    private void FindWordMeaningsFromDaumEndic(String searchText, final EditText pronText,final EditText meanText, WebView wb){
        pronText.setText(R.string.loading);
        meanText.setText(R.string.loading);

        wb.setVisibility(View.GONE);
        wb.setWebViewClient(new webViewClient_pronunciation() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        // yourMethod();
                        pronText.setText(txtPronunciation);
                        meanText.setText(txtMeanings);
                    }
                }, 200);
                //출처: http://jabstorage.tistory.com/5 [개발자 블로그]
            }
        });
        wb.getSettings().setJavaScriptEnabled(true);
        class MyJavascriptInterface2 {
            @JavascriptInterface
            public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
                System.out.println(html);
                txtPronunciation = FindPronunciationFromDaumEndic(html);
                txtMeanings = FindMeaningsFromDaumEndic(html);
            }
        }
        wb.addJavascriptInterface(new MyJavascriptInterface2(), "Android");

        searchText = Manager_TxtMathTools.ReplaceText(searchText," ","+");
        String DaumEndicUrl = "http://dic.daum.net/search.do?q=" + searchText;

        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setDisplayZoomControls(false);

        wb.loadUrl(DaumEndicUrl);
    }
    private String FindPronunciationFromDaumEndic(String txt){
        txt= Manager_TxtMathTools.WrapedText(txt,"<span class=\"txt_pronounce\">[","]</span>");
        txt=Manager_TxtMathTools.ReplaceText(txt,"<daum:pron>","");
        txt=Manager_TxtMathTools.ReplaceText(txt,"</daum:pron>","");
        return txt;
    }
    private String FindMeaningsFromDaumEndic(String txt){
        txt= Manager_TxtMathTools.WrapedText(txt,"<meta property=\"og:description\" content=\"","\">");
        return txt;
    }
    private class webViewClient_pronunciation extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String tmpUri = Uri.parse(url).getHost();

            if (Uri.parse(url).getHost().equals("m.daum.net")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            return true;
        }
    }
    private String txtSpace2Plus(String txt){
        int idxSpace = txt.indexOf(" ");
        while(idxSpace!=-1){
            txt = txt.substring(0,idxSpace) + "+" + txt.substring(idxSpace+1,txt.length());
            idxSpace = txt.indexOf(" ");
        }
        return txt;
    }
    private static void SaveIndexAndTopPosition(){
        //리스트뷰의 위치 저장
        //ref. https://stackoverflow.com/questions/3014089/maintain-save-restore-scroll-position-when-returning-to-a-listview
        listViewPosition_index = wordList.getFirstVisiblePosition();
        View v = wordList.getChildAt(0);
        listViewPosition_top = (v == null) ? 0 : (v.getTop() - wordList.getPaddingTop());
    }
    private void selectPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }
    private String getImageFullPathNameFromUri(Uri data) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        //String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgPath;
    }
    private void ListViewClickEventSetting(){
        ListView wordList = findViewById(R.id.wordList);
        //wordList.setOnItemClickListener(new WordListActivity.ListClickHandler());
        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
                if(db_word.getContent(IDs[position])==null){
                    ReadWordDataBase();
                    DisplayList();
                }else{
                    EditWord(position);
                }
            }
        });
        wordList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if(db_word.getContent(IDs[position])==null) {
                    ReadWordDataBase();
                    DisplayList();
                }else{
                    showListSelectCheckBox();
                }
                return true;
            }
        });
    }
    private static void ReadWordDataBase(){
        contents = db_word.getAllContents();

        numWord = contents.size();
        IDs = new int[numWord];

        int i=0;
        for(Content_Word cn : contents){
            IDs[i] = cn.getID();
            i++;
        }

        content_Book.setNumItem(numWord);
        db_book.updateContent(content_Book);
    }
    private void DeleteYN(int id){
        LongClickSelectedItem=id;
        Content_Word cn = db_word.getContent(id);
        String WordName = cn.getText1();
        AlertDialog.Builder builderDelete = new AlertDialog.Builder(WordListActivity.this);     // 여기서 this는 Activity의 this
        builderDelete.setTitle(R.string.confirmDeletion)
                .setMessage("Delete a word \""+ WordName +"\" ?")
                .setCancelable(true) // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        //finish();
                        DeleteWord(LongClickSelectedItem);
                        dialog.dismiss();
                        ReadWordDataBase();
                        DisplayList();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener(){
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton){
                        dialog.cancel();
                        //dialog.dismiss();
                        //recreate();
                    }
                });
        AlertDialog dialogDelete = builderDelete.create();    // 알림창 객체 생성
        dialogDelete.show();    // 알림창 띄우기
    }
    private void DeleteWord(int id){
        Content_Word cn = db_word.getContent(id);
        db_word.deleteContent(cn);

        Manager_PreviewImage.setDataBaseName(Word_DB_Name);
        Manager_PreviewImage.deletePreviewImage(id);
    }
    private static void DisplayList(){
        customAdapter_word.setContents(contents);
        customAdapter_word.setCheckBoxVisibility(false);
        SaveIndexAndTopPosition();
        wordList.setAdapter(customAdapter_word);
        wordList.setSelectionFromTop(listViewPosition_index, listViewPosition_top);
    }
    private void DisplayListWithCheckBox(){
        customAdapter_word.setContents(contents);
        customAdapter_word.setCheckBoxVisibility(true);
        wordList = findViewById(R.id.wordList);
        SaveIndexAndTopPosition();
        wordList.setAdapter(customAdapter_word);
        wordList.setSelectionFromTop(listViewPosition_index, listViewPosition_top);
    }
    public void ExportNoteToZipFile(String Word_DB_Name){
        ExportNoteToFolder(Word_DB_Name);
        String ExportedPath = NaimoDataExportPath + Word_DB_Name + "/";
        String[] allFiles = Manager_SystemControl.getAllFilePathNames(ExportedPath);
        String downLoadPath = Manager_SystemControl.getDownloadPath();
        String zipFileName = downLoadPath+Word_DB_Name + "_" + bookName +".zip";
        Manager_SystemControl.makeZipFile(allFiles,zipFileName);
        Manager_SystemControl.deleteFolder(ExportedPath);
        Toast.makeText(this,"Generated Naimo ZIP file : " + zipFileName ,Toast.LENGTH_LONG).show();
    }
    public static void ExportNoteToFolder(String Word_DB_Name){
        String TargetFolder =  NaimoDataExportPath + Word_DB_Name + "/";
        File folder = new File(TargetFolder);
        folder.mkdirs();
        ReadWordDataBase();
        //ExportBookDataToFolder_csv(TargetFolder);
        ExportBookDataToFolder_xls(TargetFolder);
    }
    private static void ExportBookDataToFolder_xls(String TargetFolder){
        Manager_SystemControl.deleteAllFile(TargetFolder);
        String xlsPathName = TargetFolder + FileName_NaimoDataList + ".xls";

        Workbook wb = new HSSFWorkbook();
        try (OutputStream fileOut = new FileOutputStream(xlsPathName)) {
            wb.write(fileOut);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Sheet sheet = wb.createSheet("sheet1");
        CreationHelper createHelper = wb.getCreationHelper();

        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0); cell.setCellValue("ID_word");
        row.createCell(1).setCellValue("names");
        row.createCell(2).setCellValue("mean");
        row.createCell(3).setCellValue("memo");
        row.createCell(4).setCellValue("pronunciation");

            for(int i=0;i<numWord;i++){
                Content_Word cw = db_word.getContent(IDs[i]);
                String text1 = cw.getText1();
                String text2 = cw.getText2();
                String text3 = cw.getText3();
                String text4 = cw.getText4();
                if(text3==null) text3="";
                if(text4==null) text4="";

                row = sheet.createRow(i+1);
                cell = row.createCell(0); cell.setCellValue(IDs[i]);
                row.createCell(1).setCellValue(text1);
                row.createCell(2).setCellValue(text2);
                row.createCell(3).setCellValue(text3);
                row.createCell(4).setCellValue(text4);
                Manager_PreviewImage.savePreviewImage(IDs[i],TargetFolder, Manager_PreviewImage.getPreviewImageFileName(IDs[i]));
            }

        // Write the output to a file
        try (OutputStream fileOut = new FileOutputStream(xlsPathName)) {
            wb.write(fileOut);
            wb.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void ExportBookDataToFolder_csv(String TargetFolder){
        File file = new File(TargetFolder + FileName_NaimoDataList + ".csv");
        Manager_SystemControl.deleteAllFile(TargetFolder);

        FileOutputStream fos = null;
        Writer out = null;

        try {
            fos = new FileOutputStream(file);
            out = new OutputStreamWriter(fos, "euc-kr");
            out.write("ID_word,names,mean,memo,pronunciation\n");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i=0;i<numWord;i++){
            Content_Word cw = db_word.getContent(IDs[i]);
            String text1 = cw.getText1();
            String text2 = cw.getText2();
            String text3 = cw.getText3();
            String text4 = cw.getText4();
            if(text3==null) text3="";
            if(text4==null) text4="";
            text1= Manager_SystemControl.Convert_CSV_String(text1);
            text2= Manager_SystemControl.Convert_CSV_String(text2);
            text3= Manager_SystemControl.Convert_CSV_String(text3);
            text4= Manager_SystemControl.Convert_CSV_String(text4);
            String strWrite =IDs[i] +","+ text1 +","+ text2  +","+ text3  +","+ text4 + ",\n";
            try {
                out.write(strWrite);
            } catch (Exception e) {
                e.printStackTrace() ;
            }
            Manager_PreviewImage.savePreviewImage(IDs[i],TargetFolder, Manager_PreviewImage.getPreviewImageFileName(IDs[i]));
        }
        if (out != null) {
            try {
                out.close() ;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static void ImportWordsFromFolder_xls(final String ExportedBookDataFolder){
        String NaimoZipFilePathName = ExportedBookDataFolder + FileName_NaimoDataList + ".xls";

        File file = new File(NaimoZipFilePathName);

        try {
            FileInputStream is = new FileInputStream(file);
            POIFSFileSystem fs = new POIFSFileSystem(is);
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet mySheet = wb.getSheetAt(0);

            int col = 0;
            String[] text = new String[10];

            Iterator rowIter = mySheet.rowIterator();
            rowIter.next();
            while (rowIter.hasNext())
            {
                HSSFRow myRow = (HSSFRow) rowIter.next();
                col = 0;

                Iterator cellIter = myRow.cellIterator();
                while (cellIter.hasNext()) {
                    col += 1;
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    text[col]=myCell.toString();
                }
                Content_Word cn = new Content_Word();
                cn.setText1(text[2]);
                cn.setText2(text[3]);
                cn.setText3(text[4]);
                cn.setText4(text[5]);
                int id = db_word.addContent(cn);

                int exportedID = (int) Float.parseFloat(text[1]);
                String ImagePathName = ExportedBookDataFolder + Manager_PreviewImage.getPreviewImageFileName(exportedID);
                Manager_PreviewImage.deletePreviewImage(id);
                if(Manager_PreviewImage.setPreviewImage(ImagePathName,id)){
                    setHasImage(id,true);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void ImportWordsFromFolder_csv(final String ExportedBookDataFolder){
        String ImagePathName;
        String ExportedBookDataPathName = ExportedBookDataFolder + FileName_NaimoDataList + ".csv";
        int lastPosition = numWord-1;
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ExportedBookDataPathName)),"euc-kr"));
            //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(ExportedBookDataPathName)),"utf-8"));
            //BufferedReader br = new BufferedReader(new FileReader(ExportedBookDataPathName));
            //ref. https://www.androidpub.com/521127
            String lineString = "";
            String titleText = br.readLine();
            while(((lineString = br.readLine()) != null)){
                //String[] row = lineString.split(",");
                String[] row = Manager_SystemControl.splitCSV(lineString);

                String[] texts = new String[5];
                System.arraycopy(row,0,texts,0,row.length);
                for(int i=row.length;i<5;i++){
                    texts[i]="";
                }
                int id = db_word.addContent(new Content_Word(texts[1],texts[2],texts[3],texts[4]));

                int csvID = Integer.parseInt(row[0]);
                ImagePathName = ExportedBookDataFolder + Manager_PreviewImage.getPreviewImageFileName(csvID);
                Manager_PreviewImage.deletePreviewImage(id);
                if(Manager_PreviewImage.setPreviewImage(ImagePathName,id)){
                    setHasImage(id,true);
                }
            }
            br.close();
        }catch (FileNotFoundException e){
            e.printStackTrace();
            //Toast.makeText(this, "File not Found", Toast.LENGTH_SHORT).show();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static void ImportWordsFromZipFile(String zipFile_ExportedBookData){
        if(zipFile_ExportedBookData==null) return;
        File file = new File(zipFile_ExportedBookData);if(!file.exists()) return;
        String path = Manager_SystemControl.getPathFromPathName(zipFile_ExportedBookData);
        String fnwoext = Manager_SystemControl.getFileNameWithoutExtensionFromPathName(zipFile_ExportedBookData);
        String fn = Manager_SystemControl.getFileName(zipFile_ExportedBookData);
        Manager_SystemControl.unpackZipToSubFolder(zipFile_ExportedBookData);
        ImportWordsFromFolder_xls(path + fnwoext +"/");
        Manager_SystemControl.deleteFolder(path + fnwoext +"/");
    }
    private void importContactPictures_code_particles(){
        int id = db_word.addContent(new Content_Word("test", ""));

        getContactList();

        InputStream photo = openPhoto(1);
        image_bitmap = BitmapFactory.decodeStream(photo);
        Manager_PreviewImage.setPreviewImage(image_bitmap,id);


        Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode("AA"));
        Uri photoUri = null;
        ContentResolver cr = this.getContentResolver();
        Cursor contact = cr.query(phoneUri,new String[] { ContactsContract.Contacts._ID }, null, null, null);

        if (contact.moveToFirst()) {
            long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
            photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);

        }
        if (photoUri != null) {
            InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, photoUri);
            if (input != null) {
                image_bitmap = BitmapFactory.decodeStream(input);
                Manager_PreviewImage.setPreviewImage(image_bitmap,id);
            }
        }
    }
    private void importContactPictures(){
        InputStream photo;
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String idString = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                int contactId = Integer.parseInt(idString);
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

//                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
//                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id}, null);
//                    while (pCur.moveToNext()) {
//                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        Log.i("ㅁ", "Name: " + name);
//                        Log.i("ㅁ", "Phone Number: " + phoneNo);
//                    }
//                    pCur.close();
//                }

                String photo_id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID));
                if(photo_id!=null){
                    int id = db_word.addContent(new Content_Word(name, ""));

                    String photoFileID = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_FILE_ID));
                    //String PHOTO_THUMBNAIL_URI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    //String PHOTO_URI = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI));

                    if(photoFileID==null){
                        photo = openPhoto(contactId); //small
                    }else{
                        photo = openDisplayPhoto(contactId);  //big
                    }
                    image_bitmap = BitmapFactory.decodeStream(photo);
                    Manager_PreviewImage.setPreviewImage(image_bitmap,id);
                    setHasImage(id,true);
                }


            }
        }else{
            Toast.makeText(this,"오류(3401): 연락처가 하나도 없거나 접근할 수 없습니다.",Toast.LENGTH_LONG).show();
        }
        if(cur!=null){
            cur.close();
        }
        ReadWordDataBase();
        DisplayList();

    }
    private void getContactList() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(
                        cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME));

                if (cur.getInt(cur.getColumnIndex(
                        ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        Log.i("ㅁ", "Name: " + name);
                        Log.i("ㅁ", "Phone Number: " + phoneNo);
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
    }
    private void getContactsDetails() {
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while (phones.moveToNext()) {
            String Name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String Number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            String image_uri = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));

            System.out.println("Contact1 : " + Name + ", Number " + Number + ", image_uri " + image_uri);

            if (image_uri != null) {
                //image.setImageURI(Uri.parse(image_uri));
            }
        }
    }
    public InputStream openPhoto(long contactId) { //Retrieving the thumbnail-sized photo;
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }
    public InputStream openDisplayPhoto(long contactId) { //Retrieving the larger photo version
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }
    private void getPicturesFolderByUser(){
        String initialPath = Environment.getExternalStorageDirectory().toString()+"/";
        Intent intent = new Intent(this, FolderSelectionActivity.class);
        intent.putExtra("InitialFolder",initialPath);
        startActivityForResult(intent, REQUEST_CODE_IMPORT_PICTURE_FOLDER);
    }
    private void showListSelectCheckBox(){
        //ListView wordList = findViewById(R.id.wordList);
        //int FirstVisiblePosition = wordList.getFirstVisiblePosition();
        DisplayListWithCheckBox();
        //wordList.setSelection(FirstVisiblePosition);

        FloatingActionButton fab_plus = findViewById(R.id.fab_plus);
        fab_plus.setVisibility(View.INVISIBLE);
        FloatingActionButton fab_play = findViewById(R.id.fab_play);
        fab_play.setVisibility(View.INVISIBLE);
        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setVisibility(View.VISIBLE);
    }
    private void adSetting(){
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    private void setting_quizArraySize(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_setting_arraysize, null);
        builder.setView(view);
        final EditText txtRow = view.findViewById(R.id.editText_QuizArrayRowSize);
        final EditText txtCol = view.findViewById(R.id.editText_QuizArrayColSize);
        txtRow.setText(String.valueOf(quizArraySizeRow));
        txtCol.setText(String.valueOf(quizArraySizeCol));
        Button submit = view.findViewById(R.id.btn_QuizArraySize);

        SeekBar seekBarRow = view.findViewById(R.id.seekBar_maxtrixSize_Row);
        SeekBar seekBarCol = view.findViewById(R.id.seekBar_maxtrixSize_Column);
        seekBarRow.setProgress(quizArraySizeRow);
        seekBarCol.setProgress(quizArraySizeCol);

        dialog_word_edit = builder.create();

        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int quizArraySizeRow = Integer.parseInt(txtRow.getText().toString());
                int quizArraySizeCol = Integer.parseInt(txtCol.getText().toString());
                setQuizArraySize(quizArraySizeRow,quizArraySizeCol);
                dialog_word_edit.dismiss();
                //ActivityResultsPreperation();

            }
        });
        seekBarRow.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtRow.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBarCol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtCol.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        dialog_word_edit.show();
    }
    public void setQuizArraySize(int row, int col){
        quizArraySizeRow=row;
        quizArraySizeCol=col;
        content_Book.setQuizSizeRow(quizArraySizeRow);
        content_Book.setQuizSizeCol(quizArraySizeCol);
        db_book.updateContent(content_Book);
    }
    public void ImportNaimoZipFileByUser(){
        //File mPath = new File(Environment.getExternalStorageDirectory() + "//DIR//");
        File mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        final Manager_FileDialog managerFileDialog = new Manager_FileDialog(this, mPath, ".zip");
        managerFileDialog.addFileListener(new Manager_FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                managerFileDialog.removeFileListener(this);
                //ImportNaimoZipFile(file.getPath());
                new ImportNaimoZipFile(WordListActivity.this,file.getPath()).execute();
            }
        });
        managerFileDialog.showDialog();
    }
    public void getExportedBookDataXlsFileByUser(){
        //InputStream databaseInputStream = getResources().openRawResource(R.raw.kbc_words);
        File mPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        //String pathName = mPath.getPath() + "/" + "sample_wordbook_daum.xls";
        //Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        final Manager_FileDialog managerFileDialog = new Manager_FileDialog(this, mPath, ".xls");
        managerFileDialog.addFileListener(new Manager_FileDialog.FileSelectedListener() {
            public void fileSelected(File file) {
                managerFileDialog.removeFileListener(this);
                new LoadDaumXlsFile(WordListActivity.this,file.getPath()).execute();
            }
        });
        managerFileDialog.showDialog();
    }
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this,SelectedFolder+"를 선택했습니다.",Toast.LENGTH_LONG).show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        //Toast.makeText(getBaseContext(), "resultCode : "+resultCode,Toast.LENGTH_SHORT).show();
        if(requestCode == REQ_CODE_SELECT_IMAGE) {
            if(resultCode== Activity.RESULT_OK) {
                String imageFilePathName = Manager_TxtMathTools.getPathNameFromURI(getApplicationContext(),intent.getData());
                boolean hasImage = Manager_PreviewImage.setPreviewImage(imageFilePathName, selectedID);
                Content_Word cw = db_word.getContent(selectedID);
                cw.setHasImage(hasImage);
                wordEditingDialog = new WordEditingDialog(this,db_word,cw);
                wordEditingDialog.addEditEndListener(new WordEditingDialog.EditEndedListener(){
                    public void editEnded() {
                        wordEditingDialog.removeEditEndListener(this);
                        ReadWordDataBase();
                        DisplayList();
                    }
                });
                wordEditingDialog.showDialog();
            }
        }
        if(requestCode == REQUEST_CODE_IMPORT_PICTURE_FOLDER){
            if(resultCode==12) {
                String PictureFolderPath = intent.getStringExtra("selectedFolder");
                //ImportPicturesFromFolder(PictureFolderPath);
                //ReadWordDataBase();
                //DisplayList();
                new ImportWordsFromPictureFolder(WordListActivity.this,PictureFolderPath).execute();
            }
        }
        if(requestCode==REQ_CODE_FAMILIARITY){
            db_word.setSortingColumn(4);
            db_word.setSortDescending(false);
            ReadWordDataBase();
            DisplayList();
        }
    }
    private void ToolbarFloatingButtonSetting(){
        Manager_SystemControl.setStatusBarColor(this);

        Toolbar toolbar = findViewById(R.id.toolbar_word);
        setSupportActionBar(toolbar);

        FloatingActionButton fab_plus = findViewById(R.id.fab_plus);
        fab_plus.setVisibility(View.VISIBLE);
        fab_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                //AddWord();
                AddWordFromDaum();
            }
        });

        FloatingActionButton fab_play = findViewById(R.id.fab_play);
        fab_play.setVisibility(View.VISIBLE);
        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(numWord<1){
                    Toast.makeText(getApplicationContext(), R.string.noWordMessage,Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(WordListActivity.this, GameActivity_PairSelection.class);
                intent.putExtra("DB_Name",Word_DB_Name);
                intent.putExtra("QuizArraySize_Row",quizArraySizeRow);
                intent.putExtra("QuizArraySize_Col",quizArraySizeCol);
                startActivityForResult(intent,REQ_CODE_FAMILIARITY);
            }
        });

        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setVisibility(View.INVISIBLE);
        fab_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int numList = customAdapter_word.getCount();
                int numSelectedList = 0;
                FabVisibleSetting_SelectMode();

                for(int i=0;i<numList;i++){
                    boolean chkBoxState = CustomAdapter_Word.checkBoxState[i];
                    if(chkBoxState){
                        numSelectedList=numSelectedList+1;
                    }
                }

                if(numSelectedList!=0){
                    AlertDialog.Builder builderDelete = new AlertDialog.Builder(WordListActivity.this);     // 여기서 this는 Activity의 this
                    builderDelete.setTitle(R.string.confirmDeletion)        // 제목 설정
                            .setMessage("Delete " + numSelectedList +" words.")        // 메세지 설정
                            .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton){
                                    int numList = customAdapter_word.getCount();
                                    for(int i=0;i<numList;i++){
                                        boolean chkBoxState = CustomAdapter_Word.checkBoxState[i];
                                        if(chkBoxState){
                                            DeleteWord(IDs[i]);
                                        }
                                    }
                                    FabVisibleSetting_NormalMode();
                                    ReadWordDataBase();
                                    DisplayList();
                                }
                            })
                            .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener(){
                                // 취소 버튼 클릭시 설정
                                public void onClick(DialogInterface dialog, int whichButton){
                                    dialog.cancel();
                                    //dialog.dismiss();
                                    //recreate();
                                }
                            });
                    AlertDialog dialogDelete = builderDelete.create();    // 알림창 객체 생성
                    dialogDelete.show();    // 알림창 띄우기
                }else{
                    Toast.makeText(WordListActivity.this, R.string.noSelection,Toast.LENGTH_LONG).show();
                    FabVisibleSetting_NormalMode();
                    ReadWordDataBase();
                    DisplayList();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void AddWordFromDaum(){
        Intent intent = new Intent(this, GetWordFromDaumActivity.class);
        intent.putExtra("WordDBName",Word_DB_Name);
        startActivityForResult(intent, REQUEST_CODE_GET_WORD);
    }
    private void FabVisibleSetting_SelectMode(){
        FloatingActionButton fab_plus = findViewById(R.id.fab_plus);
        fab_plus.setVisibility(View.VISIBLE);
        FloatingActionButton fab_play = findViewById(R.id.fab_play);
        fab_play.setVisibility(View.VISIBLE);
        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setVisibility(View.INVISIBLE);
    }
    private void FabVisibleSetting_NormalMode(){
        FloatingActionButton fab_delete = findViewById(R.id.fab_delete);
        fab_delete.setVisibility(View.INVISIBLE);
        FloatingActionButton fab_plus = findViewById(R.id.fab_plus);
        fab_plus.setVisibility(View.VISIBLE);
        FloatingActionButton fab_play = findViewById(R.id.fab_play);
        fab_play.setVisibility(View.VISIBLE);
    }
    private void getContactPermissionAndImportContactPictures(){
        Toast.makeText(this,"사진이 있는 연락처 목록을 가져옵니다.",Toast.LENGTH_LONG).show();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                .READ_CONTACTS}, REQ_ADD_CONTACT_PICTURES);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case (REQ_ADD_CONTACT_PICTURES): {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importContactPictures();
                } else {
                    Toast.makeText(this, "연락처 사진을 가져오려면 주소록 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_word, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                db_word.setSearchingWord(newText);
                ReadWordDataBase();
                DisplayList();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                db_word.setSearchingWord(query);
                ReadWordDataBase();
                DisplayList();
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);
        return true;
    }
    private class LoadDaumXlsFile extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String xlsFile_ExportedBookData;

        LoadDaumXlsFile(WordListActivity activity, String xlsFile) {
            dialog = new ProgressDialog(activity);
            xlsFile_ExportedBookData=xlsFile;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Importing words from a xls file, please wait.");
            dialog.show();
            dialog.setCancelable(false);
        }

        protected Void doInBackground(Void... args) {
            if(xlsFile_ExportedBookData==null) return null;
            File file = new File(xlsFile_ExportedBookData);if(!file.exists()) return null;

            FileInputStream myInput = null;

            try {
                myInput = new FileInputStream(file);
                POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
                HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);
                HSSFSheet mySheet = myWorkBook.getSheetAt(0);

                int row = 0;
                int col = 0;
                String[] text = new String[10];

                Iterator rowIter = mySheet.rowIterator();
                while (rowIter.hasNext())
                {
                    row += 1;
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    col = 0;

                    Iterator cellIter = myRow.cellIterator();
                    while (cellIter.hasNext())
                    {
                        col += 1;
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        //Toast.makeText(this, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                        text[col]=myCell.toString();
                    }
                    if(row!=1){
                        Content_Word cn = new Content_Word();
                        cn.setText1(text[1]);
                        cn.setText2(text[3]);
                        cn.setText3(text[4]);
                        cn.setText4(text[2]);
                        db_word.addContent(cn);
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            int row =getResources().getInteger(R.integer.defaultQuizArraySize_Row_for_WordGame);
            int col =getResources().getInteger(R.integer.defaultQuizArraySize_Column_for_WordGame);
            setQuizArraySize(row,col);
            ReadWordDataBase();
            DisplayList();
        }
    }
    public static class LoadSampleBook_zipFile extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String naimoZipPathName;


        LoadSampleBook_zipFile(Activity activity,String naimoZipPathName) {
            dialog = new ProgressDialog(activity);
            this.naimoZipPathName = naimoZipPathName;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Importing words from a zip file, please wait.");
            dialog.show();
            dialog.setCancelable(false);
        }

        protected Void doInBackground(Void... args) {
            ImportWordsFromZipFile(naimoZipPathName);
            zip_xls_ImportComplete = true;
            Manager_SystemControl.deleteFile(naimoZipPathName);
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            ReadWordDataBase();
            DisplayList();
        }
    }
    public static class ImportNaimoZipFile extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String zipFile_ExportedBookData;

        ImportNaimoZipFile(Activity activity, String zipFilePathName) {
            dialog = new ProgressDialog(activity);
            zipFile_ExportedBookData = zipFilePathName;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Naimo ZIP 파일에서 단어를 불러오고 있습니다.");
            dialog.show();
            dialog.setCancelable(false);
        }

        protected Void doInBackground(Void... args) {
            //if(zipFile_ExportedBookData==null) finish();
            File file = new File(zipFile_ExportedBookData);
            //if(!file.exists()) {
            //    finish();
            //}
            String path = Manager_SystemControl.getPathFromPathName(zipFile_ExportedBookData);
            String fnwoext = Manager_SystemControl.getFileNameWithoutExtensionFromPathName(zipFile_ExportedBookData);
            String fn = Manager_SystemControl.getFileName(zipFile_ExportedBookData);
            Manager_SystemControl.unpackZipToSubFolder(zipFile_ExportedBookData);
            ImportWordsFromFolder_xls(path + fnwoext +"/");
            Manager_SystemControl.deleteFolder(path + fnwoext +"/");
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            ReadWordDataBase();
            DisplayList();
            //Toast.makeText(dialog.getContext()," " + db_word.getCount() + " 건",Toast.LENGTH_SHORT).show();
        }
    }
    public static class ExportWordsToZipFile extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        ExportWordsToZipFile(Activity activity) {
            dialog = new ProgressDialog(activity);
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("단어장을 다운로드 폴더로 내보내고 있습니다.");
            dialog.show();
            dialog.setCancelable(false);
        }

        protected Void doInBackground(Void... args) {
            ExportNoteToFolder(Word_DB_Name);
            String ExportedPath = NaimoDataExportPath + Word_DB_Name + "/";
            String[] allFiles = Manager_SystemControl.getAllFilePathNames(ExportedPath);
            String downLoadPath = Manager_SystemControl.getDownloadPath();
            String zipFileName = downLoadPath+Word_DB_Name + "_" + bookName +".zip";
            Manager_SystemControl.makeZipFile(allFiles,zipFileName);
            Manager_SystemControl.deleteFolder(ExportedPath);
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            //Toast.makeText(WordListActivity.this,"다운로드 폴더에 저장했습니다." ,Toast.LENGTH_LONG).show();
        }
    }
    private static class ImportWordsFromPictureFolder_old extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String pictureFolderPath;

        ImportWordsFromPictureFolder_old(WordListActivity activity, String pictureFolderPath) {
            dialog = new ProgressDialog(activity);
            this.pictureFolderPath =  pictureFolderPath;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("폴더에 있는 그림파일을 불러오고 있습니다.");
            dialog.show();
            dialog.setCancelable(false);
        }

        protected Void doInBackground(Void... args) {
            String[] fns = Manager_SystemControl.getAllFilePathNames(pictureFolderPath);
            for(String fn : fns){
                String ext = Manager_SystemControl.getExtension(fn).toLowerCase();
                String fnwoext = Manager_SystemControl.getFileNameWithoutExtensionFromPathName(fn);
                if(ext.equals("png") || ext.equals("jpg")){
                    if(fnwoext.length() != 0){
                        int id = db_word.addContent(new Content_Word(fnwoext, " "));
                        Manager_PreviewImage.setPreviewImage(fn,id);
                        setHasImage(id,true);
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            //Toast.makeText(getApplicationContext(),"폴더에 있는 그림파일을 추가했습니다." ,Toast.LENGTH_LONG).show();
            ReadWordDataBase();
            DisplayList();
        }
    }
    private static class ImportWordsFromPictureFolder extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;
        private String pictureFolderPath;
        private String[] pathNames;
        private int numFile;

        ImportWordsFromPictureFolder(WordListActivity activity, String pictureFolderPath) {
            dialog = new ProgressDialog(activity);
            this.pictureFolderPath =  pictureFolderPath;
        }
        @Override
        protected void onPreExecute() {
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMessage("폴더에 있는 그림파일을 불러오고 있습니다.");
            dialog.setCancelable(false);
            dialog.show();

            pathNames = Manager_SystemControl.getAllFilePathNames(pictureFolderPath,new String[] {"png","jpg"});
            numFile = pathNames.length;

            dialog.setMax(numFile);
        }

        protected Void doInBackground(Void... args) {
            int i=0;
            for(String fn : pathNames){
                String fnwoext = Manager_SystemControl.getFileNameWithoutExtensionFromPathName(fn);
                if(fnwoext.length() != 0){
                    int id = db_word.addContent(new Content_Word(fnwoext, " "));
                    Manager_PreviewImage.setPreviewImage(fn,id);
                    setHasImage(id,true);
                    dialog.setProgress(i);
                    i+=1;
                }
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            // do UI work here
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            //Toast.makeText(getApplicationContext(),"폴더에 있는 그림파일을 추가했습니다." ,Toast.LENGTH_LONG).show();
            ReadWordDataBase();
            DisplayList();
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_delete_words) {
            showListSelectCheckBox();
            return true;
        }
        if (id == R.id.menu_Export_ZIP) {
            if(numWord<1){
                Toast.makeText(getApplicationContext(), R.string.noWordMessage,Toast.LENGTH_LONG).show();
                return false;
            }
            new ExportWordsToZipFile(WordListActivity.this).execute();
            return true;
        }
        if (id == R.id.action_import_picture_folder) {
            Toast.makeText(this,"그림파일이 있는 폴더를 선택하세요.",Toast.LENGTH_LONG).show();
            getPicturesFolderByUser();
            return true;
        }
        if (id == R.id.search) {
            Toast.makeText(this,"Coming soon.",Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.quizArraySize) {
            setting_quizArraySize();
            return true;
        }
        if (id == R.id.action_import_words_from_zipfile) {
            Toast.makeText(this,"Naimo ZIP 파일이 있는 폴더를 선택하세요.",Toast.LENGTH_LONG).show();

            ImportNaimoZipFileByUser();
            return true;
        }
        if (id == R.id.action_import_words_from_xlsfile) {
            Toast.makeText(this,"DAUM 공개 단어장 XLS 파일이 있는 폴더를 선택하세요.",Toast.LENGTH_LONG).show();
            getExportedBookDataXlsFileByUser();
            return true;
        }
        if (id == R.id.action_import_words_from_contacts) {
            getContactPermissionAndImportContactPictures();
            return true;
        }
        if (id == R.id.memu_Sort_FI) {
            db_word.setSortingColumn(4);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_Sort_Name) {
            db_word.setSortingColumn(1);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_Sort_Text) {
            db_word.setSortingColumn(2);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_Sort_Date_Generate) {
            db_word.setSortingColumn(8);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_Sort_Date_Modify) {
            db_word.setSortingColumn(9);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_Sort_Date_Expose) {
            db_word.setSortingColumn(10);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        if (id == R.id.memu_word_sort_ID) {
            db_word.setSortingColumn(0);
            ReadWordDataBase();
            DisplayList();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

