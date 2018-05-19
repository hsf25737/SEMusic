package com.se.music.utils.database.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import com.se.music.utils.database.DataBaseMetaData
import com.se.music.utils.database.MusicDBHelper

/**
 *Author: gaojin
 *Time: 2018/5/19 下午10:44
 */

class SongListContentProvider : ContentProvider() {
    /**
     * 访问所有的列
     */
    val ALL = 0

    /**
     * 访问单独的列
     */
    val SINGLE = 1

    val AUTHORITIES = DataBaseMetaData.SongList.AUTHORITIES
    val TABLE_NAME = DataBaseMetaData.SongList.TABLE_NAME

    private var uriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private var dbHelper: MusicDBHelper? = null

    init {
        uriMatcher.addURI(AUTHORITIES, "/$TABLE_NAME", ALL)
        uriMatcher.addURI(AUTHORITIES, "/$TABLE_NAME/#", SINGLE)
    }

    override fun onCreate(): Boolean {
        dbHelper = MusicDBHelper.instance
        return dbHelper != null
    }

    override fun insert(uri: Uri?, values: ContentValues?): Uri {
        val db = dbHelper!!.readableDatabase
        val rowId = db.insert(TABLE_NAME, null, values)
        if (rowId > 0) {
            val rowUri = ContentUris.withAppendedId(DataBaseMetaData.SongList.CONTENT_URI, rowId)
            context.contentResolver.notifyChange(rowUri, null)
            return rowUri
        }
        throw SQLException("Failed to insert row into" + uri + "嘤嘤嘤")
    }

    override fun query(uri: Uri?, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
        val queryBuilder = SQLiteQueryBuilder()
        val db = dbHelper!!.readableDatabase
//        when (uriMatcher.match(uri)) {
//            ALL -> {
//                queryBuilder.tables = TABLE_NAME
//
//            }
//            SINGLE -> {
//
//            }
//        }
        queryBuilder.tables = TABLE_NAME
        val c = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        c.setNotificationUri(context.contentResolver, uri)
        return c
    }

    override fun update(uri: Uri?, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uri: Uri?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(uri: Uri?): String {
        return ""
    }
}