package com.omang.app.ui.admin.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.omang.app.R
import com.omang.app.databinding.FragmentAdminBinding
import com.omang.app.databinding.FragmentAppDiagnosticBinding
import com.omang.app.databinding.FragmentHomeBinding
import com.omang.app.ui.admin.viewmodel.DiagnosisViewModel
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.utils.csdk.CSDKConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class AdminFragment : Fragment() {

    private var binding: FragmentAdminBinding? = null
    private val viewModel: DiagnosisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        val root: View = binding!!.root
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.admin_page
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    private fun bindView() {
        binding?.apply {
            llUnpin.setOnClickListener {
                (activity as HomeActivity).unPinDevice()
            }
        }

        binding?.apply {
            tvLog.text = viewModel.sharedPref.logs
        }
    }

}