package com.omang.app.ui.myClassroom.fragments.subjectContent.docsAndVideos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.omang.app.R
import com.omang.app.databinding.FragmentDocsVideosContentBinding
import com.omang.app.ui.myClassroom.viewmodel.DocAndVideoViewModel
import com.omang.app.utils.ToastMessage
import com.omang.app.utils.extensions.setButtonBackground
import com.omang.app.utils.extensions.setButtonTextColor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class DocsVideosContentsFragment : Fragment() {
    @Inject
    lateinit var toastMessage: ToastMessage

    private val viewModel: DocAndVideoViewModel by viewModels()

    private var _binding: FragmentDocsVideosContentBinding? = null
    private val binding get() = _binding!!


    var classroomId = 99

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDocsVideosContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindView()
        observeData()
    }

    private fun observeData() {
        with(viewModel) {
            resourceLiveData.observe(viewLifecycleOwner) {
            }
        }
    }

    private fun bindView() {
        with(requireArguments()) {
            classroomId = getInt(CLASS_ROOM_ID)
        }
        binding.apply {
            val navHostFragment =
                childFragmentManager.findFragmentById(R.id.nav_videos_docs) as NavHostFragment
            if (navHostFragment.navController.currentDestination!!.id == R.id.subjectContentVideosFragment) {
                setEnableSelectedTab(btnVideos)
                resetTabUnSelected(btnDocs)
            } else {
                setEnableSelectedTab(btnDocs)
                resetTabUnSelected(btnVideos)
            }

            btnVideos.setOnClickListener {
                val navHostFragment =
                    childFragmentManager.findFragmentById(R.id.nav_videos_docs) as NavHostFragment
                if (navHostFragment.navController.currentDestination!!.id != R.id.subjectContentVideosFragment) {
                    setEnableSelectedTab(btnVideos)
                    resetTabUnSelected(btnDocs)
                    navHostFragment.findNavController()
                        .navigate(R.id.action_docsFragment_to_subjectContentVideosFragment)
                }


            }
            btnDocs.setOnClickListener {
                val navHostFragment =
                    childFragmentManager.findFragmentById(R.id.nav_videos_docs) as NavHostFragment
                if (navHostFragment.navController.currentDestination!!.id != R.id.docsFragment) {
                    setEnableSelectedTab(btnDocs)
                    resetTabUnSelected(btnVideos)
                    navHostFragment.findNavController()
                        .navigate(R.id.action_subjectContentVideosFragment_to_docsFragment)
                }

            }
        }
    }


    private fun setOnPageSelected(position: Int) {
        when (position) {
            0 -> updateButtonStatus(binding.btnDocs, binding.btnVideos)
            1 -> updateButtonStatus(binding.btnVideos, binding.btnDocs)
        }
    }

    private fun updateButtonStatus(selectedButton: Button, otherButton: Button) {
        selectedButton.run {
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
        otherButton.run {
            setTextColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_grey
                )
            )
            backgroundTintList =
                ContextCompat.getColorStateList(requireActivity(), R.color.light_blue)
            elevation = 0f
            bringToFront()
        }
    }

    private fun resetTabUnSelected(
        btn1: Button,
    ) {
        btn1.run {
            elevation = 0f
            buttonUnselect()
        }

    }

    private fun Button.buttonUnselect() {
        setButtonTextColor(R.color.color_grey)
        setButtonBackground(R.color.color_light_blue)
    }


    private fun setEnableSelectedTab(button: Button) {
        button.run {
            buttonSelect()
            elevation = 5f
            this.bringToFront()
        }

    }

    private fun Button.buttonSelect() {
        setButtonTextColor(R.color.white)
        setButtonBackground(R.color.deep_blue)
    }

    companion object {
        private const val CLASS_ROOM_ID = "classRoomId"

        @JvmStatic
        fun newInstance(classRoomId: Int) = DocsVideosContentsFragment().apply {
            arguments = bundleOf(
                CLASS_ROOM_ID to classRoomId
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}