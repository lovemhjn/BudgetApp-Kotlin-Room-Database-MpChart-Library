package com.app.budgetapp.model

import android.media.Image
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
class Expenses(
    @PrimaryKey(autoGenerate = true)
    val id: Int?,
    val categoryId: Int?,
    val expenseName: String,
    val expense: Double,
    val image: String?,
    val dateTime: String
) {
    constructor(categoryId: Int?, expenseName: String, expense: Double, image: String?,dateTime: String) : this(
        null,
        categoryId,
        expenseName,
        expense,
        image,
        dateTime
    )


}