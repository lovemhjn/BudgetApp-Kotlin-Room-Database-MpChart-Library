package com.app.budgetapp.ui

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.AnalyticsChartData
import com.app.budgetapp.model.Budget
import com.app.budgetapp.utils.Db
import com.app.budgetapp.utils.clearAllGoToActivity
import com.app.budgetapp.utils.getMonth
import com.app.budgetapp.utils.getYear
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialog
import com.github.dewinjm.monthyearpicker.MonthYearPickerDialogFragment
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_analytics.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap
import kotlin.math.ceil
import kotlin.math.floor

class AnalyticsActivity : AppCompatActivity() {

    var fromDate:Date? = null
    var toDate:Date? = null
    val budgetList by lazy { ArrayList<Budget>() }
    var catIndex = 0
    var month:Int ?= null
    var year:Int ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        budgetList.addAll(RoomDb.getDatabase(this).budgetDao().getBudgetList())
        initPieChart()
        initPieChartMonthly()
        setSpinner()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        tvFrom.setOnClickListener {
            selectFromDate()
        }

        tvTo.setOnClickListener {
            selectToDate()
        }

        tvMonth.setOnClickListener {
            selectMonth()
        }

        imgHome.setOnClickListener{
            clearAllGoToActivity(MainActivity::class.java)
        }
    }

    private fun selectMonth(){


        val dialogFragment: MonthYearPickerDialogFragment = MonthYearPickerDialogFragment
            .getInstance(getMonth(), getYear(),0,Calendar.getInstance().timeInMillis)

        dialogFragment.setOnDateSetListener { year, monthOfYear ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.MONTH,monthOfYear)
            calendar.set(Calendar.YEAR,year)
            val dateFormat =
                SimpleDateFormat("MMMM yyyy")
            this.year = year;
            this.month=monthOfYear;
            tvMonth.text = dateFormat.format(calendar.time)
            fromDate==null
            toDate==null
            initPieChartMonthly()
        }

        dialogFragment.show(supportFragmentManager, null)
    }

    private fun selectFromDate(){
        val builderDatePicker =
            AlertDialog.Builder(this)
        val picker = DatePicker(this)
        picker.maxDate = toDate?.time?:Calendar.getInstance().timeInMillis

        builderDatePicker.setView(picker)

        builderDatePicker.setPositiveButton(
            "Select"
        ) { dialog: DialogInterface?, which: Int ->
            val year = picker.year
            val mon = picker.month
            val day = picker.dayOfMonth
            fromDate = GregorianCalendar(year, mon, day).time
            tvFrom.text = getTime(fromDate)
            if(toDate!=null){
                initPieChartMonthly()
            }

        }
        builderDatePicker.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface?, whichButton: Int -> dialog?.dismiss() }
        builderDatePicker.show()
    }

    private fun selectToDate(){
        val builderDatePicker =
            AlertDialog.Builder(this)
        val picker = DatePicker(this)
        picker.maxDate = Calendar.getInstance().timeInMillis
        picker.minDate = fromDate?.time?:Calendar.getInstance().timeInMillis
        builderDatePicker.setView(picker)


        builderDatePicker.setPositiveButton(
            "Select"
        ) { dialog: DialogInterface?, which: Int ->
            val year = picker.year
            val mon = picker.month
            val day = picker.dayOfMonth
            toDate = GregorianCalendar(year, mon, day).time
            tvTo.text = getTime(toDate)
            initPieChartMonthly()

        }
        builderDatePicker.setNegativeButton(
            "Cancel"
        ) { dialog: DialogInterface?, whichButton: Int -> dialog?.dismiss() }
        builderDatePicker.show()
    }

    private fun getTime(date:Date?):String{
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.US)
        return outputFormat.format(date)
    }

    private fun getDate(date:String?):Date{
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy", Locale.US)
        return outputFormat.parse(date)
    }

    private fun initPieChart(){
        tvBudget.append(" ${getString(R.string.currency)} ${Db().fundsDao().getTotalOfYear(getYear())}")
        tvExpenses.append(" ${getString(R.string.currency)} ${Db().fundsDao().getTotalExpense(getYear())}")

        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChart.dragDecelerationFrictionCoef = 0.95f


        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(Color.WHITE)

        pieChart.setTransparentCircleColor(Color.WHITE)
        pieChart.setTransparentCircleAlpha(110)

        pieChart.holeRadius = 58f
        pieChart.transparentCircleRadius = 61f

        pieChart.setDrawCenterText(true)

        pieChart.rotationAngle = 0f
        // enable rotation of the pieChart by touch
        // enable rotation of the pieChart by touch
        pieChart.isRotationEnabled = true
        pieChart.isHighlightPerTapEnabled = true

        // add a selection listener
        //pieChart.setOnChartValueSelectedListener(this)

      

        pieChart.animateY(1400, Easing.EaseInOutQuad)
        // pieChart.spin(2000, 0, 360);
        pieChart.legend.isEnabled = false


        // entry label styling
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setEntryLabelTextSize(12f)

        makeDataForChart()

    }

    private fun makeDataForChart() {
        val filteredList = ArrayList<AnalyticsChartData>()
        var overAllExpenses = 0.0

            budgetList.forEach { budget ->
                val expenseList = RoomDb.getDatabase(this).expenseDao().getExpensesList(budget.id)
                var totalExpenses = 0.0
                expenseList.forEach {
                    val year =Calendar.getInstance().apply {  time = getDate(it.dateTime)}.get(Calendar.YEAR)
                    if(year == Calendar.getInstance().get(Calendar.YEAR)){
                        totalExpenses += it.expense
                        overAllExpenses += it.expense
                        }
                }
                if (totalExpenses > 0)
                    filteredList.add(AnalyticsChartData(budget.category, totalExpenses))

            }


        setChartData(filteredList,overAllExpenses)
    }

    private fun setChartData(
        filteredList: ArrayList<AnalyticsChartData>,
        overAllExpenses: Double
    ) {
        val entries = ArrayList<PieEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until filteredList.size) {
           entries.add(
                PieEntry(
                    filteredList[i].amount.toFloat(),
                    "${filteredList[i].name} ${getString(R.string.currency)} ${filteredList[i].amount}")
                )

        }
        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        val colorsList = ArrayList<Int>()


        for (c in ColorTemplate.JOYFUL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colorsList.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.MATERIAL_COLORS) colorsList.add(c)
        for (c in ColorTemplate.VORDIPLOM_COLORS) colorsList.add(c)

        colorsList.remove(Color.rgb(255, 247, 140))
        colorsList.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colorsList
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChart))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)
        pieChart.data = data

        // undo all highlights
        pieChart.highlightValues(null)
        pieChart.invalidate()
    }



    private fun initPieChartMonthly(){

        pieChartMonthly.setUsePercentValues(true)
        pieChartMonthly.description.isEnabled = false
        pieChartMonthly.setExtraOffsets(5f, 10f, 5f, 5f)

        pieChartMonthly.dragDecelerationFrictionCoef = 0.95f


        pieChartMonthly.isDrawHoleEnabled = true
        pieChartMonthly.setHoleColor(Color.WHITE)

        pieChartMonthly.setTransparentCircleColor(Color.WHITE)
        pieChartMonthly.setTransparentCircleAlpha(110)

        pieChartMonthly.holeRadius = 58f
        pieChartMonthly.transparentCircleRadius = 61f

        pieChartMonthly.setDrawCenterText(true)

        pieChartMonthly.rotationAngle = 0f
        // enable rotation of the pieChartMonthly by touch
        // enable rotation of the pieChartMonthly by touch
        pieChartMonthly.isRotationEnabled = true
        pieChartMonthly.isHighlightPerTapEnabled = true

        // add a selection listener
        //pieChartMonthly.setOnChartValueSelectedListener(this)



        pieChartMonthly.animateY(1400, Easing.EaseInOutQuad)
        // pieChartMonthly.spin(2000, 0, 360);
        pieChartMonthly.legend.isEnabled = false


        // entry label styling
        pieChartMonthly.setEntryLabelColor(Color.BLACK)
        pieChartMonthly.setEntryLabelTextSize(12f)

        makeDataForChartMonthly()

    }

    private fun makeDataForChartMonthly() {
        val filteredList = ArrayList<AnalyticsChartData>()
        var overAllExpenses = 0.0
        var totalBudget = 0.0

        if(fromDate == null && toDate==null && month ==null){
            budgetList.forEach { budget ->
                val expenseList = RoomDb.getDatabase(this).expenseDao().getExpensesList(budget.id)
                var totalExpenses = 0.0
                expenseList.forEach {
                    val month =Calendar.getInstance().apply {  time = getDate(it.dateTime)}.get(Calendar.MONTH)
                    if(month == Calendar.getInstance().get(Calendar.MONTH)) {
                        totalExpenses += it.expense
                        overAllExpenses += it.expense
                    }
                }
                if (totalExpenses > 0)
                    filteredList.add(AnalyticsChartData(budget.category, totalExpenses))

            }
            totalBudget = Db().fundsDao().getTotal(getMonth(),getYear())
            tvBudgetMonthly.visibility=View.VISIBLE
            tvBudgetMonthly.text = "Total Budget: ${getString(R.string.currency)} $totalBudget"

        }else if(fromDate == null && toDate==null){
            budgetList.forEach { budget ->
                val expenseList = RoomDb.getDatabase(this).expenseDao().getExpensesList(budget.id)
                var totalExpenses = 0.0
                expenseList.forEach {
                    val mMonth =Calendar.getInstance().apply {  time = getDate(it.dateTime)}.get(Calendar.MONTH)
                    val mYear =Calendar.getInstance().apply {  time = getDate(it.dateTime)}.get(Calendar.YEAR)
                    if(mMonth ==month && mYear==year) {
                        totalExpenses += it.expense
                        overAllExpenses += it.expense
                    }
                }
                if (totalExpenses > 0)
                    filteredList.add(AnalyticsChartData(budget.category, totalExpenses))

            }
            totalBudget = Db().fundsDao().getTotal(month,year)
            tvBudgetMonthly.visibility=View.VISIBLE
            tvBudgetMonthly.text = "Total Budget: ${getString(R.string.currency)} $totalBudget"
        }else {
            budgetList.forEach { budget ->
                val expenseList = RoomDb.getDatabase(this).expenseDao().getExpensesList(budget.id)
                var totalExpenses = 0.0
                expenseList.forEach {
                    val dateTime = getDate(it.dateTime)
                    if (!dateTime.before(fromDate) && !dateTime.after(toDate)) {
                        totalExpenses += it.expense
                        overAllExpenses += it.expense
                    }
                }
                if (totalExpenses > 0)
                    filteredList.add(AnalyticsChartData(budget.category, totalExpenses))

            }
            tvBudgetMonthly.visibility=View.GONE
        }

        tvExpensesMonthly.text = "Total Expneses: ${getString(R.string.currency)} $overAllExpenses"
        setChartDataMonthly(filteredList,overAllExpenses)
    }

    private fun setChartDataMonthly(
        filteredList: ArrayList<AnalyticsChartData>,
        overAllExpenses: Double
    ) {
        val entries = ArrayList<PieEntry>()

        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        // the chart.
        for (i in 0 until filteredList.size) {
            entries.add(
                PieEntry(
                    filteredList[i].amount.toFloat(),
                    "${filteredList[i].name} ${getString(R.string.currency)} ${filteredList[i].amount}")
            )

        }
        val dataSet = PieDataSet(entries, "Expenses")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 3f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 5f

        val colorsList = ArrayList<Int>()

        for (c in ColorTemplate.JOYFUL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.COLORFUL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.LIBERTY_COLORS) colorsList.add(c)

        for (c in ColorTemplate.PASTEL_COLORS) colorsList.add(c)

        for (c in ColorTemplate.MATERIAL_COLORS) colorsList.add(c)
        for (c in ColorTemplate.VORDIPLOM_COLORS) colorsList.add(c)


        colorsList.remove(Color.rgb(255, 247, 140))
        colorsList.add(ColorTemplate.getHoloBlue())
        dataSet.colors = colorsList
        //dataSet.setSelectionShift(0f);
        val data = PieData(dataSet)
        data.setValueFormatter(PercentFormatter(pieChartMonthly))
        data.setValueTextSize(11f)
        data.setValueTextColor(Color.BLACK)
        pieChartMonthly.data = data

        // undo all highlights
        pieChartMonthly.highlightValues(null)
        pieChartMonthly.invalidate()
    }

    private fun formatYAxis(value: Double): String? {
        val shortForm = floor(value / 1000 * 10) / 10
        return "$shortForm K"
    }

    private fun setSpinner() {
        val categoryArr = arrayOfNulls<String>(budgetList.size)
        var index = 0;
        budgetList.forEach {
            categoryArr[index++] = it.category
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categoryArr)
        categorySpinner.adapter = adapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                catIndex = position
                initMonthsBarChart()
            }

        }
        initMonthsBarChart()
    }

    private fun initMonthsBarChart() {
        var xLabels = ArrayList<String>()
        var yLabels = ArrayList<Float>()
        var maxValue = Int.MIN_VALUE

        val expenseList = RoomDb.getDatabase(this).expenseDao().getExpensesList(budgetList[catIndex].id)
        val hashMap = LinkedHashMap<String,Double>()
        hashMap["Jan"] = 0.0
        hashMap["Feb"] = 0.0
        hashMap["Mar"] = 0.0
        hashMap["Apr"] = 0.0
        hashMap["May"] = 0.0
        hashMap["Jun"] = 0.0
        hashMap["Jul"] = 0.0
        hashMap["Aug"] = 0.0
        hashMap["Sep"] = 0.0
        hashMap["Oct"] = 0.0
        hashMap["Nov"] = 0.0
        hashMap["Dec"] = 0.0

        expenseList.forEach {
            val monthName = getMonthName(it.dateTime)
            if(hashMap.containsKey(monthName))
                hashMap[monthName] = hashMap[monthName]?.plus(it.expense)?:0.0
            else
                hashMap[monthName] = it.expense

        }
        hashMap.entries.forEach {
            xLabels.add(it.key)
            yLabels.add(it.value.toFloat())
        }
        monthBarChart.setDrawBarShadow(false)
        monthBarChart.setDrawValueAboveBar(true)

        monthBarChart.description.isEnabled = false

        // if more than 60 entries are displayed in the barChart, no values will be
        // drawn

        // if more than 60 entries are displayed in the barChart, no values will be
        // drawn
        monthBarChart.setMaxVisibleValueCount(60)

        // scaling can now only be done on x- and y-axis separately

        // scaling can now only be done on x- and y-axis separately
        monthBarChart.setPinchZoom(false)

        monthBarChart.setDrawGridBackground(false)
        // barChart.setDrawYLabels(false);

        // ValueFormatter xAxisFormatter = new DayAxisValueFormatter(barChart);

        // barChart.setDrawYLabels(false);

        // ValueFormatter xAxisFormatter = new DayAxisValueFormatter(barChart);
        val xAxis: XAxis = monthBarChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM

        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.granularity = 1f // only intervals of 1 day


        xAxis.labelCount = 4


        xAxis.valueFormatter = object : ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                return if(value<xLabels.size) {
                    xLabels[value.toInt()]
                }else{
                    ""
                }
            }
        }


        val rightAxis: YAxis = monthBarChart.axisRight

        //rightAxis.setLabelCount(8, false)
        rightAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
        rightAxis.spaceTop = 15f
        rightAxis.axisMinimum = 0f
        rightAxis.setDrawAxisLine(false)
        rightAxis.setDrawLabels(false)
        // this replaces setStartAtZero(true)

        // this replaces setStartAtZero(true)
        val leftAxis: YAxis = monthBarChart.axisLeft
        leftAxis.setDrawGridLines(false)
       // leftAxis.setLabelCount(8, false)
        leftAxis.granularity = 1f

        leftAxis.spaceTop = 15f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)


        leftAxis.valueFormatter = object:ValueFormatter(){
            override fun getFormattedValue(value: Float): String {
                return value.toString();
                /*return if (value < 999) {
                    "" + ceil(value.toDouble()/100.0)*100
                } else {
                    formatYAxis(ceil(value.toDouble()/100.0)*100)?:""
                }*/
            }
        }

        val l: Legend = monthBarChart.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)
        l.form = Legend.LegendForm.SQUARE
        l.formSize = 9f
        l.textSize = 11f
        l.xEntrySpace = 4f
        l.isEnabled = false
        setMonthBarChartData(yLabels,maxValue)

    }

    private fun setMonthBarChartData(
        yLabels: ArrayList<Float>,
        maxValue: Int
    ) {

        val values =ArrayList<BarEntry>()

        for (i in 0 until yLabels.size) {
            val value: Float = yLabels[i]
            values.add(BarEntry(i.toFloat(), value))
        }

        var set1: BarDataSet

        if (monthBarChart.data != null &&
            monthBarChart.data.dataSetCount > 0
        ) {
            set1 = monthBarChart.data.getDataSetByIndex(0) as BarDataSet
            set1.values = values
            monthBarChart.invalidate()
            monthBarChart.data.notifyDataChanged()
            monthBarChart.notifyDataSetChanged()
        } else {
            set1 = BarDataSet(values, "The year 2020")
            set1.setDrawIcons(false)

            val colorsList = ArrayList<Int>()
            ColorTemplate.MATERIAL_COLORS.iterator().forEach {
                colorsList.add(it)
            }
            set1.colors = colorsList

            val dataSets = ArrayList<IBarDataSet>()
            dataSets.add(set1)
            val data = BarData(dataSets)
            data.setValueTextSize(10f)
            data.setDrawValues(false)
            data.barWidth = 0.5f
            monthBarChart.data = data
            monthBarChart.setVisibleXRangeMaximum(10f) // allow 20 values to be displayed at once on the x-axis, not more
            monthBarChart.moveViewToX(10f)
            monthBarChart.notifyDataSetChanged()
            monthBarChart.invalidate()
        }
    }

    private fun getMonthName(date:String):String{
        var formattedDate = getDate(date)
        return SimpleDateFormat("MMM").format(formattedDate)
    }

}
