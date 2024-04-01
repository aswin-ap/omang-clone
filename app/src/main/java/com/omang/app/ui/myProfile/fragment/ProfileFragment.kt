package com.omang.app.ui.myProfile.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.omang.app.BuildConfig
import com.omang.app.R
import com.omang.app.databinding.FragmentProfileBinding
import com.omang.app.ui.appupdate.activity.AppUpdateActivity
import com.omang.app.ui.camera.activity.CameraActivity
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myProfile.viewmodel.ProfileViewModel
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.extensions.DateTimeFormat
import com.omang.app.utils.extensions.convertTimestampToLocale
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.loadLocalEmoji
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null

    private var syncAlert: Dialog? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var launchCam: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        bindView()
        observeUserData()

        launchCam =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val data: Intent? = result.data
                    if (data != null) {
                        val imagePath = data.getStringExtra("image_path")
                        if (imagePath != null) {
                            uploadToProfileImage(imagePath.trim('[', ']'))
                        }
                    }
                } else {

                }
            }

        return root
    }

    @SuppressLint("SetTextI18n")
    private fun bindView() {
        syncAlert = Dialog(requireContext(), R.style.mDialogTheme)
        (requireActivity() as HomeActivity).configureToolbar(CurrentActivity.PROFILE)
        binding.apply {
            grpTestPassed.visibility = View.GONE
            tvAppVersion.text = "App version : ${BuildConfig.VERSION_NAME}"

            if (requireActivity().hasInternetConnection()) {
                addNewDp.setSafeOnClickListener {
                    val options = arrayOf("Capture from Camera", "Select from Gallery", "Cancel")
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle("Choose Image Source")
                    builder.setItems(options) { dialog, which ->
                        when (which) {
                            0 -> captureImageFromCamera()
                            1 -> selectImageFromGallery()
                            2 -> dialog.dismiss()
                            else -> {}
                        }
                    }
                    builder.show()
                }
            } else {
                addNewDp.invisible()
            }
        }
    }

    private fun selectImageFromGallery() {
        findNavController().navigate(R.id.uploadImageFragment2)
    }

    private fun captureImageFromCamera() {
        val intent = Intent(activity, CameraActivity::class.java)
        (activity as HomeActivity)
        intent.putExtra("profileUpload", true)
        launchCam.launch(intent)
    }

    @SuppressLint("SetTextI18n")
    private fun observeUserData() {
        viewModel.getUserData()

        with(viewModel) {
            user.observe(viewLifecycleOwner) { userData ->
                binding.apply {
                    tvUserName.text = "${userData.firstName} ${userData.lastName}"
                    tvGrade.text = userData.dropPoints.toString()
                    tvAccessionNumber.text = userData.accessionNumber.toString()
                    tvEmail.text = userData.email
                    tvSchool.text = userData.school!!.name
                    txtDropBalance.text = userData.dropPoints.toString()
                    tvRegsiteredOn.text = "${
                        userData.registeredAt?.let {
                            convertTimestampToLocale(it, DateTimeFormat.TIME_N_DATE)
                        }
                    }"
                    if (userData.avatar != null) {
                        userData.avatar.let {
                            ivProfile.loadLocalEmoji(it)
                        }
                    } else {
                        ivProfile.load(R.drawable.user_place_holder)
                    }
                }
            }

            classroomList.observe(viewLifecycleOwner) { classroomList ->
                classroomList.joinToString(",").also {
                    binding.tvClassroom.text = if (ValidationUtil.isNotNullOrEmpty(it)) it else "-"
                }
            }

            clubsList.observe(viewLifecycleOwner) { clubsList ->
                clubsList.joinToString(",").also {
                    binding.tvClub.text = if (ValidationUtil.isNotNullOrEmpty(it)) it else "-"
                }
            }

            loadingStatus.observe(viewLifecycleOwner) {
                when (it) {
                    NetworkLoadingState.LOADING -> loadingDialog(
                        syncAlert!!,
                        true,
                        requireContext()
                    )

                    NetworkLoadingState.SUCCESS -> loadingDialog(
                        syncAlert!!,
                        false,
                        requireContext()
                    )

                    NetworkLoadingState.ERROR -> loadingDialog(syncAlert!!, false, requireContext())
                }
            }

            internetSpeed.observe(viewLifecycleOwner) {
                showInternetSpeedDialog(it)
            }

            isSyncing.observe(viewLifecycleOwner) { state ->
                when (state) {
                    NetworkLoadingState.LOADING -> {
                        loadingDialog(syncAlert!!, true, requireContext())
                    }

                    NetworkLoadingState.SUCCESS -> {
                        loadingDialog(syncAlert!!, false, requireContext())
                    }

                    NetworkLoadingState.ERROR -> {
                        loadingDialog(syncAlert!!, false, requireContext())
                    }
                }
            }
        }

        binding.apply {
            btUpdate.setOnClickListener {
                if (requireContext().hasInternetConnection()) {
                    val intent = Intent(requireContext(), AppUpdateActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                } else {
                    showToast(getString(R.string.no_internet))

                }
            }

            btnSpeedCheck.setSafeOnClickListener {
                viewModel.getInternetSpeed()
            }
            btnDiaganosis.setOnClickListener {
                findNavController().navigate(R.id.action_navigation_profile_to_navigation_app_diagonosis)
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")
            ?.observe(viewLifecycleOwner) { result ->
                Timber.e("list of images $result")
                val param = result.trim('[', ']')
                uploadToProfileImage(param)
            }
    }

    private fun uploadToProfileImage(param: String) {
        viewModel.uploadNewProfilePic(param)

        viewModel.imageUpload.observe(viewLifecycleOwner) { result ->
            val avatarId = result.data.id
            viewModel.updateProfilePic(avatarId)
        }
    }

    private fun showInternetSpeedDialog(speedText: String) {

        Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_internet_speed)
            setCancelable(true)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val textView = findViewById<TextView>(R.id.tvText)
            textView.text = speedText
            val lp = WindowManager.LayoutParams()
            lp.copyFrom(window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            window!!.attributes = lp
            show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ProfileFragment().apply {

            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}