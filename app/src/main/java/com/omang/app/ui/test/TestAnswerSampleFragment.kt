package com.omang.app.ui.test

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.databinding.FragmentTestAnswerSampleBinding
import com.omang.app.ui.test.adapter.TestAnswerAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.loadLocalImage
import com.omang.app.utils.extensions.visible


class TestAnswerSampleFragment : Fragment() {

    private var _binding: FragmentTestAnswerSampleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TestViewModel by activityViewModels()

    private lateinit var adapter: TestAnswerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestAnswerSampleBinding.inflate(inflater, container, false)
        val root: View = binding.root
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    private fun bindView() {
        //FIXME: Viewpager animation goes fast when pressing next, fix me before release :)
        binding.apply {
            layout.apply {
                viewModel.questionAnsweredList.let { questionList ->
                    arguments?.getInt(POSITION)
                        ?.let { currentQuestionPosition ->
                            questionList[currentQuestionPosition].apply {
                                if (ValidationUtil.isNotNullOrEmpty(question)) {
                                    txtQuestion.text = question
                                    txtQuestion.visible()
                                }
                                if (ValidationUtil.isNotNullOrEmpty(questionUrl)) {
                                    ivQuestion.loadLocalImage(questionUrl!!)
                                }
                                adapter = TestAnswerAdapter(
                                    requireContext(),
                                    this
                                ) { questionId, optionId ->
                                    viewModel.updateTestAnswerByOptionId(questionId, optionId)
                                }
                                rvTest.apply {
                                    layoutManager = LinearLayoutManager(requireContext())
                                    adapter = this@TestAnswerSampleFragment.adapter
                                }
                            }
                        }
                }
            }
        }
    }

    companion object {

        const val POSITION = "position"

        @JvmStatic
        fun newInstance(position: Int) =
            TestAnswerSampleFragment()
                .apply {
                    arguments = bundleOf(
                        POSITION to position
                    )
                }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}