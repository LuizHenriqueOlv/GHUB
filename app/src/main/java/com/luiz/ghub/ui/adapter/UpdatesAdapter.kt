package com.luiz.ghub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luiz.ghub.databinding.ItemUpdateBinding
import com.luiz.ghub.models.ProjectUpdate
import java.text.SimpleDateFormat
import java.util.Locale

class UpdatesAdapter(private val updatesList: List<ProjectUpdate>) :
    RecyclerView.Adapter<UpdatesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemUpdateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpdateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val update = updatesList[position]

        holder.binding.txtUpdateTitle.text = update.title
        holder.binding.txtUpdateDesc.text = update.desc

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.binding.txtUpdateDate.text = update.createdAt?.let { dateFormat.format(it) } ?: ""

        if (update.imgs.isNotEmpty()) {
            holder.binding.imgUpdate.isVisible = true
            Glide.with(holder.itemView.context)
                .load(update.imgs[0])
                .centerCrop()
                .into(holder.binding.imgUpdate)
        } else {
            holder.binding.imgUpdate.isVisible = false
        }
    }

    override fun getItemCount() = updatesList.size
}