package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.databinding.FragmentTestSubmitBinding
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.test.adapter.TestSubmitListAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.extensions.showTestCompletedDialog
import timber.log.Timber

class TestSubmitFragment : Fragment() {


    private var _binding: FragmentTestSubmitBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestSubmitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        viewModel.questionsLiveData.observe(viewLifecycleOwner) { listOfQuestionsEntity ->
            updateQuestions(listOfQuestionsEntity)
        }
    }

    private fun updateQuestions(listOfQuestionsEntity: List<QuestionEntity>?) {
        binding.apply {
            txtTestName.text = viewModel.testDetailLiveData.value?.name
            txtTestDetails.text = getString(
                R.string.no_questions,
                listOfQuestionsEntity?.size
            )
            progressBar.progress = 100
            ivTimer.visibility =
                if (viewModel.testType == MCQType.UNIT) View.GONE else View.VISIBLE
            TestSubmitListAdapter(viewModel.questionAnsweredList, onEditClick = {
                findNavController().navigate(
                    R.id.action_testSubmitFragment_to_testQuestionFragment,
                    bundleOf(
                        TestQuestionFragment.QUESTION_POSITION to it,
                        TestQuestionFragment.IS_EDIT to true
                    )
                )
            }).also {
                rvQa.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(requireContext())
                    adapter = it
                }
            }
        }
    }

    private fun bindView() {
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TEST,
            titleResourceId = R.string.title_text
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        _binding?.apply {
            btnSubmit.setOnClickListener {
                showTestCompletedDialog() {
                    viewModel.calculateResult()
                    try {
                        if (!isClickRecently()) {
                            findNavController().navigate(R.id.action_testSubmitFragment_to_testCompleteFragment)
                        }
                    } catch (e: IllegalArgumentException) {
                        Timber.tag("TEST SUBMIT NAVIGATION").e("Exception Message is ${e.message}")
                    }
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TestSubmitFragment().apply {}
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}