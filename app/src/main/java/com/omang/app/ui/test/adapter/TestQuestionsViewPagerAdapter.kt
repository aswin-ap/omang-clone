package com.omang.app.ui.test.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.test.TestAnswerSampleFragment

class TestQuestionsViewPagerAdapter(fragment: FragmentActivity, val count: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment =
        com.omang.app.ui.test.TestAnswerSampleFragment.newInstance(position)


}