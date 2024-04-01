package com.omang.app.ui.myLibrary.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.myLibrary.fragments.MyLibraryDocsFragment
import com.omang.app.ui.myLibrary.fragments.MyLibraryVideosFragment

class MyLibraryViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                MyLibraryDocsFragment()
            }

            1 -> {
                MyLibraryVideosFragment()
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }
        }
    }
}