package com.omang.app.ui.test

import android.app.Dialog
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.FragmentTestStartBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.test.adapter.setTestTime
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible


class TestStartFragment : Fragment() {
    private var _binding: FragmentTestStartBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by activityViewModels()

    private lateinit var loadingDialog: Dialog

    private var testId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            testId = it.getInt(TEST_ID)
            viewModel.setUnitId(it.getInt(UNIT_ID))
            viewModel.setClassRoomId(it.getInt(CLASSROOM_ID))
            if (it.getInt(TestViewModel.TEST_TYPE) == MCQType.UNIT.type)
                viewModel.setTestType(MCQType.UNIT)
            else
                viewModel.setTestType(MCQType.CLASSROOM)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    private fun observeData() {
        with(viewModel) {
            testDetailLiveData.observe(viewLifecycleOwner) {
                showTestDetails(it)
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

            isSyncing.observe(viewLifecycleOwner) {
                when (it) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(loadingDialog, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(loadingDialog, false, requireContext())
                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(loadingDialog, false, requireContext())
                    }
                }
            }
        }
    }

    private fun showTestDetails(it: TestEntity?) {
        it?.let { testEntity ->
            binding.apply {
                txtTestName.text = testEntity.name
                txtTestDetails.text = getString(R.string.no_questions, testEntity.questionsId.size)
                ivTimer.visibility =
                    if (viewModel.testType == MCQType.UNIT) View.GONE else View.VISIBLE
                if (viewModel.testType == MCQType.CLASSROOM)
                    setTestTime(txtTimer, testEntity.startTime, testEntity.endTime)
                //if one test remains add a warning message to user
                if (testEntity.isOneTestRemains) {
                    val builder = SpannableStringBuilder()

                    val instruction = testEntity.instructions
                    val redSpannable = SpannableString(instruction)
                    redSpannable.setSpan(
                        ForegroundColorSpan(
                            resources.getColor(
                                R.color.text_content_color,
                                activity?.theme
                            )
                        ), 0, instruction.length, 0
                    )
                    builder.append(redSpannable)

                    val warning = getString(R.string.last_test_warning)
                    val whiteSpannable = SpannableString(warning)
                    whiteSpannable.setSpan(
                        ForegroundColorSpan(
                            resources.getColor(
                                R.color.delete_red,
                                activity?.theme
                            )
                        ), 0, warning.length, 0
                    )
                    builder.append(whiteSpannable)

                    txtInstruction.setText(builder, TextView.BufferType.SPANNABLE)

                    /*  StringBuilder(testEntity.instructions).append(
                          getString(R.string.last_test_warning)
                      ).also {
                          txtInstruction.text = it.toString()
                      }*/
                } else
                    txtInstruction.text = testEntity.instructions
                tvNoContent.gone()
                groupData.visible()
            }
        } ?: run {
            binding.apply {
                tvNoContent.visible()
                groupData.gone()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTestsFromDb(testId)
        observeData()
    }

    private fun bindView() {
        loadingDialog = Dialog(requireContext(), R.style.mDialogTheme)
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.title_text)
        binding.apply {
            btnStart.setOnClickListener {
                viewModel.testDetailLiveData.value?.let { testEntity ->
                    if (testEntity.questionsId.isNotEmpty()) {
                        viewModel.startTimer()
                        viewModel.initTestTime()
                        findNavController().navigate(R.id.action_testStartFragment_to_testQuestionFragment)
                    }
                } ?: kotlin.run {
                    showToast(getString(R.string.empty_question))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val UNIT_ID = "unit_id"
        const val CLASSROOM_ID = "classRoomId"
        const val UNIT_NAME = "unit_name"
        const val TEST_ID = "test_id"
    }
}