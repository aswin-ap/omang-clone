package com.omang.app.ui.pdfViewer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.dataAnalytics.AnalyticsEntity
import com.omang.app.databinding.FragmentPdfViewerBinding
import com.omang.app.ui.home.activity.CurrentActivity
import com.omang.app.ui.home.activity.HomeActivity
import com.omang.app.ui.pdfViewer.viewmodel.PdfStatus
import com.omang.app.ui.pdfViewer.viewmodel.PdfViewerViewModel
import com.omang.app.utils.ValidationUtil
import com.omang.app.utils.ViewUtil
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.showToast
import com.omang.app.utils.extensions.visible
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class DocumentViewerFragment : Fragment() {

    private lateinit var binding: FragmentPdfViewerBinding
    private val viewModel: PdfViewerViewModel by viewModels()
    private val args: DocumentViewerFragmentArgs by navArgs()

    private lateinit var dAE: AnalyticsEntity

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPdfViewerBinding.inflate(layoutInflater)

        dAE = AnalyticsEntity(
            resourceId = args.resourceId,
            lessonId = if (args.lessonId != 0) args.lessonId else null,
            classroomId = if (args.classroomId != 0) args.classroomId else null,
            unitId = if (args.unitId != 0) args.unitId else null,
            menu = args.menu
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        observeData()
    }

    override fun onResume() {
        super.onResume()
        if (args.menu == DBConstants.AnalyticsMenu.GALLERY.value) {
            viewModel.loadPdf(binding.pdfView, File(args.file))
        } else {
            viewModel.fetchFile(fetchFileUrl = args.file)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!dAE.startTime.isNullOrEmpty()) { // making sure that the data inserting has star-time with it
            dAE.endTime = ViewUtil.getUtcTimeWithMSec()
            viewModel.insertWatchData(dAE)
        }
    }

    private fun observeData() {
        with(viewModel) {
            uiState.observe(viewLifecycleOwner) {
                when (it) {
                    is PdfStatus.OnError -> {
                        showToast(it.error.message.toString())
                        showErrorUI()
                    }

                    is PdfStatus.Initialized -> {
                        showPdf()
                        loadPdf(binding.pdfView, it.file)
                    }

                    is PdfStatus.Encrypt -> {
                        showProgressUI(it.progress)
                    }

                    is PdfStatus.Loaded -> {
                        dAE.startTime = ViewUtil.getUtcTimeWithMSec()

                    }
                }
            }
        }
    }

    private fun initView() {
        (requireActivity() as HomeActivity).configureToolbar(
            CurrentActivity.PDF,
            0, "PDF"
        )
        binding.apply {
            btnGotopage.setOnClickListener {
                showPageNumberDialog { pageNum ->
                    if(pageNum != null) {
                        if (ValidationUtil.isNotNullOrEmpty(pageNum)) {
                            updatePage(pageNum.toInt())
                        } else
                            showToast("Please enter page number")
                    }
                }
            }
            pdfView.setOnClickListener {
                viewModel.isFullScreen = !viewModel.isFullScreen
                if (viewModel.isFullScreen != null) {
                    (activity as HomeActivity).setDeviceFullScreen(viewModel.isFullScreen)
                }
                actionBarView.visibility = if (viewModel.isFullScreen) View.GONE
                else View.VISIBLE
            }
        }
    }

    private fun showPdf() {
        binding.apply {
            pdfView.visible()
            tvError.gone()
        }
    }

    private fun showErrorUI() {
        binding.apply {
            pdfView.gone()
            tvError.visible()
            tvError.text = getString(R.string.file_loading_error)
        }
    }

    private fun showProgressUI(progress: Int) {
        binding.apply {
            pdfView.gone()
            tvError.visible()
            tvError.text = if (progress == 0) getString(R.string.loading_inprogress)
            else getString(R.string.downloading_status, progress)
        }
    }

    private fun updatePage(num: Int?) {
        num?.let { number ->
            try {
                binding.pdfView.jumpTo(
                    number - 1
                )
            } catch (e: Exception) {
                Timber.tag(TAG).d("Exception ${e.message.toString()}")
            }
        }
    }

    companion object {
        private val TAG = this::class.java.simpleName
        fun DocumentViewerFragment.showPageNumberDialog(
            onOkListener: (String?) -> Unit,
        ) {
            val view = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_gotopage, binding.root, false)
            val goToDialog = MaterialAlertDialogBuilder(requireContext())
                .setView(view)
                .show()

            val etNumber = view.findViewById<TextInputEditText>(R.id.et_page_number)
            val btnOk = view.findViewById<Button>(R.id.btn_ok)
            btnOk.setOnClickListener {
                onOkListener(etNumber.text.toString())
                goToDialog.dismiss()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        (activity as HomeActivity).setDeviceFullScreen(false)
    }
}