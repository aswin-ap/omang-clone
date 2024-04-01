package com.omang.app.ui.test

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.databinding.FragmentTestCompleteBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.fragments.TestSuccessEvent
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.extensions.floorToInt
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.setSafeOnClickListener
import org.greenrobot.eventbus.EventBus


class TestCompleteFragment : Fragment() {
    private var _binding: FragmentTestCompleteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by activityViewModels()

    private lateinit var loadingDialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestCompleteBinding.inflate(inflater, container, false)
        bindView()
        return binding.root
    }

    private fun bindView() {
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TEST,
            titleResourceId = R.string.title_text
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        viewModel.clearSync()
        loadingDialog = Dialog(requireContext(), R.style.mDialogTheme)
        binding.btnExit.setSafeOnClickListener {
            viewModel.cancelCountDown()
            viewModel.sendTests(findNavController())
        }
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        with(viewModel) {
            testResultLiveData.observe(viewLifecycleOwner) { testEntity ->
                binding.apply {
                    if (testEntity.isTestPassed) {
                        binding.txtGreetings.apply {
                            text = getString(R.string.congratulation)
                            setTextColor(
                                resources.getColor(
                                    R.color.color_shamrock_green,
                                    activity?.theme
                                )
                            )
                        }
                    } else {
                        binding.txtGreetings.apply {
                            text = getString(R.string.failed)
                            setTextColor(
                                resources.getColor(
                                    R.color.delete_red,
                                    activity?.theme
                                )
                            )
                        }
                    }
                    txtCompletedDescription.visibility =
                        if (testEntity.isTestPassed) View.VISIBLE else View.GONE
                    txtTotalQuestion.text = testEntity.totalQuestions.toString()
                    txtAttempted.text = testEntity.attemptedQuestions.toString()
                    txtCorrect.text = testEntity.correctAnswers.toString()
                    txtWrong.text = testEntity.wrongAnswers.toString()
                    txtObtained.text =
                        getString(
                            R.string.percentage,
                            testEntity.percentageObtained.floorToInt()
                        )
                    groupTime.visibility =
                        if (viewModel.testType == MCQType.UNIT) View.GONE else View.VISIBLE
                    txtTime.text = testEntity.testTime
                    txtTimeTaken.text = testEntity.totalTimeTaken
                }
            }

            testSyncing.observe(viewLifecycleOwner) { loadingState ->
                loadingState?.let {
                    when (loadingState) {
                        NetworkLoadingState.LOADING -> {
                            loadingDialog(loadingDialog, true, requireContext())
                        }

                        NetworkLoadingState.SUCCESS -> {
                            loadingDialog(loadingDialog, false, requireContext())
                            //event bus to trigger summary response after test is sent
                            EventBus.getDefault().post(TestSuccessEvent())
                            findNavController().apply {
                                popBackStack(R.id.testStartFragment, true)
                            }
                        }

                        NetworkLoadingState.ERROR -> {
                            loadingDialog(loadingDialog, false, requireContext())
                            findNavController().apply {
                                popBackStack(R.id.testStartFragment, true)
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            TestCompleteFragment().apply {

            }
    }

    override fun onStop() {
        super.onStop()
        viewModel.clearValues()
        viewModel.onCleared()

//        requireActivity().viewModelStore.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        //   viewModel.networkInspectorScope.cancel()
    }
}