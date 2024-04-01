package com.omang.app.ui.test.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.test.TestAttemptedFragment
import com.omang.app.ui.test.TestExpiredFragment
import com.omang.app.ui.test.TestNewFragment

class TestViewPagerAdapter(
    fragment: Fragment, private val count: Int,
    private val unitId: Int? = null,
) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return count
    }

    override fun createFragment(position: Int): Fragment {
        if (count == 3) {
            return when (position) {
                0 -> {
                    TestNewFragment()
                }

                1 -> {
                    TestExpiredFragment()

                }

                2 -> {
                    TestAttemptedFragment()
                }

                else -> throw IllegalArgumentException("Invalid Page")
            }
        } else {
            return when (position) {
                0 -> {
                    TestNewFragment()
                }

                1 -> {
                    TestAttemptedFragment()
                }

                else -> throw IllegalArgumentException("Invalid Page")
            }
        }

    }


}