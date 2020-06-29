package com.example.imagerecognition.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.app.budgetapp.R


class PermissionsUtil(private var context: Activity?) {
    private var permissionListener: PermissionListener? = null
    // --Commented out by Inspection (9/17/2018 5:36 PM):String tag;
    private val requestCode = 1001
    private var permission: String? = null
    private var selpermission1: String? = null
    private var selpermission2: String? = null
    private var permissionsArray: Array<String>?=null

    fun askPermission(
        context: Activity,
        selPermission: String,
        permissionListener: PermissionListener
    ) {
        this.permissionsArray= arrayOf(selPermission)
        this.context = context
        this.permissionListener = permissionListener
        permission = selPermission
        if (ContextCompat.checkSelfPermission(
                context,
                permission!!
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(context, arrayOf(permission!!), requestCode)
        } else {
            permissionListener.onPermissionResult(true)
        }
    }

    fun askPermissions(
        context: Activity,
        permission1: String,
        permission2: String,
        permissionListener: PermissionListener
    ) {
        this.permissionsArray= arrayOf(permission1,permission2)
        this.context = context
        this.permissionListener = permissionListener
        selpermission1 = permission1
        selpermission2 = permission2
        if (ContextCompat.checkSelfPermission(
                context,
                permission1
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                context,
                permission2
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(permission1, permission2),
                requestCode
            )
        } else {
            permissionListener.onPermissionResult(true)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        permissionsArray=permissions
        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionListener!!.onPermissionResult(true)
        } else if (grantResults.size > 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                permissionListener!!.onPermissionResult(true)
            } else {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permission = selpermission1
                    showAgainPermissionDialog(selpermission1)
                } else{
                    permission= selpermission2
                    showAgainPermissionDialog(selpermission2)
                }
            }
        } else {
            showAgainPermissionDialog(permission)
        }
    }

    fun onRequestPermissionsResultFrag(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
        permissionListener: PermissionListener
    ) {
        this.permissionsArray=permissions
        this.permissionListener = permissionListener
        if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            permissionListener.onPermissionResult(true)
        } else if (grantResults.size > 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                permissionListener.onPermissionResult(true)
            } else {

                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    permission = selpermission1
                    showAgainPermissionDialog(selpermission1)
                } else {
                    permission = selpermission2
                    showAgainPermissionDialog(selpermission2)
                }
            }
        } else {
            showAgainPermissionDialog(permission)
        }
    }

    private fun showAgainPermissionDialog(permission: String?) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(context!!, permission!!)) {
            val alertBuilder = AlertDialog.Builder(context!!)
            alertBuilder.setCancelable(true)
            alertBuilder.setTitle("Permission Required")
            when (permission) {
                STORAGE -> alertBuilder.setMessage(storagePermInfoMsg)
                READ_SMS -> alertBuilder.setMessage(smsPermInfoMsg)
                RECEIVE_SMS -> alertBuilder.setMessage(smsPermInfoMsg)
                ACCOUNTS -> alertBuilder.setMessage(accountsPermInfoMsg)
                READ_CALENDAR -> alertBuilder.setMessage(calendarPermInfoMsg)
                WRITE_CALENDAR -> alertBuilder.setMessage(calendarPermInfoMsg)
                CAMERA -> alertBuilder.setMessage(cameraPermInfoMsg)
                LOCATION -> alertBuilder.setMessage(locationPermInfoMsg)
                READ_CONTACTS -> alertBuilder.setMessage(contactsPermInfoMsg)
                WRITE_CONTACTS -> alertBuilder.setMessage(contactsPermInfoMsg)
                RECORD_AUDIO -> alertBuilder.setMessage(RecordPermInfoMsg)
            }



            alertBuilder.setPositiveButton("Grant") { _, _ ->
                ActivityCompat.requestPermissions(
                    context!!,
                    permissionsArray!!,
                    requestCode
                )
            }
            alertBuilder.setNegativeButton("Deny") { dialog, _ ->
                dialog.dismiss()
                permissionListener!!.onPermissionResult(false)
            }
            val alert = alertBuilder.create()
            alert.show()
        } else {
            when (permission) {
                STORAGE -> Toast.makeText(context, storagePermErrorMsg, Toast.LENGTH_LONG).show()
                READ_SMS -> Toast.makeText(context, smsPermErrorMsg, Toast.LENGTH_LONG).show()
                RECEIVE_SMS -> Toast.makeText(context, smsPermErrorMsg, Toast.LENGTH_LONG).show()
                ACCOUNTS -> Toast.makeText(context, accountsPermErrorMsg, Toast.LENGTH_LONG).show()
                READ_CALENDAR -> Toast.makeText(context, calendarPermErrorMsg, Toast.LENGTH_LONG).show()
                WRITE_CALENDAR -> Toast.makeText(context, calendarPermErrorMsg, Toast.LENGTH_LONG).show()
                CAMERA -> Toast.makeText(context, cameraPermErrorMsg, Toast.LENGTH_LONG).show()
                LOCATION -> Toast.makeText(context, locationPermErrorMsg, Toast.LENGTH_LONG).show()
                READ_CONTACTS -> Toast.makeText(context, contactsPermErrorMsg, Toast.LENGTH_LONG).show()
                WRITE_CONTACTS -> Toast.makeText(context, contactsPermErrorMsg, Toast.LENGTH_LONG).show()
                RECORD_AUDIO -> Toast.makeText(context, RecordPermInfoMsg, Toast.LENGTH_LONG).show()
            }

            permissionListener!!.onPermissionResult(false)
        }
    }

    interface PermissionListener {
        fun onPermissionResult(isGranted: Boolean)
    }

    companion object {
        private const val READ_CONTACTS = Manifest.permission.READ_CONTACTS
        private const val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS
        var STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE
        var RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
        var CAMERA = Manifest.permission.CAMERA
        private const val READ_SMS = Manifest.permission.READ_SMS
        private const val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
        var LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private const val READ_CALENDAR = Manifest.permission.READ_CALENDAR
        private const val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR
        private const val ACCOUNTS = Manifest.permission.GET_ACCOUNTS


        private const val storagePermInfoMsg =
            "App needs this permission to store files on phone's storage. Are you sure you want to deny this permission ?"
        private const val storagePermErrorMsg =
            "Storage permission denied. You can enable permission from settings"

        private const val contactsPermInfoMsg =
            "App needs this permission to access phone contacts. Are you sure you want to deny this permission ?"
        private const val contactsPermErrorMsg =
            "Contacts permission denied. You can enable permission from settings"

        private const val smsPermInfoMsg =
            "App needs this permission to receive/read SMS for auto detection of OTP. Are you sure you want to deny this permission ?"
        private const val smsPermErrorMsg =
            "SMS permission denied. You can enable permission from settings"

        private const val accountsPermInfoMsg =
            "App needs this permission to access Google Account on phone. Are you sure you want to deny this permission ?"
        private const val accountsPermErrorMsg =
            "Contacts permission denied. You can enable permission from settings"

        private const val cameraPermInfoMsg =
            "App needs this permission to capture photos using phone's camera. Are you sure you want to deny this permission ?"
        private const val cameraPermErrorMsg =
            "Camera permission denied. You can enable permission from settings"

        private const val calendarPermInfoMsg =
            "App needs this permission to access phone's calendar. Are you sure you want to deny this permission ?"
        private const val calendarPermErrorMsg =
            "Calendar permission denied. You can enable permission from settings"

        private const val locationPermInfoMsg =
            "App needs this permission to access your location. Are you sure you want to deny this permission ?"

        private const val RecordPermInfoMsg =
            "App needs this permission to access your Audio. Are you sure you want to deny this permission ?"

        private const val locationPermErrorMsg =
            "Location permission denied. You can enable permission from settings"
    }
}