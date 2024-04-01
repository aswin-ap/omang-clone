package com.omang.app.ui.myClassroom.fragments.subjectContent.adapter

import android.content.Context
import android.graphics.BlurMaskFilter
import android.graphics.MaskFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.data.database.resource.ResourcesEntity
import com.omang.app.data.ui.DownloadStatus
import com.omang.app.databinding.ItemDocsBinding
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.visible
import jp.wasabeef.glide.transformations.BlurTransformation
import okhttp3.internal.lowercase
import timber.log.Timber
import java.util.Locale


enum class SortOrder { ASCENDING, DESCENDING }
class DocsVideosAdapter(
    private val context: Context,
    private val documents: MutableList<ResourcesEntity>,
    val onDownloadClick: (id: Int, url: String) -> Unit,
    val onItemClick: (item: ResourcesEntity) -> Unit,
) : RecyclerView.Adapter<DocsVideosAdapter.ViewHolder>(), Filterable {
    private var filterDocuments: MutableList<ResourcesEntity> = ArrayList()

    init {
        filterDocuments = documents.toMutableList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemDocsBinding>(
            LayoutInflater.from(parent.context),
            R.layout.item_docs, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(filterDocuments[position])
        holder.isFileAlreadyDownloaded(filterDocuments[position])

        val mTextFilter = BlurMaskFilter(2f, BlurMaskFilter.Blur.NORMAL)
        holder.binding.tvTitle.paint.setMaskFilter(mTextFilter)
        holder.binding.tvDescription.paint.setMaskFilter(mTextFilter)


        if (filterDocuments[position].downloadStatus == DownloadStatus.FINISHED) {
            Glide.with(context)
                .load(filterDocuments[position].logo)
                .into(holder.binding.ivBookCover)
            holder.binding.overlay.gone()
            holder.binding.tvTitle.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            holder.binding.tvDescription.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            holder.binding.tvTitle.paint.maskFilter = null
            holder.binding.tvDescription.paint.maskFilter = null
        } else {
            Glide.with(context)
                .load(filterDocuments[position].logo)
                .apply(RequestOptions.bitmapTransform(BlurTransformation(1, 3)))
                .into(holder.binding.ivBookCover)
            holder.binding.overlay.visible()
        }

        holder.binding.clDownload.setOnClickListener {
            onDownloadClick.invoke(
                filterDocuments[position].id,
                filterDocuments[position].file ?: ""
            )
        }
        holder.binding.ivRetry.setOnClickListener {
            onDownloadClick.invoke(
                filterDocuments[position].id,
                filterDocuments[position].file ?: ""
            )
        }
        holder.binding.root.setOnClickListener {
            onItemClick(filterDocuments[position])
        }
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.firstOrNull() != null) {
            holder.binding.tvProgress.text = "${(payloads.first() as Int)}"
            holder.binding.pbDownload.progress = payloads.first() as Int
            holder.bindItem(documents[position])
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }

        /* val file: File = File(OmangApplication.getKeyDirectory() + filterBookItems[position].cover)
            val lastmodified = file.lastModified()
            val lastModified = Date(file.lastModified())
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
            val formattedDateString = formatter.format(lastModified)*/
        //        holder.tv_publications.setText(""+formattedDateString);
        /*ViewUtil.loadImage(file.getAbsolutePath(), filterBookItems.get(position).getCoverpathurl(),
                context, R.drawable.book_placeholder, R.drawable.book_placeholder, holder.iv_book_cover);*/

        /*  if (ViewUtil.isFileExists(booksItems.get(position).getCover())) {
            Bitmap bitmap = ViewUtil.loadImageFromUrl(booksItems.get(position).getCover());
            holder.iv_book_cover.setImageBitmap(bitmap);
        } else {
            holder.iv_book_cover.setImageDrawable(context.getDrawable(R.drawable.book_placeholder));
        }*/
    }

    override fun getItemCount(): Int {
        return filterDocuments.size
    }

    fun sort(sortOrder: SortOrder) {
        when (sortOrder) {
            SortOrder.ASCENDING -> {
                filterDocuments.sortBy {
                    it.name
                }
            }

            SortOrder.DESCENDING -> {
                filterDocuments.sortByDescending {
                    it.name
                }
            }
        }
        notifyDataSetChanged()

    }

    fun updateList(dataList: List<ResourcesEntity>) {
        filterDocuments.clear()
        documents.clear()
        documents.addAll(dataList)
        filterDocuments.addAll(dataList)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charstring = charSequence.toString()
                filterDocuments = if (charstring.isEmpty()) {
                    documents.toMutableList()
                } else {
                    val filterDocs: MutableList<ResourcesEntity> = ArrayList()
                    for (item in documents) {
                        if (item.name.lowercase(Locale.getDefault())
                                .contains(charstring.lowercase(Locale.getDefault()))
                        ) {
                            filterDocs.add(item)
                        }
                        item.description?.let {
                            if (it.lowercase(Locale.getDefault()).contains(
                                    charstring.lowercase(
                                        Locale.getDefault()
                                    )
                                )
                            ) {
                                filterDocs.add(item)
                            }
                        }
                    }
                    filterDocs
                }
                val filterResults = FilterResults()
                filterResults.values = filterDocuments
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterDocuments = filterResults.values as MutableList<ResourcesEntity>
                val items: MutableSet<ResourcesEntity> = LinkedHashSet()
                items.addAll(filterDocuments)
                filterDocuments = ArrayList()
                filterDocuments.addAll(items)
                notifyDataSetChanged()
            }
        }
    }

    fun updateDownload(id: Int, progress: Int, downloadStatus: DownloadStatus) {
        Timber.tag("PROGRESS_IN_ADAPTER").v("$progress $downloadStatus")
        val position = filterDocuments.indexOfFirst { it.id == id }
        if (position != -1) {
            filterDocuments[position].apply {
                this.progress = progress
                this.downloadStatus = downloadStatus
            }

            notifyItemChanged(position, progress)
        }


    }

    inner class ViewHolder(val binding: ItemDocsBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.pbDownload.max = 100
        }

        fun bindItem(document: ResourcesEntity) {
            binding.apply {
                when (document.downloadStatus) {
                    DownloadStatus.ERROR -> {
                        clDownload.gone()
                        lytDownload.gone()
                        lytRetry.visible()
                    }

                    DownloadStatus.FINISHED -> {
                        clDownload.gone()
                        lytDownload.gone()
                        lytRetry.gone()
                        overlay.gone()

                        Glide.with(context)
                            .load(document.logo)
                            .into(ivBookCover)

                        tvTitle.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        tvDescription.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                        tvTitle.paint.maskFilter = null
                        tvDescription.paint.maskFilter = null
//                        view2.gone()
                    }

                    DownloadStatus.PROGRESS -> {
                        clDownload.gone()
                        lytDownload.visible()
                        lytRetry.gone()
                    }

                    DownloadStatus.INTIALISED -> {
                        clDownload.gone()
                        lytDownload.visible()
                        lytRetry.gone()
                    }

                    else -> {}
                }
            }
        }
        /* init {
            tv_title = itemView.findViewById(R.id.tv_title)
            tv_description = itemView.findViewById(R.id.tv_description)
            iv_book_cover = itemView.findViewById(R.id.iv_book_cover)
            tv_publications = itemView.findViewById(R.id.tv_publications)
            cl_book = itemView.findViewById(R.id.cl_book)
            TextUtil.setProximaSemiboldTextView(context as Activity, tv_title)
            TextUtil.setProximaNovRegularTextView(context as Activity, tv_description)
            cl_book.setOnClickListener(View.OnClickListener { *//* Uri url = Uri.fromFile(new File(OmangApplication.getInstance().getExternalFilesDir(null).getAbsolutePath() +"/"+ "sample.pdf"));
                    Intent intent = new Intent(context, DocumentViewer.class);
                    intent.putExtra("pdf_url",url.toString());
                    context.startActivity(intent);*//*


                *//*    Uri uri = java.io.File pdffile = new java.io.File((getActivity()
                                    .getApplicationContext().getFileStreamPath("FileName.xml")
                                    .getPath()));*//*


                *//*        NavController navController = Navigation.findNavController((Activity) context, R.id.nav_host_fragment);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable(Constants.BOOKS_KEY, filterBookItems.get(getAdapterPosition()));
                            bundle.putString(Constants.FRAGMENT_TITLE, title);
                            bundle.putInt(Constants.BOOK_TYPE, 0);
                            bundle.putBoolean(Constants.isNotification, false);
                            navController.navigate(R.id.navigation_pdf_viewer, bundle);*//*
            })
        }*/

        fun bind(obj: ResourcesEntity) {
            binding.setVariable(BR.doc, obj)
            binding.executePendingBindings()
        }

        fun isFileAlreadyDownloaded(resourcesEntity: ResourcesEntity) {
            if (resourcesEntity.downloadStatus == DownloadStatus.FINISHED) {
                Timber.tag("Download").d("in finished")
                binding.apply {
                    clDownload.invisible()
//                    view2.gone()
                }
            } else {
                binding.apply {
                    clDownload.visible()
//                    view2.visible()
                }
            }
        }
    }
}
