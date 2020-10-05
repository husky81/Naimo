package com.real.bckim.naimo2000;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.text.BreakIterator;

import static com.real.bckim.naimo2000.WordListActivity.txtMeanings;
import static com.real.bckim.naimo2000.WordListActivity.txtPronunciation;

public class GetWordFromDaumActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word_from_daum);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        WebView wb = findViewById(R.id.webView_getDaumWord);

        wb.setWebViewClient(new WordEditingDialog.webViewClient_pronunciation() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.loadUrl("javascript:window.Android.getHtml(document.getElementsByTagName('html')[0].innerHTML);"); //<html></html> 사이에 있는 모든 html을 넘겨준다.
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    public void run() {
                        // yourMethod();
                        // pronText.setText(txtPronunciation);
                        // meanText.setText(txtMeanings);
                    }
                }, 100);
                //출처: http://jabstorage.tistory.com/5 [개발자 블로그]
            }
        });
        wb.getSettings().setJavaScriptEnabled(true);
        class MyJavascriptInterface2 {
            @JavascriptInterface
            public void getHtml(String html) { //위 자바스크립트가 호출되면 여기로 html이 반환됨
                System.out.println(html);
                //txtPronunciation = FindPronunciationFromDaumEndic(html);
                //txtMeanings = FindMeaningsFromDaumEndic(html);
                //txtMeanings = delete123(txtMeanings);
            }
        }
        wb.addJavascriptInterface(new MyJavascriptInterface2(), "Android");

        String searchText = "";
        searchText = Manager_TxtMathTools.ReplaceText(searchText, " ", "+");
        String DaumEndicUrl = "http://dic.daum.net/search.do?q=" + searchText;

        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setDisplayZoomControls(false);

        wb.loadUrl(DaumEndicUrl);


    }

}
