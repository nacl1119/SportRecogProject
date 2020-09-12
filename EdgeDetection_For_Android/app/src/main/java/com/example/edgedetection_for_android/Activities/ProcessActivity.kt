package com.example.edgedetection_for_android.Activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.edgedetection_for_android.Popup.LoadingPopupFragment
import com.example.edgedetection_for_android.R
import com.example.edgedetection_for_android.Utils.DEFINES
import kotlinx.android.synthetic.main.activity_process.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okio.IOException
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


val IMAGE_PICK_CODE = 1000
val PERMISSION_REQ_CODE = 2000
val ORIGIN_IMG_NAME_PREFIX = "origin_"
val PROCESSED_IMG_NAME_PREFIX = "processed_"

class ProcessActivity : AppCompatActivity() {

    var mSelectedImgURI: Uri? = null
    var mPopupWindow: LoadingPopupFragment? = null
    var mHTTPClient = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        checkPermission()
        initWidgets()
    }

    private fun initWidgets() {
        activity_process_start_process_layout.setOnClickListener {
            //갤러리에 들어가서 이미지를 선택한다.
            //Intent to pick image
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(
                intent,
                IMAGE_PICK_CODE
            )
        }

        activity_process_upload_btn.setOnClickListener {
            //그림을 서버로 업로드한다.

            val origin_img_bitmap =
                MediaStore.Images.Media.getBitmap(contentResolver, mSelectedImgURI)

            visiblePopupWindow(true, "원본 이미지 처리중입니다..")

            //원본 이미지를 파일로 저장한다.
            bitmapToFile(origin_img_bitmap, "$ORIGIN_IMG_NAME_PREFIX")

            visiblePopupWindow(false)

            //업로드 시작.
            uploadTalentShareDataToSvr()

        }

        activity_process_download_processed_image.setOnClickListener {
            copyToDownloadFolder()
            Toast.makeText(applicationContext,"Download 폴더에 저장이 완료되었습니다.",Toast.LENGTH_SHORT).show()
        }

    }
    
    private fun uploadTalentShareDataToSvr() {

        val wrapper = ContextWrapper(this)
        var imgFolder = wrapper.getDir("Images", Context.MODE_PRIVATE)
        val files: Array<File> = imgFolder.listFiles()

        visiblePopupWindow(true, "서버로 데이터 업로드 중입니다..")

        var requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", "profile.png",
                files[0].asRequestBody("image/jpeg".toMediaTypeOrNull())
            )
            .build()


        val request = Request.Builder()
            .url(DEFINES.REST.BASE_URL + DEFINES.REST.IMG_UPLOAD_PATH)
            .post(requestBody)
            .build()

        mHTTPClient
            .newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")

                        /*
                        val res_string = response.body!!.string()
                        Log.i("kky","res_string : $res_string")
                         */

                        val res = response.body!!.byteStream()
                        Log.i("kky", "res : $res")


                        if (res != null) {
                            try {
                                openFileOutput("aa.jpg", Context.MODE_PRIVATE).use { output ->
                                    output.write(res.readBytes())
                                }
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }

                        response.body!!.close()

                        CoroutineScope(Dispatchers.Main).launch {

                            activity_process_preview_processed_image_download.visibility =
                                View.VISIBLE
                            activity_process_download_processed_image.visibility = View.VISIBLE

                            val image = BitmapFactory.decodeStream(openFileInput("aa.jpg"))
                            activity_process_processed_image.setImageBitmap(image)

                            //copyToDownloadFolder()

                            visiblePopupWindow(false)



                        }
                    }
                }
            })


    }


    //갤러리에서 선택한 이미지의 데이터가 여기로 전달된다.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode){
            IMAGE_PICK_CODE->{
                if(resultCode == Activity.RESULT_OK){
                    try {
                        mSelectedImgURI = data!!.data
                        activity_process_start_process_layout_no_image_layout.visibility = View.GONE
                        activity_process_start_process_layout_original_image_layout.visibility =
                            View.VISIBLE
                        activity_process_upload_btn.visibility = View.VISIBLE
                        activity_process_start_process_layout_original_image.setImageURI(mSelectedImgURI)

                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            PERMISSION_REQ_CODE->{
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                ){
                    checkPermission()
                }
            }
        }

    }

    private fun copyToDownloadFolder() {
        val baseFolder: String
        baseFolder = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        } else {
            filesDir.absolutePath
        }

        val file = File(baseFolder + File.separator.toString() + "output.jpg")
        file.parentFile.mkdirs()

        val fos = FileOutputStream(file)
        fos.write(openFileInput("aa.jpg").readBytes())
        fos.flush()
        fos.close()
    }

    //비트맵 데이터를 파일로 쓰는 함수이다.
    private fun bitmapToFile(bitmap: Bitmap, fileName: String): Boolean {

        Log.i("kky", "[MakeTalentShareFragment]bitmapToFile is Called. fileName : $fileName")

        val wrapper = ContextWrapper(this)
        var file = wrapper.getDir("Images", Context.MODE_PRIVATE)
        //file = File(file,"${UUID.randomUUID()}.jpg")
        file = File(file, "${fileName}.jpg")

        try {
            // Compress the bitmap and save in jpg format
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.flush()
            stream.close()

            Log.i("kky", "[MakeTalentShareFragment]bitmapToFile is End. fileName : $fileName")
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

    }

    private fun visiblePopupWindow(visible: Boolean, msg: String = "로딩중입니다..") {
        mPopupWindow = LoadingPopupFragment

        when (visible) {
            true -> {
                mPopupWindow?.show(supportFragmentManager, "POPUP_TAG")
                mPopupWindow?.setMessage(msg)
            }
            false -> {
                mPopupWindow?.dismiss()
            }
        }
    }

    private fun checkPermission() {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
        ){
            //외부 저장소 읽기, 쓰기 런타임 권한을 요청한다.
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, PERMISSION_REQ_CODE)
        }

    }


}