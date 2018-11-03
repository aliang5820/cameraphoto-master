package com.example.test.cameraphoto.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;

import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.mtp.MTPService;
import com.example.test.cameraphoto.ui.base.BaseAct;

import java.util.List;

import butterknife.BindView;
import io.reactivex.functions.Consumer;

public class CameraPicListAct extends BaseAct implements Consumer<List> {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    private FragmentManager mFragmentManager;
    EmptyFragment mEmptyFragment = new EmptyFragment();
    PhotoFragment mPhotoFragment = new PhotoFragment();
    MTPService mService;

    @Override
    protected int getLayout() {
        return R.layout.act_camera;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "选择照片");
        permissioncheck();
        openFragment(mEmptyFragment);
        mService = new MTPService(this);
    }

    private void permissioncheck() {
        if (!checkPermissionAllGranted(new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        })) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    },
                    1
            );
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            boolean isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
        }
    }


    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.close();
    }

    private void openFragment(Fragment fragment) {
        mFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (Fragment f : fragments) {
            fragmentTransaction.hide(f);
        }
        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.fl_content, fragment);
        }
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void accept(List list) throws Exception {
        if (list.size() <= 0) {
            openFragment(mEmptyFragment);
        } else {
            mPhotoFragment.setData(list);
            openFragment(mPhotoFragment);
        }
        //mService.stopScanPic();
    }
}
