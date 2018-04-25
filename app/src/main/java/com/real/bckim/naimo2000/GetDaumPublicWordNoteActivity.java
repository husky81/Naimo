package com.real.bckim.naimo2000;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class GetDaumPublicWordNoteActivity extends AppCompatActivity {
    private FloatingActionButton fabDown;
    private WebView webView;
    private DownloadManager downloadManager;
    private long refid;
    private String downloadFilePathName;
    private String wordBookTitle;
    ArrayList<Long> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_daum_public_word_note);

        ToolbarFloatingButtonSetting();


        WebViewSetting();

        ProgressBar pb = findViewById(R.id.progressBar_webview);
        pb.setVisibility(View.VISIBLE);

        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }
    private void ToolbarFloatingButtonSetting(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fabDown = findViewById(R.id.fabDownLoadOpenWordBook);
        fabDown.setBackgroundColor(0xFFFF4400); //클래스가 달라서 그런지 안됨. 서브루틴으로 바꿔볼까?
        fabDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ImportDaumOpenWordBook();
            }
        });
    }
    private void WebViewSetting(){
        String DaumPublicWordNoteUrl = "http://wordbook.daum.net/open/wordbook/list.do?dic_type=endic";
        webView = findViewById(R.id.webView);
        webView.setWebViewClient(new MyWebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                //출처: http://jabstorage.tistory.com/5 [개발자 블로그]
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,String contentDisposition, String mimetype,long contentLength) {
                String wordBookFileName = "DaumWordBook.xls";

                //Toast.makeText(GetDaumPublicWordNoteActivity.this,"다운로드가 시작되었습니다.", Toast.LENGTH_LONG).show();

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,wordBookFileName);
                downloadFilePathName = Manager_FileAndFolderControl.getDownloadPath() + wordBookFileName;
                Manager_FileAndFolderControl.deleteFile(downloadFilePathName); //기존에 다운받은 단어장 삭제.
                //request.setTitle("GadgetSaint Downloading " + "Sample" + ".png");
                //request.setDescription("Downloading " + "Sample" + ".png");
                request.setVisibleInDownloadsUi(true);
                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                refid = downloadManager.enqueue(request);
                list.add(refid);

                Toast.makeText(getApplicationContext(), "\"DaumWordBook.xls\" 다운로드를 시작합니다.", Toast.LENGTH_SHORT).show();
            }
        });

        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavascriptInterface(), "Android");

        //줌인 가능하게 설정. 아래것 없으면 줌 컨트롤이 안됨.
        //https://stackoverflow.com/questions/5125851/enable-disable-zoom-in-android-webview
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.loadUrl(DaumPublicWordNoteUrl);
    }
    private void ActivityResultsPreperation() {
        Intent data = new Intent();
        data.putExtra("DownloadFile",downloadFilePathName);
        data.putExtra("WordBookTitle",wordBookTitle);
        setResult(MainActivity.RLT_CODE_DOWNLOAD_DAUM_OPEN_WORD_NOTE,data);
        //super.onDestroy();
        finish();
    }
        BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            // get the refid from the download manager
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

            // remove it from our list
            list.remove(referenceId);

            // if list is empty means all downloads completed
            if (list.isEmpty())
            {
                Toast.makeText(GetDaumPublicWordNoteActivity.this, "다운로드가 완료되었습니다.",Toast.LENGTH_SHORT).show();
                ActivityResultsPreperation();

                // show a notification
//                NotificationCompat.Builder mBuilder =
//                        new NotificationCompat.Builder(GetDaumPublicWordNoteActivity.this)
//                                .setSmallIcon(R.mipmap.ic_launcher)
//                                .setContentTitle("GadgetSaint")
//                                .setContentText("All Download completed");
//                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//                notificationManager.notify(455, mBuilder.build());
            }
        }
    };
    public void ImportDaumOpenWordBook(){
        Toast.makeText(this,"Download the word note",Toast.LENGTH_SHORT).show();
    }
    public String FindWordNoteTitle(String html){
        String subStr = Manager_TxtMathTools.WrapedText(html,"<div class=\"book_tit\">","</div>");
        subStr = Manager_TxtMathTools.WrapedText(subStr,"<h3 ","<" + "/" + "h" + "3>");
        subStr = Manager_TxtMathTools.WrapedText(subStr,"\">","");
        return subStr;
    }
    private class MyJavascriptInterface {
        @JavascriptInterface
        public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
            System.out.println(html);
            wordBookTitle = FindWordNoteTitle(html);

            ProgressBar pb = findViewById(R.id.progressBar_webview);
            pb.setVisibility(View.INVISIBLE);

            if(wordBookTitle!=""){
                Toast.makeText(GetDaumPublicWordNoteActivity.this,"다운로드 버튼을 클릭하세요.",Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(GetDaumPublicWordNoteActivity.this,"불러올 단어장을 선택하세요.",Toast.LENGTH_LONG).show();
            }
        }
    }
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            String tmpUri = Uri.parse(url).getHost();

            if (Uri.parse(url).getHost().equals("wordbook.daum.net")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            return true;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }
}

