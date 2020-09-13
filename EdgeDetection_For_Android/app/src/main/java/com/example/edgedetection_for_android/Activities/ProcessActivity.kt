package com.example.edgedetection_for_android.Activities

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.edgedetection_for_android.Popup.LoadingPopupFragment
import com.example.edgedetection_for_android.R
import com.example.edgedetection_for_android.Utils.DEFINES
import com.google.android.gms.ads.*
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
import java.text.SimpleDateFormat
import java.util.*


val IMAGE_PICK_CODE = 1000
val PERMISSION_REQ_CODE = 2000
val ORIGIN_IMG_NAME_PREFIX = "origin_"
val PROCESSED_IMG_NAME_PREFIX = "processed_"

class ProcessActivity : AppCompatActivity() {

    var mSelectedImgURI: Uri? = null
    var mPopupWindow: LoadingPopupFragment? = null
    var mHTTPClient = OkHttpClient()
    var mSavedFileURL = ""

    lateinit var mAdView : AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_process)

        checkPermission()
        initWidgets()
        initGoogleAd()
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
            showCompleteSavePopup()

            //Toast.makeText(applicationContext,"Download 폴더에 저장이 완료되었습니다.",Toast.LENGTH_SHORT).show()
        }

    }

    private fun initGoogleAd() {
        MobileAds.initialize(this) {}
        mAdView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        mAdView.adListener = object: AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            override fun onAdFailedToLoad(adError : LoadAdError) {
                // Code to be executed when an ad request fails.
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            override fun onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
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

    private fun showCompleteSavePopup(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("저장 완료").setMessage("저장이 완료되었습니다. 저장된 폴더로 이동할까요?")

        builder.setPositiveButton("OK") { dialog, id ->

            /*
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)

             */
            //intent.type = "*/*"
            //startActivityForResult(intent, 4000)

             var i = Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("image/*"); //여러가지 Type은 아래 표로 정리해두었습니다.
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

            //startActivity(i)
            //startActivityForResult(Intent.createChooser(i, "Get Album"), 3000)

            startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))


        }

        builder.setNegativeButton("Cancel") { dialog, id ->
            //Toast.makeText(applicationContext, "Cancel Click", Toast.LENGTH_SHORT).show()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
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

        val sdf = SimpleDateFormat("yyyyddMM_hhmmss")
        val currentDate = sdf.format(Date())

        mSavedFileURL = baseFolder + File.separator.toString() + "output_$currentDate.jpg"

        val file = File(mSavedFileURL)
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