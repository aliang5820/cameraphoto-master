package com.example.test.cameraphoto.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.FileUtils;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.mtp.PicInfo;
import com.example.test.cameraphoto.ui.base.BaseAct;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class PrintPicListAct extends BaseAct {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.list)
    protected RecyclerView mListView;
    @BindView(R.id.tip)
    protected TextView mTip;
    private PicAdapter adapter;
    private Disposable disposable;

    @Override
    protected int getLayout() {
        return R.layout.act_print_list;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "打印照片");
        disposable = Flowable.just("")
                .map(new Function<String, List<PicInfo>>() {
                    @Override
                    public List<PicInfo> apply(String s) throws Exception {
                        try {
                            return FileUtils.getPrintPicInfoList(Constant.PIC_PATH_RESULT);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return new ArrayList<>();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<PicInfo>>() {
                    @Override
                    public void accept(final List<PicInfo> list) throws Exception {
                        if (Tools.isListNullOrEmpty(list)) {
                            mTip.setText("没有发现可打印的图片！");
                        } else {
                            mTip.setVisibility(View.GONE);
                            adapter = new PicAdapter(mListView, mContext, list, 3);
                            mListView.setAdapter(adapter);
                            mListView.setLayoutManager(new GridLayoutManager(mContext, 3));
                            adapter.setOnClickListener(new PicAdapter.OnItemClickListener() {
                                @Override
                                public void onClick(int position) {
                                    PicInfo picInfo = list.get(position);
                                    final String realPath = picInfo.getmThumbnailPath();
                                    if (new File(realPath).exists()) {
                                        AlertDialog dialog = new AlertDialog.Builder(mContext)
                                                .setTitle("提示")//设置对话框的标题
                                                .setMessage("是否确定打印?")//设置对话框的内容
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
                });
    }

    @Override
    protected void onDestroy() {
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }

    private void doPhotoPrint(String path) {
        File file = new File(path);
        if (file.exists()) {
            PrintHelper photoPrinter = new PrintHelper(this);
            photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            photoPrinter.printBitmap(file.getName(), bitmap);
        } else {
            Toast.makeText(mContext, "图片异常，无法打印！请检查是否存在该图片", Toast.LENGTH_SHORT).show();
        }
    }
}
