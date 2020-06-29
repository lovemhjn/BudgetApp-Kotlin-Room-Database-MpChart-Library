package com.app.budgetapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
class Fund(
    @PrimaryKey(autoGenerate = true)
    var id: Int?,
    var categoryId: Int?,
    var month: Int,
    var year: Int,
    var initialBudget: Double?,
    var remainingBudget: Double?
) {
    constructor(categoryId: Int?, initialBudget: Double?, remainingBudget: Double?)
            : this(
        null,
        categoryId,
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.YEAR),
        initialBudget,
        remainingBudget
    )

}