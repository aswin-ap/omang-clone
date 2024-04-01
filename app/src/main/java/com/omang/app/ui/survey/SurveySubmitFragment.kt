package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.omang.app.databinding.FragmentSurveySubmitBinding

class SurveySubmitFragment : Fragment() {


    private var _binding: FragmentSurveySubmitBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveySubmitBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    private fun bindView() {

    }

    companion object {
        @JvmStatic
        fun newInstance() =
            SurveySubmitFragment().apply {

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}