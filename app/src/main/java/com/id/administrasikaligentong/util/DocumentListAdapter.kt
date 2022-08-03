package com.id.administrasikaligentong.util

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.id.administrasikaligentong.databinding.ItemDocumentBinding
import com.id.administrasikaligentong.entity.DocumentEntity

class DocumentListAdapter(
    private val list: List<DocumentEntity>
    ) : RecyclerView.Adapter<DocumentListAdapter.DocumentViewHolder>() {

    private lateinit var itemOnClickListener: (item: DocumentEntity) -> Unit

    inner class DocumentViewHolder(private val binding: ItemDocumentBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(context: Context, item: DocumentEntity) {
            binding.tvDocumentTitle.text = item.name
            binding.root.setOnClickListener {
                if (::itemOnClickListener.isInitialized) itemOnClickListener(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocumentViewHolder {
        val binding = ItemDocumentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
	    return DocumentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DocumentViewHolder, position: Int) {
        holder.bind(holder.itemView.context, list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setItemOnClickListener(listener: (item: DocumentEntity) -> Unit) = apply {
        itemOnClickListener = listener
    }
}