package com.example.test.cameraphoto.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.mtp.PicInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by apple on 2018/3/23.
 */

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.ViewHolder> {
    private List<PicInfo> mList = new ArrayList<>();
    private Context context;
    private RecyclerView mRecyclerView;
    int columns = 1;

    public PicAdapter(RecyclerView rv, Context context, List<PicInfo> list, int columns) {
        mRecyclerView = rv;
        this.columns = columns;
        this.context = context;
        setData(list);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pic, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final PicInfo image;
        synchronized (obj) {
            image = mList.get(position);
        }
        //Picasso.get().load(new File(image.getmThumbnailPath())).into(holder.img);
        Glide.with(context)
                .load(image.getmThumbnailPath())
                .into(holder.img);
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(position);
            }
        });
    }

    public void setData(List<PicInfo> list) {
        synchronized (obj) {
            mList = list;
        }

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public List<PicInfo> getData() {
        return mList;
    }

    public static Object obj = new Object();


    class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView img;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            img = itemView.findViewById(R.id.iv_pic);
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            fl.height = getWidth(context) / columns;
            img.setLayoutParams(fl);
        }
    }

    /**
     * 获取屏幕宽
     */
    public static int getWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }

    OnItemClickListener mListener;

    public void setOnClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    interface OnItemClickListener {
        void onClick(int position);
    }


}
