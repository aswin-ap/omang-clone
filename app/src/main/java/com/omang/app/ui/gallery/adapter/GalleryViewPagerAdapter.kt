package com.omang.app.ui.gallery.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.gallery.fragments.DocumentsFragment
import com.omang.app.ui.gallery.fragments.PhotosFragment
import com.omang.app.ui.gallery.fragments.VideosFragment

class GalleryViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                PhotosFragment()
            }

            1 -> {
                VideosFragment()
            }

            2 -> {
                DocumentsFragment()
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }
        }
    }
}