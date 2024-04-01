package com.omang.app.ui.myClassroom.fragments.subjectContent.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.omang.app.ui.myClassroom.fragments.subjectContent.docsAndVideos.fragments.DocsVideosContentsFragment
import com.omang.app.ui.myClassroom.fragments.subjectContent.feed.fragment.ClassroomFeedsFragment
import com.omang.app.ui.myClassroom.fragments.subjectContent.lessons.LessonsFragment
import com.omang.app.ui.myClassroom.fragments.subjectContent.platform.PlatformFragment
import com.omang.app.ui.test.TestFragment

class SubjectContentViewPagerAdapter(fragment: FragmentActivity, val classRoomId: Int) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int {
        return 5
    }

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                LessonsFragment.newInstance(classRoomId)
            }

            1 -> {
                DocsVideosContentsFragment.newInstance(classRoomId = classRoomId)
            }

            2 -> {
                PlatformFragment.newInstance(classRoomId)
            }

            3 -> {
                ClassroomFeedsFragment.newInstance(classRoomId)
            }

            4 -> {
                TestFragment.newInstance(classRoomId)
            }

            else -> {
                throw IllegalStateException("Invalid Pager")
            }
        }

    }


}