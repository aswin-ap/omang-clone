package com.omang.app.ui.techSupport.reportIssue

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.omang.app.R
import com.omang.app.databinding.FragmentReportIssueBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.techSupport.ticketList.viewmodel.TicketListViewModel
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.hideKeyBoard
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
class ReportIssue : Fragment() {

    private var imageList = mutableListOf<String>()

    private var _binding: FragmentReportIssueBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TicketListViewModel by viewModels()

    @Inject
    lateinit var toast: ToastMessage

    private lateinit var dialog: Dialog

    private var emailData: String? = null
    private var phoneNumberData: String? = null
    private var issueData: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentReportIssueBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        observeData()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", emailData)
        outState.putString("phoneNumber", phoneNumberData)
        outState.putString("issue", issueData)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()

        if (savedInstanceState != null) {
            emailData = savedInstanceState.getString("email")
            phoneNumberData = savedInstanceState.getString("phoneNumber")
            issueData = savedInstanceState.getString("issue")
        }

        binding.apply {

            tvReportIssue.setSafeOnClickListener {
                if (etIssue.text.isNullOrEmpty()) {
                    toast.showToast("Please explain the issue..")

                } else if (etEmail.text.isNullOrEmpty()) {
                    toast.showToast("Please fill up the E-Mail field")

                } else if (etPhone.text.isNullOrEmpty()) {
                    toast.showToast("Please fill up the Phone number field")

                } else {
                    viewModel.saveIssue(
                        requireActivity().filesDir,
                        if (imageList.isNotEmpty()) imageList[0] else null,
                        email = etEmail.text.toString()!!,
                        issue = etIssue.text.toString()!!,
                        phone = etPhone.text.toString()!!,
                        hasInternetConnection = requireActivity().hasInternetConnection()
                    )
                }
            }

            root.setOnClickListener {
                requireActivity().hideKeyBoard()
            }

            findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("key")
                ?.observe(viewLifecycleOwner) { result ->
                    Timber.e("list of images $result")
                    imageList = result.parseStringList()
                    initImageView(imageList)
                    removeImage(imageList)
                }
        }
    }

    private fun removeImage(imageList: MutableList<String>) {
        if (imageList.isNotEmpty()) {
            binding.apply {
                ivDelete.setOnClickListener{
                    imageList.removeAt(0)
                    ivPhotoPreview.setImageDrawable(null)
                    ivDelete.invisible()
                }
            }

        }
    }


    private fun initImageView(result: MutableList<String>) {

        if (result.isNotEmpty()) {
            binding.ivPhotoPreview.load(result[0])
            binding.ivDelete.visible()
        }
    }

    private fun initView() {
        dialog = Dialog(requireContext(), R.style.mDialogTheme)
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TECH_SUPPORT,
            R.string.report_issue_title
        )

        binding.apply {

            if (emailData != null) {

                etEmail.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        emailData = s.toString()
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        emailData = s.toString()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        emailData = s.toString()
                    }
                })
            }

            if (phoneNumberData != null) {

                etPhone.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        phoneNumberData = s.toString()
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        phoneNumberData = s.toString()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        phoneNumberData = s.toString()
                    }
                })
            }

            if (issueData != null) {
                etIssue.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {
                        issueData = s.toString()
                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {
                        issueData = s.toString()
                    }

                    override fun afterTextChanged(s: Editable?) {
                        issueData = s.toString()
                    }
                })
            }

            btnSubmit.setOnClickListener {
                findNavController().navigate(R.id.uploadImageFragment)
            }
        }

    }

    private fun observeData() {
        viewModel.getUserData()

        viewModel.issueStatus.observe(viewLifecycleOwner) {
            onBackToTicketListing()
        }
        viewModel.user.observe(viewLifecycleOwner) { userData ->
            binding.apply {
                tvWelcome.text = Editable.Factory.getInstance()
                    .newEditable(getString(R.string.hi_user, userData.firstName))
                if (userData.email?.isNotEmpty() == true) {
                    emailData = userData.email
                    etEmail.text = Editable.Factory.getInstance().newEditable(userData.email)
                } else {
                    if (emailData != null) {
                        etEmail.text = Editable.Factory.getInstance().newEditable(emailData)
                    }
                }

                if (userData.phone?.isNotEmpty() == true) {
                    phoneNumberData = userData.phone
                    etPhone.text = Editable.Factory.getInstance().newEditable(userData.phone)
                } else {
                    if (phoneNumberData != null) {
                        etPhone.text = Editable.Factory.getInstance().newEditable(phoneNumberData)
                    }
                }

            }
        }
        viewModel.isSyncing.observe(viewLifecycleOwner) { state ->
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

        viewModel.imageUpload.observe(viewLifecycleOwner) { ticket ->
            var ticketNumber = ticket.id
            showToast(ticketNumber.toString())
        }
    }

    private fun onBackToTicketListing() {
        syncAlert?.dismiss()

        findNavController().apply {
            popBackStack(R.id.techSupportFragment, false)
        }
    }

    private var syncAlert: Dialog? = null

    private fun syncAlert(flag: Boolean) {
        Timber.e("syncAlert : $flag")
        if (flag) {
            syncAlert?.dismiss()
            syncAlert = Dialog(requireContext())
            syncAlert!!.setContentView(R.layout.dialog_syncing)
            syncAlert!!.setCancelable(false)

            syncAlert!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val lp = WindowManager.LayoutParams()
            lp.copyFrom(syncAlert!!.window!!.attributes)
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
            syncAlert!!.window!!.attributes = lp

            syncAlert!!.show()
        } else {
            syncAlert?.dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        syncAlert?.dismiss()
    }

    fun String.parseStringList(): MutableList<String> {
        return this
            .trim('[', ']')
            .split(",")
            .map { it.trim() }
            .toMutableList()
    }

}