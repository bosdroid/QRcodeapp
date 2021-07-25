package com.expert.qrgenerator.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import java.io.IOException

object TextRecogniser {
    private val TAG: String = TextRecogniser::class.java.name
    fun runTextRecognition(context: Context, container: TextInputEditText, imageUri: Uri) {
        var inputImage: InputImage? = null
        try {
            inputImage = InputImage.fromFilePath(context, imageUri)
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        val image = InputImage.fromBitmap(bitMap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        if (inputImage != null) {
            recognizer.process(inputImage)
                    .addOnSuccessListener { texts ->
                        processTextRecognitionResult(context, container, texts)
                    }
                    .addOnFailureListener { e -> // Task failed with an exception
                        e.printStackTrace()
                        Log.d(TAG, "runTextRecognition: ")
                    }
        } else {
            Toast.makeText(context, "Please select appropriate image.", Toast.LENGTH_LONG).show()
        }
    }

    private fun processTextRecognitionResult(context: Context, container: TextInputEditText, texts: Text) {
        val blocks = texts.textBlocks
        if (blocks.size == 0) {
            Toast.makeText(context, "No text found", Toast.LENGTH_LONG).show()
            return
        }
        for (i in blocks.indices) {
            val lines = blocks[i].lines
            for (j in lines.indices) {
                val str = lines[j].text
                container.setText(str + "\n")
            }
        }
    }
}