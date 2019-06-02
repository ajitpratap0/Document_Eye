package com.hyperverge.eye

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.*
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.scanlibrary.ScanConstants
import com.scanlibrary.ScanActivity
import android.content.Intent
import android.provider.MediaStore
import android.graphics.Bitmap
import android.R.attr.data
import androidx.core.app.NotificationCompat.getExtras
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.recyclerview_item_row.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.util.ArrayList


class MainActivity : AppCompatActivity(), RecyclerViewListener {

    private val PERMISSION_REQUEST_CODE = 200
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: RecyclerAdapter
    private lateinit var gridLayoutManager: GridLayoutManager
    private var photosList: ArrayList<Bitmap> = ArrayList()
    private var fileName:String?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view_main)
        linearLayoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, 2)

        recyclerView.layoutManager = gridLayoutManager
        adapter = RecyclerAdapter(photosList, this)
        recyclerView.adapter = adapter
        setRecyclerViewItemListener()

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation_main)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_camera ->
                    if (checkPermission()) {
                        openCamera()
                    } else {
                        requestPermission()
                    }
                R.id.action_gallery -> if (checkPermission()) {
                    openGallery()
                } else {
                    requestPermission()
                }

            }
            true
        }

    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf<String>(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(applicationContext, "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(
                                this,
                                Manifest.permission.CAMERA
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            showMessageOKCancel("You need to allow access permissions",
                                DialogInterface.OnClickListener { _, _ ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermission()
                                    }
                                })
                        }
                    }
                }
            }
        }
    }

    private fun showMessageOKCancel(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun openCamera() {
        Toast.makeText(
            this@MainActivity,
            "Camera Option Clicked",
            Toast.LENGTH_SHORT
        ).show()

        val REQUEST_CODE = 99
        val preference = ScanConstants.OPEN_CAMERA
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, REQUEST_CODE)

    }

    private fun openGallery() {
        Toast.makeText(
            this@MainActivity,
            "Gallery Option Selected",
            Toast.LENGTH_SHORT
        ).show()

        val REQUEST_CODE = 100
        val preference = ScanConstants.OPEN_MEDIA
        val intent = Intent(this, ScanActivity::class.java)
        intent.putExtra(ScanConstants.OPEN_INTENT_PREFERENCE, preference)
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 99 || requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.extras?.getParcelable(ScanConstants.SCANNED_RESULT)!!
            var bitmap: Bitmap? = null
            try {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                contentResolver.delete(uri, null, null)
                photosList.add(bitmap)
                adapter.notifyItemInserted(photosList.size)
                adapter.notifyDataSetChanged()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }

    }


    private fun setRecyclerViewItemListener() {
        val itemTouchCallback =
            object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    viewHolder1: RecyclerView.ViewHolder
                ): Boolean {
                    //2
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
                    //3
                    val position = viewHolder.adapterPosition
                    photosList.removeAt(position)
                    recycler_view_main.adapter!!.notifyItemRemoved(position)
                }
            }
        //4
        val itemTouchHelper = ItemTouchHelper(itemTouchCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view_main)
    }

    override fun onItemClicked(photo: Bitmap) {
        val showPhotoIntent = Intent(this, PhotoActivity::class.java)
        val stream = ByteArrayOutputStream()
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream)
        val byteArray = stream.toByteArray()
        fileName="Hyperverge"+System.currentTimeMillis()+".jpg"
        write(fileName!!,photo)
//        showPhotoIntent.putExtra("picture",byteArray)
        showPhotoIntent.putExtra("filename", fileName)
        startActivity(showPhotoIntent)
    }

    private fun write(fileName: String, bitmap: Bitmap) {
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
        val outputStream: FileOutputStream
        val file = File(Environment.getExternalStorageDirectory().absolutePath + File.separator + fileName)
        try {
            file.createNewFile()
            outputStream = FileOutputStream(file)
            outputStream.write(bos.toByteArray())
            outputStream.close()
        } catch (error: Exception) {
            error.printStackTrace()
        }

    }


}
