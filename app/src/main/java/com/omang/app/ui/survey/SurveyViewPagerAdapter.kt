package com.omang.app.ui.survey

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class SurveyViewPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                SurveyNewFragment()
            }

            1 -> {
                SurveyAttemptedFragment()
            }

            else -> throw IllegalArgumentException("Invalid page")


        }

    }


}