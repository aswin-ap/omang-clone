package com.omang.app.ui.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.omang.app.databinding.FragmentDashboardBinding
import com.omang.app.ui.dashboard.viewmodel.DashboardViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null

    private val binding get() = _binding!!
    private val dashboardViewModel: DashboardViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        binding.btnDashboardDetail.setOnClickListener {
           // findNavController().navigate(R.id.action_navigation_dashboard_to_dashBoardDetailFragment)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}