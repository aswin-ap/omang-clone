package com.omang.app.ui.appupdate.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.R
import com.omang.app.databinding.LayoutItemUpdateDescriptionBinding

/**
 * Adapter to view the description of the latest app version.
 * @constructor creates the object
 * @param descriptionList list of description
 * @param onItemClick Lambda function to get callback of the
 * clicked item
 */
class AppUpdateDetailsAdapter(
    private var descriptionList: List<String>,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<AppUpdateDetailsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = DataBindingUtil.inflate<LayoutItemUpdateDescriptionBinding>(
            LayoutInflater.from(parent.context),
            R.layout.layout_item_update_description, parent, false
        )
        return ViewHolder(binding)
    }

    fun setDescriptionList(descriptionList: List<String>) {
        this.descriptionList = descriptionList
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val description = descriptionList[position]
        holder.bind(description)
        holder.itemView.setOnClickListener {
            onItemClick(description)
        }
    }

    override fun getItemCount(): Int = descriptionList.size


    inner class ViewHolder(private var itemRowBinding: LayoutItemUpdateDescriptionBinding) :
        RecyclerView.ViewHolder(
            itemRowBinding.root
        ) {
        fun bind(value: String) {
            itemRowBinding.text = value
        }
    }
}
