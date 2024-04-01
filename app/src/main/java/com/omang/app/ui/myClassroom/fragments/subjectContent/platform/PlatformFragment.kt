package com.omang.app.ui.myClassroom.fragments.subjectContent.platform

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.omang.app.R
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.FragmentPlatformBinding
import com.omang.app.ui.explore.adapter.WebPlatformAdapter
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myClassroom.viewmodel.PlatformsViewModel
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class PlatformFragment : Fragment() {

    private lateinit var binding: FragmentPlatformBinding
    private lateinit var webPlatformAdapter: WebPlatformAdapter
    private val viewModel: PlatformsViewModel by viewModels()
    private lateinit var syncAlert: Dialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPlatformBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPlatforms()
    }

    private fun updateRecyclerview(webList: List<MyWebPlatformEntity>?) {
        webList?.let { myWebPlatformEntities ->
            if (myWebPlatformEntities.isNotEmpty()) {
                webPlatformAdapter = WebPlatformAdapter(myWebPlatformEntities,
                    onItemClick = {

//                        viewModel.getWebsiteData(it, findNavController())
                        viewModel.getIndividualWebData(it, findNavController())


                    }, favClick = { id,isFav ->

                        Toast.makeText(context, id.toString(), Toast.LENGTH_LONG).show()

                    })
                binding.apply {
                    rvMyWebPlatforms.adapter = webPlatformAdapter
                    webPlatformAdapter.hideFavButton()
                    rvMyWebPlatforms.visible()
                    tvNoContent.gone()
                }
            } else {
                binding.apply {
                    rvMyWebPlatforms.gone()
                    tvNoContent.visible()
                }
            }
        }
    }

    private fun initView() {
        syncAlert = Dialog(requireContext(), R.style.mDialogTheme)
        getClassRoomId()
        binding.rvMyWebPlatforms.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 3)
        }
    }

    private fun observeData() {
        with(viewModel) {
            platformsLiveData.observe(viewLifecycleOwner) { webList ->
                Timber.tag(MyWebPlatformsFragment.TAG).d("PlatformList : ${webList.size}")
                updateRecyclerview(webList)
            }

            individualWebResponse.observe(viewLifecycleOwner) { individualWebList ->
                Toast.makeText(context, individualWebList[0].data.url, Toast.LENGTH_LONG).show()
            }

            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(syncAlert, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(syncAlert, false, requireContext())
                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(syncAlert, false, requireContext())
                    }
                }
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is UIMessageState.StringMessage -> {
                        showToast(it.message)
                        viewModel.resetUIUpdate()
                    }

                    is UIMessageState.StringResourceMessage -> {
                        showToast(getString(it.resId))
                        viewModel.resetUIUpdate()
                    }

                    else -> {}
                }
            }
        }

    }

    private fun getClassRoomId() {
        val classRoomId = arguments?.getInt(CLASSROOM_ID)
        viewModel.setClassRoomId(classRoomId!!)
    }

    companion object {
        private const val CLASSROOM_ID = "platFormClassRoomId"
        fun newInstance(id: Int): PlatformFragment = PlatformFragment().apply {
            arguments = Bundle().apply {
                putInt(CLASSROOM_ID, id)
            }
        }
    }
}