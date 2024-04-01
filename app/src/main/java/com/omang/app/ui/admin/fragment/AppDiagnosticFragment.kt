package com.omang.app.ui.admin.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ViewUtils
import androidx.compose.material3.TimePicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.omang.app.R
import com.omang.app.data.repository.DataManagerRepository
import com.omang.app.databinding.FragmentAppDiagnosticBinding
import com.omang.app.ui.admin.adapter.AppDiagnosticAdapter
import com.omang.app.ui.admin.model.AppDiagnosticsData
import com.omang.app.ui.admin.model.DiagnosticsStatus
import com.omang.app.ui.admin.viewmodel.DiagnosisViewModel
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertLocaleTimestampToLocale
import com.omang.app.utils.extensions.convertTimestampToLocale
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class AppDiagnosticFragment : Fragment() {
    private var binding: FragmentAppDiagnosticBinding? = null
    private lateinit var appDiagnosticAdapter: AppDiagnosticAdapter
    private val viewModel: DiagnosisViewModel by viewModels()
    lateinit var dataManagerRepository: DataManagerRepository
    lateinit var diagonisticsDataList: MutableList<AppDiagnosticsData>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAppDiagnosticBinding.inflate(inflater, container, false)
        val root: View = binding!!.root
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.diagnosis
        )
        setView()
        setLastRun()
        observeStatusData()
        clickListeners()
        return root
    }

    @SuppressLint("SetTextI18n")
    private fun observeStatusData() {
        with(viewModel) {
            diagnosisStatus.observe(viewLifecycleOwner) { diagnosisStatus ->
                diagnosisStatus.forEach{
                    result ->
                }
                for (i in diagnosisStatus.indices) { // lol
                    Timber.v(" Status of position $i is ${diagnosisStatus[i].value}")
                    appDiagnosticAdapter.updateStatus(
                        diagnosisStatus[i].position,
                        diagnosisStatus[i].value
                    )
                }
                setLastRun()
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is UIMessageState.StringMessage -> {
                        showToast(it.message)
                    }

                    is UIMessageState.StringResourceMessage -> {
                        showToast(getString(it.resId))
                    }

                    else -> {}
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setLastRun(){
        binding?.apply {
            tvLastUpdated.text = "Last run : " + convertLocaleTimestampToLocale(
                viewModel.sharedPref.analyticsTime,
                DateTimeFormat.MONTH_N_HR
            )
        }
    }

    private fun clickListeners() {
        binding?.llRun?.setOnClickListener {
            Timber.v("Diagnosis Run Button Click")
            viewModel.sharedPref.analyticsTime = ViewUtil.getUtcTime()
            setView()
            appDiagnosticAdapter.setLoading(DiagnosticsStatus.LOADING)
            viewModel.runDiagnosis()
        }
    }

    private fun setView() {
        initialiseDiagonsis(DiagnosticsStatus.INITIAL)
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        appDiagnosticAdapter = AppDiagnosticAdapter(diagonisticsDataList)
        view
        binding?.rvAppDiagnostics.apply {
            this?.layoutManager = layoutManager
            this?.adapter = appDiagnosticAdapter
        }
    }

    private fun initialiseDiagonsis(status: DiagnosticsStatus) {
        diagonisticsDataList = mutableListOf(
            AppDiagnosticsData(
                status,
                "Safe Guard Licence",

                ),
            AppDiagnosticsData(
                status,
                "Pinning",

                ),
            AppDiagnosticsData(
                status,
                "Api Connectivity",

                ),
            AppDiagnosticsData(
                status,
                "Website Access",
            ),
            /*AppDiagnosticsData(
                DiagnosticsStatus.INITIAL,
                "NETWORK SPEED",

                ),*/
        )
    }
}