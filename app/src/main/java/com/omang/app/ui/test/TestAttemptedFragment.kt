package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.FragmentTestAttemptedBinding
import com.omang.app.ui.myClassroom.fragments.subjectContent.SubjectContentsFragment
import com.omang.app.ui.myClassroom.fragments.subjectContent.SubjectContentsFragmentDirections
import com.omang.app.ui.test.adapter.TestAttemptedListAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setCardBackGroundColor
import com.omang.app.utils.setProgressBarIndicatorColor
import com.omang.app.utils.setProgressBarTrackColor


class TestAttemptedFragment : Fragment() {
    private var _binding: FragmentTestAttemptedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTestAttemptedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    private fun bindView() {
        viewModel.setTestType(MCQType = MCQType.CLASSROOM)
        val layoutManager = GridLayoutManager(requireContext(), 2)
        binding.apply {
            rvAttempted.also {
                it.layoutManager = layoutManager
            }

            pbAverage.apply {
                tvProgress.text = "90%"
                pbMain.progress = 90
                setProgressBarIndicatorColor(pbMain, 90)
                setProgressBarTrackColor(pbOuter, 90)
                setCardBackGroundColor(progressCard, 90)
                tvBooks.text = getString(R.string.learner_average)
            }
            pbAttempted.apply {
                tvProgress.text = "10%"
                pbMain.progress = 10
                setProgressBarIndicatorColor(pbMain, 10)
                setProgressBarTrackColor(pbOuter, 10)
                setCardBackGroundColor(progressCard, 10)
                tvBooks.text = getString(R.string.attempted_tests)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        observeData()
    }

    private fun observeData() {
        viewModel.fetchAttemptedGeneralMCQs()
        viewModel.testAttemptedLiveData.observe(viewLifecycleOwner) { testList ->
            updateTestRecyclerview(testList)
        }
    }

    private fun updateTestRecyclerview(testList: List<TestEntity>?) {
        testList?.let { testEntities ->
            if (testEntities.isNotEmpty()) {
                TestAttemptedListAdapter(
                    testEntities
                ) { testId, testName ->
                    SubjectContentsFragmentDirections.actionSubjectContentsFragmentToTestResultDetailsFragment(
                        testId,
                        testName
                    ).also {
                        findNavController().navigate(it)
                    }
                }.also {
                    binding.apply {
                        rvAttempted.adapter = it
                        rvAttempted.visible()
                        txtNoResult.gone()
                    }
                }
            } else {
                binding.apply {
                    rvAttempted.gone()
                    txtNoResult.visible()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            TestAttemptedFragment().apply {

            }
    }
}