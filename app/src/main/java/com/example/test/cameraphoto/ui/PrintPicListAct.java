package com.example.test.cameraphoto.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.FileUtils;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.ui.base.BaseAct;

import java.io.File;
import java.util.List;

import butterknife.BindView;

public class PrintPicListAct extends BaseAct {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.target)
    protected ListView mListView;
    @BindView(R.id.tip)
    protected TextView mTip;

    @Override
    protected int getLayout() {
        return R.layout.act_print_list;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "打印照片");

        List<String> filePathList = FileUtils.getPicFileName(Constant.PIC_PATH_RESULT);
        if (Tools.isListNullOrEmpty(filePathList)) {
            mTip.setText("没有图片可以打印");
        } else {
            mTip.setVisibility(View.GONE);
            final String[] filesArray = new String[filePathList.size()];
            filePathList.toArray(filesArray);
            //参考ArrayAdapter的构造函数
            mListView.setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1,
                    filesArray));
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    String fileName = filesArray[i];
                    final String realPath = Constant.PIC_PATH_RESULT + fileName;
                    if (new File(realPath).exists()) {
                        AlertDialog dialog = new AlertDialog.Builder(mContext)
                                .setTitle("提示")//设置对话框的标题
                                .setMessage("是否确定打印：" + fileName)//设置对话框的内容
                                //设置对话框的按钮
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        doPhotoPrint(realPath);
                                    }
                                }).create();
                        dialog.show();
                    }
                }
            });
        }
    }


    private void doPhotoPrint(String path) {
        PrintHelper photoPrinter = new PrintHelper(this);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        photoPrinter.printBitmap("droids.jpg - test print", bitmap);
    }
}
