package com.past.music.adapter

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.past.music.fragment.FolderFragment
import com.past.music.fragment.LocalAlbumFragment
import com.past.music.fragment.LocalMusicFragment
import com.past.music.fragment.LocalSingerFragment
import java.util.ArrayList

/**
 * Created by gaojin on 2017/12/7.
 */
class KtLocalFragmentAdapter constructor(fm: FragmentManager, list: List<String>) : FragmentPagerAdapter(fm) {
    private var tabNames = ArrayList<String>()

    init {
        tabNames = list as ArrayList<String>
    }

    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> return LocalMusicFragment.newInstance("", "")
            1 -> return LocalSingerFragment.newInstance()
            2 -> return LocalAlbumFragment.newInstance()
            else -> return FolderFragment.newInstance()
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tabNames[position % 4]
    }
}