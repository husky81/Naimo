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
    static class ViewHolder {
            ImageView icon;
            TextView word;
            TextView text;
            CheckBox chkBox;
            TextView txtO;
            TextView txtX;
            TextView txtFami;
            int position;
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if(view==null){
            view = inflter.inflate(R.layout.listview_word, null);
            holder = new ViewHolder();
            view.setTag(holder);
            holder.icon = view.findViewById(R.id.icon_word);
            holder.word = view.findViewById(R.id.wordName);
            holder.text = view.findViewById(R.id.memoText_word);
            holder.chkBox = view.findViewById(R.id.checkBox_word);
            holder.txtO = view.findViewById(R.id.txtView_numO);
            holder.txtX = view.findViewById(R.id.txtView_numX);
            holder.txtFami = view.findViewById(R.id.txtView_familiarity);
        }else{
            holder = (ViewHolder) view.getTag();
        }

        Content_Word cw = contents.get(i);

        int numQuizItem = 0;

        if(cw.getHasImage()){
            Bitmap bm = Manager_PreviewImage.getPreviewImage(cw.getID());
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.setImageBitmap(bm);
            numQuizItem+=1;
        }else{
            holder.icon.setVisibility(View.GONE);
            holder.icon.setImageResource(WordListActivity.EmptyImageIconResource);
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
                holder.word.setText(builder);
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
                holder.word.setText(builder);
            }
        }else{
            holder.word.setText("");
        }
//            word.setText(Html.fromHtml("<b>" + "title" + "</b>" +  "<br />" +
//                    "<small>" + "description" + "</small>" + "<br />" +
//                    "<small>" + "DateAdded" + "</small>"));

        if(text2!=null && text2.length()>0) {
            holder.text.setText(text2);
            numQuizItem+=1;
        }else{
            holder.text.setText("");
        }

        if(numQuizItem<2){
            holder.word.setTextColor(context.getResources().getColor(R.color.listTextGrey));
        }else{
            holder.word.setTextColor(context.getResources().getColor(R.color.listTextBlack));
        }

        holder.txtO.setText(String.valueOf(cw.getNumCorrect()));
        holder.txtX.setText(String.valueOf(cw.getNumWrong()));
        String strFamiliarity =context.getString(R.string.Familiarity) + ":" + cw.getFamiliarity();
        holder.txtFami.setText(strFamiliarity);

        if(checkBoxVisible){
            holder.chkBox.setVisibility(View.VISIBLE);
            holder.chkBox.setWidth(120);
            holder.chkBox.setChecked(checkBoxState[i]);
            final int checkBoxPosition = i;
            holder.chkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBoxState[checkBoxPosition]=!checkBoxState[checkBoxPosition];
                }
            });
        }else{
            //chkBox.setVisibility(View.INVISIBLE);
            holder.chkBox.setVisibility(View.GONE);
            holder.chkBox.setWidth(0);
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
