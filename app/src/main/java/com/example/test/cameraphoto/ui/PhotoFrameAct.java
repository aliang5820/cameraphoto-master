package com.example.test.cameraphoto.ui;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.print.PrintHelper;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.test.cameraphoto.BitmapUtil;
import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.FileUtils;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.ui.base.BaseAct;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
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
    /*@BindView(R.id.viewPager)
    protected ViewPager mViewPager;*/
    @BindView(R.id.image_view)
    protected ImageView mImageView;
    @BindView(R.id.grid)
    protected GridView frameGridView;
    //private PhotoFrameAdapter mTopAdapter;
    private FrameHorizontalAdapter mFrameAdapter;
    private List<Integer> sourceIdList;
    private Integer sourcePosition;
    private CompositeDisposable compositeDisposable;
    private Bitmap mSourceBitmap;
    private Bitmap mFrameBitmap;
    private Bitmap mResultBitmap;
    private BitmapFactory.Options mOptions;
    private BitmapUtil mBitmapUtil;
    private static final float FLIP_DISTANCE = 100;
    private GestureDetector mDetector;

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
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    protected int getLayout() {
        return R.layout.act_frame;
    }

    @Override
    protected void initEventAndData() {
        setToolBar(mToolbar, "合成照片");
        compositeDisposable = new CompositeDisposable();
        sourceIdList = getIntent().getIntegerArrayListExtra(Constant.EXTRA_SOURCE);
        sourcePosition = getIntent().getIntExtra(Constant.EXTRA_KEY, 0);
        initSource();
        initFrameList();
    }

    private void initSource() {
        mBitmapUtil = new BitmapUtil();
        mOptions = new BitmapFactory.Options();
        //如果这是非空的，解码器将尝试解码到这个颜色空间中。原图为ARGB_8888,设置其为RGB_565
        mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Integer picObjectId = sourceIdList.get(sourcePosition);
        String sourcePath = FileUtils.importFile(mContext, picObjectId);
        //初始化默认图片
        if (!Tools.isNullOrEmpty(sourcePath)) {
            int degree = mBitmapUtil.readPictureDegree(sourcePath);
            mSourceBitmap = BitmapFactory.decodeFile(sourcePath, mOptions);
            if (degree > 0) {
                mSourceBitmap = mBitmapUtil.rotateBitmap(mSourceBitmap, degree);
            }
            mResultBitmap = mSourceBitmap;
            mImageView.setImageBitmap(mResultBitmap);
        }
        mDetector = new GestureDetector(this, new GestureDetector.OnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() - e2.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向左滑...");
                    if (sourcePosition > 0) {
                        changeSource(sourceIdList.get(--sourcePosition));
                    }
                    return true;
                }
                if (e2.getX() - e1.getX() > FLIP_DISTANCE) {
                    Log.i(TAG, "向右滑...");
                    if (sourcePosition < sourceIdList.size() - 1) {
                        changeSource(sourceIdList.get(++sourcePosition));
                    }
                    return true;
                }
                return false;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return false;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
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
                changeFrame(framePath);
            }
        });
    }

    //切换原照片
    private void changeSource(int picObjectId) {
        Disposable disposable = Flowable.just(picObjectId)
                .flatMap(new Function<Integer, Publisher<Bitmap>>() {
                    @Override
                    public Publisher<Bitmap> apply(Integer s) throws Exception {
                        String sourcePath = FileUtils.importFile(mContext, s);
                        File sourceFile = new File(sourcePath);
                        if (sourceFile.exists()) {
                            int degree = mBitmapUtil.readPictureDegree(sourcePath);
                            mSourceBitmap = BitmapFactory.decodeFile(sourcePath, mOptions);
                            if (degree > 0) {
                                mSourceBitmap = mBitmapUtil.rotateBitmap(mSourceBitmap, degree);
                            }
                            if (mFrameBitmap != null) {
                                mResultBitmap = mBitmapUtil.newBitmap(mFrameBitmap, mSourceBitmap);
                                return Flowable.just(mResultBitmap);
                            } else {
                                mResultBitmap = mSourceBitmap;
                                return Flowable.just(mSourceBitmap);
                            }
                        } else {
                            throw new Exception("没有找到原图！");
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) {
                        if (bitmap != null) {
                            mImageView.setImageBitmap(bitmap);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
        compositeDisposable.add(disposable);
    }

    //切换相框
    private void changeFrame(String framePath) {
        if (!Tools.isNullOrEmpty(framePath)) {
            File frameFile = new File(framePath);
            if (frameFile.exists()) {
                Disposable disposable = Flowable.just(framePath)
                        .flatMap(new Function<String, Publisher<Bitmap>>() {
                            @Override
                            public Publisher<Bitmap> apply(String path) {
                                mFrameBitmap = BitmapFactory.decodeFile(path, mOptions);
                                mResultBitmap = mBitmapUtil.newBitmap(mFrameBitmap, mSourceBitmap);
                                return Flowable.just(mResultBitmap);
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap bitmap) {
                                if (bitmap != null) {
                                    mImageView.setImageBitmap(bitmap);
                                }
                            }
                        });
                compositeDisposable.add(disposable);
            }
        } else {
            mResultBitmap = mSourceBitmap;
            mImageView.setImageBitmap(mResultBitmap);
            mFrameBitmap.recycle();
            mFrameBitmap = null;
        }
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
            Disposable disposable = Flowable.just(mResultBitmap)
                    .flatMap(new Function<Bitmap, Publisher<Boolean>>() {
                        @Override
                        public Publisher<Boolean> apply(Bitmap bitmap) {
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
            compositeDisposable.add(disposable);
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
                Glide.with(mContext)
                        .load(Constant.PIC_PATH_FRAME + fileName)
                        .into(image);
            } else {
                image.setBackgroundResource(R.color.secondaryText);
                textView.setText(R.string.empty_frame);
            }
            return convertView;
        }
    }
}
