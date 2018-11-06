package com.example.test.cameraphoto;

import android.mtp.MtpDevice;
import android.os.Environment;

/**
 * Created by apple on 2018/4/3.
 */

public class Constant {

    public static MtpDevice mtpDevice;
    public static String usbDeviceName = "";
    public static boolean isUsbConnected = false;
    public static String EXTRA_KEY = "extra_key";
    public static String EXTRA_SOURCE = "extra_source";

    public static String PIC_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ACP/";
    public static String PIC_PATH_FRAME = PIC_PATH + "/frame/";
    public static String PIC_PATH_RESULT = PIC_PATH + "/result/";
    public static String PIC_PATH_CRASH = PIC_PATH + "/crash/";
}
