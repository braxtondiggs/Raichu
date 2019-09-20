package com.cymbit.plastr.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.cymbit.plastr.R
import com.cymbit.plastr.helpers.Preferences
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.Iconics.applicationContext

class SubAdapter(private val data: MutableList<String>, private val selected: MutableList<Int>) : RecyclerView.Adapter<SubAdapter.ViewHolder>() {
    private var removedPosition: Int = 0
    private lateinit var removedItem: String

    class ViewHolder(v: View, val context: Context) : RecyclerView.ViewHolder(v) {
        val checkbox: CheckBox = v.findViewById(R.id.checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.grid_sub, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.checkbox.text = data[position]
        holder.checkbox.isChecked = selected.contains(position)
        holder.checkbox.setOnCheckedChangeListener { button, isChecked -> onChecked(data[position], isChecked, holder.context, holder.itemView, button) }
    }

    fun removeItem(viewHolder: RecyclerView.ViewHolder) {
        removedPosition = viewHolder.adapterPosition
        removedItem = data[viewHolder.adapterPosition]

        data.removeAt(viewHolder.adapterPosition)
        notifyItemRemoved(viewHolder.adapterPosition)

        selected.remove(viewHolder.adapterPosition)
        Preferences().setAllSubs(applicationContext, data.toSet())
        val items = Preferences().getSelectedSubs(applicationContext)
        if (items.contains(removedItem)) {
            items.remove(removedItem)
            Preferences().setSelectedSubs(applicationContext, items)
        }
        Snackbar.make(viewHolder.itemView, "$removedItem deleted.", Snackbar.LENGTH_LONG).setAction("UNDO") {
            data.add(removedPosition, removedItem)
            notifyItemInserted(removedPosition)
        }.show()
    }

    fun addItem(item: String) {
        data.add(itemCount, item)
        selected.add(itemCount)
        notifyItemInserted(itemCount)
    }

    private fun onChecked(item: String, isChecked: Boolean, context: Context, view: View, button: CompoundButton) {
        val items = Preferences().getSelectedSubs(context)
        if (items.size > 1 || (items.size == 1 && isChecked)) {
            if (isChecked) items.add(item) else items.remove(item)
            Preferences().setSelectedSubs(context, items)
            if (view.isAttachedToWindow) Snackbar.make(view, "Saved", Snackbar.LENGTH_SHORT).show()
        } else {
            button.isChecked = true
            MaterialDialog(context).show {
                title(R.string.bitmap_error_title)
                message(R.string.minimum)
                positiveButton(R.string.ok)
            }
        }
    }
}