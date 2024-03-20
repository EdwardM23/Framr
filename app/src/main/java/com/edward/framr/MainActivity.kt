package com.edward.framr

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.edward.framr.databinding.ActivityMainBinding
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnPickImage.setOnClickListener{
            pickImageFromGallery()
        }
    }

    var activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val image = result.data!!.data
            var img_final = image?.let {
                uriToBitmap(this,
                    it
                )?.let { addWhiteFrameToImage(it, 100) }
            }
            binding.imgDisplay.setImageBitmap(img_final)
        } else if (result.resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Pick image canceled", Toast.LENGTH_SHORT).show()
        }
    }

    fun pickImageFromGallery() {
        val pickPhoto = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )

        activityResultLauncher.launch(pickPhoto)
    }

    fun addWhiteFrameToImage(inputBitmap: Bitmap, frameWidth: Int): Bitmap {
        val width = inputBitmap.width
        val height = inputBitmap.height

        // Calculate the dimensions for the result bitmap (including the frame)
        val resultWidth = width + 2 * frameWidth
        val resultHeight = height + 2 * frameWidth + 300

        // Create a new bitmap with the dimensions for the result image (including the frame)
        val resultBitmap = Bitmap.createBitmap(resultWidth, resultHeight, Bitmap.Config.ARGB_8888)

        // Create a canvas using the result bitmap
        val canvas = Canvas(resultBitmap)

        // Draw the white frame (a rectangle) onto the canvas
        val framePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL // Fill the rectangle with the specified color
        }
        canvas.drawRect(0f, 0f, resultWidth.toFloat(), resultHeight.toFloat(), framePaint)

        // Draw the input bitmap onto the canvas, shifted by the frame width
        canvas.drawBitmap(inputBitmap, frameWidth.toFloat(), frameWidth.toFloat(), null)

        return resultBitmap
    }

    fun uriToBitmap(context: Context, imageUri: Uri): Bitmap? {
        var inputStream: InputStream? = null
        try {
            // Open an input stream from the image URI
            inputStream = context.contentResolver.openInputStream(imageUri)

            // Decode the input stream into a Bitmap
            return BitmapFactory.decodeStream(inputStream)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } finally {
            try {
                // Close the input stream
                inputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}