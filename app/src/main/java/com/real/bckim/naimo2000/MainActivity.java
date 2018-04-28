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
import android.widget.ImageButton;
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
    static final int REQ_CODE_IMPORT_CONTACT_PICTURES=302;
    static final int REQ_ADD_SAMPLE_NOTES =303;

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
                    .WRITE_EXTERNAL_STORAGE}, REQ_ADD_SAMPLE_NOTES);
        }
    }
    private void ToolbarFabDrawerNaviewSetting(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab_plus_menu = findViewById(R.id.fab_plus_menu);
        fab_plus_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddBook("새 노트","");
                ReadDataBase();
                DisplayList();
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
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case (REQ_ADD_SAMPLE_NOTES): {
                Manager_SystemControl.makeFolder(NaimoDataExportPath);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AddSampleBook_Basics();
                } else {
                    Toast.makeText(this, "기본 단어장을 불러오기 위해서는 외부 저장장치 읽기/쓰기 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                }
            }
        }
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
        ImageButton delete = view.findViewById(R.id.buttonDelete);
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
    public void AddSampleBook_Basics(){
        InputStream databaseInputStream = getResources().openRawResource(R.raw.ex0_elementry);
        String pathName = NaimoDataExportPath + "sample_element.zip";
        Manager_SystemControl.saveFileFromInputStream(databaseInputStream,pathName);

        String bookName = "초등 단어장";
        AddBookFromZipFile_DIRECT(bookName,pathName);
    }
    private void DownloadDAUM_openWordBook(){
        Intent intent = new Intent(MainActivity.this, GetDaumPublicWordNoteActivity.class);
        startActivityForResult(intent,REQ_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE);
    }
    private void AddBookFromContactPictures(){
        long bookID = AddBook("사람이름 외우기"," ");
        Content_Book cb = db_book.getContent(bookID);

        Intent intent = new Intent(MainActivity.this, WordListActivity.class);
        intent.putExtra("bookName", cb.getText1());
        intent.putExtra("bookID", cb.getID());
        intent.putExtra("QuizArraySize_Row",cb.getQuizSizeRow());
        intent.putExtra("QuizArraySize_Col",cb.getQuizSizeCol());
        intent.putExtra("importContactPictures", "true");
        startActivityForResult(intent,REQ_CODE_NUM_ITEM);
    }
    private void AddBookFromPictureFolders(){
        long bookID = AddBook("그림 외우기"," ");
        Content_Book cb = db_book.getContent(bookID);

        Intent intent = new Intent(MainActivity.this, WordListActivity.class);
        intent.putExtra("bookName", cb.getText1());
        intent.putExtra("bookID", cb.getID());
        intent.putExtra("QuizArraySize_Row",cb.getQuizSizeRow());
        intent.putExtra("QuizArraySize_Col",cb.getQuizSizeCol());
        intent.putExtra("importPictureFolder", "true");
        startActivityForResult(intent,REQ_CODE_NUM_ITEM);
    }
    public void AddBookFromZipFile_DIRECT(String bookName, String zipFilePathName){
        long bookID = AddBook(bookName," ");
        String Word_DB_Name = WordListActivity.pre_Word_DB_Name + bookID;
        WordListActivity.db_word = new ListDataHandler_Word(this, Word_DB_Name);

        new WordListActivity.ImportNaimoZipFile(this,zipFilePathName).execute();
    }
    public void AddBookFromZipFile(String bookName, String zipFilePathName){
        long bookID = AddBook(bookName," ");
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
        long bookID = AddBook(bookName," ");
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
    private long AddBook(String text1, String text2){
        if(text1.length() != 0){
            return db_book.addContent(new Content_Book(text1, text2));
        }else{
            return -1;
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Toast.makeText(getBaseContext(), "resultCode : " + resultCode, Toast.LENGTH_SHORT).show();
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
        if (id == R.id.add_sample_note_presidents) {
            //AddSampleBook_Presidents();
            String pathName = NaimoDataExportPath + "sample_presidents.zip";
            String bookName = "Presidents Name(sample)";
            AddBookFromZipFile_DIRECT(bookName,pathName);

            return true;
        }
        if (id == R.id.add_sample_note_natinal_flags) {
            return true;
        }
        if (id == R.id.add_sample_note_president_rok) {
            return true;
        }
        if (id == R.id.add_sample_note_english_korean) {
            return true;
        }
        if (id == R.id.import_wordNote_DAUM) {
            DownloadDAUM_openWordBook();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_importDaumDictionary) {
            DownloadDAUM_openWordBook();
        } else if (id == R.id.nav_importContactPictures) {
            AddBookFromContactPictures();
        } else if (id == R.id.nav_importPictureFolder) {
            AddBookFromPictureFolders();
        } else if (id == R.id.nav_review) {
            openPlayStoreReviewPage();
        } else if (id == R.id.nav_privacyPolicy) {
            openPersonalInformationPage();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    private void openPlayStoreReviewPage(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.real.bckim.naimo2000"));
        startActivity(browserIntent);
    }
    private void openPersonalInformationPage(){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.naver.com/husky81/221260969030"));
        startActivity(browserIntent);
    }
}
