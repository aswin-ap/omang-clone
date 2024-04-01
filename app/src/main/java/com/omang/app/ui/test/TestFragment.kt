package com.omang.app.ui.test

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.omang.app.R
import com.omang.app.databinding.FragmentTestBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.home.fragments.TestSuccessEvent
import com.omang.app.ui.test.adapter.TestViewPagerAdapter
import com.omang.app.ui.test.viewmodel.TestViewModel
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.UIMessageState
import com.omang.app.utils.extensions.showToast
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class TestFragment : Fragment() {

    @Inject
    lateinit var toastMessage: ToastMessage

    enum class TEST(val type: Int) {
        NEW(0), EXPIRED(2), ATTEMPTED(1)
    }

    private var _binding: FragmentTestBinding? = null
    private val binding get() = _binding!!

    private lateinit var loadingDialog: Dialog

    private val testViewModel: TestViewModel by activityViewModels()

    private lateinit var pagerAdapter: TestViewPagerAdapter

    private var currentPosition = 0

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this@TestFragment)) {
            EventBus.getDefault().register(this@TestFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentTestBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initView()
        bindView()
        observeData()
        return root
    }

    private fun observeData() {
        with(testViewModel) {
            uiMessageStateLiveData.observe(viewLifecycleOwner) {
                when (it) {
                    is UIMessageState.StringMessage -> {
                        showToast(it.message)
                        testViewModel.resetUIUpdate()
                    }

                    is UIMessageState.StringResourceMessage -> {
                        showToast(getString(it.resId))
                        testViewModel.resetUIUpdate()
                    }

                    else -> {}
                }
            }

            /*  isSyncing.observe(viewLifecycleOwner) {
                  when (it) {
                      NetworkLoadingState.LOADING -> {
                          loadingDialog(loadingDialog, true, requireContext())
                      }

                      NetworkLoadingState.SUCCESS -> {
                          loadingDialog(loadingDialog, false, requireContext())
                      }

                      NetworkLoadingState.ERROR -> {
                          loadingDialog(loadingDialog, false, requireContext())
                      }
                  }
              }*/

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(testComplete: TestSuccessEvent?) {
        //after test completed, re fetches the unit and lessons
        reInitAdapter(currentPosition)
    }

    private fun reInitAdapter(position: Int) {
        binding.apply {
            viewpager.adapter = pagerAdapter
            viewpager.setCurrentItem(position, false)
        }
    }

    private fun initView() {
       /* (activity as HomeActivity).configureToolbar(
            CurrentActivity.OTHER,
            R.string.test
        )*/
        loadingDialog = Dialog(requireContext(), R.style.mDialogTheme)
        arguments?.getInt(CLASSROOM_ID)?.let { testViewModel.setClassRoomId(it) }
    }

    private fun bindView() {
        (activity as HomeActivity).hideBottomNavigation(false)
        binding.apply {
            pagerAdapter = TestViewPagerAdapter(this@TestFragment, 3)
            viewpager.orientation = ViewPager2.ORIENTATION_HORIZONTAL
            viewpager.adapter = pagerAdapter
            viewpager.currentItem = 0

            btnNewTest.setOnClickListener {
                viewpager.currentItem = 0
            }

            btnAttempted.setOnClickListener {
                viewpager.currentItem = 1
            }

            btnExpired.setOnClickListener {
                viewpager.currentItem = 2
            }

            btnSync.setOnClickListener {
                testViewModel.syncTestsFromAPI(getCurrentTestFragment(binding.viewpager.currentItem))
            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        registerPagerChangeCallback()
    }

    private fun registerPagerChangeCallback() {
        binding.viewpager.registerOnPageChangeCallback(object : OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPosition = position
                setOnPageSelected(position)

            }


        })
    }

    private fun setOnPageSelected(type: Int) {
        when (type) {
            TEST.EXPIRED.type -> {
                setEnableSelectedTab(binding.btnExpired)
                resetTabUnSelected(binding.btnNewTest, binding.btnAttempted)

            }

            TEST.ATTEMPTED.type -> {
                setEnableSelectedTab(binding.btnAttempted)
                resetTabUnSelected(binding.btnNewTest, binding.btnExpired)
            }

            TEST.NEW.type -> {
                setEnableSelectedTab(binding.btnNewTest)
                resetTabUnSelected(binding.btnExpired, binding.btnAttempted)

            }
        }
    }

    private fun resetTabUnSelected(btn1: Button, btn2: Button) {
        btn1.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.card_txt_primary
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
        }
        btn2.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.card_txt_primary
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
        }
    }

    private fun setEnableSelectedTab(button: Button) {
        button.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.white
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.deep_blue)
            elevation = 5f
            bringToFront()
        }

    }

    private fun getCurrentTestFragment(position: Int): TEST {
        return when (position) {
            0 -> TEST.NEW
            1 -> TEST.EXPIRED
            2 -> TEST.ATTEMPTED
            else -> TEST.NEW
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this@TestFragment)
    }

    companion object {
        private const val CLASSROOM_ID = "classRoomId"

        @JvmStatic
        fun newInstance(classroomId: Int) =
            TestFragment().apply {
                arguments = Bundle().apply {
                    putInt(CLASSROOM_ID, classroomId)
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}