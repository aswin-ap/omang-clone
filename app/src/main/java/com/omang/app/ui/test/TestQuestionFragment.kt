package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.omang.app.R
import com.omang.app.data.database.test.QuestionEntity
import com.omang.app.databinding.FragmentTestQuestionBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.test.adapter.TestQuestionsViewPagerAdapter
import com.omang.app.ui.test.viewmodel.CountDownStatus
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class TestQuestionFragment : Fragment() {
    private var _binding: FragmentTestQuestionBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewPagerAdapter: TestQuestionsViewPagerAdapter
    private val viewModel: TestViewModel by activityViewModels()
    private var isEdit = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestQuestionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        //selects the question position from arguments
        arguments?.let {
            it.getInt(QUESTION_POSITION).let { position ->
                lifecycleScope.launch(Dispatchers.Main) {
                    binding.viewpager.setCurrentItem(position, false)
                }
            }
            it.getBoolean(IS_EDIT).let {
                isEdit = true
                //  binding.buttonPrev.gone()
            }
        }

    }

    private fun bindView() {
        (activity as HomeActivity).hideBottomNavigation(true)
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TEST,
            titleResourceId = R.string.title_text
        )
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {}
        binding.apply {
            btnLayout.apply {
                buttonNext.setOnClickListener {
                    if (isEdit)
                        findNavController().navigate(R.id.action_testQuestionFragment_to_testSubmitFragment)
                    else
                        viewpager.currentItem += 1
                }

                /*buttonPrev.setOnClickListener {
                    viewpager.currentItem = viewpager.currentItem - 1
                }*/

                buttonSubmit.setOnClickListener {
                    findNavController().navigate(R.id.action_testQuestionFragment_to_testSubmitFragment)
                }
            }
        }
    }

    private fun observeData() {
        with(viewModel) {
            questionsLiveData.observe(viewLifecycleOwner) { listOfQuestionsEntity ->
                updateQuestions(listOfQuestionsEntity)
            }
            countdownTime.observe(viewLifecycleOwner) { countDownStatus ->
                when (countDownStatus) {
                    CountDownStatus.CountDownFinished -> {
                        showTimesUpPopUp()
                        viewModel.resetTimer()
                    }

                    is CountDownStatus.CountDownStarted -> {
                        binding.txtTimer.text = countDownStatus.time
                    }

                    else -> {}
                }
            }
        }
    }

    private fun showTimesUpPopUp() {
        val materialAlertDialogBuilder = MaterialAlertDialogBuilder(
            requireContext()
        ).setView(R.layout.times_up)
            .show()

        materialAlertDialogBuilder.findViewById<Button>(R.id.btn_submit)?.apply {
            setOnClickListener {
                materialAlertDialogBuilder.dismiss()
                findNavController().navigate(R.id.action_testQuestionFragment_to_testSubmitFragment)
            }
        }
    }

    private fun updateQuestions(listOfQuestionsEntity: List<QuestionEntity>?) {
        binding.apply {
            txtTestName.text = viewModel.testDetailLiveData.value?.name
            txtTestDetails.text = getString(
                R.string.no_questions,
                listOfQuestionsEntity?.size
            )
            txtTotalQuestions.text = listOfQuestionsEntity?.size.toString()
            progressBar.max = listOfQuestionsEntity!!.size
            ivTimer.visibility =
                if (viewModel.testType == MCQType.UNIT) View.GONE else View.VISIBLE
            viewPagerAdapter =
                TestQuestionsViewPagerAdapter(requireActivity(), listOfQuestionsEntity.size)
            viewpager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                isSaveEnabled = false
                adapter = viewPagerAdapter
                currentItem = 0
                // make it true to do enable swipe
                isUserInputEnabled = false
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        updateProgress(position, listOfQuestionsEntity.size)
                    }
                })
            }
        }
    }

    private fun updateProgress(position: Int, totalSize: Int) {
        binding.apply {
            progressBar.progress = position + 1
            txtCurrentQuestionNumber.text = "${position + 1}"
            when (position) {
                0 -> {
                    if (totalSize == 1) {
                        buttonNext.gone()
                        // buttonPrev.gone()
                        buttonSubmit.visible()
                    } else {
                        buttonNext.visible()
                        //   buttonPrev.gone()
                        buttonSubmit.gone()
                    }
                }

                totalSize - 1 -> {
                    buttonNext.gone()
                    // buttonPrev.visible()
                    buttonSubmit.visible()
                }

                else -> {
                    buttonNext.visible()
                    // buttonPrev.visible()
                    buttonSubmit.gone()
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.cancelCountDown()
        viewModel.countdownTime.removeObservers(viewLifecycleOwner)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.viewpager.adapter = null
        _binding = null

    }


    companion object {
        const val QUESTION_POSITION = "question_position"
        const val IS_EDIT = "is_on_edit_mode"

        @JvmStatic
        fun newInstance() =
            TestQuestionFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}