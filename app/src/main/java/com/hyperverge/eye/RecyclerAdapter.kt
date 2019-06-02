package com.hyperverge.eye

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recyclerview_item_row.view.*


class RecyclerAdapter(private val photos: ArrayList<Bitmap>, val context: Context) : RecyclerView.Adapter<RecyclerAdapter.ViewHolder>() {

    private var mCallback: RecyclerViewListener? = null

    init {
        if (context is RecyclerViewListener)
            mCallback = context
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = parent.inflate(R.layout.recyclerview_item_row, false)
        return ViewHolder(inflatedView)

    }

    override fun getItemCount(): Int = photos.size

    inner class ViewHolder(var view: View) : RecyclerView.ViewHolder(view),View.OnClickListener {


        init {
            view.recycler_view_imageView.setOnClickListener(this)
        }

        override fun onClick(v:View) {
            when(v){
                view.recycler_view_imageView-> mCallback!!.onItemClicked(photos[adapterPosition])
            }
        }

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemPhoto = photos[position]
        holder.view.recycler_view_imageView.setImageBitmap(itemPhoto)

    }


}