package com.example.test.cameraphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * Created by apple on 2018/7/13.
 */

public class FileUtils {

    @NonNull
    public static String importFile(Context context, int handler) {
        StringBuilder filePath = new StringBuilder();
        filePath.append(context.getCacheDir())
                .append(File.separator)
                .append(handler)
                .append(".jpg");
        if(Constant.mtpDevice!=null){
            Constant.mtpDevice.importFile(handler,filePath.toString());
            return filePath.toString();
        }
        return "";
    }

    /**
     * 把字节数组保存为一个文件
     *
     * @param
     */
    public static File bytes2File(byte[] b, String outputFile) {
        BufferedOutputStream stream = null;
        File file = null;
        try {
            file = new File(outputFile);
            if (!file.getParentFile().exists()) {
                boolean mkdirs = file.getParentFile().mkdirs();
            }
            boolean newFile = file.createNewFile();
            FileOutputStream fstream = new FileOutputStream(file);
            stream = new BufferedOutputStream(fstream);
            stream.write(b);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return file;
    }

    //遍历指定文件夹下的所有照片
    public static List<String> getPicFileName(String fileAbsolutePath) {
        Vector<String> vecFile = new Vector<>();
        File file = new File(fileAbsolutePath);
        File[] subFile = file.listFiles();

        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                // 判断是否为
                if (filename.trim().toLowerCase().endsWith(".jpg") || filename.trim().toLowerCase().endsWith(".jpeg")
                        || filename.trim().toLowerCase().endsWith(".png")) {
                    vecFile.add(filename);
                }
            }
        }
        return vecFile;
    }

    /**
     * @param bitmap
     * @param destPath
     * @param quality
     */
    public static void writeImage(Bitmap bitmap, String destPath, int quality) throws Exception {
        FileOutputStream out = null;
        try {
            deleteFile(destPath);
            if (createFile(destPath)) {
                out = new FileOutputStream(destPath);
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)) {
                    out.flush();
                    out.close();
                    out = null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            if (out != null) {
                out.flush();
                out.close();
                out = null;
            }
        }
    }

    public static boolean createFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }

                return file.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 删除一个文件
     *
     * @param filePath 要删除的文件路径名
     * @return true if this file was deleted, false otherwise
     */
    public static boolean deleteFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
