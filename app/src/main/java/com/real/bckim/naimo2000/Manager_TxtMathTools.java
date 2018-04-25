package com.real.bckim.naimo2000;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Manager_TxtMathTools {
    public static String getPathNameFromURI(Context context, Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        if(cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }
    public static String TrimLastBytes(String text,int numTrimBytes){
        int byteLength = getByteLength(text);
        if(byteLength<numTrimBytes) return "";

        String trimmingText ="";
        int trimmedLength = 0;
        int trimmedByteLength = 0;
        while(trimmedByteLength<numTrimBytes){
            trimmedLength = trimmedLength + 1;
            trimmingText = text.substring(text.length()-trimmedLength,text.length()-trimmedLength + 1);
            trimmedByteLength = trimmedByteLength +  getByteLength(trimmingText);
        }
        String trimmedText = text.substring(0,text.length() - trimmedLength);
        return trimmedText;
    }
    public static int getByteLength(String txt){
        int byteLength=0;
        for(int i=0;i<txt.length();i++){
            String tmpChar = txt.substring(i,i+1);
            int tmpByteLength = tmpChar.getBytes().length;
            if(tmpByteLength==3) tmpByteLength=2;
            byteLength=byteLength+tmpByteLength;
        }
        return byteLength;
    }
    public static String WrapedText(String text, String preText,String postText){
        if(text=="") return "";

        int strIdx;
        int endIdx;

        if(preText==""){
            strIdx=0;
        }else{
            strIdx = text.indexOf(preText);
        }

        if(postText==""){
            endIdx = text.length();
        }else{
            endIdx = text.indexOf(postText, strIdx);
        }
        if(strIdx==-1||endIdx==-1) return "";
        String subStr = text.substring(strIdx + preText.length(), endIdx);
        return subStr;
    }
    public static String ReplaceText(String txt,String findTxt,String replaceTxt){
        int idxSpace = txt.indexOf(findTxt);
        int len_findTxt = findTxt.length();
        int len_replaceTxt = replaceTxt.length();
        while(idxSpace!=-1){
            txt = txt.substring(0,idxSpace) + replaceTxt + txt.substring(idxSpace+ len_findTxt,txt.length());
            idxSpace = txt.indexOf(findTxt,idxSpace+len_replaceTxt);
        }
        return txt;
    }
    public static int safeLongToInt(long i) {
        if (i < Integer.MIN_VALUE){
            i = Integer.MIN_VALUE + 10000;
        } else if(i > Integer.MAX_VALUE) {
            i = Integer.MAX_VALUE - 10000;
        }
        return (int) i;
    }
    public static int RoundUp_int(float value){
        int ans = (int) (value +0.999999f);
        return ans;
    }
    public static int Round_int(float value){
        int ans = (int) (value +0.5f);
        return ans;
    }
    public static int RoundDown_int(float value){
        int ans = (int) (value);
        return ans;
    }
    public static String formatDateTime(Context context, String timeToFormat) {
        //Best way to work with dates in Android SQLite
        //https://stackoverflow.com/questions/7363112/best-way-to-work-with-dates-in-android-sqlite
        String finalDateTime = "";

        SimpleDateFormat iso8601Format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date date = null;
        if (timeToFormat != null) {
            try {
                date = iso8601Format.parse(timeToFormat);
            } catch (ParseException e) {
                date = null;
            }

            if (date != null) {
                long when = date.getTime();
                int flags = 0;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_TIME;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_DATE;
                flags |= android.text.format.DateUtils.FORMAT_ABBREV_MONTH;
                flags |= android.text.format.DateUtils.FORMAT_SHOW_YEAR;

                finalDateTime = android.text.format.DateUtils.formatDateTime(context,
                        when + TimeZone.getDefault().getOffset(when), flags);
            }
        }
        return finalDateTime;
    }
    public static String[] DivideStringMultiline(String text,int nMaxByte,int nMaxLine){
        MultiLineStringMaker wp = new MultiLineStringMaker(text);
        String[] RegEx = {" ", ",", ";", ":","|"};
        wp.setDivider(RegEx);
        String[] mTexts = wp.getMultiLineText(nMaxByte,nMaxLine);

//        int bytLengh = text.getBytes().length;
//        int numLine = Manager_TxtMathTools.RoundUp_int((float) bytLengh / (float) nMaxByte);
//
//        String[] mTexts = new String[numLine];
//        int[] mTextLength = new int[numLine];
//
//        //int remTexts = lenText;
//        for(int i=0;i<numLine;i++){
//            int oneLineLength = text.length()/numLine+1;
//            oneLineLength+=1;
//            mTextLength[i]=oneLineLength;
//        }
//        int strIdx = 0;
//        int endIdx;
//        for(int i=0;i<numLine;i++){
//            endIdx = strIdx+mTextLength[i];
//            if(endIdx>text.length()) endIdx = text.length();
//            mTexts[i]=text.substring(strIdx,endIdx);
//            strIdx += mTextLength[i];
//        }

        //        int strIdx=0;
//        int endIdx= nMaxByte;
//        String[] mTexts = new String[5];
//
//        while(strIdx<lenText && numLine<numMaxLineOfBtn){
//            numLine+=1;
//            if(endIdx>lenText) endIdx = lenText;
//            String lineText = text.substring(strIdx,endIdx);
//            mTexts[numLine-1]=lineText;
//            strIdx+=lineText.length();
//            endIdx=strIdx+ nMaxByte;
//        }
//        if(strIdx<lenText){
//            String tmpTxt = mTexts[numLine-1];
//            mTexts[numLine-1] = tmpTxt.substring(0,tmpTxt.length()) + "..";
//        }

        return mTexts;
    }
}

class MultiLineStringMaker {
    String text;
    private int textLength;
    private int textByteLength;
    int MaxByteOfOneLine;

    String[] divider;

    private int numWords;
    private int[] charByteLengths;
    private boolean[] isDividers;
    private int[] wordIndexsCorrespondByteIndexs;

    private String[] words;
    private int[] wordLengths;
    private int[] wordStartIndexs;
    private int[] wordEndIndexs;
    private int[] wordByteLengths;
    private int[] wordStartByteIndexs;
    private int[] wordEndByteIndexs;


    String partText;
    String preDivider;
    String postDivider;

    public String[] getMultiLineText(int nMaxByte, int nMaxLine){
        //int numLine = Manager_TxtMathTools.RoundUp_int((float) textByteLength / (float) nMaxByte);
        String[] mText = new String[50];
        boolean textTrimed = false;

        int row=0;
        int strByteIdx = 0;
        int strIdx;
        int endByteIdx;
        int endIdx;
        while(strByteIdx < textByteLength){
                endByteIdx = strByteIdx + nMaxByte;
            strIdx = wordIndexsCorrespondByteIndexs[strByteIdx];

            if(endByteIdx > textByteLength-1){
                endIdx = textLength;
            }else{
                endIdx = wordIndexsCorrespondByteIndexs[endByteIdx];
            }

            mText[row]=text.substring(strIdx,endIdx);

            strByteIdx = strByteIdx + Manager_TxtMathTools.getByteLength(mText[row]);
            row+=1;
        }
        int numLine = row;

        if(numLine>nMaxLine){
            numLine=nMaxLine;
            textTrimed=true;
        }

        String[] rltText = new String[numLine];
        for(int i=0;i<numLine;i++){
            rltText[i]=mText[i];
        }

        if(textTrimed){
            rltText[numLine-1] = Manager_TxtMathTools.TrimLastBytes(rltText[numLine-1],2) + "..";
        }

        return rltText;
    }


    MultiLineStringMaker(String text){
        this.text = text;
        this.textLength=text.length();
        this.textByteLength= Manager_TxtMathTools.getByteLength(text);
    }
    private boolean isDivider(String text){
        int numRegEx = divider.length;
        boolean ans = false;
        for(int i=0;i<numRegEx;i++){
            if(text.equals(divider[i])) ans = true;
        }
        return ans;
    }
    public void setDivider(String[] divider) {
        this.divider = divider;
        String tmpChar;

        numWords = 0;
        boolean isDividerMarker = true;
        charByteLengths = new int[textLength];
        isDividers = new boolean[textLength];
        for(int i=0;i<text.length();i++){
            tmpChar = text.substring(i,i+1);
            charByteLengths[i]= Manager_TxtMathTools.getByteLength(tmpChar);
            isDividers[i]=isDivider(tmpChar);
            if(!isDividers[i]){
                if(isDividerMarker){
                    numWords+=1;
                }
            }
            isDividerMarker=isDividers[i];
        }

        int idWord=0;
        words = new String[numWords];
        words[idWord]="";
        wordStartIndexs = new int[numWords];
        wordEndIndexs = new int[numWords];
        for(int i=0;i<text.length();i++){
            tmpChar = text.substring(i,i+1);
            if(!isDividers[i]){
                if(isDividerMarker){
                    idWord+=1;
                    if(idWord==numWords) idWord = numWords - 1;
                    words[idWord]="";
                    wordStartIndexs[idWord]=i;
                }
                words[idWord]=words[idWord]+tmpChar;
            }else{
                if(!isDividerMarker){
                    wordEndIndexs[idWord]=i;
                }
            }
            isDividerMarker=isDividers[i];
        }
        wordEndIndexs[numWords-1]=wordStartIndexs[numWords-1]+words[numWords-1].length();

        wordStartByteIndexs = new int[numWords];
        wordEndByteIndexs = new int[numWords];
        for(int i=0;i<numWords;i++){
            wordStartByteIndexs[i]= Manager_TxtMathTools.getByteLength(text.substring(0,wordStartIndexs[i]));
            wordEndByteIndexs[i]= Manager_TxtMathTools.getByteLength(text.substring(0,wordEndIndexs[i]));
        }

        wordLengths = new int[numWords];
        wordByteLengths = new int[numWords];
        for(int i=0;i<numWords;i++){
            wordLengths[i]=words[i].length();
            wordByteLengths[i] = Manager_TxtMathTools.getByteLength(words[i]);
        }

        wordIndexsCorrespondByteIndexs = new int[textByteLength];
        int n=0;
        for(int i=0;i<textLength;i++){
            tmpChar = text.substring(i,i+1);
            for(int j = 0; j< Manager_TxtMathTools.getByteLength(tmpChar); j++){
                wordIndexsCorrespondByteIndexs[n]=i+j;
                n+=1;
            }
        }
    }
}