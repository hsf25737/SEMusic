package com.se.music.fragment

import android.support.v4.app.Fragment
import com.se.music.activity.BaseActivity
import com.se.music.activity.MusicStateListener

/**
 * Created by gaojin on 2018/3/6.
 */
open class BaseFragment : Fragment(), MusicStateListener {

    override fun updatePlayInfo() {
    }

    override fun updateTime() {
    }

    override fun reloadAdapter() {
    }


    override fun onResume() {
        super.onResume()
        (activity as BaseActivity).setMusicStateListenerListener(this)
        reloadAdapter()
    }

    override fun onStop() {
        super.onStop()
        (activity as BaseActivity).removeMusicStateListenerListener(this)
    }
}