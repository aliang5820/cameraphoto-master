package com.example.test.cameraphoto;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.Image;
import android.support.annotation.Dimension;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Edison on 2018/10/19.
 */
public class BitmapUtil {

    public Bitmap newBitmap(Bitmap frameBitmap, Bitmap sourceBitmap) {
        Bitmap bitmap3 = Bitmap.createBitmap(frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        if (frameBitmap.getWidth() > frameBitmap.getHeight()) {
            //横向
            //计算边框间距
            int frameSpace = (int) (frameBitmap.getHeight() * 0.815 / 10);
            int newHeight = frameBitmap.getHeight() - frameSpace * 2;
            int newWidth = newHeight * 3 / 2;
            Bitmap newSourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);

            canvas.drawBitmap(frameBitmap, new Matrix(), null);
            canvas.drawBitmap(newSourceBitmap, frameSpace, frameSpace, null); //20、20为bitmap2写入点的x、y坐标
        } else {
            //纵向
            //计算边框间距
            int frameSpace = (int) (frameBitmap.getWidth() * 0.75 / 13);
            int newWidth = frameBitmap.getWidth() - frameSpace * 2;
            int newHeight = newWidth * 2 / 3;
            Bitmap newSourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);

            canvas.drawBitmap(frameBitmap, new Matrix(), null);
            canvas.drawBitmap(newSourceBitmap, frameSpace, frameSpace, null);
        }
        canvas.save();
        canvas.restore();
        return bitmap3;
    }

    /**
     * 旋转图片，使图片保持正确的方向。
     *
     * @param bitmap  原始图片
     * @param degrees 原始图片的角度
     * @return Bitmap 旋转后的图片
     */
    public Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        if (degrees == 0 || null == bitmap) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(degrees, bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bmp;
    }


    /**
     * 读取图片属性：旋转的角度
     *
     * @param path 拍摄图片的完整路径
     * @return degree旋转的角度
     */
    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return degree;
        }
        return degree;
    }
}
