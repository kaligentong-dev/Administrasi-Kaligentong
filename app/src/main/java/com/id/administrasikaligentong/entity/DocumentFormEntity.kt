package com.id.administrasikaligentong.entity

import android.os.Parcelable
import com.id.administrasikaligentong.util.InputType
import kotlinx.parcelize.Parcelize

@Parcelize
data class DocumentFormEntity(
    val fieldDisplayName: String,
    val fieldName: String,
    var fieldValue: String = "",
    val helperText: String = "",
    val inputType: InputType = InputType.SingleLine
) : Parcelable
