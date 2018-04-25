package com.real.bckim.naimo2000;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 폴더 선택을 위한 엑티비티
 * 에러가 좀 있기는 하지만 대충 쓰자
 */
//ref http://mainia.tistory.com/1188
//ref http://webnautes.tistory.com/1020

public class FolderSelectionActivity extends AppCompatActivity {
    private List<String> item;
    private List<String> path;
    private String root = "/";
    private String StorageRootDirectory = Environment.getExternalStorageDirectory().toString()+"/"; // "/storage/emulated/0"
    public String SelectedFolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_selection);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Manager_SystemControl.setStatusBarColor(this);


        //if(Build.VERSION.SDK_INT>=21) getWindow().setStatusBarColor(Color.BLACK); //생태표시줄 색상설정: 이걸 안하면 하얗게 됨.

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
                Intent data = new Intent();
                data.putExtra("selectedFolder",SelectedFolder +"/");
                setResult(12,data);
                finish();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        SelectedFolder=intent.getStringExtra("InitialFolder");
        //getDir(StorageRootDirectory);
        getDir(SelectedFolder);
    }
    protected void onDestroy() {
        super.onDestroy();
        //Toast.makeText(this,SelectedFolder+"를 선택했습니다.",Toast.LENGTH_LONG).show();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
    public void getDir(String dirPath){
        SelectedFolder=dirPath;
        item = new ArrayList<>();
        path = new ArrayList<>();
        File f = new File(dirPath);
        File[] files = f.listFiles();
        if(dirPath!=StorageRootDirectory){
            //item.add(root);
            //path.add(StorageRootDirectory);
            item.add("../");
            path.add(f.getParent());
        }

        for (File file : files) {
            path.add(file.getPath());
            if (file.isDirectory()) {
                item.add(file.getName() + "/");
            } else {
                item.add(file.getName());
            }
        }

        final ListView listView = findViewById(R.id.listView_FolderSelect);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,item);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
                File file = new File(path.get(position));
                if (file.isDirectory()) {
                    if (file.canRead())
                        getDir(path.get(position));
                    else {
                        msgCantBeRead(file);
                    }
                }else{
                    msgWhat(file);
                }
                //String selected_item = (String)adapterView.getItemAtPosition(i);
            }
        });


//        String StorageRootDirectory = Environment.getExternalStorageDirectory().toString()+"/"; // "/storage/emulated/0"
//        String tmp11 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
//        String dirPath = StorageRootDirectory;

//        item.add("사과");
//        item.add("배");
//        item.add("귤");
//        item.add("바나나");

    }
    private void msgCantBeRead(File file){
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle("[" + file.getName() + "] folder can't be read!")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
    private void msgWhat(File file){
        new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_launcher_foreground)
                .setTitle("["+file.getName()+"]")
                .setPositiveButton("OK",new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int which){
                    }
                }).show();
    }
}
