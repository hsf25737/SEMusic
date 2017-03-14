package com.past.music.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.past.music.entity.ArtistEntity;
import com.past.music.pastmusic.R;
import com.past.music.utils.MusicUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LocalSingerFragment extends BaseFragment {

    @BindView(R.id.local_singer_recycle)
    RecyclerView mRecyclerView;

    private List<ArtistEntity> list = new ArrayList<>();

    private LocalSingerFragmentAdapter mAdapter;

    public static LocalSingerFragment newInstance() {
        LocalSingerFragment fragment = new LocalSingerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_local_singer, container, false);
        ButterKnife.bind(this, view);
        mAdapter = new LocalSingerFragmentAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mAdapter);
        reloadAdapter();
        return view;
    }


    //更新adapter界面
    public void reloadAdapter() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(final Void... unused) {
                list.addAll(MusicUtils.queryArtist(mContext));
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                mAdapter.notifyDataSetChanged();
            }
        }.execute();
    }

    private class LocalSingerFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new SingerItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.singer_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((SingerItemViewHolder) holder).onBindData(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    class SingerItemViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_singer)
        SimpleDraweeView simpleDraweeView;

        @BindView(R.id.singer_list_title)
        TextView mTitle;

        @BindView(R.id.singer_list_info)
        TextView mInfo;

        public SingerItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void onBindData(ArtistEntity artistEntity) {
            mTitle.setText(artistEntity.artist_name);
            mInfo.setText(artistEntity.getNumber_of_tracks() + "首");
        }
    }
}