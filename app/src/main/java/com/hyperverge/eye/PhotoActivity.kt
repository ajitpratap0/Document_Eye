package com.hyperverge.eye

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class PhotoActivity : AppCompatActivity() {
    var fileName:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo)
        val imageView: ImageView = findViewById(R.id.imageview_fullscreen)
        val extras = intent.extras
        fileName=extras!!.getString("filename")
        Glide.with(this).load(Environment.getExternalStorageDirectory().absolutePath + File.separator + fileName)
            .transition(DrawableTransitionOptions.withCrossFade())
            .apply(
                RequestOptions()
                    .transforms(CenterCrop(), RoundedCorners(20))
                    .priority(Priority.HIGH)
                    .override(1280, 960)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
            .into(imageView)
        val shareImageView: ImageView = findViewById(R.id.shareView)
        shareImageView.setOnClickListener() {
            shareImage()
        }
        val downloadImageView: ImageView = findViewById(R.id.downloadView)
        downloadImageView.setOnClickListener() {
            downloadImage()
        }

    }


    private fun downloadImage() {
        Toast.makeText(this,"Image saved: $fileName",Toast.LENGTH_SHORT).show()
    }

    private fun shareImage() {
        val imageShareIntent = Intent(Intent.ACTION_SEND)
        imageShareIntent.type = "image/jpeg"
        imageShareIntent.putExtra(
            Intent.EXTRA_STREAM,
            Uri.parse(Environment.getExternalStorageDirectory().absolutePath + File.separator +fileName )
        )
        startActivity(Intent.createChooser(imageShareIntent, "Share intent"))

    }
}
