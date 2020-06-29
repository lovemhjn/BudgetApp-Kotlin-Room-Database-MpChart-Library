package com.app.budgetapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import com.app.budgetapp.R
import com.app.budgetapp.db.RoomDb
import com.app.budgetapp.model.Budget
import com.app.budgetapp.model.Fund
import com.app.budgetapp.utils.Coroutines
import com.app.budgetapp.utils.clearAllGoToActivity
import com.app.budgetapp.utils.showToast
import com.example.imagerecognition.utils.ImagePicker
import com.example.imagerecognition.utils.PermissionsUtil
import kotlinx.android.synthetic.main.activity_create_budget.*
import kotlinx.android.synthetic.main.activity_create_budget.etAmount
import kotlinx.android.synthetic.main.activity_create_budget.imgHome
import kotlinx.android.synthetic.main.activity_create_budget.toolBar
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CreateBudgetActivity : AppCompatActivity() {

    var budgetId: Int = 0
    var mbudget: Budget? = null
    var mFund: Fund? = null
    var permissionsUtil: PermissionsUtil? = null
    var image: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_budget)

        getIntentExtras()
        btnSaveBudget.setOnClickListener {
            if (intent.hasExtra("BUDGET_ID")) {
                updateBudget()
            } else {
                saveBudget()
            }
        }

        btnSelectImage.setOnClickListener {
            getImage()
        }
        imgHome.setOnClickListener {
            clearAllGoToActivity(MainActivity::class.java)
        }
    }

    private fun getIntentExtras() {
        if (intent.hasExtra("BUDGET_ID")) {
            budgetId = intent.getIntExtra("BUDGET_ID", 0)
            mbudget = RoomDb.getDatabase(this).budgetDao().getBudget(budgetId)
            mFund = RoomDb.getDatabase(this).fundsDao().getFund(
                budgetId, Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.YEAR)
            )
            etCategory.setText(mbudget?.category)
            etCategory.isEnabled = false
            btnSaveBudget.text = "Add Funds"
            toolBar.title = "Update Head"
            etAmount.hint = "Amount"
            image = mbudget?.image
            mbudget?.image?.let {
                val imageBytes = Base64.decode(it, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                img.setImageBitmap(decodedImage)
            }

        }
    }

    private fun saveBudget() {

        if (!TextUtils.isEmpty(etCategory.text)) {
            if (!TextUtils.isEmpty(etAmount.text)) {
                Coroutines.main {
                    val budget = Budget(
                        etCategory.text.toString(), image
                    )
                    val catId = RoomDb.getDatabase(this).budgetDao().insertBudget(budget)
                    val fund = Fund(
                        catId.toInt(), etAmount.text.toString().toDouble(),
                        etAmount.text.toString().toDouble()
                    )
                    val id = RoomDb.getDatabase(this).fundsDao().insertFund(fund)
                    if (id != -1L) {
                        showToast("Head created successfully.")
                        finish()
                    }

                }
            } else {
                showToast("Please enter head amount.")
            }
        } else {
            showToast("Please enter category name.")
        }

    }

    private fun updateBudget() {

        if (!TextUtils.isEmpty(etCategory.text)) {
            if (!TextUtils.isEmpty(etAmount.text)) {
                Coroutines.main {
                    val budget = Budget(
                        budgetId,
                        etCategory.text.toString(),
                        image
                    )
                    RoomDb.getDatabase(this).budgetDao().updateBudget(budget)
                    val fund = Fund(
                        budgetId,
                        etAmount.text.toString().toDouble().plus(mFund?.initialBudget ?: 0.0),
                        etAmount.text.toString().toDouble().plus(mFund?.remainingBudget ?: 0.0)
                    )
                    val id = RoomDb.getDatabase(this).fundsDao().upsert(fund)

                    if (id != -1) {
                        showToast("Head updated successfully.")
                        finish()
                    }

                }
            } else {
                showToast("Please enter head amount.")
            }
        } else {
            showToast("Please enter category name.")
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
                        this@CreateBudgetActivity,
                        object : ImagePicker.Companion.ImagePickerListener {
                            override fun onImagePicked(imageFile: File?, tag: String?) {
                                img.setImageBitmap(BitmapFactory.decodeFile(imageFile?.path))
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


    fun encoder(filePath: String?): String {
        val bytes = File(filePath).readBytes()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun getTime(): String {
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
