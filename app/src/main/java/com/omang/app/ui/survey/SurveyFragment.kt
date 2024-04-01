package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.viewpager2.widget.ViewPager2
import com.omang.app.R
import com.omang.app.databinding.FragmentSurveyBinding
import com.omang.app.ui.test.viewmodel.TestViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

enum class SURVEY(val type: Int) {
    NEW(0), ATTEMPTED(1)
}

@AndroidEntryPoint
class SurveyFragment : Fragment() {
    private var _binding: FragmentSurveyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TestViewModel by hiltNavGraphViewModels(R.id.navigation_survey)
    //val viewModel by viewModels<SurveyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPagerChangeCallback()
        Timber.d(viewModel.toString())
    }

    private fun bindView() {
        binding.apply {
            val pagerAdapter = SurveyViewPagerAdapter(this@SurveyFragment)
            viewPagerSurvey.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            viewPagerSurvey.adapter = pagerAdapter
            viewPagerSurvey.currentItem = 0

        }

    }

    private fun registerPagerChangeCallback() {
        binding.viewPagerSurvey.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setOnPageSelected(position)

            }


        })
    }

    private fun setOnPageSelected(type: Int) {
        when (type) {
            SURVEY.NEW.type -> {
                setEnableSelectedTab(binding.btnNew)
                resetTabUnSelected(binding.btnAttempted)

            }

            SURVEY.ATTEMPTED.type -> {
                setEnableSelectedTab(binding.btnAttempted)
                resetTabUnSelected(binding.btnNew)
            }

        }
    }

    private fun resetTabUnSelected(btn1: Button) {
        btn1.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.text_content_color
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.color_silver)
        }

    }

    private fun setEnableSelectedTab(button: Button) {
        button.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_white
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
        }

    }

    companion object {


        @JvmStatic
        fun newInstance() =
            SurveyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}