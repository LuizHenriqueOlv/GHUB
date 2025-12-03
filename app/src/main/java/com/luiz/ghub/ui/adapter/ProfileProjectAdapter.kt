package com.luiz.ghub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luiz.ghub.R
import com.luiz.ghub.databinding.ItemProjectBinding
import com.luiz.ghub.models.Project

class ProfileProjectAdapter(
    private val projectList: List<Project>,
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProfileProjectAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val layoutParams = binding.root.layoutParams

        val widthInPx = (250 * parent.context.resources.displayMetrics.density).toInt()

        layoutParams.width = widthInPx
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        binding.root.layoutParams = layoutParams

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val project = projectList[position]

        holder.binding.txtProjectTitle.text = project.title

        if (project.cover.isNotEmpty()) {
            val secureUrl = project.cover.replace("http:", "https:")
            Glide.with(holder.itemView.context)
                .load(secureUrl)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.binding.imgProjectCover)
        } else {
            holder.binding.imgProjectCover.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.itemView.setOnClickListener { onClick(project) }
    }

    override fun getItemCount() = projectList.size
}