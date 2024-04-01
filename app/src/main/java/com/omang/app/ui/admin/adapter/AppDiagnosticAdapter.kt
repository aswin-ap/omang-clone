package com.omang.app.ui.admin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.R
import com.omang.app.databinding.ItemAppDiagnosticsBinding
import com.omang.app.ui.admin.model.AppDiagnosticsData
import com.omang.app.ui.admin.model.DiagnosticsStatus
import com.omang.app.utils.extensions.gone
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.visible

class AppDiagnosticAdapter(val appDiagnosticsData: List<AppDiagnosticsData>) :
    RecyclerView.Adapter<AppDiagnosticAdapter.ViewHolder>() {
    class ViewHolder(var binding: ItemAppDiagnosticsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: AppDiagnosticsData) {
            binding.apply {
                textServices.text = data.item
                if (data.item.equals("Network Speed", true)) {
                    imgActivate.gone()
                    textStatus.visible()
                } else {
                    imgActivate.visible()
                    textStatus.gone()
                }
                when (data.status) {
                    DiagnosticsStatus.INITIAL -> imgActivate.invisible()
                    DiagnosticsStatus.LOADING -> {
                        //imgActivate.visible()
                            // imgActivate.setImageResource(R.drawable.loading)
                        imgActivate.gone()
                        progressBar.visible()
                    }

                    DiagnosticsStatus.SUCCESS -> {
                        progressBar.gone()
                        imgActivate.visible()
                        imgActivate.setImageResource(R.drawable.tick_green)
                    }

                    DiagnosticsStatus.FAILED -> {
                        progressBar.gone()
                        imgActivate.visible()
                        imgActivate.setImageResource(R.drawable.no_red)
                    }
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemAppDiagnosticsBinding>(
            LayoutInflater.from(parent.context), R.layout.item_app_diagnostics, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return appDiagnosticsData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = appDiagnosticsData[position]
        holder.bind(data)
    }

    fun setLoading(status: DiagnosticsStatus) {
        appDiagnosticsData.forEach {
            it.status = status
        }
        notifyDataSetChanged()
    }

    fun updateStatus(position: Int, status: DiagnosticsStatus) {
        val item = appDiagnosticsData[position]
        item.status = status.also {
            notifyItemChanged(position)
        }
    }

}