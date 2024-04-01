package com.omang.app.ui.survey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.databinding.FragmentSurveyAttemptedBinding
import com.omang.app.ui.survey.adapter.SurveyListAdapter
import com.omang.app.ui.survey.models.Survey
import com.omang.app.ui.survey.survey.SurveyViewModel
import com.omang.app.utils.ToastMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class SurveyAttemptedFragment : Fragment() {
    @Inject
    lateinit var toast: ToastMessage
    private var _binding: FragmentSurveyAttemptedBinding? = null
    private val sharedViewModel by viewModels<SurveyViewModel>(
        ownerProducer = { requireParentFragment() }
    )
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
        _binding = FragmentSurveyAttemptedBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        return root
    }

    private fun bindView() {
        sharedViewModel.setValue(2)
        sharedViewModel.userList.observe(viewLifecycleOwner) {
            toast.showToast("Attemted $it")
        }
        val surveyList = listOf(
            Survey(
                "1",
                "Test Survey M3",
                "test survey",
                "",
                "",
                "",
                listOf(), "", 0
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
            rvSurveyAttempted.also {
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
            SurveyAttemptedFragment().apply {

            }
    }
}