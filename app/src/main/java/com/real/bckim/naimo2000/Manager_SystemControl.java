package com.real.bckim.naimo2000;

import android.app.Activity;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by bckim on 2018-03-26.
 */

public class Manager_SystemControl {
    public static void setStatusBarColor(Activity activity){
        activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity,R.color.colorPrimaryDark));
    }
    public static boolean FileExistCheck(String PathName){
        File files = new File(PathName);
        return files.exists();
    }
    public static void deleteFile(String PathName){
        File file = new File(PathName);
        file.delete();
    }
    public static void deleteAllFile(String FolderPath){
        String mPath = FolderPath;
        File dir = new File(mPath);

        String[] children = dir.list();
        if (children != null) {
            for (int i=0; i<children.length; i++) {
                String filename = children[i];
                File f = new File(mPath + filename);

                if (f.exists()) {f.delete();}
            }
        }
    }
    public static void deleteFolder(String FolderPath){
        File dir = new File(FolderPath);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
        dir.delete();
    }
    public static String getFileName(String PathName){
        String filename=PathName.substring(PathName.lastIndexOf("/")+1);
        return filename;
    }
    public static String getFileNameWithoutExtensionFromFileName(String FileNameWithExtension){
        String FNWE = FileNameWithExtension;
        String FileNameWithoutExtension = FNWE.substring(0, FNWE.lastIndexOf('.'));
        return FileNameWithoutExtension;
    }
    public static String getFileNameWithoutExtensionFromPathName(String PathName){
        String FileName = getFileName(PathName);
        return getFileNameWithoutExtensionFromFileName(FileName).toLowerCase();
    }
    public static String getPathFromPathName(String PathName){
        File file = new File(PathName);
        return file.getParent() + "/";
    }
    public static String getExtension(String PathName){
        String extension = "";
        int i = PathName.lastIndexOf('.');
        if (i > 0) {
            extension = PathName.substring(i+1);
        }
        return extension;
    }
    public static String[] getAllFileNames(String FolderPath){
        //File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(FolderPath);
        int numFile=0;
        for (File f : yourDir.listFiles()) {
            if (f.isFile()){
                numFile+=1;
            }
        }
        String[] allFiles = new String[numFile];
        int i = 0;
        for (File f : yourDir.listFiles()) {
            if (f.isFile()){
                allFiles[i] = f.getName();
                i+=1;
            }
        }
        return allFiles;
    }
    public static String[] getAllFilePathNames(String FolderPath){
        //File sdCardRoot = Environment.getExternalStorageDirectory();
        File yourDir = new File(FolderPath);
        int numFile=0;
        for (File f : yourDir.listFiles()) {
            if (f.isFile()){
                numFile+=1;
            }
        }
        String[] allFiles = new String[numFile];
        int i = 0;
        for (File f : yourDir.listFiles()) {
            if (f.isFile()){
                allFiles[i] = f.getPath();
                i+=1;
            }
        }
        return allFiles;
    }
    public static String[] getAllFilePathNames(String FolderPath,String[] extensions){
        File dir = new File(FolderPath);
        String pathName;
        String ext;
        boolean extMatch;
        ArrayList<String> pathNameList = new ArrayList<>();

        for(int i=0;i<extensions.length;i++){
            extensions[i] = extensions[i].toLowerCase();
        }

        for (File f : dir.listFiles()) {
            if (f.isFile()){
                pathName = f.getPath();
                ext = Manager_SystemControl.getExtension(pathName).toLowerCase();
                extMatch=false;
                for (String extension : extensions) {
                    if (ext.equals(extension)) extMatch = true;
                }
                if(extMatch) {
                    pathNameList.add(pathName);
                }
            }
        }
        return pathNameList.toArray(new String[pathNameList.size()]);
    }
    public static String getDownloadPath(){
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        return file.getAbsolutePath()+"/";
    }
    private static final int BUFFER = 2048;
    public static void makeZipFile(String[] files, String zipFile) {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);

            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));

            byte data[] = new byte[BUFFER];

            for (int i = 0; i < files.length; i++) {
                Log.v("Compress", "Adding: " + files[i]);
                FileInputStream fi = new FileInputStream(files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(files[i].substring(files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void makeFolder(String path){
        File folder = new File(path);
        folder.mkdirs();
    }
    public static void unpackZipToSubFolder(String zipFile){
        File fileZip = new File(zipFile);
        String path = getPathFromPathName(zipFile);
        String fn = getFileName(zipFile);
        String fnwoext = getFileNameWithoutExtensionFromPathName(zipFile);
        String newPath = path + fnwoext + "/";
        String newPathName = path + fnwoext + "/" + fn;
        makeFolder(newPath);
        fileZip.renameTo(new File(newPathName));
        unpackZip(newPathName);
        fileZip = new File(newPathName);
        fileZip.renameTo(new File(zipFile));
    }
    static boolean unpackZip(String zipFilePathName) {
        String path = getPathFromPathName(zipFilePathName);
        String fn = getFileName(zipFilePathName);
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + fn);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public static boolean unpackZip(String path, String zipFileName) {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(path + zipFileName);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(path + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    public static void saveFileFromInputStream(InputStream inputStream, String PathName){
        makeFolder(getPathFromPathName(PathName));

        OutputStream os = null;
        try {
            os = new FileOutputStream(new File(PathName));
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    //CSV control
    public static String[] splitCSV(String lineText){
        List<String> txtList = new ArrayList<>();
        String[] texts;

        String term;
        int strTerm = 0;
        int endTerm;
        String FirstLetter;

        while(strTerm<lineText.length()){
            FirstLetter =lineText.substring(strTerm, strTerm+1);
            if(FirstLetter.equals("\"")) {
                strTerm = strTerm + 1;
                endTerm = lineText.indexOf("\",", strTerm);
                if(endTerm==-1){
                    //제일 마지막 변수라서 ", 로 끝나지 않는 경우 " 로 끝날테니 .length()에서 -1 까지로 설정.
                    endTerm = lineText.length()-1;
                }
                term = lineText.substring(strTerm, endTerm);
                strTerm = endTerm+2;
                term = ChangeDoubleQuotesToSingle(term);
                txtList.add(term);
            }else{
                //strTerm = strTerm;
                endTerm = lineText.indexOf(",", strTerm);
                if(endTerm==-1){
                    //제일 마지막 변수라서 , 로 끝나지 않는 경우 끝까지 설정
                    endTerm = lineText.length();
                }
                term = lineText.substring(strTerm, endTerm);
                strTerm = endTerm+1;
                txtList.add(term);
            }
        }

        int numTerm = txtList.size();
        texts = new String[numTerm];
        for(int i=0;i<numTerm;i++){
            texts[i]=txtList.get(i);
        }
        return texts;
    }
    public static String Convert_CSV_String(String text){
        //CSV파일 출력을 위해서 문자열에
        //쉼표가 있는 경우 따옴표로 둘러쌈.
        //따옴표가 있는 경우 ""로 수정
        boolean hasQuote = false;
        int idxQuota = text.indexOf("\"");
        int txtLength = text.length();
        while(idxQuota!=-1){
            hasQuote = true;
            text = text.substring(0,idxQuota+1) + "\"" + text.substring(idxQuota+1,txtLength);
            txtLength=txtLength+1;
            idxQuota = text.indexOf("\"",idxQuota+2);
        }
        if(text.indexOf(",")!=-1 || hasQuote){
            text ="\"" + text + "\"";
        }
        return text;
    }
    private static String ChangeDoubleQuotesToSingle(String text){
        //todo check for quote include CSV importing
        int idxDoubleQuotes= text.indexOf("\"\"");
        int strText;
        while(idxDoubleQuotes!=-1){
            text = text.substring(0,idxDoubleQuotes) + text.substring(idxDoubleQuotes+1,text.length());
            strText = idxDoubleQuotes + 2;
            idxDoubleQuotes= text.indexOf("\"\"",strText);
        }
        return text;
    }

}