package com.luiz.ghub.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.luiz.ghub.R
import com.luiz.ghub.databinding.ItemMemberBinding
import com.luiz.ghub.models.User

class MemberAdapter(
    private val memberList: List<User>,
    private val onClick: (User) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val user = memberList[position]

        holder.binding.txtMemberName.text = user.name ?: "Sem nome"

        holder.binding.txtMemberRole.text = "Membro"
        if (!user.photoURL.isNullOrEmpty()) {
            val secureUrl = user.photoURL.replace("http:", "https:")
            Glide.with(holder.itemView.context)
                .load(secureUrl)
                .circleCrop()
                .placeholder(R.drawable.account)
                .into(holder.binding.imgMemberPhoto)
        } else {
            holder.binding.imgMemberPhoto.setImageResource(R.drawable.account)
        }

        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }

    override fun getItemCount() = memberList.size

    class MemberViewHolder(val binding: ItemMemberBinding) : RecyclerView.ViewHolder(binding.root)
}