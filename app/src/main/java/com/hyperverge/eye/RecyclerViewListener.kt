package com.hyperverge.eye

import android.graphics.Bitmap

interface RecyclerViewListener {
    fun onItemClicked(photo: Bitmap)
}