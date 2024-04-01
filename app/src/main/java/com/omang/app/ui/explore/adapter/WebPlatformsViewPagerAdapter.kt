package com.omang.app.ui.explore.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.explore.fragment.FavouritesWebPlatformsFragment
import com.omang.app.ui.explore.fragment.AllWebPlatformsFragment

class WebPlatformsViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                AllWebPlatformsFragment()
            }

            1 -> {
                FavouritesWebPlatformsFragment()
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }
        }
    }
}