package com.omang.app.ui.myClassroom.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.databinding.FragmentClubsBinding
import com.omang.app.ui.myClassroom.adapter.SubjectsAdapter
import com.omang.app.ui.myClassroom.viewmodel.MyClassroomViewModel
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class ClubsFragment : Fragment() {

    private lateinit var binding: FragmentClubsBinding
    private lateinit var subjectsAdapter: SubjectsAdapter
    private val viewModel: MyClassroomViewModel by hiltNavGraphViewModels(R.id.navigation_classroom)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentClubsBinding.inflate(layoutInflater, container, false)
        initView()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeData()
    }

    private fun initView() {
        binding.rvMyClubs.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeData() {
        viewModel.myClubsLiveData.observe(viewLifecycleOwner) { myClassRoomsList ->
            Timber.tag(MyWebPlatformsFragment.TAG).d("myClubsList : ${myClassRoomsList.size}")
            updateRecyclerview(myClassRoomsList)
        }
    }

    private fun updateRecyclerview(classRoomList: List<MyClassroomEntity>?) {
        classRoomList?.let { myClassRoomEntities ->
            if (myClassRoomEntities.isNotEmpty()) {
                subjectsAdapter =
                    SubjectsAdapter(myClassRoomEntities) { subjectsItem ->
                        val action =
                            MyClassRoomFragmentDirections.actionMyClassRoomFragment2ToSubjectContentsFragment(
                                subjectsItem.id
                            )
                        findNavController().navigate(action)
                    }
                binding.apply {
                    rvMyClubs.adapter = subjectsAdapter
                    rvMyClubs.visible()
                    tvNoContent.gone()
                    pbLoading.gone()
                }
            } else {
                binding.apply {
                    rvMyClubs.gone()
                    tvNoContent.visible()
                    pbLoading.gone()
                }
            }
        }
    }
}