package com.real.bckim.naimo2000;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

/**
 * Created by bckim on 2018-02-22.
 * 권한을 얻기 위한 모든 설정을 여기서 관리할 것.
 호출 방법:
 Manager_Permission myPermission = new Manager_Permission();
 myPermission.requestReadExternalStoragePermission(this,this);
 */

public class Manager_Permission extends MainActivity {
    private static final int PERMISSION_REQUEST_EXTERNAL_STORAGE = 0;

    public void requestReadExternalStoragePermission(Context context, Activity activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
            Toast.makeText(context,"기능 실행을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ForcedQuit();
        }
    }
    public void requestWriteExternalStoragePermission(Context context, Activity activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
            Toast.makeText(context,"기능 실행을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ForcedQuit();
        }
    }
    public void requestReadContactsPermission(Context context, Activity activity) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_CONTACTS},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_EXTERNAL_STORAGE);
            }
            Toast.makeText(context,"기능 실행을 위해 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            ForcedQuit();
        }
    }
    public void ForcedQuit(){
        moveTaskToBack(true);
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
