package com.example.test.cameraphoto;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return bitmap3;
    }

    private void releaseImageViewResource(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    /**
     * Bitmap对象是否为空。
     */
    public boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
