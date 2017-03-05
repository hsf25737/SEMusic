package com.past.music.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by gaojin on 2017/3/4.
 */

public class MusicEntity implements Parcelable {

    /**
     * 数据库中音乐文件的列名
     */
    public long songId = -1;
    public int albumId = -1;
    public String albumName;
    public String albumData;
    public int duration;
    public String musicName;
    public String artist;
    public long artistId;
    public String data;
    public String folder;
    public String lrc;
    public boolean islocal;
    public String sort;


    public int size;
    /**
     * 0表示没有收藏 1表示收藏
     */
    public int favorite = 0;

    protected MusicEntity(Parcel in) {
        songId = in.readLong();
        albumId = in.readInt();
        albumName = in.readString();
        albumData = in.readString();
        duration = in.readInt();
        musicName = in.readString();
        artist = in.readString();
        artistId = in.readLong();
        data = in.readString();
        folder = in.readString();
        lrc = in.readString();
        islocal = in.readByte() != 0;
        sort = in.readString();
        size = in.readInt();
        favorite = in.readInt();
    }

    public static final Creator<MusicEntity> CREATOR = new Creator<MusicEntity>() {
        @Override
        public MusicEntity createFromParcel(Parcel in) {
            return new MusicEntity(in);
        }

        @Override
        public MusicEntity[] newArray(int size) {
            return new MusicEntity[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(songId);
        dest.writeInt(albumId);
        dest.writeString(albumName);
        dest.writeString(albumData);
        dest.writeInt(duration);
        dest.writeString(musicName);
        dest.writeString(artist);
        dest.writeLong(artistId);
        dest.writeString(data);
        dest.writeString(folder);
        dest.writeString(lrc);
        dest.writeByte((byte) (islocal ? 1 : 0));
        dest.writeString(sort);
        dest.writeInt(size);
        dest.writeInt(favorite);
    }

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumData() {
        return albumData;
    }

    public void setAlbumData(String albumData) {
        this.albumData = albumData;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getMusicName() {
        return musicName;
    }

    public void setMusicName(String musicName) {
        this.musicName = musicName;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getLrc() {
        return lrc;
    }

    public void setLrc(String lrc) {
        this.lrc = lrc;
    }

    public boolean islocal() {
        return islocal;
    }

    public void setIslocal(boolean islocal) {
        this.islocal = islocal;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}