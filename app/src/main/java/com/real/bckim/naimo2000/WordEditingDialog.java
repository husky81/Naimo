package com.real.bckim.naimo2000;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Locale;

import static com.real.bckim.naimo2000.MainActivity.REQ_CODE_SELECT_IMAGE;

class WordEditingDialog {
    private Activity activity;
    private ListDataHandler_Word db_word;
    private Content_Word cw;
    private String txtPronunciation;
    private String loadingString = "Loading...";

    private static String txtMeanings;
    public interface EditEndedListener {
        void editEnded();
    }
    private ListenerList<EditEndedListener> editEndedListenerList = new ListenerList<EditEndedListener>();

    TextToSpeech tts;
    String speachText;

    public void addEditEndListener(EditEndedListener listener) {
        editEndedListenerList.add(listener);
    }
    public void removeEditEndListener(EditEndedListener listener) {
        editEndedListenerList.remove(listener);
    }
    WordEditingDialog(Activity activity, final ListDataHandler_Word db_word, final Content_Word cw){
        this.activity = activity;
        this.db_word = db_word;
        this.cw = cw;
        txtPronunciation="";
    }
    public void showDialog() {
        createWordEditingDialog().show();
    }
    private void fireEditEndEvent() {
        editEndedListenerList.fireEvent(new ListenerList.FireHandler<EditEndedListener>() {
            public void fireEvent(EditEndedListener listener) {
                listener.editEnded();
            }
        });
    }
    private Dialog createWordEditingDialog(){
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_word_edit, null);

        final ImageView icon = view.findViewById(R.id.itemimageIcon);
        Button submit = view.findViewById(R.id.buttonSubmit_word);
        ImageButton delete = view.findViewById(R.id.buttonDelete_word);
        ImageButton btnGetDaumPronunciation = view.findViewById(R.id.getDaumPronunciation);
        ImageButton btnTTS = view.findViewById(R.id.btnSpeaker);
        final EditText editText1 = view.findViewById(R.id.edittextItemName);
        final EditText editText2 = view.findViewById(R.id.edittextItemMemo);
        final EditText editText3 = view.findViewById(R.id.edittextItemMemo2);
        final EditText editText4 = view.findViewById(R.id.edittextItem_Pronunciation);
        final WebView webViewPronunciation = view.findViewById(R.id.webViewPronunciation);

        if(cw.getHasImage()){
            Bitmap image_bitmap = Manager_PreviewImage.getPreviewImage(cw.getID());
            icon.setImageBitmap(image_bitmap);
        }else{
            icon.setImageResource(R.drawable.plus3);
        }

        editText1.setText(cw.getText1());
        editText2.setText(cw.getText2());
        editText3.setText(cw.getText3());
        editText4.setText(cw.getText4());

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setView(view);
        final AlertDialog dialog_word_edit_pairSelection = builder.create();

        icon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SaveWordEditDialogInfoToDataBase(editText1,editText2,editText3,editText4);
                selectPhoto();
                dialog_word_edit_pairSelection.dismiss();
            }
        });
        icon.setOnLongClickListener(new View.OnLongClickListener(){
            public boolean onLongClick(View v) {
                AlertDialog.Builder builderDelete = new AlertDialog.Builder(v.getContext());     // 여기서 this는 Activity의 this
                builderDelete.setTitle(R.string.confirmDeletion)
                        .setMessage("Do you want to delete this image?")
                        .setCancelable(true) // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int whichButton){
                                icon.setImageResource(R.drawable.plus3);
                                Manager_PreviewImage.deletePreviewImage(cw.getID());
                                cw.setHasImage(false);
                                db_word.updateContent(cw);
                                dialog_word_edit_pairSelection.show();
                            }
                        })
                        .setNegativeButton(R.string.cancle, new DialogInterface.OnClickListener(){
                            // 취소 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton){
                                dialog.cancel();
                            }
                        });
                AlertDialog dialogDelete = builderDelete.create(); // 알림창 객체 생성
                dialogDelete.show(); // 알림창 띄우기

                return true;
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SaveWordEditDialogInfoToDataBase(editText1,editText2,editText3,editText4);
                dialog_word_edit_pairSelection.dismiss();
                fireEditEndEvent();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dialog_word_edit_pairSelection.dismiss();

                String WordName = cw.getText1();
                AlertDialog.Builder builderDelete = new AlertDialog.Builder(v.getContext());     // 여기서 this는 Activity의 this
                builderDelete.setTitle(R.string.confirmDeletion)
                        .setMessage("Delete a word \""+ WordName +"\" ?")
                        .setCancelable(true) // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener(){
                            // 확인 버튼 클릭시 설정
                            public void onClick(DialogInterface dialog, int whichButton){
                                db_word.deleteContent(cw);
                                dialog.dismiss();
                                fireEditEndEvent();
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
        });
        btnGetDaumPronunciation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String searchText = editText1.getText().toString();
                FindWordMeaningsFromDaumEndic(searchText, editText4, editText2, webViewPronunciation);
            }
        });
        btnGetDaumPronunciation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                String searchText = editText1.getText().toString();
                FindWordMeaningsFromDaumEndic(searchText, editText4, editText2, webViewPronunciation);
                webViewPronunciation.setVisibility(View.VISIBLE);
                return false;
            }
        });
        btnTTS.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                speachText = editText1.getText().toString();
                tts=new TextToSpeech(v.getContext(), new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        if(status == TextToSpeech.SUCCESS){
                            int result=tts.setLanguage(Locale.US);
                            if(result!=TextToSpeech.LANG_MISSING_DATA && result!=TextToSpeech.LANG_NOT_SUPPORTED){
                                ConvertTextToSpeech();
                            }
                        }
                    }
                });
            }
        });
        return dialog_word_edit_pairSelection;
    }
    private void ConvertTextToSpeech() {
        tts.speak(speachText, TextToSpeech.QUEUE_FLUSH, null);
//        speachText = et.getText().toString();
//        if(speachText==null||"".equals(speachText))
//        {
//            speachText = "Content not available";
//            tts.speak(speachText, TextToSpeech.QUEUE_FLUSH, null);
//        }else
//            tts.speak(speachText+"is saved", TextToSpeech.QUEUE_FLUSH, null);
    }
    private void selectPhoto(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(intent, REQ_CODE_SELECT_IMAGE);
    }
    private void SaveWordEditDialogInfoToDataBase(EditText et1, EditText et2, EditText et3, EditText et4) {
        String text1 = et1.getText().toString();
        String text2 = et2.getText().toString();
        String text3 = et3.getText().toString();
        String text4;
        if (txtPronunciation == "") {
            text4 = et4.getText().toString();
        } else {
            text4 = txtPronunciation;
        }
        if (text1.length() != 0) {
            cw.setText1(text1);
            if (!text2.equals(loadingString));
                cw.setText2(text2);
            cw.setText3(text3);
            if (!text4.equals(loadingString));
                cw.setText4(text4);
            db_word.updateContent(cw);
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
                }, 100);
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
                txtMeanings = delete123(txtMeanings);
            }
        }
        wb.addJavascriptInterface(new MyJavascriptInterface2(), "Android");

        searchText = Manager_TxtMathTools.ReplaceText(searchText," ","+");
        String DaumEndicUrl = "http://dic.daum.net/search.do?q=" + searchText;

        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setDisplayZoomControls(false);

        wb.loadUrl(DaumEndicUrl);
    }
    private String delete123(String txt){
        int idx = txt.indexOf("1.");
        if(idx!=-1){
            txt = txt.substring(0,idx) + txt.substring(idx+2,txt.length());
        }
        idx = txt.indexOf(" 2.");
        if(idx!=-1){
            txt = txt.substring(0,idx) + "," + txt.substring(idx+3,txt.length());
        }
        idx = txt.indexOf(" 3.");
        if(idx!=-1){
            txt = txt.substring(0,idx) + "," + txt.substring(idx+3,txt.length());
        }
        idx = txt.indexOf(" 4.");
        if(idx!=-1){
            txt = txt.substring(0,idx) + "," + txt.substring(idx+3,txt.length());
        }
        idx = txt.indexOf(" 5.");
        if(idx!=-1){
            txt = txt.substring(0,idx) + "," + txt.substring(idx+3,txt.length());
        }
        return txt;
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
}
