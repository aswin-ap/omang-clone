package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.omang.app.R


class SurveyDetailFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_survey_detail, container, false)
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            SurveyDetailFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}