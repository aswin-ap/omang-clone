package com.omang.app.ui.feeds.fragments

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.omang.app.R
import com.omang.app.databinding.FragmentFeedPostBinding
import com.omang.app.ui.feeds.viewmodel.FeedsViewModel
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.home.activity.ControlNavigation.safeNavigateWithArgs
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.fragments.HomeFragmentDirections
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class FeedPostFragment : Fragment() {

    private lateinit var binding: FragmentFeedPostBinding
    private val viewModel: FeedsViewModel by viewModels()
    private lateinit var dialog: Dialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFeedPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    private fun observeData() {
        with(viewModel) {
            postLiveData.observe(viewLifecycleOwner) { postData ->
                binding.apply {
                    //setting adapter to spinners
                    ArrayAdapter.createFromResource(
                        requireContext(),
                        R.array.post_to_array,
                        R.layout.custom_spinner_text
                    ).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.custom_spinner_text)
                        spinnerPost.adapter = adapter
                        spinnerPost.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {
                                    val selectedItem = parent?.getItemAtPosition(position)
                                    //shows the classroom spinner if the selected item is classroom or clubs
                                    spinnerClassroom.visibility =
                                        if (selectedItem == "Classroom or Clubs" || selectedItem == "Teachers")
                                            View.VISIBLE else View.GONE
                                    viewModel.postToValue = selectedItem.toString()
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}

                            }
                    }

                    ArrayAdapter(
                        requireContext(),
                        R.layout.custom_spinner_text,
                        postData.classRooms.map { it.key }
                    ).also { adapter ->
                        adapter.setDropDownViewResource(R.layout.custom_spinner_text)
                        spinnerClassroom.adapter = adapter
                        spinnerClassroom.onItemSelectedListener =
                            object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>?,
                                    view: View?,
                                    position: Int,
                                    id: Long
                                ) {

                                    val selectedItem = parent?.getItemAtPosition(position)
                                    viewModel.classRoomId = postData.classRooms[selectedItem]!!
                                }

                                override fun onNothingSelected(parent: AdapterView<*>?) {}

                            }
                    }

                    //Checks if the feed is opened from classroom
                    if (HomeActivity.selectedClassroomId != null) {
                        spinnerPost.setSelection(2)
                        val itemPos =
                            postData.classRooms.entries.indexOfFirst { it.value == HomeActivity.selectedClassroomId }
                        itemPos.let { spinnerClassroom.setSelection(it) }
                    }
                    //set user data
                    ivAvatar.load(postData.imageUrl)
                    tvUser.text = postData.name
                }
                getInputData()
            }

            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                it?.let {
                    when (it) {
                        is UIMessageState.StringMessage -> {
                            showToast(it.message)
                            viewModel.resetUIUpdate()

                            if (it.message == "Feed posted successfully"){
                                findNavController().popBackStack()
                            }

                        }

                        is UIMessageState.StringResourceMessage -> {
                            showToast(getString(it.resId))
                            viewModel.resetUIUpdate()
                        }

                        else -> {
                            return@let
                        }
                    }
                }
            }

            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(dialog, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(dialog, false, requireContext())
                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(dialog, false, requireContext())
                    }
                }
            }
        }

        //observes the data from image picker
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")
            ?.observe(
                viewLifecycleOwner
            ) { imageList ->
                Timber.tag("FeedUpload").d("list of images for post $imageList")
                val param = imageList.trim('[', ']')
                viewModel.selectedImageUrl = param
                binding.tvAddImage.gone()
                binding.ivPost.load(param) {
                    crossfade(true)
                }
            }
    }

    //fetches the input data from the sharedpref if exists
    private fun getInputData() {
        binding.apply {
            val savedPostTo = viewModel.sharedPref.selectedPostToItem
            val savedClassRoom = viewModel.sharedPref.selectedClassRoomItem
            val savedDescription = viewModel.sharedPref.postDescription
            if (ValidationUtil.isNotNullOrEmpty(savedPostTo)) {
                val postArray = resources.getStringArray(R.array.post_to_array)
                val itemPos = postArray.indexOf(savedPostTo)
                spinnerPost.setSelection(itemPos)
            }
            if (ValidationUtil.isNotNullOrEmpty(savedDescription)) {
                etDescription.setText(savedDescription)
            }
            val itemPos =
                viewModel.postLiveData.value?.classRooms?.entries?.indexOfFirst { it.value == savedClassRoom }
            //sets only if feed is posting from other fragments
            if (HomeActivity.selectedClassroomId == null)
                itemPos.let {
                    if (it != null) {
                        spinnerClassroom.setSelection(it)
                    }
                }
        }
    }

    private fun initView() {
        dialog = Dialog(requireContext(), R.style.mDialogTheme)
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.title_feed
        )
        binding.apply {
            etDescription.movementMethod = ScrollingMovementMethod()
            ivPost.setOnClickListener {
                viewModel.savePostData(
                    viewModel.postToValue,
                    viewModel.classRoomId,
                    viewModel.postDescription
                )
                //findNavController().navigate(R.id.action_navigation_add_to_uploadImageFragment2)
                try {
                    if (!isClickRecently()) {
                        findNavController().safeNavigateWithArgs(
                            FeedPostFragmentDirections.actionNavigationAddToUploadImageFragment2()
                        )
                    }
                } catch (e: IllegalArgumentException) {
                    Timber.tag("FEED POST UPLOAD IMAGE NAVIGATION").e("Exception Message is ${e.message}")
                }
            }
            etDescription.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.postDescription = s.toString()
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            btnPost.setOnClickListener {
                viewModel.postFeed()
            }
        }
        viewModel.getPostDetails()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //clears the current classroomId in HomeActivity static field
        HomeActivity.selectedClassroomId = null
    }


}