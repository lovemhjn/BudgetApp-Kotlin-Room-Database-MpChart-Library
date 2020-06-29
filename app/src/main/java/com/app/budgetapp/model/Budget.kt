package com.app.budgetapp.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Budget(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    val category: String,
    var image:String?
) {
    constructor(
        category: String,
        image:String?
    ) : this(null, category,image)
}