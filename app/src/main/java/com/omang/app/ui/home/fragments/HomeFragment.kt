package com.omang.app.ui.home.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.FragmentHomeBinding
import com.omang.app.ui.home.activity.ControlNavigation.isClickRecently
import com.omang.app.ui.home.activity.ControlNavigation.safeNavigateWithArgs
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.viewmodel.HomeViewModel
import com.omang.app.ui.home.viewmodel.LastUpdatedData
import com.omang.app.utils.extensions.loadLocalImage
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.updateAlpha
import com.omang.app.utils.setLastSyncedOn
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import timber.log.Timber

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        bindView()
        observe()
        // classroomBubble()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@HomeFragment)) {
            EventBus.getDefault().register(this@HomeFragment)
        }
    }

    private fun observe() {
        /*homeViewModel.bubble.observe(viewLifecycleOwner) {
            Timber.e("classroom bubble : ${BubbleData.classroomList}")
            //TODO : remove the bell logic in future
            classroomBubble()

        }*/
        //homeViewModel.startListeningInternetStatus()
        with(homeViewModel) {
            lastUpdatedLiveData.observe(viewLifecycleOwner) { lastUpdatedData ->
                setLastUpdatedDates(lastUpdatedData!!)
            }

            /*networkStatus.observe(viewLifecycleOwner) { hasInternet ->
                updateInternetStatus(hasInternet)
            }*/
            isConnectionStatus.observe(viewLifecycleOwner){ hasConnection ->
                    updateInternetStatus(hasConnection.networkStatus)
            }

            doeLiveData.observe(viewLifecycleOwner) { doeEntity ->
                updateDoe(doeEntity)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(testComplete: TestSuccessEvent?) {
        Timber.tag("Test").d("Summary called")
        homeViewModel.getSummaryData()
    }

    private fun updateDoe(doeItem: MyWebPlatformEntity?) {
        doeItem?.let { doe ->
            binding.apply {
                txtMyDoe.text = doe.name
                imvDoe.loadLocalImage(doe.logo)
                cvDoe.setOnClickListener {
                    if (!isClickRecently()) {
                        homeViewModel.getDoeDetails(doe.id, findNavController())
                    }
                }
            }
        }
    }

    // update internet status of cards
    private fun updateInternetStatus(hasNetwork: Boolean) {
        binding.apply {
            if (hasNetwork) {
                cvExplore.updateAlpha(1.0f)
                cvMyWebPlatforms.updateAlpha(1.0f)
                cvDoe.updateAlpha(1.0f)
            } else {
                cvExplore.updateAlpha(0.5f)
                cvMyWebPlatforms.updateAlpha(0.5f)
                cvDoe.updateAlpha(0.5f)
                cvDoe.foreground = null
            }
        }
    }

    //updates the last synced date on each card
    private fun setLastUpdatedDates(lastUpdatedData: LastUpdatedData) {
        binding.apply {
            setLastSyncedOn(tvExploreLastUpdated, lastUpdatedData.exploreDate)
            setLastSyncedOn(tvMyLibraryLastUpdated, lastUpdatedData.myLibraryDate)
            setLastSyncedOn(tvMyWebPlatforms, lastUpdatedData.myWebPlatformDate)
            /*
             setLastUpdatedDate(lastUpdatedData.exploreDate)
             tvMyLibraryLastUpdated setLastUpdatedDate(lastUpdatedData.myLibraryDate)
             tvMyWebPlatforms setLastUpdatedDate(lastUpdatedData.myWebPlatformDate)
             */
        }
    }

    private fun bindView() {
        // homeViewModel.checkInternetSpeed()
        homeViewModel.getAvailableNetworkStatus(requireContext())
        binding.apply {
            cvMyClassroom.setOnClickListener {
                //findNavController().navigate(R.id.action_navigation_home_to_navigation_classroom)
                if (findNavController().currentDestination?.id == R.id.navigation_home) {
                   findNavController().safeNavigateWithArgs(
                        HomeFragmentDirections.actionNavigationHomeToNavigationClassroom()

                    )
                }
            }
            cvMyGallery.setSafeOnClickListener{
                // findNavController().navigate(R.id.action_navigation_home_to_galleryFragment)
                if (findNavController().currentDestination?.id == R.id.navigation_home) {
                    findNavController().safeNavigateWithArgs(
                        HomeFragmentDirections.actionNavigationHomeToGalleryFragment()
                    )
                }
            }
            cvExplore.setSafeOnClickListener {
                // findNavController().navigate(R.id.action_navigation_home_to_navigation_web_platform)
                if (findNavController().currentDestination?.id == R.id.navigation_home) {
                    findNavController().safeNavigateWithArgs(
                        HomeFragmentDirections.actionNavigationHomeToNavigationWebPlatform()

                    )
                }
            }
            cvMyWebPlatforms.setSafeOnClickListener {
                    if (findNavController().currentDestination?.id == R.id.navigation_home) {
                        findNavController().safeNavigateWithArgs(
                            HomeFragmentDirections.actionNavigationHomeToMyWebPlatformsFragment()
                        )
                }
               //   findNavController().navigate(R.id.action_navigation_home_to_myWebPlatformsFragment)
            }
            cvMyLibrary.setSafeOnClickListener{
                //  findNavController().navigate(R.id.action_navigation_home_to_navigation_my_library)
                if (findNavController().currentDestination?.id == R.id.navigation_home) {
                    findNavController().safeNavigateWithArgs(
                        HomeFragmentDirections.actionNavigationHomeToNavigationMyLibrary()
                    )
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        (activity as HomeActivity).configureToolbar(CurrentActivity.HOME)
        with(homeViewModel) {
            addToNavigation(
                event = DBConstants.Event.VISIT,
                page = HomeFragment::class.java.name,
                comment = "Visited Home page",
            )
            fetchLastUpdatedContentDates()
            getDeviceUpdates()
            getDoeDetails()
//            sendTestResults()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@HomeFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class TestSuccessEvent {}