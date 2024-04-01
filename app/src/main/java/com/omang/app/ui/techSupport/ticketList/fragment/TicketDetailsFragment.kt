package com.omang.app.ui.techSupport.ticketList.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.RatingBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.databinding.DialogRatingBinding
import com.omang.app.databinding.FragmentTicketDetailsBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.techSupport.ticketList.viewmodel.TicketDetailsViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.loadingDialog
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.timeInSpecificFormat
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class TicketDetailsFragment : Fragment() {

    private lateinit var binding: FragmentTicketDetailsBinding
    private var ticketsEntity: TicketsEntity? = null
    private var ratingValue: Float = 0.0f
    private val viewModel: TicketDetailsViewModel by viewModels()
    private var syncAlert: Dialog? = null
    private var dialog: Dialog? = null
    private var flag: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTicketDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ticketsEntity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("ticket_detail", TicketsEntity::class.java)
        } else {
            arguments?.getParcelable("ticket_detail")
        }
        bindView()
        observeData()
    }

    private fun observeData() {
        viewModel.isSyncing.observe(viewLifecycleOwner) { state ->
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
        viewModel.postStatus.observe(viewLifecycleOwner) {
            flag = it
            if (flag) {
                binding.btnRating.gone()
            }
        }
    }

    private fun showDialog() {
        dialog = Dialog(requireActivity())
        val dialogMainBinding = DialogRatingBinding.inflate(layoutInflater)
        dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog!!.setCancelable(true)
        dialog!!.setContentView(dialogMainBinding.root)
        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialogMainBinding.apply {
            ratingBar.onRatingBarChangeListener =
                RatingBar.OnRatingBarChangeListener { _, rating, _ ->
                    ratingValue = rating
                }
            btnSubmit.setOnClickListener {
                val comment = edComment.text.toString().trim()

                if (ratingValue > 0 && comment.isNotEmpty()) {
                    viewModel.postRating(ticketsEntity!!.id, ratingValue, comment)
                    dialog!!.dismiss()
                } else {
                    showToast("Please provide a rating and comment")
                }
            }
        }
        dialog!!.show()
    }

    private fun bindView() {
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TECH_SUPPORT,
            R.string.issue_details
        )
        //TODO : Handle the closed and reported UI
        syncAlert = Dialog(requireContext(), R.style.mDialogTheme)
        ticketsEntity?.let {
            binding.tvIssueDescription.text = it.issue
            if (it.isClosed) {
                binding.tvReportedStatus.text = " Closed"
                binding.tvRaisedOnResolved.text =  getString(R.string.raised_date, it.createdAt.timeInSpecificFormat("d MMM yyyy"))
                binding.lytDetailsResolved.visible()
            } else {
                binding.tvReportedStatus.text = " Open"
                binding.tvRaisedOnReport.text =  getString(R.string.raised_date, it.createdAt.timeInSpecificFormat("d MMM yyyy"))
                binding.lytDetailsReported.visible()
            }
            binding.btnRating.setOnClickListener {
                showDialog()
            }
        }

        binding.btnRating.visibility =
            if (ticketsEntity?.rating == null && ticketsEntity?.isClosed == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    override fun onResume() {
        super.onResume()
        if (flag) {
            binding.btnRating.invisible()
        }
    }

}

