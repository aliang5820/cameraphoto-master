package com.example.test.cameraphoto.ui.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.test.cameraphoto.ui.Tools;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseAct extends AppCompatActivity {
    private Unbinder mBinder;
    protected String TAG = BaseAct.class.getSimpleName();
    protected BaseAct mContext;
    public RxPermissions mRxPermissions;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayout());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mContext = this;
        mBinder = ButterKnife.bind(this);
        mRxPermissions = new RxPermissions(this);
        mRxPermissions.setLogging(true);
        initEventAndData();
    }

    @Override
    protected void onDestroy() {
        mBinder.unbind();
        super.onDestroy();
    }

    public void setToolBar(Toolbar toolbar, int resId) {
        setToolBar(toolbar, getString(resId));
    }

    public void setToolBar(Toolbar toolbar, String title) {
        if (!Tools.isNullOrEmpty(title)) {
            toolbar.setTitle(title);
        }
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    protected abstract int getLayout();

    protected abstract void initEventAndData();
}
