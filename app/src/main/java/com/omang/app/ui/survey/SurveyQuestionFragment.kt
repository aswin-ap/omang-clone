package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.omang.app.databinding.FragmentSurveyQuestionBinding


class SurveyQuestionFragment : Fragment() {
    private var _binding: FragmentSurveyQuestionBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyQuestionBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    private fun bindView() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            SurveyQuestionFragment().apply {

            }
    }
}