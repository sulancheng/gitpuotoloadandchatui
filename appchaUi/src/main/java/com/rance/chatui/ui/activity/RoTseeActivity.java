package com.rance.chatui.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bm.library.PhotoView;
import com.rance.chatui.R;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.rance.chatui.R.id.iv_photo;
import static com.rance.chatui.R.id.pv_photo;


public class RoTseeActivity extends Activity {
    @Bind(pv_photo)
    PhotoView mypv_photo;

    @Bind(iv_photo)
    ImageView myiv_photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ro_tsee);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        mypv_photo.enable();

    }
    //主线程中执行
    @Subscribe(threadMode = ThreadMode.MAIN,sticky = true)
    public void onMainEventBus(String path) {
        Log.i("图片地址so",path);
        Picasso.with(this).load(path).into(mypv_photo);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
