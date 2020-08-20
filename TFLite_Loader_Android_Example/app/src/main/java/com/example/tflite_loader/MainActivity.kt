package com.example.tflite_loader

import android.R.attr
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tflite_loader.Keys.MODEL_PATH
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


object Keys {
    const val MODEL_PATH = "result.tflite"
    const val LABEL_PATH = "labels.txt"
    const val INPUT_SIZE = 224
    const val MAX_RESULTS = 3
    const val DIM_BATCH_SIZE = 1
    const val DIM_PIXEL_SIZE = 3
    const val DIM_IMG_SIZE_X = 224
    const val DIM_IMG_SIZE_Y = 224
}

class MainActivity : AppCompatActivity() {

    var interpreter : Interpreter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initTFLiteFile()

    }

    //Interpreter 한테 tflite파일을 집어넣고, 이놈이 실제적으로 추론 등을 실시하는 오브젝트인 듯 하네요.
    private fun initTFLiteFile() {

        val assetManager = applicationContext.assets
        val model = loadModelFile(assetManager,MODEL_PATH)

        val options = Interpreter.Options()
        interpreter = Interpreter(model, options)

        //이니셜라이즈가 완료되면 실행버튼 활성화
        btn_run.setOnClickListener {
            classify((resources.getDrawable(R.drawable.answer_example) as BitmapDrawable).bitmap)
        }
        btn_run.isEnabled = true

        Log.i("kky","모델 input 부분의 shape을 가져옵니다.")
        val inputShape = interpreter!!.getInputTensor(0).shape()
        inputShape.forEach {
            //mnist 28 x 28 이 출력됨을 확인할 수 있다.
            // [1 , 28 , 28 , 1]
            // 각각 float_type , width, height, pixel size
            Log.i("kky","inputShape : ${it}")
        }
        
        Log.i("kky","모델 output 부분의 shape을 가져옵니다.")
        val outputShape = interpreter!!.getOutputTensor(0).shape()
        outputShape.forEach {
            //mnist의 label은 10개이고 , 1 x 10 이 출력됨을 확인할 수 있다.
            // [1 , 10]
            Log.i("kky","outputShape : ${it}")
        }

        //resIdToByteArray(R.drawable.answer_example)

    }

    /*
    private fun resIdToByteArray(resId : Int) : ByteArray{
        var bitmap = (resources.getDrawable(resId) as BitmapDrawable).bitmap
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        val image_byte_array = stream.toByteArray()
        return image_byte_array
    }
     */

    //TODO: 데이터 타입때문에 classify하다 죽는데, 아래 링크 참고해야함.
    //https://github.com/ZZANZUPROJECT/TFLite-Object-Detection/blob/master/app/src/main/java/com/example/android/alarmapp/tflite/TensorFlowImageClassifier.java
    fun classify(bitmap: Bitmap) {

        val resizedImage = Bitmap.createScaledBitmap(bitmap, 28, 28, true)
        val byteBuffer = convertBitmapToByteBuffer(resizedImage)

        val result = Array(1) { Array(128) { FloatArray(98)} }
        interpreter?.run(byteBuffer, result)

        Log.i("kky","[MainActivity]result : $result")

    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteArray {
        val size: Int = bitmap.rowBytes * bitmap.height
        val byteBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(byteBuffer)
        val byteArray = byteBuffer.array()

        return byteArray
    }


    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

}