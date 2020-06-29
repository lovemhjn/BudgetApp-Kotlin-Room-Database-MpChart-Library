package com.app.budgetapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Budget
import com.app.budgetapp.model.Expenses
import com.app.budgetapp.utils.*
import com.example.imagerecognition.utils.ImagePicker
import com.example.imagerecognition.utils.PermissionsUtil
import kotlinx.android.synthetic.main.activity_add_expense.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddExpenseActivity : AppCompatActivity() {

    private val budgetList by lazy { ArrayList<Budget>() }
    var permissionsUtil: PermissionsUtil? = null
    var image: String? = null
    var catIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)
        setSpinner()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        btnSelectImage.setOnClickListener {
            getImage()
        }

        btnSaveExpense.setOnClickListener {
            saveExpense()
        }

        imgHome.setOnClickListener{
            clearAllGoToActivity(MainActivity::class.java)
        }
    }

    private fun saveExpense() {
        val fund = Db().fundsDao().getFund(budgetList[catIndex].id,getMonth(),getYear())
        if(fund!=null) {
            if (!TextUtils.isEmpty(etExpenseName.text)) {
                if (!TextUtils.isEmpty(etAmount.text)) {
                    val amt = etAmount.text.toString().toDouble()
                    if (fund?.remainingBudget?.compareTo(amt) != -1) {
                        val expense = Expenses(
                            budgetList[catIndex].id,
                            etExpenseName.text.toString(),
                            etAmount.text.toString().toDouble(),
                            image, getTime()
                        )
                        val budget = budgetList[catIndex]
                        val remainingBudget =
                            fund?.remainingBudget?.minus(etAmount.text.toString().toDouble())
                        fund?.remainingBudget = remainingBudget
                        Coroutines.main {
                            RoomDb.getDatabase(this).expenseDao().insertExpense(expense)
                            RoomDb.getDatabase(this).fundsDao().updateFund(fund)
                            showToast("Expenditure saved successfully.")
                            finish()
                        }
                    } else {
                        showToast("Funds not sufficient.")
                    }
                } else {
                    showToast("Please enter expenditure.")
                }
            } else {
                showToast("Please enter expense name.")
            }
        }else{
            showToast("Funds not sufficient.")
        }
    }

    private fun getImage() {
        permissionsUtil = PermissionsUtil(this)
        permissionsUtil?.askPermissions(
            this,
            PermissionsUtil.CAMERA,
            PermissionsUtil.STORAGE,
            object : PermissionsUtil.PermissionListener {
                override fun onPermissionResult(isGranted: Boolean) {
                    ImagePicker.selectImage(
                        this@AddExpenseActivity,
                        object : ImagePicker.Companion.ImagePickerListener {
                            override fun onImagePicked(imageFile: File?, tag: String?) {
                                imgReceipt.setImageBitmap(BitmapFactory.decodeFile(imageFile?.path))
                                image = encoder(imageFile?.absolutePath)
                            }

                        },
                        "Captured",
                        "Select",
                        true
                    )
                }

            })
    }

    private fun setSpinner() {
        budgetList.addAll(RoomDb.getDatabase(this).budgetDao().getBudgetList())
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
            }

        }
    }

    fun encoder(filePath: String?): String {
        val bytes = File(filePath).readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun getTime():String{
        val outputFormat = SimpleDateFormat("dd MMMM, yyyy hh:mma", Locale.US)
        return outputFormat.format(Calendar.getInstance().time)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsUtil?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        ImagePicker.onActivityResult(requestCode, resultCode, data)
    }
}
