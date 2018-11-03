package com.example.test.cameraphoto;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

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
    private Context mContext;

    public BitmapUtil(Context context) {
        this.mContext = context;
    }

    public Bitmap newBitmap(Bitmap frameBitmap, Bitmap sourceBitmap) {
        Bitmap bitmap3 = Bitmap.createBitmap(frameBitmap.getWidth(), frameBitmap.getHeight(), frameBitmap.getConfig());
        Canvas canvas = new Canvas(bitmap3);
        if (frameBitmap.getWidth() > frameBitmap.getHeight()) {
            //横向
            int newHeight = frameBitmap.getHeight() - 60;
            int newWidth = newHeight * 3 / 2;
            Bitmap newSourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);

            canvas.drawBitmap(frameBitmap, new Matrix(), null);
            canvas.drawBitmap(newSourceBitmap, 30, 30, null); //20、20为bitmap2写入点的x、y坐标
        } else {
            //纵向
            int newWidth = frameBitmap.getWidth() - 60;
            int newHeight = newWidth * 2 / 3;
            Bitmap newSourceBitmap = Bitmap.createScaledBitmap(sourceBitmap, newWidth, newHeight, true);

            canvas.drawBitmap(frameBitmap, new Matrix(), null);
            canvas.drawBitmap(newSourceBitmap, 30, 30, null);
        }
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap3;
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
    public boolean save(Bitmap src, File file, Bitmap.CompressFormat format, boolean recycle) {
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
    public boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
