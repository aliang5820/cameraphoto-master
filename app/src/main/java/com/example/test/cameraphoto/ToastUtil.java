package com.example.test.cameraphoto;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Edison on 2018/9/18.
 */
public class ToastUtil {
    private Context context;

    public ToastUtil(Context context) {
        this.context = context;
    }

    public void showMsg(String msg) {
        Toast toast = new Toast(context);
        toast.setText(msg);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.show();
    }
}
