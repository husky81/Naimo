package com.real.bckim.naimo2000;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter_Word extends BaseAdapter {
    Context context;
    private List<Content_Word> contents;
    //String wordList[];
    static boolean checkBoxState[];
    private LayoutInflater inflter;
    private boolean checkBoxVisible;
    private View thisListView;

    private void setCheckBoxState(boolean[] chkBoxState){
        checkBoxState = chkBoxState;
    }

    CustomAdapter_Word(Context applicationContext, List<Content_Word> contents) {
        this.context=applicationContext;
        this.contents=contents;
        boolean[] tmpCheckBoxState = new boolean[contents.size()];
        setCheckBoxState(tmpCheckBoxState);

        inflter = (LayoutInflater.from(applicationContext));
        thisListView = inflter.inflate(R.layout.listview_word, null);
    }
    public void setContents(List<Content_Word> contents) {
        this.contents=contents;
        boolean[] tmpCheckBoxState = new boolean[contents.size()];
        setCheckBoxState(tmpCheckBoxState);
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.listview_word, null);
        ImageView icon = view.findViewById(R.id.icon_word);
        TextView word = view.findViewById(R.id.wordName);
        TextView text = view.findViewById(R.id.memoText_word);
        CheckBox chkBox = view.findViewById(R.id.checkBox_word);
        TextView txtO = view.findViewById(R.id.txtView_numO);
        TextView txtX = view.findViewById(R.id.txtView_numX);
        TextView txtFami = view.findViewById(R.id.txtView_familiarity);

        Content_Word cw = contents.get(i);

        int numQuizItem = 0;

        if(cw.getHasImage()){
            Bitmap bm = Manager_PreviewImage.getPreviewImage(cw.getID());
            icon.setVisibility(View.VISIBLE);
            icon.setImageBitmap(bm);
            numQuizItem+=1;
        }else{
            icon.setVisibility(View.GONE);
            icon.setImageResource(WordListActivity.EmptyImageIconResource);
        }

        String text1 = cw.getText1();
        String text2 = cw.getText2();
        String text4 = cw.getText4();

        if(text1!=null && text1.length()>0) {
            numQuizItem+=1;
            if(text4!=null&&text4.length()>0) {
                //pnon.setText("[" + text4 + "]");
                SpannableString s1 = new SpannableString(text1);
                SpannableString s2 = new SpannableString(" [" + text4 + "]");
                int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                s1.setSpan(new StyleSpan(Typeface.BOLD), 0, s1.length(), flag);
                s2.setSpan(new StyleSpan(Typeface.NORMAL), 0, s2.length(), flag);
                s2.setSpan(new ForegroundColorSpan(Color.GRAY), 0, s2.length(), flag);
                s2.setSpan(new RelativeSizeSpan(0.8f), 0, s2.length(), flag);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(s1);
                builder.append(s2);
                word.setText(builder);
                //ref. https://stackoverflow.com/questions/1529068/is-it-possible-to-have-multiple-styles-inside-a-textview
                // enables clicking on spans for clickable span and url span
                //textView.setMovementMethod(LinkMovementMethod.getInstance());
            }else{
                //word.setText(text1);
                SpannableString s1 = new SpannableString(text1);
                int flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
                s1.setSpan(new StyleSpan(Typeface.BOLD), 0, s1.length(), flag);
                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(s1);
                word.setText(builder);
            }
        }else{
            word.setText("");
        }
//            word.setText(Html.fromHtml("<b>" + "title" + "</b>" +  "<br />" +
//                    "<small>" + "description" + "</small>" + "<br />" +
//                    "<small>" + "DateAdded" + "</small>"));

        if(text2!=null && text2.length()>0) {
            text.setText(text2);
            numQuizItem+=1;
        }else{
            text.setText("");
        }

        if(numQuizItem<2){
            word.setTextColor(context.getResources().getColor(R.color.listTextGrey));
        }else{
            word.setTextColor(context.getResources().getColor(R.color.listTextBlack));
        }

        txtO.setText(String.valueOf(cw.getNumCorrect()));
        txtX.setText(String.valueOf(cw.getNumWrong()));
        String strFamiliarity =context.getString(R.string.Familiarity) + ":" + cw.getFamiliarity();
        txtFami.setText(strFamiliarity);

        if(checkBoxVisible){
            chkBox.setVisibility(View.VISIBLE);
            chkBox.setWidth(120);
            chkBox.setChecked(checkBoxState[i]);
            final int checkBoxPosition = i;
            chkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBoxState[checkBoxPosition]=!checkBoxState[checkBoxPosition];
                }
            });
        }else{
            //chkBox.setVisibility(View.INVISIBLE);
            chkBox.setVisibility(View.GONE);
            chkBox.setWidth(0);
        }
        return view;
    }
    @Override
    public int getCount() {
        return contents.size();
    }
    @Override
    public Object getItem(int i) {
        return null;
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
    public void setCheckBoxVisibility(boolean visible){
        checkBoxVisible = visible;
    }
}
