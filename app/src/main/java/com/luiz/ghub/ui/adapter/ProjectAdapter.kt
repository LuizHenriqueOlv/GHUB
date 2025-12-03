package com.luiz.ghub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luiz.ghub.R
import com.luiz.ghub.databinding.ItemProjectBinding
import com.luiz.ghub.models.Project

class ProjectAdapter(
    private val projectList: List<Project>,
    private val onClick: (Project) -> Unit
) : RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    class ProjectViewHolder(val binding: ItemProjectBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val binding = ItemProjectBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ProjectViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projectList[position]

        holder.binding.txtProjectTitle.text = project.title

        if (project.cover.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(project.cover)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_background)
                .into(holder.binding.imgProjectCover)
        } else {
            holder.binding.imgProjectCover.setImageResource(R.drawable.ic_launcher_background)
        }
        holder.itemView.setOnClickListener {
            onClick(project)
        }
    }

    override fun getItemCount() = projectList.size
}