package com.omang.app.ui.myClassroom.adapter

import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.data.database.myClassroom.MyClassroomEntity
import com.omang.app.data.model.bubble.BubbleData
import com.omang.app.databinding.SubjectsItemLayoutBinding
import com.omang.app.utils.extensions.invisible
import com.omang.app.utils.extensions.visible


class SubjectsAdapter(
    private var subjectsList: List<MyClassroomEntity>,
    private val onSubjectClick: (MyClassroomEntity) -> Unit,
) : RecyclerView.Adapter<SubjectsAdapter.SubjectsViewHolder>(), SubjectClickListener {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectsViewHolder {
        val binding =
            SubjectsItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SubjectsViewHolder(binding)
    }

    override fun getItemCount(): Int = subjectsList.size

    override fun onBindViewHolder(holder: SubjectsViewHolder, position: Int) {
        val subjectItem = subjectsList[position]
        holder.bindItem(subjectItem)
    }

    fun setSubjectsList(subList: List<MyClassroomEntity>) {
        this.subjectsList = subList
    }

    inner class SubjectsViewHolder(
        private val itemBinding: SubjectsItemLayoutBinding,
    ) : RecyclerView.ViewHolder(itemBinding.root) {
        /* init {
             itemBinding.apply {
                 lytUpdate.apply {
                     ivDot.layoutParams.height = 12
                     ivDot.layoutParams.width = 12
                     ivDot.setColorFilter(
                         ContextCompat.getColor(
                             context,
                             R.color.text_success_color
                         ), android.graphics.PorterDuff.Mode.SRC_IN
                     )
                     tvUpdate.setTextColor(
                         context.resources.getColor(
                             R.color.color_deep_blue,
                             null
                         )
                     )
                 }
             }
         }*/

        fun bindItem(subjectItem: MyClassroomEntity) {
            itemBinding.subject = subjectItem
            itemBinding.click = this@SubjectsAdapter
            itemBinding.executePendingBindings()
            /*itemBinding.cardItem.apply {
                alpha = if (hasContents(subjectItem)) 1.0f else 0.4f
            }*/
            animateProgressBar(subjectItem.progress)

            if (BubbleData.classroomList.contains(subjectItem.id)) {
                itemBinding.ivBubble.visible()

            } else {
                itemBinding.ivBubble.invisible()

            }
        }

        //Animates the progressbar using LinearInterpolator
        private fun animateProgressBar(targetProgress: Int) {
            val animator = ObjectAnimator.ofInt(itemBinding.pbMain, "progress", targetProgress)
            animator.duration = 1000
            animator.interpolator = LinearInterpolator()
            animator.start()
        }
    }

    override fun itemCardClicked(item: MyClassroomEntity) {
        onSubjectClick.invoke(item)
    }

    companion object {
        fun hasContents(item: MyClassroomEntity): Boolean {
            item.contents.apply {
                val contentsCount = books + lessons + videos
                return contentsCount > 0
            }
        }
    }
}

interface SubjectClickListener {
    fun itemCardClicked(item: MyClassroomEntity)
}