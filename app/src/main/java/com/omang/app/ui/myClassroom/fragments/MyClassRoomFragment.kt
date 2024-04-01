package com.omang.app.ui.myClassroom.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.omang.app.R
import com.omang.app.databinding.FragmentMyClassRoomBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.adapter.MyClassroomViewPagerAdapter
import com.omang.app.ui.myClassroom.viewmodel.MyClassroomViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.toJson
import com.omang.app.utils.extensions.visible
import timber.log.Timber

class MyClassRoomFragment : Fragment() {

    private lateinit var binding: FragmentMyClassRoomBinding
    private lateinit var viewPagerAdapter: MyClassroomViewPagerAdapter
    private val viewModel: MyClassroomViewModel by activityViewModels()
    private lateinit var loadingDialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentMyClassRoomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
    }

    override fun onResume() {
        super.onResume()
        //observeData()
    }

    private fun observeData() {
        with(viewModel) {
            offlineTestDataList.observe(viewLifecycleOwner) {
                offlineTestDataList
                Timber.tag("Classroom Test")
                    .d("Attempted Offline Test List are: ${offlineTestDataList.value?.toJson()}")
                binding.apply {
                    if (requireContext().hasInternetConnection() && offlineTestDataList.value?.toJson()?.length!! > 0)
                        btnSendTest.visible()
                    else
                        btnSendTest.gone()
                }

            }
            testSyncing.observe(viewLifecycleOwner) { loadingState ->
                loadingState?.let {
                    when (loadingState) {
                        NetworkLoadingState.LOADING -> {
                            loadingDialog(loadingDialog, true, requireContext())
                        }

                        NetworkLoadingState.SUCCESS -> {
                            binding.btnSendTest.gone()
                            loadingDialog(loadingDialog, false, requireContext())

                        }

                        NetworkLoadingState.ERROR -> {
                            loadingDialog(loadingDialog, false, requireContext())
                        }
                    }
                }
            }
        }
    }

    private fun bindView() {
        (activity as HomeActivity).configureToolbar(CurrentActivity.OTHER, R.string.my_classroom)
        loadingDialog = Dialog(requireContext(), R.style.mDialogTheme)
        viewPagerAdapter = MyClassroomViewPagerAdapter(this@MyClassRoomFragment)
        binding.apply {
            viewpager.apply {
                orientation = ViewPager2.ORIENTATION_HORIZONTAL
                adapter = viewPagerAdapter
                currentItem = 0
                offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        setOnPageSelected(position)
                    }
                })
            }
            btnMyClassRoom.setOnClickListener {
                it.bringToFront()
                viewpager.currentItem = 0
            }
            btnMyClubs.setOnClickListener {
                it.bringToFront()
                viewpager.currentItem = 1
            }
            /*viewModel.getTestOfflineStatus()
            btnSendTest.setOnClickListener {
                viewModel.sendTestResultsToServer()
            }*/
        }

    }

    //updates the selected buttons
    private fun setOnPageSelected(position: Int) {
        when (position) {
            0 -> classroomSelected()
            1 -> clubSelected()
        }
    }

    private fun classroomSelected() {
        binding.apply {
            btnMyClassRoom.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_true)
                elevation = 5f
            }

            btnMyClubs.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_false)
                elevation = 0f

            }
        }
    }

    private fun clubSelected() {
        binding.apply {
            btnMyClubs.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.white
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_true)
                elevation = 5f

            }

            btnMyClassRoom.run {
                setTextColor(
                    ContextCompat.getColor(
                        requireActivity(),
                        R.color.black
                    )
                )
                backgroundTintList =
                    ContextCompat.getColorStateList(requireActivity(), R.color.bt_selector_false)
                elevation = 0f

            }
        }
    }

}