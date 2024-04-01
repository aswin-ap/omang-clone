package com.omang.app.ui.myClassroom.fragments.subjectContent.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.R
import com.omang.app.data.database.DBConstants
import com.omang.app.data.database.myClassroom.unit.UnitEntity
import com.omang.app.data.database.myClassroom.unit.lessons.LessonsEntity
import com.omang.app.data.model.unitWithLessons.UnitWithLessons
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.databinding.ExpandableLessonsHeaderBinding
import com.omang.app.databinding.ItemLessonLayoutBinding
import com.omang.app.ui.myClassroom.viewmodel.DocId
import com.omang.app.ui.myClassroom.viewmodel.Progress
import com.omang.app.utils.extensions.expand
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.setSafeOnClickListener
import com.omang.app.utils.extensions.toJson
import com.omang.app.utils.extensions.visible
import timber.log.Timber

enum class TYPE {
    PARENT, CHILD
}

class LessonsHeaderAdapter(
    private val context: Context,
    private val itemList: List<UnitWithLessons>,
    private val onBulkDownload: (id: DocId, url: List<LessonsEntity>, totalDownloadedItems: Int) -> Unit,
    private val onIndividualDownload: (id: Int, url: String, downloadedFiles: Int) -> Unit,
    private val onItemClick: (item: LessonsEntity, unitId: Int) -> Unit,
    private val onWeblinkClick: (lessonId: Int) -> Unit,
    private val onTestClick: (unitId: Int, unitName: String) -> Unit,
    private val isLessonExpanded: (isExpanded: Boolean) -> Unit,
    private val onRatingClick: (lessonId: Int) -> Unit,

    ) : RecyclerView.Adapter<LessonsHeaderAdapter.HeadHolder>() {
    private var filterDocuments: MutableList<UnitWithLessons> = ArrayList()
    private var lessonList: List<LessonsEntity> = ArrayList()

    // private lateinit var itemAdapter: LessonsInnerAdapter

    init {
        filterDocuments = itemList.toMutableList()
    }

    inner class HeadHolder(val binding: ExpandableLessonsHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        /* val itemAdapter = LessonsInnerAdapter(emptyList(), onDownloadClicked = { id, url ->
             onIndividualDownload(id, url)
         }, onItemClicked = { item ->
             onItemClick(item)
         }, onWeblinkClicked = {
             onWeblinkClick(it)
         })*/

        init {
            binding.pbDownload.max = 100
            binding.tvUnitName.movementMethod = ScrollingMovementMethod()
            binding.tvUnitDescription.movementMethod = ScrollingMovementMethod()
            /*binding.rvLessons.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = itemAdapter
            }*/
        }



        @SuppressLint("ClickableViewAccessibility")
        fun bindItem(unit: UnitEntity, itemList: List<LessonsEntity>) {
            binding.tvItem.text = unit.name
            binding.tvItem.tooltipText = unit.name
            binding.tvHeading.text = unit.name
            binding.tvUnitName.originalText = "Unit Objective : ${unit.objective}"
            binding.tvUnitDescription.originalText = "Unit Description : ${unit.description}"
            binding.tvCompleted.text = /*"olake ntemood"*/context.getString(
                R.string.download_completed_count,
                unit.downloadedFiles, unit.totalFiles
            )
            binding.lytChild.visibility = if (unit.isExpanded) View.VISIBLE else View.GONE
            when (unit.isTestPassed) {
                DBConstants.TestStatus.NOT_ATTEMPTED -> {
                    binding.ivTest.setImageResource(
                        R.drawable.ic_take_test
                    )
                    ImageViewCompat.setImageTintList(binding.ivTest, ColorStateList.valueOf(
                        context.resources.getColor(R.color.deep_blue)
                    ))
                }

                DBConstants.TestStatus.PASSED -> {
                    binding.ivTest.setImageResource(
                        R.drawable.ic_download_completed
                    )
                    ImageViewCompat.setImageTintList(binding.ivTest, ColorStateList.valueOf(
                        context.resources.getColor(R.color.color_shamrock_green)
                    ))
                }

                DBConstants.TestStatus.FAILED -> {
                    binding.ivTest.setImageResource(
                        R.drawable.ic_clear
                    )
                    ImageViewCompat.setImageTintList(binding.ivTest, ColorStateList.valueOf(
                        context.resources.getColor(R.color.delete_red)
                    ))
                }
            }
            binding.downArrow.animate().setDuration(200).rotation(
                if (unit.isExpanded) 90f else 0f
            ).start()

            /* binding.rvLessons.apply {
                 val childAdapter = adapter as LessonsInnerAdapter
                 childAdapter.updateList(itemList)
             }*/
            val itemAdapter = LessonsInnerAdapter(itemList, onDownloadClicked = { id, url ->
                onIndividualDownload(id, url, unit.downloadedFiles)
            }, onItemClicked = { item ->
                onItemClick(item, unit.id)
            }, onWeblinkClicked = {
                onWeblinkClick(it)
            }, onRatingClicked = {
                onRatingClick(it)
            }, hasInternet = unit.hasInternetConnection)

            binding.rvLessons.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(binding.root.context)
                adapter = itemAdapter
            }
            when (unit.downloadStatus) {
                DownloadStatus.ERROR -> {
                    binding.apply {
                        lytRetry.visible()
                        lytDownload.gone()
                        ivDownload.gone()
                    }
                }

                DownloadStatus.NOT_START -> {
                    binding.apply {
                        ivDownload.visible()
                        lytRetry.gone()
                        lytDownload.gone()
                    }
                }

                DownloadStatus.STARTED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        lytDownload.visible()
                    }
                }

                DownloadStatus.FINISHED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        view2.gone()
                        lytDownload.gone()
                        ivDownloadComplete.gone()
                        // ivTest.visible()
                    }
                }

                DownloadStatus.PROGRESS -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        lytDownload.visible()
                        pbDownload.progress = unit.progress
                        tvProgress.text = context.getString(R.string.percentage, unit.progress)
                    }
                }

                DownloadStatus.INTIALISED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        lytDownload.visible()
                    }
                }
            }
        }

        fun updateDownloadStatus(
            progress: Progress, downloadStatus: DownloadStatus,
            downloadedFiles: Int, totalFiles: Int,
        ) {
            Timber.tag("Download").d("Parent status: $downloadStatus")
            binding.tvCompleted.text = context.getString(
                R.string.download_completed_count,
                downloadedFiles, totalFiles
            )
            when (downloadStatus) {
                DownloadStatus.ERROR -> {
                    binding.apply {
                        lytRetry.visible()
                        lytDownload.gone()
                        ivDownload.gone()
                        ivDownloadComplete.gone()
                    }
                }

                DownloadStatus.NOT_START -> {
                    binding.apply {
                        ivDownload.visible()
                        lytRetry.gone()
                        lytDownload.gone()
                        ivDownloadComplete.gone()
                    }
                }

                DownloadStatus.STARTED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        ivDownloadComplete.gone()
                        lytDownload.visible()
                        pbDownload.isIndeterminate = false
                    }
                }

                DownloadStatus.FINISHED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        view2.gone()
                        lytDownload.gone()
                        ivDownloadComplete.gone()
                        //   ivTest.visible()
                    }
                }

                DownloadStatus.PROGRESS -> {
                    binding.apply {
                        pbDownload.isIndeterminate = false
                        ivDownload.gone()
                        lytRetry.gone()
                        ivDownloadComplete.gone()
                        lytDownload.visible()
                        pbDownload.progress = progress
                        tvProgress.text = context.getString(R.string.percentage, progress)
                    }
                }

                DownloadStatus.INTIALISED -> {
                    binding.apply {
                        ivDownload.gone()
                        lytRetry.gone()
                        ivDownloadComplete.gone()
                        lytDownload.visible()
                        pbDownload.isIndeterminate = true
                    }
                }
            }
        }

        fun updateChildDownloadStatus(
            progress: Progress, downloadStatus: DownloadStatus, childPosition: Int,
        ) {
            binding.rvLessons.adapter?.notifyItemChanged(
                childPosition, listOf(
                    progress, downloadStatus
                )
            )
            Timber.tag("Download").d("Child  Notified")
        }
    }

    fun updateInternetStatus(hasInternet: Boolean) {
        filterDocuments.forEach {
            it.unit.hasInternetConnection = hasInternet
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadHolder {
        val binding =
            ExpandableLessonsHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return HeadHolder(binding)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: HeadHolder, position: Int) {
        val headerItem = itemList[position].unit
        val itemList = itemList[position].lessons
        holder.bindItem(headerItem, itemList)
        with(holder) {
            binding.ivDownload.setSafeOnClickListener {
                headerItem.isExpanded = false
                notifyItemChanged(position)
                onBulkDownload(
                    headerItem.id, this@LessonsHeaderAdapter.itemList[position].lessons,
                    headerItem.downloadedFiles
                )
            }
            binding.lytRetry.setSafeOnClickListener {
                headerItem.isExpanded = false
                notifyItemChanged(position)
                onBulkDownload(
                    headerItem.id, this@LessonsHeaderAdapter.itemList[position].lessons,
                    this@LessonsHeaderAdapter.itemList[position].unit.downloadedFiles
                )
            }
            binding.lytHeader.setOnClickListener {
                if (headerItem.downloadStatus == DownloadStatus.PROGRESS) {
                    headerItem.isExpanded = false
                    notifyItemChanged(position)
                } else {
                    headerItem.isExpanded = !headerItem.isExpanded
                    isLessonExpanded(headerItem.isExpanded)
                    notifyItemChanged(position)
                }
            }
            binding.ivTest.setOnClickListener {
                if (headerItem.isTestPassed == DBConstants.TestStatus.NOT_ATTEMPTED)
                    onTestClick(headerItem.id, headerItem.name)
            }
        }
    }

    //Called  while downloading to avoid flickering the views and notify download status
    override fun onBindViewHolder(holder: HeadHolder, position: Int, payloads: MutableList<Any>) {
        val totalItems = itemList[position].unit.totalFiles
        if (payloads.firstOrNull() != null) {
            val payloadList = payloads.first() as List<*>
            val downloadType = payloadList.first() as TYPE
            if (downloadType == TYPE.PARENT) {
                Timber.tag("Download").d("In Parent")
                val downloadProgress = payloadList[1] as Int
                val downloadStatus = payloadList[2] as DownloadStatus
                val downloadedFileCount = payloadList[3] as Int
                Timber.tag("Download").d("In Parent status : $downloadStatus")
                holder.updateDownloadStatus(
                    downloadProgress,
                    downloadStatus,
                    downloadedFileCount,
                    totalItems
                )
            } else {
                Timber.tag("Download").d("In Child")
                val downloadProgress = payloadList[1] as Int
                val downloadStatus = payloadList[2] as DownloadStatus
                val childPosition = payloadList[3] as Int
                holder.updateChildDownloadStatus(
                    downloadProgress, downloadStatus, childPosition
                )
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    fun updateGroupDownload(
        id: DocId,
        progress: Progress,
        downloadStatus: DownloadStatus,
        downloadedFiles: Int,
    ) {
        val groupPos = filterDocuments.indexOfFirst { it.unit.id == id }
        if (groupPos != -1) {
            filterDocuments[groupPos].apply {
                this.unit.progress = progress
                this.unit.downloadStatus = downloadStatus
                this.unit.downloadedFiles = downloadedFiles
            }
            // Call the onBindViewHolder function with payload
            notifyItemChanged(
                groupPos,
                listOf(TYPE.PARENT, progress, downloadStatus, downloadedFiles)
            )
            if (downloadStatus == DownloadStatus.FINISHED)
                updateChildDownloadStatusToDownloaded(id)
        }
    }

    //updates the whole recyclerview to update the parent recyclerview
    private fun updateGroupIndividualDownload(
        id: DocId,
        downloadedFiles: Int,
    ) {
        val groupPos = filterDocuments.indexOfFirst { it.unit.id == id }
        if (groupPos != -1) {
            filterDocuments[groupPos].apply {
                this.unit.progress = 100
                this.unit.downloadStatus = DownloadStatus.FINISHED
                this.unit.downloadedFiles = downloadedFiles
            }
            notifyDataSetChanged()
        }
    }

    fun updateChildDownload(
        id: Int,
        progress: Progress,
        downloadStatus: DownloadStatus,
        downloadCount: Int
    ) {
        filterDocuments.forEachIndexed { pos, continent ->
            val position = continent.lessons.indexOfFirst { it.id == id }
            if (position != -1) {
                val groupPos = continent.unit.id
                continent.lessons[position].apply {
                    this.progress = progress
                    this.downloadStatus = downloadStatus
                }
                notifyItemChanged(
                    pos,
                    listOf(TYPE.CHILD, progress, downloadStatus, position)
                )
                if (downloadStatus == DownloadStatus.FINISHED) {
                    updateGroupIndividualDownload(
                        groupPos,
                        downloadCount
                    )
                }
            }
        }
    }
     fun updateChildRatingStatus(lessonId: Int, ratingValue: Float){
         filterDocuments.forEachIndexed { pos, units ->
            val position = units.lessons.indexOfFirst{ it.id == lessonId }
            if(position != -1){
               units.lessons[position].apply {
                   this.lessonRate = ratingValue
               }
                Timber.tag("RATING VALUE POSITION BEFORE NOTIFY").e("Lesson Id position $position")
                notifyItemChanged(position)
                notifyDataSetChanged()
            }
        }

    }
    private fun updateChildDownloadStatusToDownloaded(id: DocId) {
        val groupPos = filterDocuments.indexOfFirst { it.unit.id == id }
        if (groupPos != -1) {
            filterDocuments[groupPos].lessons.forEachIndexed { position, country ->
                country.apply {
                    this.downloadStatus = DownloadStatus.FINISHED
                }
                notifyItemChanged(
                    position,
                    listOf(TYPE.CHILD, 0, DownloadStatus.FINISHED, groupPos)
                )
            }
        }
    }
}


class LessonsInnerAdapter(
    private var itemList: List<LessonsEntity>,
    private val onDownloadClicked: (Int, String) -> Unit,
    private val onItemClicked: (LessonsEntity) -> Unit,
    private val onWeblinkClicked: (lessonId: Int) -> Unit,
    private val onRatingClicked: (lessonId: Int) -> Unit,
    private val hasInternet: Boolean
) : RecyclerView.Adapter<LessonsInnerAdapter.ChildHolder>() {
    private var filterDocuments: MutableList<LessonsEntity> = ArrayList()
    private var lessons: List<LessonsEntity> = emptyList()

    init {
        filterDocuments = itemList.toMutableList()
    }

    fun updateList(list: List<LessonsEntity>) {
        filterDocuments.clear()
        filterDocuments.addAll(list)
        this.itemList = list
        notifyDataSetChanged()
    }

    inner class ChildHolder(val binding: ItemLessonLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.apply {
                updateImageTint(
                    binding.ivPlaceHolder, R.color.imageview_tint
                )
                ivDownload.visible()
                pbProgress.invisible()
            }
        }

        fun bindItem(item: LessonsEntity) {
            binding.apply {
                tvUpdate.text = item.createdBy.name
                ivPlaceHolder.load(item.logo)
                ivAvatar.load(item.createdBy.avatar)
                tvWeblink.visibility = if (hasInternet) View.VISIBLE else View.GONE
                ivWeblink.visibility = if (hasInternet) View.VISIBLE else View.GONE
                if (item.description.isEmpty()) {
                    tvDescription.visibility = View.GONE
                } else {
                    tvDescription.originalText = "Lesson Description : ${item.description}"
                }
                ivAvatar.setOnClickListener {
                    Timber.e("avatar pinnak work akanend ")
                }
                /*   tvDescription.setOnClickListener {
                       Timber.e("pinnak work akanend ")
                   }*/
                if (item.downloadStatus == DownloadStatus.FINISHED) {
                    updateImageTint(
                        binding.ivPlaceHolder, null
                    )
                    ivDownload.invisible()
                    pbProgress.invisible()
                    tvProgress.invisible()
                }
                if (hasInternet) {
                    if (item.lessonRate != null) {
                        llRating.invisible()
                    } else {
                        llRating.visible()
                    }
                } else {
                    llRating.invisible()
                }
            }
            setPlaceHolder(item.mimeType)
        }

        /*
        * Sets the placeholder based on the status ie video, pdf and image
         */
        private fun setPlaceHolder(mimeType: String) {
            //  if (status == DownloadStatus.FINISHED) {
            binding.apply {
                when (mimeType) {
                    "pdf" -> {
                        //   ivPdfViewholder.visible()
                        ivVideoViewholder.gone()
                    }

                    "mp4" -> {
                        ivVideoViewholder.visible()
                        //  ivPdfViewholder.gone()
                    }

                    else -> {
                        //     ivPdfViewholder.gone()
                        ivVideoViewholder.gone()
                    }
                }
            }
            /*} else {
                binding.apply {
                    ivPdfViewholder.gone()
                    ivVideoViewholder.gone()
                }
            }*/
        }

        fun updateDownloadStatus(
            progress: Int,
            downloadStatus: DownloadStatus,
            currentMimeType: String,
        ) {
            Timber.tag("Download").d("child status: $downloadStatus")
            binding.apply {
                when (downloadStatus) {
                    DownloadStatus.ERROR -> {
                        updateImageTint(
                            binding.ivPlaceHolder, R.color.imageview_tint
                        )
                        pbProgress.invisible()
                        tvProgress.invisible()
                    }

                    DownloadStatus.NOT_START -> {
                        updateImageTint(
                            binding.ivPlaceHolder, R.color.imageview_tint
                        )
                        pbProgress.invisible()
                        ivDownload.visible()
                        tvProgress.invisible()
                    }

                    DownloadStatus.STARTED -> {}

                    DownloadStatus.FINISHED -> {
                        updateImageTint(
                            binding.ivPlaceHolder, null
                        )
                        ivDownload.invisible()
                        pbProgress.invisible()
                        tvProgress.invisible()
                        setPlaceHolder(currentMimeType)
                    }

                    DownloadStatus.PROGRESS -> {
                        ivDownload.invisible()
                        pbProgress.visible()
                        pbProgress.progress = progress
                        tvProgress.visible()
                        tvProgress.text = "$progress%"
//                        ivPdfViewholder.gone()
                        ivVideoViewholder.gone()
                    }

                    DownloadStatus.INTIALISED -> {
                        ivDownload.invisible()
                        pbProgress.visible()
                        tvProgress.visible()
//                        ivPdfViewholder.gone()
                        ivVideoViewholder.gone()
                    }
                }
            }
        }
    }

    @SuppressLint("ResourceAsColor")
    private fun updateImageTint(view: ImageView, tintColor: Int?) {
        tintColor?.let {
            view.alpha = 0.3f
        } ?: kotlin.run {
            view.alpha = 1.0f
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChildHolder {
        val binding =
            ItemLessonLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ChildHolder(binding)
    }

    override fun getItemCount(): Int = itemList.size

    override fun onBindViewHolder(holder: ChildHolder, position: Int) {
        val item = itemList[position]
        holder.bindItem(item,)
        with(holder.binding) {
            ivDownload.setSafeOnClickListener {
                onDownloadClicked(item.id, item.file)
            }
            ivPlaceHolder.setOnClickListener {
                onItemClicked(item)
            }
            tvWeblink.setOnClickListener {
                onWeblinkClicked(item.id)
            }
            llRating.setOnClickListener {
                onRatingClicked(item.id)
            }
            /*     with(tvDescription) {
                     setOnClickListener {
                         this.expand()
                     }
                 }*/
        }
    }

    override fun onBindViewHolder(holder: ChildHolder, position: Int, payloads: MutableList<Any>) {
        val currentMimeType = itemList[position].mimeType
        if (payloads.firstOrNull() != null) {
            val payloadList = payloads.first() as List<*>
            val downloadProgress = payloadList.first() as Int
            val downloadStatus = payloadList[1] as DownloadStatus
            holder.updateDownloadStatus(downloadProgress, downloadStatus, currentMimeType)
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}




