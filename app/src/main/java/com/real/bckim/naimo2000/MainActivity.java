package com.real.bckim.naimo2000;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String Book_DB_Name = "NaimoBookDB_835";
    public static ListDataHandler_Book db_book;
    List<Content_Book> contents;
    int numBook;
    int[] IDs;

    String name;
    int SelectedItem;
    public final static String NaimoDataFolderPath = Environment.getExternalStorageDirectory().toString() + "/Naimo2000/";
    final static String NaimoDataExportPath = NaimoDataFolderPath + "Export/";
    private AdView mAdView;
    private Manager_PreviewImage PIM = new Manager_PreviewImage(Book_DB_Name);

    static final int REQ_CODE_NUM_ITEM=289;
    static final int RLT_CODE_NUM_ITEM=299;
    static final int REQ_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE=300;
    static final int RLT_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE=301;
    final int REQ_ADD_SAMPLE_NOTE_PRESIDENTS = 302;

    AlertDialog dialog_book_edit;
    Bitmap image_bitmap; //메모리 절약을 위해 비트맵은 Activity마다 한개만 쓰면 좋을 듯.
    final static int REQ_CODE_SELECT_IMAGE = 100; //그림파일을 아이콘으로 바꿀 때 저장되는 해상도
    final static int NotePadIconResource = R.drawable.notepad; //노트 기본아이콘.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ToolbarFabDrawerNaviewSetting();
        //permissionSetting();
        adSetting();

        setTitle(R.string.app_name);

        ReadDataBase();
        ListViewClickEventSetting();
        DisplayList();

        if (IDs.length < 1) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, REQ_ADD_SAMPLE_NOTE_PRESIDENTS);
        }
    }
    private void ToolbarFabDrawerNaviewSetting(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab_plus_menu = findViewById(R.id.fab_plus_menu);
        final FloatingActionButton fab_plus_EmptyNote = findViewById(R.id.fab_plus_EmptyNote);
        final FloatingActionButton fab_plus_SampleNote = findViewById(R.id.fab_plus_SampleNote);
        final FloatingActionButton fab_plus_PictureFoler = findViewById(R.id.fab_plus_PictureFolder);
        fab_plus_EmptyNote.setVisibility(View.INVISIBLE);
        fab_plus_SampleNote.setVisibility(View.INVISIBLE);
        fab_plus_PictureFoler.setVisibility(View.INVISIBLE);

        fab_plus_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if(fab_plus_EmptyNote.getVisibility()==View.VISIBLE){
                    setFabPlusOpen(false);
                }else{
                    setFabPlusOpen(true);
                }
            }
        });
        fab_plus_EmptyNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFabPlusOpen(false);
                AddBook();
            }
        });
        fab_plus_SampleNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFabPlusOpen(false);
                AddSampleBook_PresidentsKOR();
            }
        });
        fab_plus_PictureFoler.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFabPlusOpen(false);
                DownloadDAUM_openWordBook();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case (REQ_ADD_SAMPLE_NOTE_PRESIDENTS): {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Manager_SystemControl.makeFolder(NaimoDataExportPath);
                    AddSampleBook_Presidents();
                } else {
                    Toast.makeText(this, "단어장을 사용하기 위해서는 외부 저장장치 읽기/쓰기 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
    private void permissionSetting_old() {
        Manager_Permission myPermission = new Manager_Permission();
        myPermission.requestReadExternalStoragePermission(this, this);
        myPermission.requestWriteExternalStoragePermission(this, this);
    }
    private void adSetting() {
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    private void ReadDataBase() {
        db_book = new ListDataHandler_Book(this, Book_DB_Name);
        contents = db_book.getAllContents();

        numBook = contents.size();
        IDs = new int[numBook];
        int i = 0;
        for (Content_Book cn : contents) {
            IDs[i] = cn.getID();
            i++;
        }
    }
    private void ListViewClickEventSetting(){
        //ListView Click Setting
        ListView bookList = findViewById(R.id.listView);
        bookList.setOnItemClickListener(new ListClickHandler());
        //bookList.setOnItemClickListener(new MainActivity.ListClickHandler());
        bookList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                EditBook(position);
                return true;
            }
        });
    }
    private class ListClickHandler implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
            Content_Book cb = db_book.getContent(IDs[position]);
            Intent intent = new Intent(MainActivity.this, WordListActivity.class);
            intent.putExtra("bookName", cb.getText1());
            intent.putExtra("bookID", cb.getID());
            intent.putExtra("QuizArraySize_Row",cb.getQuizSizeRow());
            intent.putExtra("QuizArraySize_Col",cb.getQuizSizeCol());
            startActivityForResult(intent,REQ_CODE_NUM_ITEM);
        }
    }
    private void DeleteYN_Old(int id) {
        SelectedItem = id;
        Content_Book cn = db_book.getContent(id);
        String BookName = cn.getText1();
        AlertDialog.Builder builderDelete = new AlertDialog.Builder(this);     // 여기서 this는 Activity의 this
        builderDelete.setTitle(R.string.confirmDeletion)        // 제목 설정
                .setMessage(getString(R.string.preDeleteConfirmMessage) + BookName + getString(R.string.postDeleteConfirmMessage))        // 메세지 설정
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //finish();
                        DeleteBook(SelectedItem);
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialogDelete = builderDelete.create();    // 알림창 객체 생성
        dialogDelete.show();    // 알림창 띄우기
    }
    private void DeleteYN(int id) {
        SelectedItem = id;
        Content_Book cn = db_book.getContent(id);
        String BookName = cn.getText1();
        AlertDialog.Builder builderDelete = new AlertDialog.Builder(MainActivity.this);     // 여기서 this는 Activity의 this
        builderDelete.setTitle(R.string.confirmDeletion)        // 제목 설정
                .setMessage("Delete a note \"" + BookName + "\" ?")        // 메세지 설정
                .setCancelable(true)        // 뒤로 버튼 클릭시 취소 가능 설정
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    // 확인 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //finish();
                        dialog.cancel();
                        DeleteBook(SelectedItem);
                        //dialog.dismiss();
                        //recreate();
                    }
                })
                .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener() {
                    // 취소 버튼 클릭시 설정
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                        //dialog.dismiss();
                        //recreate();
                    }
                });
        AlertDialog dialogDelete = builderDelete.create();    // 알림창 객체 생성
        dialogDelete.show();    // 알림창 띄우기
    }
    private void DeleteBook(int id) {
        Content_Book cn = db_book.getContent(id);
        int bookID = cn.getID();
        db_book.deleteContent(cn);
        final String DB_Name = WordListActivity.pre_Word_DB_Name + bookID;

        final ProgressDialog ringProgressDialog = ProgressDialog.show(MainActivity.this, "Please wait ...",	"Delete words ...", true);
        ringProgressDialog.setCancelable(false);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //delete all preview images
                    Manager_PreviewImage.setDataBaseName(DB_Name);
                    Manager_PreviewImage.deletePreviewImageFolder();

                    //delete all words at the DB
                    ListDataHandler_Word db_word = new ListDataHandler_Word(MainActivity.this, DB_Name);
                    List<Content_Word> contents = db_word.getAllContents();

                    for (Content_Word cnw : contents) {
                        db_word.deleteContent(cnw);
                    }
                } catch (Exception e) {

                }
                ringProgressDialog.dismiss();
            }
        }).start();

        ReadDataBase();
        DisplayList();
    }
    private void DisplayList() {
        //customAdapter_book = new CustomAdapter_Book(getApplicationContext(), IDs, names, memos, numItems, contents);
        CustomAdapter_Book customAdapter_book = new CustomAdapter_Book(getApplicationContext(), contents);
        //customAdapter_book = new CustomAdapter_Book(getApplicationContext(), contents);
        ListView bookList = findViewById(R.id.listView);
        bookList.setAdapter(customAdapter_book);
        //customAdapter_book.notifyDataSetChanged();
    }
    private void AddBook() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_book_edit, null);
        builder.setView(view);
        ImageView icon = view.findViewById(R.id.imageIcon);
        Button add = view.findViewById(R.id.buttonSubmit);
        Button cancle = view.findViewById(R.id.buttonDelete);
        final EditText editText1 = view.findViewById(R.id.edittextBookName);
        final EditText editText2 = view.findViewById(R.id.edittextBookMemo);

        add.setText(R.string.add);
        cancle.setText(R.string.cancle);
        editText1.setText(R.string.New_Book);

        dialog_book_edit = builder.create();
        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectPhoto();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();
                if (text1.length() != 0) db_book.addContent(new Content_Book(text1, text2));
                dialog_book_edit.dismiss();
                ReadDataBase();
                DisplayList();
            }
        });
        cancle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog_book_edit.dismiss();
            }
        });
        dialog_book_edit.show();
    }
    private void EditBook(int position) {
        SelectedItem = position;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_book_edit, null);
        builder.setView(view);
        ImageView icon = view.findViewById(R.id.imageIcon);
        Button submit = view.findViewById(R.id.buttonSubmit);
        Button delete = view.findViewById(R.id.buttonDelete);
        final EditText editText1 = view.findViewById(R.id.edittextBookName);
        final EditText editText2 = view.findViewById(R.id.edittextBookMemo);

        Content_Book cn = db_book.getContent(IDs[position]);
        PIM.setDataBaseName(Book_DB_Name);
        image_bitmap = PIM.getPreviewImage(IDs[position]);
        if (image_bitmap == null) {
            icon.setImageResource(NotePadIconResource);
        } else {
            icon.setImageBitmap(image_bitmap);
        }

        String text1 = cn.getText1();
        String text2 = cn.getText2();

        editText1.setText(text1);
        editText2.setText(text2);
        submit.setText(R.string.ok);
        delete.setText(R.string.delete);

        dialog_book_edit = builder.create();
        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectPhoto();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Content_Book cn = db_book.getContent(IDs[SelectedItem]);
                String text1 = editText1.getText().toString();
                String text2 = editText2.getText().toString();
                if (text1.length() != 0) {
                    cn.setText1(text1);
                    cn.setText2(text2);
                    db_book.updateContent(cn);
                }
                dialog_book_edit.dismiss();
                ReadDataBase();
                DisplayList();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Content_Book cn = db_book.getContent(IDs[SelectedItem]);
                DeleteYN(cn.getID());
                dialog_book_edit.dismiss();
            }
        });
        dialog_book_edit.show();
    }
    private void selectPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }
    private String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }
    private String getImageFullPathNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String imgPath = cursor.getString(column_index);
        //String imgName = imgPath.substring(imgPath.lastIndexOf("/")+1);

        return imgPath;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    public void AddSampleBook_Presidents(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.sample_presidents);
        String pathName = NaimoDataExportPath + "sample_presidents.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        String bookName = "Presidents Name(sample)";
        AddBookFromZipFile(bookName,pathName);
    }
    public void AddSampleBook_NationalFlags(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.sample_flags);
        String pathName = NaimoDataExportPath + "sample_national_flags.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        String bookName = "National Flags(sample)";
        AddBookFromZipFile(bookName,pathName);
    }
    public void AddSampleBook_PresidentsKOR(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.sample_president_kor);
        String pathName = NaimoDataExportPath + "sample_presidents_korea.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        String bookName = "한국의대통령(sample)";
        AddBookFromZipFile(bookName,pathName);
    }
    public void AddSampleBook_EnglishKorean(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.sample_english_word);
        String pathName = NaimoDataExportPath + "sample_english_korean.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        String bookName = "영어단어장(4000제)";
        AddBookFromZipFile(bookName,pathName);
    }
    private void DownloadDAUM_openWordBook(){
        Intent intent = new Intent(MainActivity.this, GetDaumPublicWordNoteActivity.class);
        startActivityForResult(intent,REQ_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE);
    }
    public void AddBookFromZipFile(String bookName, String zipFilePathName){
        AddBook(bookName," ");
        int bookID = Manager_TxtMathTools.safeLongToInt(db_book.lastInsertedContentID);
        Content_Book cb = db_book.getContent(bookID);

        Intent intent = new Intent(MainActivity.this, WordListActivity.class);
        intent.putExtra("bookName", cb.getText1());
        intent.putExtra("bookID", cb.getID());
        intent.putExtra("QuizArraySize_Row",cb.getQuizSizeRow());
        intent.putExtra("QuizArraySize_Col",cb.getQuizSizeCol());
        intent.putExtra("importZipFile", zipFilePathName);
        startActivityForResult(intent,REQ_CODE_NUM_ITEM);
    }
    public void AddBookFromXlsFile(String bookName, String xlsFilePathName){
        AddBook(bookName," ");
        int bookID = Manager_TxtMathTools.safeLongToInt(db_book.lastInsertedContentID);
        Content_Book cb = db_book.getContent(bookID);

        Intent intent = new Intent(MainActivity.this, WordListActivity.class);
        intent.putExtra("bookName", cb.getText1());
        intent.putExtra("bookID", cb.getID());
        intent.putExtra("QuizArraySize_Row",getResources().getInteger(R.integer.defaultQuizArraySize_Row_for_WordGame));
        intent.putExtra("QuizArraySize_Col",getResources().getInteger(R.integer.defaultQuizArraySize_Column_for_WordGame));
        intent.putExtra("importZipFile", "");
        intent.putExtra("importXlsFile", xlsFilePathName);
        startActivityForResult(intent,REQ_CODE_NUM_ITEM);
    }
    private void AddBook(String text1, String text2){
        if(text1.length() != 0) db_book.addContent(new Content_Book(text1, text2));
    }
    private void setFabPlusOpen(Boolean isOpen){
        final FloatingActionButton fab_plus_menu = findViewById(R.id.fab_plus_menu);
        final FloatingActionButton fab_plus_EmptyNote = findViewById(R.id.fab_plus_EmptyNote);
        final FloatingActionButton fab_plus_SampleNote = findViewById(R.id.fab_plus_SampleNote);
        final FloatingActionButton fab_plus_PictureFoler = findViewById(R.id.fab_plus_PictureFolder);
        if(isOpen){
            //fab_plus_menu.setVisibility(View.INVISIBLE);
            fab_plus_menu.setMaxWidth(20);
            fab_plus_menu.setMaxHeight(20);
            fab_plus_EmptyNote.setVisibility(View.VISIBLE);
            fab_plus_SampleNote.setVisibility(View.VISIBLE);
            fab_plus_PictureFoler.setVisibility(View.VISIBLE);
        }else{
            //fab_plus_menu.setVisibility(View.VISIBLE);
            fab_plus_menu.setMaxWidth(80);
            fab_plus_menu.setMaxHeight(80);
            fab_plus_EmptyNote.setVisibility(View.INVISIBLE);
            fab_plus_SampleNote.setVisibility(View.INVISIBLE);
            fab_plus_PictureFoler.setVisibility(View.INVISIBLE);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getBaseContext(), "resultCode : " + resultCode, Toast.LENGTH_SHORT).show();
        setFabPlusOpen(false);

        if(requestCode==REQ_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE){
            if(resultCode==RLT_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE){
                String DaumWordXlsFilePathName = data.getStringExtra("DownloadFile");
                String DaumWordBookTitle = data.getStringExtra("WordBookTitle");
                AddBookFromXlsFile(DaumWordBookTitle,DaumWordXlsFilePathName);
            }
        }
        if (requestCode == REQ_CODE_NUM_ITEM) {
            if(resultCode==RLT_CODE_NUM_ITEM){
                //int bookID = data.getIntExtra("bookID",-1);
                //int numItem = data.getIntExtra("numItem",0);
                //int QuizArraySizeRow = data.getIntExtra("QuizArraySize_Row",0);
                //int QuizArraySizeCol = data.getIntExtra("QuizArraySize_Col",0);
                //Content_Book cw = db_book.getContent(bookID);
                //cw.setNumItem(numItem);
                //cw.setQuizSizeRow(QuizArraySizeRow);
                //cw.setQuizSizeCol(QuizArraySizeCol);
                //db_book.updateContent(cw);
                ReadDataBase();
                DisplayList();
            }
        }
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                //DB에 이미지 이름을 올림
                final Content_Book cn2 = db_book.getContent(IDs[SelectedItem]);
                String imageFilePathName = getImageFullPathNameToUri(data.getData());
                //cn2.setNumItem(imageFilePathName);
                db_book.updateContent(cn2);

                PIM.setPreviewImage(imageFilePathName, IDs[SelectedItem]);
                image_bitmap = PIM.getPreviewImage(IDs[SelectedItem]);

                //recreate();
                dialog_book_edit.dismiss();
                EditBook(SelectedItem);
                ReadDataBase();
                DisplayList();
//                LayoutInflater inflater = getLayoutInflater();
//                View view = inflater.inflate(R.layout.dialog_book_edit, null);
//                ImageView icon = view.findViewById(R.id.imageIcon);
//                icon.setImageBitmap(image_bitmap);


                //확인용 임시코드. 이거 지워도 잘 되는지 확인 필요함.
                //Content_Book cn2 = db_book.getContent(IDs[SelectedItem]);
                //String tmp = cn2.getImageString();

//                try {
//
//                    //다이얼로그에 이미지 업데이트. 안되고 있음.
//                    ImageView image = findViewById(R.id.imageIcon);
//                    image_bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
//                    image.setImageBitmap(image_bitmap);
//
//                    //다이얼로그를 닫았다가 다시 그리면 그림이 보이지 않을까 해서 시도해보려고 함.
//                    dialog_book_edit.dismiss();
//                    EditBook(SelectedItem);
//                    //recreate();
//
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            //startActivity(intent);
            Toast.makeText(this, R.string.coming_soon,Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.add_sample_note_presidents) {
            AddSampleBook_Presidents();
            return true;
        }
        if (id == R.id.add_sample_note_natinal_flags) {
            AddSampleBook_NationalFlags();
            return true;
        }
        if (id == R.id.add_sample_note_president_rok) {
            AddSampleBook_PresidentsKOR();
            return true;
        }
        if (id == R.id.add_sample_note_english_korean) {
            AddSampleBook_EnglishKorean();
            return true;
        }
        if (id == R.id.import_wordNote_DAUM) {
            DownloadDAUM_openWordBook();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
