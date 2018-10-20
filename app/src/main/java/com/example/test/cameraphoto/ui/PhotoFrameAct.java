package com.example.test.cameraphoto.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.FileUtils;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.ui.base.BaseAct;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

/**
 * Created by Edison on 2018/10/13.
 */
public class PhotoFrameAct extends BaseAct {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.viewPager)
    protected ViewPager mViewPager;
    @BindView(R.id.grid)
    protected GridView frameGridView;
    private PhotoFrameAdapter mAdapter;
    private List<Integer> sourceIdList;
    private Integer sourcePosition;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.frame, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ok:
                //合成，并保存
                final EditText et = new EditText(mContext);
                new AlertDialog
                        .Builder(mContext)
                        .setTitle("请输入保存的文件名")
                        .setView(et)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //按下确定键后的事件
                                if (et.length() > 0) {
                                    try {
                                        String resultPath = Constant.PIC_PATH_RESULT + et.getText().toString() + ".jpg";
                                        //FileUtils.writeImage(mTmpBmp, resultPath, 100);
                                        //TODO 保存or打印照片
                                        dialogInterface.dismiss();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "保存失败！", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }).setNegativeButton("取消", null).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if(mAdapter != null) {
            mAdapter.clearDisposable();
        }
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.act_frame;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "合成照片");
        sourceIdList = getIntent().getIntegerArrayListExtra(Constant.EXTRA_SOURCE);
        sourcePosition = getIntent().getIntExtra(Constant.EXTRA_KEY, 0);

        /*BitmapFactory.Options mOption = new BitmapFactory.Options();
        mOption.inSampleSize = 1;
        mBitmap = BitmapFactory.decodeFile(sourcePicPath, mOption);
        mTmpBmp = mBitmap;

        reset();
        mImageFrame = new PhotoFrame(this, mBitmap);*/
        PhotoFrameAdapter adapter = new PhotoFrameAdapter(mContext, sourceIdList);
        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(sourcePosition);
        mViewPager.setOffscreenPageLimit(1);
        initFrameList();
    }

    //初始化可选择的相框列表
    private void initFrameList() {
        //构建相框数据源
        final List<String> frameResData = FileUtils.getPicFileName(Constant.PIC_PATH_FRAME);
        //调用控制水平滚动的方法
        setHorizontalGridView(frameResData.size(), frameGridView);
        final FrameHorizontalAdapter frameAdapter = new FrameHorizontalAdapter(frameResData);
        frameGridView.setAdapter(frameAdapter);
        frameGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String framePath = Constant.PIC_PATH_FRAME + frameAdapter.getItem(position);
                //TODO 切换相框
            }
        });
    }

    /**
     * 水平滚动的GridView的控制
     */
    private void setHorizontalGridView(int size, GridView gridView) {
        int length = 100;
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;
        int gridViewWidth = (int) (size * (length) * density);
        int itemWidth = (int) ((length) * density);

        @SuppressWarnings("deprecation")
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridViewWidth, LinearLayout.LayoutParams.FILL_PARENT);
        gridView.setLayoutParams(params); // 设置GirdView布局参数,横向布局的关键
        gridView.setColumnWidth(itemWidth); // 设置列表项宽
        gridView.setHorizontalSpacing(10); // 设置列表项水平间距
        gridView.setStretchMode(GridView.NO_STRETCH);
        gridView.setNumColumns(size); // 设置列数量=列表集合数

    }

    class FrameHorizontalAdapter extends BaseAdapter {
        private List<String> dataList = new ArrayList<>();

        public FrameHorizontalAdapter(List<String> list) {
            if (!Tools.isListNullOrEmpty(list)) {
                dataList = list;
            }
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public String getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_frame, null);
            }
            String fileName = dataList.get(position);

            ImageView image = convertView.findViewById(R.id.icon);
            TextView textView = convertView.findViewById(R.id.text);
            textView.setText(fileName);

            Picasso.get().load(new File(Constant.PIC_PATH_FRAME + fileName))
                    .placeholder(R.drawable.progress_animation)
                    .error(R.drawable.ic_launcher_background)
                    .into(image);
            return convertView;
        }
    }
}
