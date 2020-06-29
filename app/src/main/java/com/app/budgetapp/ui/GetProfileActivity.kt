package com.app.budgetapp.ui

import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import com.app.budgetapp.R
import com.app.budgetapp.utils.SharedPrefs
import com.app.budgetapp.utils.clearAllGoToActivity
import com.app.budgetapp.utils.showToast
import com.example.imagerecognition.utils.ImagePicker
import com.example.imagerecognition.utils.PermissionsUtil
import kotlinx.android.synthetic.main.activity_get_profile.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class GetProfileActivity : AppCompatActivity() {

    var permissionsUtil: PermissionsUtil? = null
    var image: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_get_profile)

        btnSelectImage.setOnClickListener {
            getImage()
        }

        btnSave.setOnClickListener {
            saveData()
        }
    }

    private fun saveData() {
        if(image!=null){
            if(!TextUtils.isEmpty(etName.text)){
                if(!TextUtils.isEmpty(etEmail.text)){
                    SharedPrefs(this).saveValue("IMAGE",image)
                    SharedPrefs(this).saveValue("NAME",etName.text.toString().trim())
                    SharedPrefs(this).saveValue("EMAIL",etEmail.text.toString().trim())
                    SharedPrefs(this).saveValue("LOGGEDIN",true)
                    clearAllGoToActivity(NavigationActivity::class.java)


                }else{
                    showToast("Please enter email.")
                }
            }else{
                showToast("Please enter name.")
            }
        }else{
            showToast("Please select profile image.")
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
                        this@GetProfileActivity,
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
