package com.omang.app.ui.techSupport.ticketList.fragment


import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import com.omang.app.R
import com.omang.app.data.database.techSupport.ticketsLogs.TicketsEntity
import com.omang.app.data.database.user.User
import com.omang.app.databinding.FragmentTicketListBinding
import com.omang.app.ui.home.activity.ControlNavigation
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.NetworkLoadingState
import com.omang.app.ui.myWebPlatforms.fragment.MyWebPlatformsFragment
import com.omang.app.ui.techSupport.ticketList.adapter.TicketAdapter
import com.omang.app.ui.techSupport.ticketList.viewmodel.TicketListViewModel
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class TicketListFragment : Fragment() {

    private var _binding: FragmentTicketListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TicketListViewModel by viewModels()
    private lateinit var adapter: TicketAdapter
    private lateinit var user: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTicketListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()

    }

    override fun onResume() {
        super.onResume()
        viewModel.getTicketsFromDB()

    }

    private fun initView() {
        (activity as HomeActivity).configureToolbar(
            CurrentActivity.TECH_SUPPORT,
            R.string.tech_support
        )

        binding.apply {
            rv.apply {
                layoutManager = LinearLayoutManager(requireActivity())
                ((this.itemAnimator) as SimpleItemAnimator).supportsChangeAnimations = false

            }
        }

        binding.tvReportIssue.setOnClickListener {
            findNavController().navigate(R.id.action_ticket_listing_to_report_issue)

        }
    }

    private fun observeData() {
        viewModel.getUserData()
        viewModel.user.observe(viewLifecycleOwner) { userData ->
            binding.apply {
                user = userData
            }
        }
        viewModel.ticketsLiveData.observe(viewLifecycleOwner) { tickets ->
            Timber.tag(MyWebPlatformsFragment.TAG).d("PlatformList : ${tickets.size}")

            tickets.forEach { item ->
                Timber.d("title :  ${item.issue} ; id :  ${item.roomId}")

            }

            updateRecyclerview(tickets)
        }
        viewModel.isSyncing.observe(viewLifecycleOwner) { state ->
            when (state) {
                NetworkLoadingState.LOADING -> {
                    syncAlert(true)
                }

                NetworkLoadingState.SUCCESS -> {
                    syncAlert(false)

                }

                NetworkLoadingState.ERROR -> {
                    syncAlert(false)
                }
            }
        }
    }

    private fun updateRecyclerview(tickets: List<TicketsEntity>) {
        tickets.let {    ticketsEntities ->
            if (ticketsEntities.isNotEmpty()) {
                adapter = TicketAdapter(ticketsEntities/*.reversed()*/,
                    onItemClick = {
                        // Toast.makeText(requireContext(), "it : $it", Toast.LENGTH_SHORT).show()
                        val bundle = Bundle()
                        bundle.putParcelable("ticket_detail", it)
                        try {
                            if (!isClickRecently()) {
                                findNavController().navigate(
                                    R.id.action_techSupportFragment_to_ticketDetailsFragment,
                                    bundle
                                )
                            }
                        } catch (e: IllegalArgumentException) {
                            Timber.tag("TICKET LIST NAVIGATION")
                                .e("Exception Message is ${e.message}")
                        }
                    },
                    onChatClicked = {
                        if (requireActivity().hasInternetConnection()) {
                            val bundle = Bundle().apply {
                                putInt("roomId", it)
                                putInt("userId", user.id)
                                putString("userPic", user.avatar)
                            }
                            try {
                                if (!isClickRecently()) {
                                    findNavController().navigate(
                                        R.id.action_techSupportFragment_to_chatFragment,
                                        bundle
                                    )
                                }
                            } catch (e: IllegalArgumentException) {
                                Timber.tag("TICKET LIST NAVIGATION")
                                    .e("Exception Message is ${e.message}")
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.chat_no_internet_click),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    }
                )
                binding.apply {
                    rv.adapter = adapter
                }
                binding.rv.visible()
                binding.tvNoContent.gone()
            } else {
                binding.apply {
                    rv.gone()
                    tvNoContent.visible()
                }
            }
        }
    }

    private var syncAlert: Dialog? = null

    private fun syncAlert(flag: Boolean) {
        Timber.e("syncAlert : $flag")
        if (flag) {
            syncAlert = Dialog(requireContext())
            syncAlert!!.setContentView(R.layout.dialog_syncing)
            syncAlert!!.setCancelable(true)
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


}