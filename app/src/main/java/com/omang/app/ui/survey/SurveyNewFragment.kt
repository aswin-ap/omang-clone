package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.R
import com.omang.app.databinding.FragmentSurveyNewBinding
import com.omang.app.ui.survey.adapter.SurveyListAdapter
import com.omang.app.ui.survey.models.Survey
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.ToastMessage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class SurveyNewFragment : Fragment() {
    private var _binding: FragmentSurveyNewBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TestViewModel by hiltNavGraphViewModels(R.id.navigation_survey)

    @Inject
    lateinit var toast: ToastMessage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSurveyNewBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    private fun bindView() {
        Timber.d(viewModel.toString())
        val surveyList = listOf(
            Survey(
                "1",
                "Test Survey M3",
                "test survey",
                "",
                "",
                "",
                listOf(),
                "",
                0
            ), Survey(
                "1",
                "Test Survey M3",
                "test survey",
                "",
                "2023-04-14T16:10:39.000Z",
                "2023-04-14T16:10:39.000Z",
                listOf(), "", 0
            ),
            Survey(
                "1",
                "Test Survey M3",
                "test survey",
                "",
                "2023-04-14T16:10:39.000Z",
                "2023-04-14T16:10:39.000Z",
                listOf(), "", 0
            )

        )
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        _binding?.apply {
            rvSurveyNew.also {
                it.layoutManager = layoutManager
                it.adapter = (SurveyListAdapter(surveyList))
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
            SurveyNewFragment().apply {

            }
    }
}