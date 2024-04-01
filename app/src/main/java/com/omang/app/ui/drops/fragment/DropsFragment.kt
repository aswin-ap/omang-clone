package com.omang.app.ui.drops.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.omang.app.databinding.FragmentDropsBinding
import com.omang.app.ui.drops.viewmodel.DropsViewModel
import com.omang.app.ui.home.activity.HomeActivity
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class DropsFragment : Fragment() {

    private lateinit var binding: FragmentDropsBinding
    private val viewModel: DropsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDropsBinding.inflate(layoutInflater, container, false)

        initView()
        observer()

        return binding.root

    }

    private fun initView() {
        viewModel.setData()
    }

    private fun observer() {
        viewModel.apply {
            displayData.observe(viewLifecycleOwner) {
                binding.apply {
                    val name = it.firstName
                    hiTextView.text = "Hi $name"
                    tvDropPoint.text = it.dropPoints.toString()
                }
            }
        }
    }
}