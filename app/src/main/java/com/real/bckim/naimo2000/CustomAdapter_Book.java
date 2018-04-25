package com.real.bckim.naimo2000;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CustomAdapter_Book extends BaseAdapter {
    Context context;
    List<Content_Book> contents;
    LayoutInflater inflter;

    public CustomAdapter_Book(Context applicationContext, List<Content_Book> contents) {
        this.context = applicationContext;
        this.contents = contents;
        inflter = (LayoutInflater.from(applicationContext));
    }
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflter.inflate(R.layout.listview_book, null);
        ImageView icon = view.findViewById(R.id.icon);
        TextView book = view.findViewById(R.id.bookName);
        TextView numItem = view.findViewById(R.id.numItem);
        TextView memo = view.findViewById(R.id.memoText);
        TextView size = view.findViewById(R.id.booktext_arraySize);

        Content_Book cb = contents.get(i);

        //Image setting
        Manager_PreviewImage.setDataBaseName(MainActivity.Book_DB_Name);
        if(Manager_PreviewImage.exists(cb.getID())) {
            icon.setImageBitmap(Manager_PreviewImage.getPreviewImage(cb.getID()));
        }else{
            icon.setImageResource(MainActivity.NotePadIconResource);
        }

        book.setText(cb.getText1());
        memo.setText(cb.getText2());
        String txtSize =cb.getQuizSizeRow() + "x" + cb.getQuizSizeCol();
        size.setText(txtSize);
        String txtNum =context.getString(R.string.count) + ": " + String.valueOf(cb.getNumItem());
        numItem.setText(txtNum);
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
}
