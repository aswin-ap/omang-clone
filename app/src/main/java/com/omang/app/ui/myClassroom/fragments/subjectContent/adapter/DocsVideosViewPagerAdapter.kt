package com.omang.app.ui.myClassroom.fragments.subjectContent.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.myClassroom.fragments.subjectContent.docsAndVideos.fragments.SubjectContentDocsFragment
import com.omang.app.ui.myClassroom.fragments.subjectContent.docsAndVideos.fragments.SubjectContentVideosFragment

class DocsVideosViewPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> {
                SubjectContentDocsFragment()
            }

            1 -> {
                SubjectContentVideosFragment()
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }

        }

    }


}