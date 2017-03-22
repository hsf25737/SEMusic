package com.past.music.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.past.music.MyApplication;
import com.past.music.entity.MusicEntity;
import com.past.music.log.MyLog;
import com.past.music.pastmusic.IMediaAidlInterface;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MediaService extends Service {

    private static final String TAG = "MediaService";
    public static final String PLAYSTATE_CHANGED = "com.past.music.play_state_changed";
    public static final String META_CHANGED = "com.past.music.meta_changed";
    private static final int IDCOLIDX = 0;
    private static final int TRACK_ENDED = 1;
    private static final int TRACK_WENT_TO_NEXT = 2;
    private static final int RELEASE_WAKELOCK = 3;
    private static final int SERVER_DIED = 4;
    private static final int FOCUSCHANGE = 5;
    private static final int FADEDOWN = 6;
    private static final int FADEUP = 7;

    private static final String[] PROJECTION = new String[]{
            "audio._id AS _id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };

    private static final String[] PROJECTION_MATRIX = new String[]{
            "_id", MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST_ID
    };

    private boolean isPlaying = false;


    private Context mContext = null;
    //传进来的歌单
    private HashMap<Long, MusicEntity> mPlaylistInfo = new HashMap<>();
    private ArrayList<MusicTrack> mPlaylist = new ArrayList<MusicTrack>(100);


    /**
     * 提供访问控制音量和钤声模式的操作
     */
    private AudioManager mAudioManager;
    private MusicPlayerHandler mPlayerHandler;
    private HandlerThread mHandlerThread;

    private MultiPlayer mPlayer;
    private String mFileToPlay;
    private Cursor mCursor;

    private int mCardId;

    private int mPlayPos = -1;

    private int mNextPlayPos = -1;

    private int mOpenFailedCounter = 0;

    private int mMediaMountedCount = 0;

//    private int mShuffleMode = SHUFFLE_NONE;
//
//    private int mRepeatMode = REPEAT_ALL;

    private int mServiceStartId = -1;

    private final IBinder mBinder = new ServiceStub(this);

    public MediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        MyLog.i(TAG, "onBind");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MyLog.i(TAG, "onCreate");
        mContext = this;
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mHandlerThread = new HandlerThread("MusicPlayerHandler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mHandlerThread.start();
        mPlayerHandler = new MusicPlayerHandler(this, mHandlerThread.getLooper());
        mPlayer = new MultiPlayer(this);
    }

    public void play(boolean createNewNextTrack) {
        int status = mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return;
        }
//        final Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
//        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, getAudioSessionId());
//        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, getPackageName());
//        sendBroadcast(intent);
//
//        mAudioManager.registerMediaButtonEventReceiver(new ComponentName(getPackageName(),
//                MediaButtonIntentReceiver.class.getName()));
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            mSession.setActive(true);
//        if (createNewNextTrack) {
//            setNextTrack();
//        } else {
//            setNextTrack(mNextPlayPos);
//        }
//        if (mPlayer.isTrackPrepared()) {
//            final long duration = mPlayer.duration();
//            if (mRepeatMode != REPEAT_CURRENT && duration > 2000
//                    && mPlayer.position() >= duration - 2000) {
//                gotoNext(true);
//            }
//        }
        mPlayer.start();
//        mPlayerHandler.removeMessages(FADEDOWN);
//        mPlayerHandler.sendEmptyMessage(FADEUP);
//        setIsSupposedToBePlaying(true, true);
//        cancelShutdown();
//        updateNotification();
        notifyChange(META_CHANGED);
        MyLog.i(TAG, "play");
    }

    public void pause() {
    }

    public void stop() {
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    private final AudioManager.OnAudioFocusChangeListener mAudioFocusListener = new AudioManager.OnAudioFocusChangeListener() {

        @Override
        public void onAudioFocusChange(final int focusChange) {
            mPlayerHandler.obtainMessage(FOCUSCHANGE, focusChange, 0).sendToTarget();
        }
    };

    private void updateCursorForDownloadedFile(Context context, Uri uri) {
        synchronized (this) {
            closeCursor();
            //模拟一个cursor
            MatrixCursor cursor = new MatrixCursor(PROJECTION_MATRIX);
            String title = getValueForDownloadedFile(this, uri, MediaStore.Audio.Media.TITLE);
            cursor.addRow(new Object[]{null, null, null, title, null, null, null, null});
            mCursor = cursor;
            mCursor.moveToFirst();
        }
    }

    /**
     * 根据URI查找本地音乐文件的相应的column名称
     *
     * @param context
     * @param uri
     * @param column
     * @return
     */
    private String getValueForDownloadedFile(Context context, Uri uri, String column) {

        Cursor cursor = null;
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private Cursor openCursorAndGoToFirst(Uri uri, String[] projection,
                                          String selection, String[] selectionArgs) {
        Cursor c = getContentResolver().query(uri, projection,
                selection, selectionArgs, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }

    private synchronized void closeCursor() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
    }

    private void updateCursor(final Uri uri) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(uri, PROJECTION, null, null);
        }
    }

    private void updateCursor(final long trackId) {
        MusicEntity info = mPlaylistInfo.get(trackId);
        if (mPlaylistInfo.get(trackId) != null) {
            MatrixCursor cursor = new MatrixCursor(PROJECTION);
            cursor.addRow(new Object[]{info.songId, info.artist, info.albumName, info.musicName
                    , info.data, info.albumData, info.albumId, info.artistId});
            cursor.moveToFirst();
            mCursor = cursor;
            cursor.close();
        }
    }

    private void updateCursor(final String selection, final String[] selectionArgs) {
        synchronized (this) {
            closeCursor();
            mCursor = openCursorAndGoToFirst(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, selection, selectionArgs);
        }
    }

    public void open(final HashMap<Long, MusicEntity> infos, final long[] list, final int position) {
        MyLog.i(TAG, "open" + list.length);
        synchronized (this) {
            mPlaylistInfo = infos;
//            if (mShuffleMode == SHUFFLE_AUTO) {
//                mShuffleMode = SHUFFLE_NORMAL;
//            }
//            final long oldId = getAudioId();
            final int listlength = list.length;
            boolean newlist = true;
            if (mPlaylist.size() == listlength) {
                newlist = false;
                for (int i = 0; i < listlength; i++) {
                    if (list[i] != mPlaylist.get(i).mId) {
                        newlist = true;
                        break;
                    }
                }
            }
            if (newlist) {
                addToPlayList(list, -1);
//                notifyChange(QUEUE_CHANGED);
            }
            if (position >= 0) {
                mPlayPos = position;
            } else {
                mPlayPos++;
//                mPlayPos = mShuffler.nextInt(mPlaylist.size());
            }
//            mHistory.clear();
            openCurrentAndNextPlay(true);
//            if (oldId != getAudioId()) {
//                notifyChange(META_CHANGED);
//            }
        }
    }

    private void addToPlayList(final long[] list, int position) {
        final int addlen = list.length;
        if (position < 0) {
            mPlaylist.clear();
            position = 0;
        }

        mPlaylist.ensureCapacity(mPlaylist.size() + addlen);
        if (position > mPlaylist.size()) {
            position = mPlaylist.size();
        }

        final ArrayList<MusicTrack> arrayList = new ArrayList<MusicTrack>(addlen);
        for (int i = 0; i < list.length; i++) {
            arrayList.add(new MusicTrack(list[i], i));
        }
        mPlaylist.addAll(position, arrayList);
        if (mPlaylist.size() == 0) {
            closeCursor();
//            notifyChange(META_CHANGED);
        }
    }


    private void openCurrentAndNextPlay(boolean play) {
        openCurrentAndMaybeNext(play, true);
    }

    private void openCurrentAndMaybeNext(final boolean play, final boolean openNext) {
        synchronized (this) {
            closeCursor();
//            stop(false);
            boolean shutdown = false;
            if (mPlaylist.size() == 0 || mPlaylistInfo.size() == 0 && mPlayPos >= mPlaylist.size()) {
//                clearPlayInfos();
                return;
            }
            final long id = mPlaylist.get(mPlayPos).mId;
            updateCursor(id);
//            getLrc(id);
            if (mPlaylistInfo.get(id) == null) {
                return;
            }
            if (!mPlaylistInfo.get(id).islocal) {
//                if (mRequestUrl != null) {
//                    mRequestUrl.stop();
//                    mUrlHandler.removeCallbacks(mRequestUrl);
//                }
//                mRequestUrl = new RequestPlayUrl(id, play);
//                mUrlHandler.postDelayed(mRequestUrl, 70);
            } else {
                while (true) {
                    MyLog.i(TAG, "------执行了else后面");
                    if (mCursor != null && openFile(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI + "/"
                            + mCursor.getLong(IDCOLIDX))) {
                        MyLog.i(TAG, "------break出去了");
                        break;
                    }
                    closeCursor();
                    MyLog.i(TAG, "------关闭了Cursor");
                    if (mOpenFailedCounter++ < 10 && mPlaylist.size() > 1) {
//                        final int pos = getNextPosition(false);
                        final int pos = 0;
                        if (pos < 0) {
                            shutdown = true;
                            break;
                        }
                        mPlayPos = pos;
//                        stop(false);
                        mPlayPos = pos;
                        updateCursor(mPlaylist.get(mPlayPos).mId);
                        MyLog.i(TAG, "------执行了updateCursor");
                    } else {
                        mOpenFailedCounter = 0;
                        MyLog.w(TAG, "Failed to open file for playback");
                        shutdown = true;
                        break;
                    }
                }
            }

//            if (shutdown) {
//                scheduleDelayedShutdown();
//                if (mIsSupposedToBePlaying) {
//                    mIsSupposedToBePlaying = false;
//                    notifyChange(PLAYSTATE_CHANGED);
//                }
//            } else if (openNext) {
//                setNextTrack();
//            }
        }
    }

    private void notifyChange(final String what) {
        Intent intent = null;
        if (what.equals(META_CHANGED)) {
            intent = new Intent(META_CHANGED);
//            sendBroadcast(intent);
            sendStickyBroadcast(intent);
        }

    }


    public boolean openFile(final String path) {
        MyLog.i(TAG, "openFile: path = " + path);
        synchronized (this) {
            if (path == null) {
                return false;
            }
            if (mCursor == null) {
                Uri uri = Uri.parse(path);
                boolean shouldAddToPlaylist = true;
                long id = -1;
                try {
                    id = Long.valueOf(uri.getLastPathSegment());
                } catch (NumberFormatException ex) {
                }

                if (id != -1 && path.startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
                    updateCursor(uri);
                } else if (id != -1 && path.startsWith(MediaStore.Files.getContentUri("external").toString())) {
                    updateCursor(id);
                } else if (path.startsWith("content://downloads/")) {
                    String mpUri = getValueForDownloadedFile(this, uri, "mediaprovider_uri");
                    if (!TextUtils.isEmpty(mpUri)) {
                        if (openFile(mpUri)) {
//                            notifyChange(META_CHANGED);
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        updateCursorForDownloadedFile(this, uri);
                        shouldAddToPlaylist = false;
                    }
                } else {
                    String where = MediaStore.Audio.Media.DATA + "=?";
                    String[] selectionArgs = new String[]{path};
                    updateCursor(where, selectionArgs);
                }
                try {
                    if (mCursor != null && shouldAddToPlaylist) {
//                        mPlaylist.clear();
//                        mPlaylist.add(new MusicTrack(mCursor.getLong(IDCOLIDX), -1));
//                        notifyChange(QUEUE_CHANGED);
                        mPlayPos = 0;
//                        mHistory.clear();
                    }
                } catch (final UnsupportedOperationException ex) {
                    // Ignore
                }
            }

            mFileToPlay = path;
            mPlayer.setDataSource(mFileToPlay);
            if (mPlayer.ismIsInitialized()) {
                mOpenFailedCounter = 0;
                return true;
            }
//
//            String trackName = getTrackName();
//            if (TextUtils.isEmpty(trackName)) {
//                trackName = path;
//            }
//            sendErrorMessage(trackName);
//
//            stop(true);
            return false;
        }
    }

    public String getTrackName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.TITLE));
        }
    }

    public String getArtistName() {
        synchronized (this) {
            if (mCursor == null) {
                return null;
            }
            return mCursor.getString(mCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST));
        }
    }


    private static final class ServiceStub extends IMediaAidlInterface.Stub {

        private final WeakReference<MediaService> mService;

        private ServiceStub(final MediaService service) {
            mService = new WeakReference<MediaService>(service);
        }

        @Override
        public boolean isPlaying() throws RemoteException {
            return mService.get().isPlaying();
        }

        @Override
        public void stop() throws RemoteException {
            mService.get().stop();
        }

        @Override
        public void pause() throws RemoteException {
            mService.get().pause();
        }

        @Override
        public void play() throws RemoteException {
            mService.get().play(true);
        }

        @Override
        public void openFile(String path) throws RemoteException {
            mService.get().openFile(path);
        }

        @Override
        public void open(Map infos, long[] list, int position) throws RemoteException {
            mService.get().open((HashMap<Long, MusicEntity>) infos, list, position);
        }

        @Override
        public String getArtistName() throws RemoteException {
            return mService.get().getArtistName();
        }

        @Override
        public String getTrackName() throws RemoteException {
            return mService.get().getTrackName();
        }

        @Override
        public String getAlbumName() throws RemoteException {
            return null;
        }

        @Override
        public String getAlbumPath() throws RemoteException {
            return null;
        }

        @Override
        public String[] getAlbumPathtAll() throws RemoteException {
            return new String[0];
        }

        @Override
        public String getPath() throws RemoteException {
            return null;
        }
    }

    private static final class MultiPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        private final WeakReference<MediaService> mService;

        private MediaPlayer mCurrentMediaPlayer = new MediaPlayer();

        private MediaPlayer mNextMediaPlayer;

        private Handler mHandler;

        private boolean mIsInitialized = false;

        private String mNextMediaPath;

        private boolean isFirstLoad = true;


        private int sencondaryPosition = 0;

        private Handler handler = new Handler();

        public boolean ismIsInitialized() {
            return mIsInitialized;
        }

        public boolean ismIsTrackPrepared() {
            return mIsTrackPrepared;
        }

        public boolean ismIsTrackNet() {
            return mIsTrackNet;
        }

        public boolean ismIsNextTrackPrepared() {
            return mIsNextTrackPrepared;
        }

        public boolean ismIsNextInitialized() {
            return mIsNextInitialized;
        }

        public boolean ismIllegalState() {
            return mIllegalState;
        }

        public MultiPlayer(final MediaService service) {
            mService = new WeakReference<MediaService>(service);
            mCurrentMediaPlayer.setWakeMode(mService.get(), PowerManager.PARTIAL_WAKE_LOCK);
        }

        public void setDataSource(final String path) {

            mIsInitialized = setDataSourceImpl(mCurrentMediaPlayer, path);
            if (mIsInitialized) {
//                    setNextDataSource(null);
            }
        }


        boolean mIsTrackPrepared = false;
        boolean mIsTrackNet = false;
        boolean mIsNextTrackPrepared = false;
        boolean mIsNextInitialized = false;
        boolean mIllegalState = false;

        /**
         * @param player
         * @param path
         * @return
         */
        public boolean setDataSourceImpl(MediaPlayer player, String path) {
            try {
                player.reset();
                player.setAudioStreamType(AudioManager.STREAM_MUSIC);
                if (path.startsWith("content://")) {
                    player.setOnPreparedListener(null);
                    player.setDataSource(MyApplication.mContext, Uri.parse(path));
                    player.prepare();
                    player.setOnCompletionListener(this);
                } else {
                    player.setDataSource(path);
//                        player.setOnPreparedListener(preparedListener);
                    player.prepareAsync();
                    mIsTrackNet = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            player.setOnErrorListener(this);
//                player.setOnBufferingUpdateListener(bufferingUpdateListener);
            return true;
        }

        public void start() {
            if (!mIsTrackNet) {
//                mService.get().sendUpdateBuffer(100);
                sencondaryPosition = 100;
                mCurrentMediaPlayer.start();
            } else {
//                sencondaryPosition = 0;
//                mService.get().loading(true);
//                handler.postDelayed(startMediaPlayerIfPrepared, 50);
                mCurrentMediaPlayer.start();
            }
//            mService.get().notifyChange(MUSIC_CHANGED);
        }

        @Override
        public void onCompletion(MediaPlayer mp) {

        }

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }
    }

    private static final class MusicPlayerHandler extends Handler {

        private final WeakReference<MediaService> mService;
        private float mCurrentVolume = 1.0f;

        private MusicPlayerHandler(final MediaService service, final Looper looper) {
            super(looper);
            mService = new WeakReference<MediaService>(service);
        }

        @Override
        public void handleMessage(Message msg) {
            final MediaService service = mService.get();
            if (service == null) {
                return;
            }
            super.handleMessage(msg);
        }
    }
}
