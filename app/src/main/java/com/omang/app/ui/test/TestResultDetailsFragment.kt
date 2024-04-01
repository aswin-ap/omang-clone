package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.FragmentTestResultDetailsBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.test.adapter.TestSubmitListAdapter
import com.omang.app.ui.test.viewmodel.TestDetailsViewModel
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestResultDetailsFragment : Fragment() {

    private lateinit var binding: FragmentTestResultDetailsBinding
    private val viewModel: TestDetailsViewModel by viewModels()
    private val args: TestResultDetailsFragmentArgs by navArgs()
    private lateinit var testSubmitListAdapter: TestSubmitListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestResultDetailsBinding.inflate(inflater, container, false)
        initView()
        observeData()
        return binding.root
    }

    private fun initView() {
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, title = args.testName)
        testSubmitListAdapter =
            TestSubmitListAdapter(mutableListOf(), onEditClick = {}, isDetailsFragment = true)
        binding.apply {
            //setup recyclerview
            rvQuestionsAnswered.apply {
                adapter = testSubmitListAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }
            //setup chart
            piechart.apply {
                legend.apply {
                    formSize = 10f
                    form = Legend.LegendForm.CIRCLE
                    verticalAlignment = Legend.LegendVerticalAlignment.CENTER
                    horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                    orientation = Legend.LegendOrientation.VERTICAL
                    textSize = 12f
                    textColor = R.color.black
                    xEntrySpace = 5f
                    yEntrySpace = 5f
                }
                setDrawRoundedSlices(false)
                description.isEnabled = false
                transparentCircleRadius = 30f
                holeRadius = 20f
                setEntryLabelTextSize(0f)
                animateXY(1100, 1100)
            }
        }
    }

    private fun observeData() {
        with(viewModel) {
            testDetailLiveData.observe(viewLifecycleOwner) {
                showTestDetails(it)
            }

            chartDetailLiveData.observe(viewLifecycleOwner) {
                setupChartData(it)
            }

            questionsLiveData.observe(viewLifecycleOwner) { listOfQuestionsEntity ->
                updateTestRecyclerview(listOfQuestionsEntity)
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is UIMessageState.StringMessage -> {
                        showToast(it.message)
                        viewModel.resetUIUpdate()
                    }

                    is UIMessageState.StringResourceMessage -> {
                        showToast(getString(it.resId))
                        viewModel.resetUIUpdate()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun setupChartData(it: PieData?) {
        it?.let { pieData ->
            pieData.setValueTextSize(10f)
            pieData.setValueFormatter(PercentFormatter(binding.piechart))
            binding.piechart.setUsePercentValues(true)
            binding.piechart.data = pieData
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTestData(args.testId)
    }

    private fun showTestDetails(test: TestEntity?) {
        test?.let { testDetails ->
            binding.apply {
                txtTotalQuestionCount.text = testDetails.questions.size.toString()
                txtAttemptedCount.text = testDetails.attemptsCount.toString()
            }
        }
    }

    private fun updateTestRecyclerview(questionList: List<QuestionEntity>?) {
        questionList?.let { testEntities ->
            if (testEntities.isNotEmpty()) {
                testSubmitListAdapter.updateAdapter(questionList)
                binding.apply {
                    rvQuestionsAnswered.visible()
                    tvNoResult.gone()
                }
            } else {
                binding.apply {
                    rvQuestionsAnswered.gone()
                    tvNoResult.visible()
                }
            }
        }
    }
}



