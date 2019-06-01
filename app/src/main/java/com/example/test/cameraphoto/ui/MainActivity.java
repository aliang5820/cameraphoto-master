package com.example.test.cameraphoto.ui;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.test.cameraphoto.Constant;
import com.example.test.cameraphoto.R;
import com.example.test.cameraphoto.ui.base.BaseAct;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseAct implements View.OnClickListener {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;

    public boolean onCreateOptionsMenu(Menu menu) {
        //导入菜单布局
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //创建菜单项的点击事件
        switch (item.getItemId()) {
            case R.id.add:
                Intent intent = new Intent(Intent.ACTION_PICK, null);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                startActivityForResult(intent, Constant.ALBUM_OK);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && Constant.ALBUM_OK == requestCode) {
            Uri uri = data.getData();
            String sourcePath = Uri2Path(uri);
            Observable.just(sourcePath)
                    .subscribeOn(Schedulers.io())
                    .map(new Function<String, Boolean>() {
                        @Override
                        public Boolean apply(String source) throws Exception {
                            String fileName = source.substring(source.lastIndexOf("/"));
                            copyFile(source, Constant.PIC_PATH_FRAME + fileName);
                            return true;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Consumer<Boolean>() {
                                @Override
                                public void accept(Boolean aBoolean) {
                                    Toast.makeText(mContext, "复制成功!", Toast.LENGTH_SHORT).show();
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    throwable.printStackTrace();
                                    Toast.makeText(mContext, "复制单个文件操作出错", Toast.LENGTH_SHORT).show();
                                }
                            });
        }
    }

    private String Uri2Path(Uri uri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                //Log.d(TAG, uri.toString());
                Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            //Log.d(TAG, "content: " + uri.toString());
            imagePath = getImagePath(uri, null);
        }
        return imagePath;
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }

            cursor.close();
        }
        return path;
    }

    /**
     * 复制单个文件
     *
     * @param oldPath String 原文件路径 如：c:/fqf.txt
     * @param newPath String 复制后路径 如：f:/fqf.txt
     */
    public void copyFile(String oldPath, String newPath) throws Exception {
        int bytesum = 0;
        int byteread = 0;
        File oldfile = new File(oldPath);
        if (oldfile.exists()) { //文件存在时
            InputStream inStream = new FileInputStream(oldPath); //读入原文件
            FileOutputStream fs = new FileOutputStream(newPath);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread; //字节数 文件大小
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
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
