package com.example.test.cameraphoto.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.test.cameraphoto.BitmapUtil;
import com.example.test.cameraphoto.FileUtils;

import org.reactivestreams.Publisher;

import java.io.File;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Edison on 2018/10/19.
 */
public class PhotoFrameAdapter extends PagerAdapter {
    private Context mContext;
    private Bitmap mSourceBitmap;
    private Bitmap mFrameBitmap;
    private Bitmap mResultBitmap;
    private List<Integer> mDataList;
    private SparseArray<Disposable> mSparseArray = new SparseArray<>();
    private CompositeDisposable mDisposables = new CompositeDisposable();
    private BitmapFactory.Options mOptions;
    private BitmapUtil mBitmapUtil;
    private String mErrorMsg = "加载图片出错，请重新尝试";

    public PhotoFrameAdapter(Context context, List<Integer> resIdList, String framePath) {
        super();
        mContext = context;
        mDataList = resIdList;
        mBitmapUtil = new BitmapUtil();
        mOptions = new BitmapFactory.Options();
        //如果这是非空的，解码器将尝试解码到这个颜色空间中。原图为ARGB_8888,设置其为RGB_565
        mOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        try {
            if (!Tools.isNullOrEmpty(framePath)) {
                File frameFile = new File(framePath);
                if (frameFile.exists()) {
                    mFrameBitmap = BitmapFactory.decodeFile(framePath, mOptions);
                } else {
                    mFrameBitmap = null;
                }
            } else {
                mFrameBitmap = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public int getCount() {
        if (mDataList != null) {
            return mDataList.size();
        }
        return 0;
    }

    @Override
    public int getItemPosition(Object object) {
        if (object != null && mDataList != null) {
            Integer resId = (Integer) ((ImageView) object).getTag();
            if (resId != null) {
                for (int i = 0; i < mDataList.size(); i++) {
                    if (resId.equals(mDataList.get(i))) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public Object instantiateItem(View container, int position) {
        if (mDataList != null && position < mDataList.size()) {
            Integer picObjectId = mDataList.get(position);
            if (picObjectId != null) {
                final ImageView itemView = new MyImageView(mContext);
                Disposable disposable = Flowable.just(picObjectId)
                        .flatMap(new Function<Integer, Publisher<Bitmap>>() {
                            @Override
                            public Publisher<Bitmap> apply(Integer s) throws Exception {
                                if (mSourceBitmap != null) {
                                    mSourceBitmap.recycle();
                                }
                                String sourcePath = FileUtils.importFile(mContext, s);
                                File sourceFile = new File(sourcePath);
                                if (sourceFile.exists()) {
                                    mSourceBitmap = BitmapFactory.decodeFile(sourcePath, mOptions);
                                    if (mFrameBitmap != null) {
                                        mResultBitmap = mBitmapUtil.newBitmap(mFrameBitmap, mSourceBitmap);
                                        return Flowable.just(mResultBitmap);
                                    } else {
                                        mResultBitmap = mSourceBitmap;
                                        return Flowable.just(mSourceBitmap);
                                    }
                                } else {
                                    return null;
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Bitmap>() {
                            @Override
                            public void accept(Bitmap bitmap) throws Exception {
                                if (bitmap != null) {
                                    itemView.setImageBitmap(bitmap);
                                } else {
                                    Toast.makeText(mContext, mErrorMsg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mSparseArray.put(position, disposable);
                mDisposables.add(disposable);
                //此处假设所有的照片都不同，用resId唯一标识一个itemView；也可用其它Object来标识，只要保证唯一即可
                itemView.setTag(picObjectId);

                ((ViewPager) container).addView(itemView);
                return itemView;
            }
        }
        return null;
    }

    @Override
    public void destroyItem(View container, int position, Object object) {
        if (object != null) {
            Disposable disposable = mSparseArray.get(position);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                mSparseArray.remove(position);
            }
            ViewGroup viewPager = ((ViewGroup) container);
            int count = viewPager.getChildCount();
            for (int i = 0; i < count; i++) {
                ImageView childView = (ImageView) viewPager.getChildAt(i);
                if (childView != null) {
                    viewPager.removeView(childView);
                    releaseImageViewResource(childView);
                    break;
                }
            }
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    public void clearDisposable() {
        mDisposables.clear();
    }

    public Bitmap getResultBitmap() {
        return mResultBitmap;
    }

    public void recycleAllBitmap() {
        if (mFrameBitmap != null)
            mFrameBitmap.recycle();
        if (mSourceBitmap != null)
            mSourceBitmap.recycle();
        if (mResultBitmap != null)
            mResultBitmap.recycle();
    }

    private void releaseImageViewResource(ImageView imageView) {
        if (imageView == null) return;
        Drawable drawable = imageView.getDrawable();
        if (drawable != null && drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            Bitmap bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
    }

    public void updateData(List<Integer> itemsResId) {
        if (itemsResId == null) {
            return;
        }
        mDataList = itemsResId;
        this.notifyDataSetChanged();
    }
}