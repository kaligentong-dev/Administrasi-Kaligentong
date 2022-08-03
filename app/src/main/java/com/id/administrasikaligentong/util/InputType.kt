package com.id.administrasikaligentong.util

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDate
import java.time.LocalDateTime

@Parcelize
sealed class InputType : Parcelable {
    object SingleLine: InputType()
    data class MultiLine(val charPerLine: Int, val lineAmount: Int = 2): InputType()
    data class Date(val date: LocalDate): InputType()
    data class Time(val time: LocalDateTime): InputType()
    data class DateTime(val dateTime: LocalDateTime): InputType()
}