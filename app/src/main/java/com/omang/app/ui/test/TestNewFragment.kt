package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.FragmentTestNewBinding
import com.omang.app.ui.test.TestStartFragment.Companion.TEST_ID
import com.omang.app.ui.test.adapter.TestNewListAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.visible
import com.omang.app.utils.setCardBackGroundColor
import com.omang.app.utils.setProgressBarIndicatorColor
import com.omang.app.utils.setProgressBarTrackColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TestNewFragment : Fragment() {
    private var _binding: FragmentTestNewBinding? = null
    private val viewModel: TestViewModel by activityViewModels()
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchNewGeneralMCQs()
    }

    private fun bindView() {
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        binding.apply {
            rvNewTest.also {
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

    private fun observeData() {
        viewModel.testNewLiveData.observe(viewLifecycleOwner) { testList ->
            updateTestRecyclerview(testList)
        }
    }

    private fun updateTestRecyclerview(testList: List<TestEntity>?) {
        testList?.let { testEntities ->
            if (testEntities.isNotEmpty()) {
                TestNewListAdapter(
                    requireContext(),
                    testEntities
                ) { testId ->
//                    Timber.d("parentFragment ${parentFragment?.findNavController()?.currentDestination}")
                    findNavController().navigate(
                        R.id.action_subjectContentsFragment_to_unitTestFragment, bundleOf(
                            TestViewModel.TEST_TYPE to MCQType.CLASSROOM.type,
                            TEST_ID to testId,
                            //  UnitTestFragment.UNIT_NAME to name
                        )
                    )
                }.also {
                    binding.apply {
                        rvNewTest.adapter = it
                        rvNewTest.visible()
                        txtNoResult.gone()
                    }
                }
            } else {
                binding.apply {
                    rvNewTest.gone()
                    txtNoResult.visible()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//class to distinguish between UNIT and GENERAL mcq
enum class MCQType(val type: Int) {
    UNIT(0), CLASSROOM(1)
}