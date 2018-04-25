package com.real.bckim.naimo2000;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by bckim on 18. 3. 21.
 */

public class Manager_PreviewImage {
    private static Bitmap image_bitmap;
    private static String BaseFolderPath;
    static OutputStream outStream = null;

    public Manager_PreviewImage(String DB_Name){
        setDataBaseName(DB_Name);
    }
    public static void setDataBaseName(String DB_Name){
        BaseFolderPath = MainActivity.NaimoDataFolderPath + "previews/."+DB_Name+"/";
        makeImageFolder();
    }
    public static String getDataBaseName(){
        return  BaseFolderPath;
    }
    private static void makeImageFolder(){
        File folder = new File(BaseFolderPath);
        folder.mkdirs();
    }
    public static void deletePreviewImageFolder(){
        Manager_FileAndFolderControl.deleteFolder(BaseFolderPath);
    }
    public static String getPreviewImageFileName(int ID){
        return "prv" + ID + ".png";
    }
    public static Bitmap getPreviewImage(int ID){
        String FullPathName_PreviewImage = BaseFolderPath + getPreviewImageFileName(ID);
        if(new File(FullPathName_PreviewImage).exists()){
            return BitmapFactory.decodeFile(FullPathName_PreviewImage);
        }else{
            return null;
        }
    }
    public static boolean setPreviewImage(String ImageFileFullPathName, int ID){
        File inpFile = new File(ImageFileFullPathName);
        if(!inpFile.exists()) return false;

        //ref. http://mainia.tistory.com/1521
        File outFile = new File(BaseFolderPath, getPreviewImageFileName(ID));
        image_bitmap = BitmapFactory.decodeFile(ImageFileFullPathName);
        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        //image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        //Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        //Log.e("Original   dimensions", image_bitmap.getWidth()+" "+image_bitmap.getHeight());
        //Log.e("Compressed dimensions", decoded.getWidth()+" "+decoded.getHeight());

        try {
            outStream = new FileOutputStream(outFile);
            //image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            image_bitmap=resizeBitmap(image_bitmap);
            image_bitmap.compress(Bitmap.CompressFormat.PNG,100,outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public static boolean setPreviewImage(Bitmap imageBitmap, int ID){
        File outFile = new File(BaseFolderPath, getPreviewImageFileName(ID));
        image_bitmap = imageBitmap;
        //ByteArrayOutputStream out = new ByteArrayOutputStream();
        //image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        //Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
        //Log.e("Original   dimensions", image_bitmap.getWidth()+" "+image_bitmap.getHeight());
        //Log.e("Compressed dimensions", decoded.getWidth()+" "+decoded.getHeight());

        try {
            outStream = new FileOutputStream(outFile);
            //image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            image_bitmap=resizeBitmap(image_bitmap);
            image_bitmap.compress(Bitmap.CompressFormat.PNG,100,outStream);
            outStream.flush();
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }
    public static void deletePreviewImage(int ID){
        //ref. http://mainia.tistory.com/1521
        File file = new File(BaseFolderPath, getPreviewImageFileName(ID));
        if (file.exists()) {file.delete();}
    }
    public static boolean exists(int ID) {
        String PathName = BaseFolderPath + getPreviewImageFileName(ID);
        File file = new File(PathName);
        return file.exists();
    }
    public static void savePreviewImage(int ID, String Folder, String FileName){
        //이미 보관된 PreviewImage를 지정 폴더, 지정 이름으로 저장
        image_bitmap=getPreviewImage(ID);
        if(image_bitmap!=null){
            try {
                File bitFile = new File(Folder,FileName);
                outStream = new FileOutputStream(bitFile);
                image_bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream); // bmp is your Bitmap instance
                outStream.flush();
                outStream.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                e.printStackTrace();
                //Toast.makeText(ImageSdcardSaveActivity.this, e.toString(), Toast.LENGTH_LONG).show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static Bitmap drawable2Bitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public static Bitmap getOverlayBitmap(Context context) {
        /**
         * Drawable을 Bitmap으로 가져오기
         * ref. http://dwfox.tistory.com/37
         */
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.plus);
    }
    static public Bitmap resizeBitmap(Bitmap original) {
        /**
         * Bitmap 이미지 크기 조절
         * 가로길이를 200으로 맞춰 이미지 크기를 조절해주기
         * 출처: http://dwfox.tistorycom/37 [DWFOX]
         */

        int targetSize = 300; //가로 혹은 세로 최대폭을 targetSize에 맞춤.


        int targetWidth = original.getWidth();
        int targetHeight = original.getHeight();
        int maxSize;
        if(targetWidth>targetHeight){
            maxSize = targetWidth;
        }else{
            maxSize = targetHeight;
        }

        float aspectRatio;
        if(maxSize<targetSize){
            aspectRatio = 1.0f;
        }else{
            aspectRatio = (float) targetSize / (float) maxSize;
        }

        targetWidth = (int) ((float) targetWidth * aspectRatio);
        targetHeight = (int) ((float) targetHeight * aspectRatio);

        Bitmap result = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }
    static public Bitmap cropBitmap(Bitmap original) {
        /**
         * 비트맵이미지 잘라내기
         * 출처: http://dwfox.tistory.com/37 [DWFOX]
         */
        Bitmap result = Bitmap.createBitmap(original
                , original.getWidth() / 4 //X 시작위치 (원본의 4/1지점)
                , original.getHeight() / 4 //Y 시작위치 (원본의 4/1지점)
                , original.getWidth() / 2 // 넓이 (원본의 절반 크기)
                , original.getHeight() / 2); // 높이 (원본의 절반 크기)
        if (result != original) {
            original.recycle();
        }
        return result;
    }
    static public Bitmap overlayBitmap(Bitmap original, Context context) {
        /**
         * Bitmap 이미지 사이즈 줄이고 잘라서 겹치기(정사각형)
         * 출처: http://dwfox.tistory.com/37 [DWFOX]
         */
        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();

        int MAX_LENGTH = 120;

        int targetWidth, targetHeight;
        int startW = 0;
        int startH = 0;
        Bitmap originalResizeBmp;

        if (aspectRatio >= 1) { //세로가 긴 경우
            targetWidth = MAX_LENGTH;
            targetHeight = (int) (targetWidth * aspectRatio);
            startH = (targetHeight - targetWidth) / 2;

        } else { //가로가 긴 경우
            targetHeight = MAX_LENGTH;
            targetWidth = (int) (targetHeight / aspectRatio);
            startW = (targetWidth - targetHeight) / 2;
        }

        //하단 비트맵
        originalResizeBmp = Bitmap.createScaledBitmap(original, targetWidth, targetHeight, false);
        originalResizeBmp = originalResizeBmp.createBitmap(originalResizeBmp, startW, startH
                , (targetWidth > targetHeight ? targetHeight : targetWidth)
                , (targetWidth > targetHeight ? targetHeight : targetWidth));

        //상단 비트맵
        Bitmap overlayBmp = Bitmap.createScaledBitmap(getOverlayBitmap(context)
                , (targetWidth > targetHeight ? targetHeight : targetWidth)
                , (targetWidth > targetHeight ? targetHeight : targetWidth)
                , false);

        //결과값 저장을 위한 Bitmap
        Bitmap resultOverlayBmp = Bitmap.createBitmap(originalResizeBmp.getWidth()
                , originalResizeBmp.getHeight()
                , originalResizeBmp.getConfig());


        //상단 비트맵에 알파값을 적용하기 위한 Paint
        Paint alphaPaint = new Paint();
        alphaPaint.setAlpha(125);

        //캔버스를 통해 비트맵을 겹치기한다.
        Canvas canvas = new Canvas(resultOverlayBmp);
        canvas.drawBitmap(originalResizeBmp, new Matrix(), null);
        canvas.drawBitmap(overlayBmp, new Matrix(), alphaPaint);

        if (originalResizeBmp != original) {
            original.recycle();
        }
        if (originalResizeBmp != resultOverlayBmp) {
            originalResizeBmp.recycle();
        }
        if (overlayBmp != resultOverlayBmp) {
            overlayBmp.recycle();
        }

        return resultOverlayBmp;
    }
}
