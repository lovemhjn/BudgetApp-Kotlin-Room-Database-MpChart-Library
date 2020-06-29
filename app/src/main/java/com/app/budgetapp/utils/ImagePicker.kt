package com.example.imagerecognition.utils

import android.app.Activity
import android.content.ClipData
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class ImagePicker {
    companion object {

        var mFileCaptured: File? = null
        var mImageCaptureUri: Uri? = null
        var imagePickerListener: ImagePickerListener? = null
        var context: Activity? = null
        var fileProfilePic: File? = null
        var tag: String? = null
        var appDir: File? = null

        var picWidth = 400
        var picHeight = 400

        var cropImage = false
        var compressImage = false


        fun selectImage(
            context: Context,
            imagePickerListener: ImagePickerListener?,
            tag: String?,
            title: String,
            compressImage: Boolean
        ) {
            val items = arrayOf<CharSequence>(
                "Camera", "Gallery"
            )
            val builder =
                AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setItems(items) { dialog, item ->
                if (items[item] == "Camera") {
                    pickFromCamera2(context as Activity, imagePickerListener, tag!!, true)
                } else if (items[item] == "Gallery") {
                    pickFromGallery(context as Activity, imagePickerListener, tag, true)
                }
            }
            builder.show()
        }

        fun pickFromCamera(
            context: Activity,
            imagePickerListener: ImagePickerListener?,
            tag: String?,
            compressImage: Boolean
        ) {
            ImagePicker.tag = tag
            ImagePicker.compressImage = compressImage
            cropImage = false
            ImagePicker.imagePickerListener = imagePickerListener
            ImagePicker.context = context
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(context.packageManager) != null) { // Create the File where the photo should go
                try {
                    mFileCaptured = createImageFile()
                } catch (ex: IOException) {
                    val error = ex.localizedMessage
                    Log.e("Exception:", error)
                }
                // Continue only if the File was successfully created
                if (mFileCaptured != null) {
                    mImageCaptureUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        mFileCaptured!!
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
                    takePictureIntent.putExtra("return-data", true)
                    context.startActivityForResult(takePictureIntent, 10000)
                }
            }
        }


        fun pickFromGallery(
            context: Activity,
            imagePickerListener: ImagePickerListener?,
            tag: String?,
            compressImage: Boolean
        ) {
            ImagePicker.tag = tag
            ImagePicker.compressImage = compressImage
            cropImage = false
            ImagePicker.imagePickerListener = imagePickerListener
            ImagePicker.context = context
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT //
            context.startActivityForResult(Intent.createChooser(intent, "Select File"), 30000)
        }


        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == 10000) {
                    //imagePickerListener!!.onImagePicked(mFileCaptured, tag)
                    mFileCaptured = writeToFile(mImageCaptureUri!!);
                    imagePickerListener?.onImagePicked(mFileCaptured, tag);
                } else if (requestCode == 20000) {
                    imagePickerListener!!.onImagePicked(fileProfilePic, tag)
                } else if (requestCode == 30000 && data != null) {
                    try {
                        var selectedFile: File? = null
                        val uri = data.data
                        var fileName =
                            queryName(context!!.contentResolver, uri)
                        val filenameArray =
                            fileName.split("\\.").toTypedArray()
                        val extension = filenameArray[filenameArray.size - 1]
                        fileName = "$tag.$extension"
                        val input =
                            context!!.contentResolver.openInputStream(uri!!)
                        try {
                            selectedFile = File(context!!.filesDir, fileName)
                            val output: OutputStream =
                                FileOutputStream(selectedFile)
                            output.use { output ->
                                val buffer =
                                    ByteArray(4 * 1024) // or other buffer size
                                var read: Int
                                while (input!!.read(buffer).also { read = it } != -1) {
                                    output.write(buffer, 0, read)
                                }
                                output.flush()
                            }
                        } finally {
                            input!!.close()
                        }
                        fileProfilePic = selectedFile
                        imagePickerListener!!.onImagePicked(fileProfilePic, tag)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }
        }

        @Throws(IOException::class)
        fun createImageFile(): File? {
            appDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            return File(appDir, "$tag.png")
        }

        private fun writeToFile(fileUri: Uri): File? {
            var selectedFile: File? = null
            var fileName = queryName(context!!.contentResolver, fileUri)
            val filenameArray = fileName.split("\\.").toTypedArray()
            val extension = filenameArray[filenameArray.size - 1]
            fileName = "$tag.$extension"
            try {
                val input =
                    context!!.contentResolver.openInputStream(fileUri)
                selectedFile = File(context!!.filesDir, fileName)
                val output: OutputStream = FileOutputStream(selectedFile)
                val buffer = ByteArray(4 * 1024) // or other buffer size
                var read: Int
                while (input!!.read(buffer).also { read = it } != -1) {
                    output.write(buffer, 0, read)
                }
                output.flush()
                output.close()
                input.close()
                var bm =
                    decodeFile(selectedFile.absolutePath, 800, 800, ScalingLogic.FIT)
                bm = fixOrientation(bm, selectedFile.absolutePath)
                val outputStream = FileOutputStream(selectedFile)
                bm.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: Exception) { //e.printStackTrace();
                return null
            }
            return selectedFile
        }

        private fun decodeFile(
            path: String?, dstWidth: Int, dstHeight: Int,
            scalingLogic: ScalingLogic
        ): Bitmap {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            BitmapFactory.decodeFile(path, options)
            options.inJustDecodeBounds = false
            options.inSampleSize = calculateSampleSize(
                options.outWidth, options.outHeight, dstWidth,
                dstHeight, scalingLogic
            )
            return BitmapFactory.decodeFile(path, options)
        }

        enum class ScalingLogic {
            FIT,CROP
        }

        private fun calculateSampleSize(
            srcWidth: Int, srcHeight: Int, dstWidth: Int, dstHeight: Int,
            scalingLogic: ScalingLogic
        ): Int {
            return if (scalingLogic == ScalingLogic.FIT) {
                val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
                val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
                if (srcAspect > dstAspect) {
                    srcWidth / dstWidth
                } else {
                    srcHeight / dstHeight
                }
            } else {
                val srcAspect = srcWidth.toFloat() / srcHeight.toFloat()
                val dstAspect = dstWidth.toFloat() / dstHeight.toFloat()
                if (srcAspect > dstAspect) {
                    srcHeight / dstHeight
                } else {
                    srcWidth / dstWidth
                }
            }
        }


        private fun queryName(
            resolver: ContentResolver,
            uri: Uri?
        ): String {
            val returnCursor = resolver.query(uri!!, null, null, null, null)!!
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            val name = returnCursor.getString(nameIndex)
            returnCursor.close()
            return name
        }


        fun fixOrientation(bm: Bitmap, filePath: String?): Bitmap {
            var bitmap: Bitmap? = null
            var ei: ExifInterface? = null
            return try {
                ei = ExifInterface(filePath)
                if (ei != null) {
                    val orientation = ei.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_UNDEFINED
                    )
                    bitmap = when (orientation) {
                        ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bm, 90f)
                        ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bm, 180f)
                        ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bm, 270f)
                        ExifInterface.ORIENTATION_NORMAL -> bm
                        else -> bm
                    }
                    bitmap ?: bm
                } else {
                    bm
                }
            } catch (e: Exception) {
                e.printStackTrace()
                bm
            }
        }

        fun rotateImage(source: Bitmap, angle: Float): Bitmap? {
            val matrix = Matrix()
            matrix.postRotate(angle)
            return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height, matrix,
                true
            )
        }

        fun pickFromCamera2(
            context: Activity,
            imagePickerListener: ImagePickerListener?,
            tag: String,
            compressImage: Boolean
        ) {
            ImagePicker.tag = tag
            ImagePicker.compressImage = compressImage
            cropImage = false
            ImagePicker.imagePickerListener = imagePickerListener
            ImagePicker.context = context
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                val state = Environment.getExternalStorageState()
                if (Environment.MEDIA_MOUNTED == state) {
                    appDir =
                        Companion.context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    if (!appDir!!.exists()) {
                        appDir!!.mkdirs()
                    }
                    mFileCaptured = File(appDir, "$tag.png")
                    mImageCaptureUri = FileProvider.getUriForFile(
                        context,
                        context.applicationContext.packageName + ".provider",
                        mFileCaptured!!
                    )
                }
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) {
                    intent.clipData = ClipData.newRawUri("", mImageCaptureUri)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri)
                intent.putExtra("return-data", true)
                context.startActivityForResult(intent, 10000)
            } catch (e: java.lang.Exception) {
                val error = e.localizedMessage
                Log.e("Exception:", error)
            }
        }

        interface ImagePickerListener {
            fun onImagePicked(imageFile: File?, tag: String?)
        }

        private fun compressToFile(filePath: String?): File? {
            var selectedFile: File? = null
            try {
                var bm = decodeFile(filePath, picWidth, picHeight, ScalingLogic.CROP)
                bm = fixOrientation(bm, filePath)
                selectedFile = File(context!!.filesDir, tag.toString() + ".jpg")
                val outputStream = FileOutputStream(selectedFile)
                bm.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: java.lang.Exception) {
                return null
            }
            return selectedFile
        }

    }



}