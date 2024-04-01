package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.omang.app.R
import com.omang.app.databinding.FragmentUnitTestBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.test.TestStartFragment.Companion.UNIT_ID
import com.omang.app.ui.test.TestStartFragment.Companion.UNIT_NAME
import com.omang.app.ui.test.adapter.TestViewPagerAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class UnitTestFragment : Fragment() {

    private var _binding: FragmentUnitTestBinding? = null
    private val binding get() = _binding!!

    private var unitId: Int? = null

    private val viewModel: TestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentUnitTestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        registerPagerChangeCallback()
    }

    override fun onResume() {
        super.onResume()
        //viewModel.fetchTestsFromDb(testId)
    }

    private fun bindView() {
        (activity as HomeActivity).hideBottomNavigation(false)
        arguments?.let {
            viewModel.setUnitId(it.getInt(UNIT_ID))
            viewModel.setTestType(MCQType.UNIT)
            unitId = it.getInt(UNIT_ID)
            (activity as HomeActivity).configureToolbar(
                CurrentActivity.OTHER, title = it.getString(
                    UNIT_NAME
                ) ?: ""
            )

        }
        binding.apply {
            val pagerAdapter = TestViewPagerAdapter(this@UnitTestFragment, 2, unitId)
            viewpager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                adapter = pagerAdapter
                // currentItem = 0
            }
            btnNewTest.setOnClickListener {
                viewpager.currentItem = 0
            }

            btnAttempted.setOnClickListener {
                viewpager.currentItem = 1
            }

        }

    }

    private fun registerPagerChangeCallback() {
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setOnPageSelected(position)

            }


        })
    }

    private fun setOnPageSelected(type: Int) {
        when (type) {
            TestFragment.TEST.EXPIRED.type -> {}

            TestFragment.TEST.ATTEMPTED.type -> {
                setEnableSelectedTab(binding.btnAttempted)
                resetTabUnSelected(binding.btnNewTest)
            }

            TestFragment.TEST.NEW.type -> {
                setEnableSelectedTab(binding.btnNewTest)
                resetTabUnSelected(binding.btnAttempted)

            }
        }
    }

    private fun resetTabUnSelected(btn1: Button) {
        btn1.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.card_txt_primary
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
        }
    }

    private fun setEnableSelectedTab(button: Button) {
        button.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
            elevation = 5f
            bringToFront()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}