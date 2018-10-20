package com.example.test.cameraphoto.ui;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.ui.base.BaseAct;

import java.io.File;

import butterknife.BindView;
import butterknife.OnClick;

public class MainActivity extends BaseAct implements View.OnClickListener {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    @Override
    protected int getLayout() {
        return R.layout.act_main;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "首页");
        //初始化文件夹
        File dir = new File(Constant.PIC_PATH_FRAME);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        dir = new File(Constant.PIC_PATH_RESULT);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @OnClick({R.id.print_action, R.id.camera_action})
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.print_action:
                //打印照片
                startActivity(new Intent(mContext, PrintPicListAct.class));
                break;
            case R.id.camera_action:
                //查看单反照片
                startActivity(new Intent(mContext, CameraPicListAct.class));
                /*String path = Constant.PIC_PATH + "dsg.JPG";
                Intent intent = new Intent(mContext, PhotoFrameAct.class);
                intent.putExtra(Constant.EXTRA_KEY, path);
                startActivity(intent);*/
                break;
        }
    }
}
