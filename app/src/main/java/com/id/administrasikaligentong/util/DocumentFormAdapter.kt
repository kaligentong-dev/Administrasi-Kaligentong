package com.id.administrasikaligentong.util

import android.content.Context
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.id.administrasikaligentong.databinding.ItemMultiLineFieldBinding
import com.id.administrasikaligentong.databinding.ItemSingleLineFieldBinding
import com.id.administrasikaligentong.entity.DocumentFormEntity

class DocumentFormAdapter(
    private val list: List<DocumentFormEntity>,
    ) : RecyclerView.Adapter<BaseViewHolder>() {

    inner class SingleLineFieldViewHolder(private val binding: ItemSingleLineFieldBinding) : BaseViewHolder(binding){
        override fun bind(context: Context, item: DocumentFormEntity): Unit = with(binding.tilSingleLine) {
            hint = item.fieldDisplayName
            helperText = item.helperText
            with(editText!!) {
                text = item.fieldValue.toEditable()
                doAfterTextChanged {
                    item.fieldValue = it.toString()
                }
            }
        }
    }

    inner class MultiLineFieldViewHolder(private val binding: ItemMultiLineFieldBinding) : BaseViewHolder(binding) {
        override fun bind(context: Context, item: DocumentFormEntity): Unit = with(binding.tilMultiLine) {
            val lineAmount = if (item.inputType is InputType.MultiLine) item.inputType.lineAmount else 1
            hint = item.fieldDisplayName
            helperText = item.helperText
            with(editText!!) {
                text = item.fieldValue.toEditable()
                doAfterTextChanged {
                    item.fieldValue = it.toString()
                }
                minLines = lineAmount
                maxLines = lineAmount
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder = when(viewType) {
        1 -> MultiLineFieldViewHolder(ItemMultiLineFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        else -> SingleLineFieldViewHolder(ItemSingleLineFieldBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(holder.itemView.context, list[position])
    }

    override fun getItemViewType(position: Int): Int = when(list[position].inputType) {
        InputType.SingleLine -> 0
        is InputType.MultiLine -> 1
        is InputType.Date -> 2
        is InputType.Time -> 3
        is InputType.DateTime -> 4
    }

    override fun getItemCount(): Int = list.size

    private fun String.toEditable() = Editable.Factory.getInstance().newEditable(this)

    fun getList(): List<DocumentFormEntity> = list
}

abstract class BaseViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    open fun bind(context: Context, item: DocumentFormEntity) {}
}