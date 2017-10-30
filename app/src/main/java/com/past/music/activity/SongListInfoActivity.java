package com.past.music.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.neu.gaojin.MyOkHttpClient;
import com.neu.gaojin.response.BaseCallback;
import com.past.music.MyApplication;
import com.past.music.adapter.SongListAdapter;
import com.past.music.api.AvatarRequest;
import com.past.music.api.AvatarResponse;
import com.past.music.entity.MusicEntity;
import com.past.music.log.MyLog;
import com.past.music.pastmusic.R;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class SongListInfoActivity extends ToolBarActivity {

    public static final String TAG = "NetSongListActivity";
    public static final String SONG_LIST_ID = "TOPID";
    public static final String TITLE = "TITLE";

    @BindView(R.id.empty_layout)
    LinearLayout mEmptyLayout;

    @BindView(R.id.rl_hot_list)
    RelativeLayout relativeLayout;

    @BindView(R.id.head_image)
    SimpleDraweeView headView;

    @BindView(R.id.nested_recycle_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.hot_list_title)
    TextView mTvTitle;

    @OnClick(R.id.add_music)
    void addMusic() {
        Toast.makeText(this, "添加音乐", Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.hot_list_back)
    void back() {
        finish();
    }

    @BindView(R.id.parent_view)
    CoordinatorLayout mCoordinatorLayout;

    private String mId;
    private String mTitle;
    private List<MusicEntity> mList;
    private SongListAdapter adapter = null;

    public static void startActivity(Context context, String songListId, String title) {
        Intent intent = new Intent(context, SongListInfoActivity.class);
        intent.putExtra(SONG_LIST_ID, songListId);
        intent.putExtra(TITLE, title);
        ((BaseActivity) context).startActivityByX(intent, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onNewIntent(getIntent());
        setStatusBar_C();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_song_list_info;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mId = intent.getStringExtra(SONG_LIST_ID);
        mTitle = intent.getStringExtra(TITLE);
    }

    public void setSongInterFace() {
        if (mList.get(0).getAlbumPic() == null) {
            if (MyApplication.imageDBService.query(mList.get(0).getArtist().replace(";", "")) == null) {
                AvatarRequest avatarRequest = new AvatarRequest();
                avatarRequest.setArtist(mList.get(0).getArtist().replace(";", ""));
                MyOkHttpClient.getInstance(this).sendNet(avatarRequest, new BaseCallback<AvatarResponse>() {
                    @Override
                    public void onFailure(int code, String error_msg) {
                        MyLog.i("sssss", error_msg);
                    }

                    @Override
                    public void onSuccess(int statusCode, final AvatarResponse response) {
                        MyApplication.imageDBService.insert(mList.get(0).getArtist().replace(";", ""), response.getArtist().getImage().get(2).get_$Text112());
                        headView.setImageURI(response.getArtist().getImage().get(2).get_$Text112());
                    }
                });
            } else {
                headView.setImageURI(MyApplication.imageDBService.query(mList.get(0).getArtist().replace(";", "")));
            }
        } else {
            headView.setImageURI(mList.get(0).getAlbumPic());
        }
        adapter = new SongListAdapter(this, false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        adapter.updateList(mList);
    }

    @Override
    protected void setStatusBar() {
    }

    public void setStatusBar_C() {
        if (MyApplication.musicInfoDBService.haveSong(mId) != null) {
            mEmptyLayout.setVisibility(View.GONE);
            mList = MyApplication.musicInfoDBService.query(mId);
            mTvTitle.setText(mTitle);
            setSongInterFace();
        } else {
            setTitle(mTitle);
        }
    }
}
