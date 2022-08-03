package com.id.administrasikaligentong.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentEntity(
    val name: String,
    val pdfName: String,
    val formFields: List<DocumentFormEntity>,
) : Parcelable {
    companion object {
        fun empty() = DocumentEntity("", "", listOf())
    }
}
