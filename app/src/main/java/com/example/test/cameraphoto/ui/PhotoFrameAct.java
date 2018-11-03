package com.example.test.cameraphoto.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.print.PrintHelper;
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

import org.reactivestreams.Publisher;

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
    private PhotoFrameAdapter mTopAdapter;
    private FrameHorizontalAdapter mFrameAdapter;
    private List<Integer> sourceIdList;
    private Integer sourcePosition;
    private Disposable disposable;

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
                new AlertDialog
                        .Builder(mContext)
                        .setTitle("确认保存该照片吗？")
                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveAction(false);
                                dialogInterface.dismiss();
                            }
                        })
                        .setNeutralButton("打印", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                saveAction(true);
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeButton("取消", null).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (mTopAdapter != null) {
            mTopAdapter.clearDisposable();
            mTopAdapter.recycleAllBitmap();
        }
        disposable.dispose();
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
        mTopAdapter = new PhotoFrameAdapter(mContext, sourceIdList, null);
        mViewPager.setAdapter(mTopAdapter);
        mViewPager.setCurrentItem(sourcePosition);
        mViewPager.setOffscreenPageLimit(1);
        initFrameList();
    }

    //初始化可选择的相框列表
    private void initFrameList() {
        //构建相框数据源
        final List<String> frameResData = FileUtils.getFrameFile(Constant.PIC_PATH_FRAME);
        //调用控制水平滚动的方法
        setHorizontalGridView(frameResData.size(), frameGridView);
        mFrameAdapter = new FrameHorizontalAdapter(frameResData);
        frameGridView.setAdapter(mFrameAdapter);
        frameGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                String framePath;
                if (Tools.isNullOrEmpty(mFrameAdapter.getItem(position))) {
                    framePath = null;
                } else {
                    framePath = Constant.PIC_PATH_FRAME + mFrameAdapter.getItem(position);
                }
                //切换相框
                mTopAdapter = new PhotoFrameAdapter(mContext, sourceIdList, framePath);
                int currentPosition = mViewPager.getCurrentItem();
                mViewPager.setAdapter(mTopAdapter);
                mViewPager.setCurrentItem(currentPosition);
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

    //保存
    private void saveAction(final boolean isPrint) {
        try {
            final String resultPath = Constant.PIC_PATH_RESULT + System.currentTimeMillis() + ".jpg";
            disposable = Flowable.just(mTopAdapter.getResultBitmap())
                    .flatMap(new Function<Bitmap, Publisher<Boolean>>() {
                        @Override
                        public Publisher<Boolean> apply(Bitmap bitmap) throws Exception {
                            try {
                                FileUtils.writeImage(bitmap, resultPath, 100);
                                return Flowable.just(true);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return null;
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io())//为上下游分别指定各自的线程
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isSuccess) throws Exception {
                            //是否保存成功
                            if (isSuccess) {
                                Toast.makeText(mContext, "保存成功", Toast.LENGTH_SHORT).show();
                                //是否需要打印
                                if (isPrint) {
                                    PrintHelper photoPrinter = new PrintHelper(mContext);
                                    photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                                    Bitmap bitmap = BitmapFactory.decodeFile(resultPath);
                                    photoPrinter.printBitmap("打印照片", bitmap);
                                }
                            } else {
                                Toast.makeText(mContext, "保存失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }
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
            ImageView image = convertView.findViewById(R.id.icon);
            TextView textView = convertView.findViewById(R.id.text);

            String fileName = dataList.get(position);
            if (!Tools.isNullOrEmpty(fileName)) {
                textView.setText(fileName);
                Picasso.get().load(new File(Constant.PIC_PATH_FRAME + fileName))
                        .placeholder(R.drawable.progress_animation)
                        .error(R.drawable.ic_launcher_background)
                        .into(image);
            } else {
                image.setBackgroundResource(R.color.secondaryText);
                textView.setText(R.string.empty_frame);
            }
            return convertView;
        }
    }
}
