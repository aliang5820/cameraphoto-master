package com.example.test.cameraphoto;

import com.example.test.cameraphoto.mtp.PicInfo;

import java.util.Comparator;

/**
 * Created by Edison on 2018/11/8.
 */
public class FileComparator implements Comparator<PicInfo> {

    public int compare(PicInfo file1, PicInfo file2) {
        return Long.compare(file2.getLastModified(), file1.getLastModified());
    }
}