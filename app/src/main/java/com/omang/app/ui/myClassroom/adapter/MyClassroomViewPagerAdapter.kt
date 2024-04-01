package com.omang.app.ui.myClassroom.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.myClassroom.fragments.ClubsFragment
import com.omang.app.ui.myClassroom.fragments.SubjectsFragment

class MyClassroomViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                SubjectsFragment()
            }

            1 -> {
                ClubsFragment()
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }
        }
    }
}