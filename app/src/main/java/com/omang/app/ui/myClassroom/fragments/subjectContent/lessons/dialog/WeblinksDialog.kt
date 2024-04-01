package com.omang.app.ui.myClassroom.fragments.subjectContent.lessons.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.omang.app.R
import com.omang.app.data.database.myWebPlatforms.MyWebPlatformEntity
import com.omang.app.databinding.WebplatformItemLayoutBinding
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.hasInternetConnection
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.showSnackBar
import com.omang.app.utils.extensions.visible

class WeblinksDialog(private val context: Context) : InternetStatus {

    private lateinit var dialog: Dialog
    private lateinit var recyclerView: RecyclerView
    private lateinit var weblinkAdapter: WeblinkAdapter
    private lateinit var noContentTextView: TextView

    fun showDialog(
        itemList: List<MyWebPlatformEntity>,
        hasInternet: Boolean,
        onItemClick: (Int) -> Unit
    ) {
        dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_weblinks)
        dialog.setCancelable(true)

        dialog!!.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        recyclerView = dialog.findViewById(R.id.rv)
        noContentTextView = dialog.findViewById(R.id.tv_no_content)


        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
        }

        if (itemList.isNotEmpty()) {
            noContentTextView.gone()
            recyclerView.visible()
            weblinkAdapter = WeblinkAdapter(itemList, hasInternet) { item ->
                onItemClick(item)
                dialog.dismiss()
            }
            recyclerView.adapter = weblinkAdapter
        } else {
            noContentTextView.visible()
            recyclerView.gone()
        }

        dialog.show()
    }

    class WeblinkAdapter(
        private var webPlatformList: List<MyWebPlatformEntity>,
        private var hasInternet: Boolean,
        private val onItemClick: (Int) -> Unit,
    ) : RecyclerView.Adapter<WeblinkAdapter.WebPlatformViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebPlatformViewHolder {
            val binding =
                WebplatformItemLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            return WebPlatformViewHolder(binding)
        }

        override fun getItemCount(): Int {
            return webPlatformList.size
        }

        override fun onBindViewHolder(holder: WebPlatformViewHolder, position: Int) {
            val weblink = webPlatformList[position]
            holder.bindItem(weblink)
        }
        fun updateInternetStatus(available: Boolean) {
            this.hasInternet = available
            notifyDataSetChanged()
        }

        inner class WebPlatformViewHolder(
            private val itemBinding: WebplatformItemLayoutBinding,
        ) : RecyclerView.ViewHolder(itemBinding.root) {

            init {
                itemBinding.ivFavourite.invisible()
            }

            fun bindItem(webPlatformItem: MyWebPlatformEntity) {
                itemBinding.webplatform = webPlatformItem
                itemBinding.executePendingBindings()
                itemBinding.ivWebPlatform.load(webPlatformItem.logo){
                    crossfade(true)
                }
                itemBinding.cardView.apply {
                    alpha = if (hasInternet) {
                        1.0f
                    } else {
                        0.4f
                    }
                }

                itemView.setOnClickListener {
                    if (itemBinding.root.context.hasInternetConnection()) {
                        onItemClick(webPlatformItem.id)
                    } else
                        itemView.showSnackBar("No Internet connection!")
                }
            }

        }
    }

    override fun internetStatus(status: Boolean) {
        if (::weblinkAdapter.isInitialized)
        weblinkAdapter.updateInternetStatus(status)
    }
}

interface InternetStatus{
    fun internetStatus(status: Boolean)
}
