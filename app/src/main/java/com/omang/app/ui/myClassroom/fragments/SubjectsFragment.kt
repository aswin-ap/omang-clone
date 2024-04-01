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
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.databinding.FragmentMySubjectsBinding
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.myClassroom.adapter.SubjectsAdapter
import com.omang.app.ui.myClassroom.viewmodel.MyClassroomViewModel
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SubjectsFragment : Fragment() {

    private lateinit var binding: FragmentMySubjectsBinding
    private lateinit var subjectsAdapter: SubjectsAdapter
    private val viewModel: MyClassroomViewModel by hiltNavGraphViewModels(R.id.navigation_classroom)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMySubjectsBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.addToNavigation(
            event = DBConstants.Event.VISIT,
            page = SubjectsFragment::class.java.name,
            comment = "Visited my classroom page",
        )
    }

    private fun initView() {
        binding.rvClassroom.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
        }
    }

    private fun observeData() {
        viewModel.myClassroomsLiveData.observe(viewLifecycleOwner) { myClassRoomsList ->
            Timber.tag(MyWebPlatformsFragment.TAG).d("myClassRoomsList : ${myClassRoomsList.size}")
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
                        try {
                            if (!ControlNavigation.isClickRecently()) {
                                findNavController().navigate(action)
                            }
                        } catch (e: IllegalArgumentException) {
                            Timber.tag("SUBJECT FRAGMENT NAVIGATION")
                                .e("Exception Message is ${e.message}")
                        }
                        /*if (SubjectsAdapter.hasContents(subjectsItem)) {
                            val action =
                                MyClassRoomFragmentDirections.actionMyClassRoomFragment2ToSubjectContentsFragment(
                                    subjectsItem.id, subjectsItem.name
                                )
                            findNavController().navigate(action)
                        } else
                            binding.root.showSnackBar(getString(R.string.no_contents_available))*/
                    }
                binding.apply {
                    rvClassroom.adapter = subjectsAdapter
                    rvClassroom.visible()
                    tvNoContent.gone()
                }
            } else {
                binding.apply {
                    rvClassroom.gone()
                    tvNoContent.visible()
                }
            }
        }
    }
}