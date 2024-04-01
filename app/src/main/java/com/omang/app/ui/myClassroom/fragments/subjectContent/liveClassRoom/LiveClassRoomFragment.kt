package com.omang.app.ui.myClassroom.fragments.subjectContent.liveClassRoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.databinding.FragmentLiveClassRoomBinding
import com.omang.app.ui.myClassroom.fragments.subjectContent.liveClassRoom.model.LiveClassRoomEntity
import com.omang.app.ui.myClassroom.viewmodel.LiveClassRoomViewModel
import com.omang.app.utils.extensions.createPopUp
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.visible

class LiveClassRoomFragment : Fragment() {
    private lateinit var binding: FragmentLiveClassRoomBinding

    private lateinit var liveClassRoomAdapter: LiveClassRoomAdapter
    private val viewModel: LiveClassRoomViewModel by hiltNavGraphViewModels(R.id.navigation_classroom)

    enum class ContentStatus(val contentStatus: String) {
        RUNNING("R"), COMPLETED("C"), EXPIRED("E"), SCHEDULED("S"), ALL("")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLiveClassRoomBinding.inflate(layoutInflater, container, false)
        return binding.root.rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bindView()
      //  observer()
    }

    private fun observer() {

        with(viewModel) {
            networkStatus.observe(viewLifecycleOwner) { hasInternet ->
                handleNetworkStatusInWebPlatforms(hasInternet)
            }
        }

        val dataList = listOf(
            LiveClassRoomEntity(0, "Maths ", "", "", 0, "R"),
            LiveClassRoomEntity(1, "Sociology ", "", "", 0, "R"),
            LiveClassRoomEntity(2, "Accountancy ", "", "", 0, "C"),
            LiveClassRoomEntity(3, "Business ", "", "", 0, "C"),
            LiveClassRoomEntity(4, "English ", "", "", 0, "S"),
            LiveClassRoomEntity(5, "Arabic 1", "", "", 0, "S"),
            LiveClassRoomEntity(6, "Hindi ", "", "", 0, "E"),
            LiveClassRoomEntity(7, "Malayalam ", "", "", 0, "E"),
        )
        initRecycler(dataList)
    }

    private fun initRecycler(dataList: List<LiveClassRoomEntity>) {
        if (dataList.isNotEmpty()) {
            liveClassRoomAdapter.updateList(dataList)
            binding.apply {
                rvLiveClassrooms.adapter = liveClassRoomAdapter
                rvLiveClassrooms.visible()
                tvNoContent.gone()
            }
           // viewModel.observeNetworkStatus()
        } else {
            binding.apply {
                rvLiveClassrooms.gone()
                tvNoContent.visible()
            }
        }
    }

    private fun bindView() {

        binding.apply {

        }

        liveClassRoomAdapter = LiveClassRoomAdapter(emptyList(), onItemClick = {}, favClick = {})
        binding.rvLiveClassrooms.layoutManager = GridLayoutManager(requireActivity(), 3)
        binding.rvLiveClassrooms.adapter = liveClassRoomAdapter

    }

    private fun handleNetworkStatusInWebPlatforms(hasInternet: Boolean?) {
        hasInternet?.let {
            if (this::liveClassRoomAdapter.isInitialized)
                liveClassRoomAdapter.bindViewHolderBasedOnInternetConnection(it)
        }
    }
    companion object {
        fun newInstance(): LiveClassRoomFragment {
            return LiveClassRoomFragment()
        }
    }
}