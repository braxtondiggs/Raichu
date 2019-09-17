package com.cymbit.plastr.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.cymbit.plastr.R
import com.google.android.material.snackbar.Snackbar

class SubAdapter (private val data: MutableList<String>) : RecyclerView.Adapter<SubAdapter.ViewHolder>() {
    private var removedPosition: Int = 0
    private lateinit  var removedItem: String

    class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_sub, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       holder.title.text = data[position]
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPosition = viewHolder.adapterPosition
        removedItem = data[viewHolder.adapterPosition]

        data.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        Snackbar.make(viewHolder.itemView, "$removedItem deleted.", Snackbar.LENGTH_LONG).setAction("UNDO") {
            data.add(removedPosition, removedItem)
            notifyItemInserted(removedPosition)
        }.show()
    }

    fun addItem(item: String) {
        data.add(itemCount, item)
        notifyItemInserted(itemCount)
    }
}