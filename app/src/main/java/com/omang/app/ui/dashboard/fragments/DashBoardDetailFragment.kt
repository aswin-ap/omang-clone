package com.omang.app.ui.dashboard.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.omang.app.R

class DashBoardDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dash_board_detail, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            DashBoardDetailFragment().apply {

            }
    }
}