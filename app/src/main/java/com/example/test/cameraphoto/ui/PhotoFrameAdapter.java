package com.example.test.cameraphoto.ui;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.test.cameraphoto.FileUtils;
import com.example.test.cameraphoto.R;
import com.squareup.picasso.Picasso;

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
    private List<Integer> mDataList;
    private SparseArray<Disposable> mSparseArray = new SparseArray<>();
    private CompositeDisposable mDisposables = new CompositeDisposable();

    public PhotoFrameAdapter(Context context, List<Integer> resIdList) {
        super();
        mContext = context;
        mDataList = resIdList;
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
                final ImageView itemView = new ImageView(mContext);
                Disposable disposable = Flowable.just(picObjectId)
                        .flatMap(new Function<Integer, Publisher<String>>() {
                            @Override
                            public Publisher<String> apply(Integer s) throws Exception {
                                return Flowable.just(FileUtils.importFile(mContext, s));
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String path) throws Exception {
                                Picasso.get().load(new File(path))
                                        .placeholder(R.drawable.progress_animation)
                                        .error(R.drawable.ic_launcher_background)
                                        .into(itemView);
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
        //注意：此处position是ViewPager中所有要显示的页面的position，与Adapter mDrawableResIdList并不是一一对应的。
        //因为mDrawableResIdList有可能被修改删除某一个item，在调用notifyDataSetChanged()的时候，ViewPager中的页面
        //数量并没有改变，只有当ViewPager遍历完自己所有的页面，并将不存在的页面删除后，二者才能对应起来
        if (object != null) {
            Disposable disposable = mSparseArray.get(position);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
                mSparseArray.remove(position);
            }
            ViewGroup viewPager = ((ViewGroup) container);
            int count = viewPager.getChildCount();
            for (int i = 0; i < count; i++) {
                View childView = viewPager.getChildAt(i);
                if (childView == object) {
                    viewPager.removeView(childView);
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

    public void updateData(List<Integer> itemsResId) {
        if (itemsResId == null) {
            return;
        }
        mDataList = itemsResId;
        this.notifyDataSetChanged();
    }
}