package com.example.test.cameraphoto;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by Edison on 2018/10/19.
 */
public class BitmapUtil {
    private static final String TAG = "BitmapUtil";

    public static Bitmap newBitmap(Bitmap frameBitmap, Bitmap sourceBitmap) {
        /*Bitmap retBmp;
        int width = frameBitmap.getWidth();
        if (sourceBitmap.getWidth() != width) {
            //以第一张图片的宽度为标准，对第二张图片进行缩放。
            int h2 = sourceBitmap.getHeight() * width / sourceBitmap.getWidth();
            retBmp = Bitmap.createBitmap(width, frameBitmap.getHeight() + h2, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(retBmp);
            Bitmap newSizeBmp2 = resizeBitmap(sourceBitmap, width, h2);
            canvas.drawBitmap(frameBitmap, 0, 0, null);
            canvas.drawBitmap(newSizeBmp2, 0, frameBitmap.getHeight(), null);
        } else {
            //两张图片宽度相等，则直接拼接。
            retBmp = Bitmap.createBitmap(width, frameBitmap.getHeight() + sourceBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(retBmp);
            canvas.drawBitmap(sourceBitmap, 0, 0, null);
            canvas.drawBitmap(frameBitmap, 0, sourceBitmap.getHeight(), null);
            *//*canvas.drawBitmap(frameBitmap, 0, 0, null);
            canvas.drawBitmap(sourceBitmap, 0, frameBitmap.getHeight(), null);*//*
        }
        return retBmp;*/

        Drawable[] array = new Drawable[2];
        array[0] = new BitmapDrawable(frameBitmap);
        LayerDrawable la;
        if(frameBitmap.getWidth() > frameBitmap.getHeight()) {
            float scale = (float) frameBitmap.getHeight() / sourceBitmap.getHeight();
            Bitmap newSizeBmp2 = resizeBitmap(sourceBitmap, scale);
            array[1] = new BitmapDrawable(newSizeBmp2);
            la = new LayerDrawable(array);
            // 其中第一个参数为层的索引号，后面的四个参数分别为left、top、right和bottom
            la.setLayerInset(0, 0, 0, 0, 0);
            la.setLayerInset(1, 20, 20, 100, 20);
        } else {
            //纵向
            float scale = (float) frameBitmap.getWidth() / sourceBitmap.getWidth();
            Bitmap newSizeBmp2 = resizeBitmap(sourceBitmap, scale);
            array[1] = new BitmapDrawable(newSizeBmp2);
            la = new LayerDrawable(array);
            // 其中第一个参数为层的索引号，后面的四个参数分别为left、top、right和bottom
            la.setLayerInset(0, 0, 0, 0, 0);
            la.setLayerInset(1, 20, 20, 20, 400);
        }
        return drawableToBitmap(la.mutate());
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        Log.e(TAG, "Drawable转Bitmap");
        Bitmap.Config config =
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        //注意，下面三行代码要用到，否则在View或者SurfaceView里的canvas.drawBitmap会看不到图
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * 保存图片到文件File。
     *
     * @param src     源图片
     * @param file    要保存到的文件
     * @param format  格式
     * @param recycle 是否回收
     * @return true 成功 false 失败
     */
    public static boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
        if (isEmptyBitmap(src))
            return false;

        OutputStream os;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            ret = src.compress(format, 100, os);
            if (recycle && !src.isRecycled())
                src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * Bitmap对象是否为空。
     */
    public static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
