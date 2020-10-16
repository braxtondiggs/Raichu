package com.cymbit.plastr

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.cymbit.plastr.adapter.SubAdapter
import com.cymbit.plastr.helpers.Preferences
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.iconics.IconicsColor
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.IconicsSize
import com.mikepenz.iconics.typeface.library.googlematerial.GoogleMaterial
import com.mikepenz.iconics.utils.colorRes
import kotlinx.android.synthetic.main.activity_sub.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import java.util.*

class SubActivity : AppCompatActivity() {
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var swipeBackground: ColorDrawable = ColorDrawable(Color.parseColor("#FF0000"))
    private lateinit var deleteIcon: IconicsDrawable
    private lateinit var backIcon: IconicsDrawable
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sub)

        deleteIcon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_delete).size(IconicsSize.dp(16)).color(IconicsColor.colorInt(Color.WHITE))
        backIcon =
            IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_arrow_back).size(IconicsSize.dp(16)).color(IconicsColor.colorInt(Color.WHITE))
        toolbar.title = getString(R.string.select_sub)
        toolbar.navigationIcon = backIcon
        toolbar.setNavigationOnClickListener { finish() }
        setSupportActionBar(toolbar)

        fab_sub.setImageDrawable(IconicsDrawable(this).icon(GoogleMaterial.Icon.gmd_add).colorRes(R.color.textColorPrimary).size(IconicsSize.dp(4)))

        viewAdapter =
            SubAdapter(Preferences().getAllSubs(this).toMutableList(), Preferences().getSelectedIndices(this).toMutableList())
        viewManager = LinearLayoutManager(this)

        fab_sub.onClick { openAddDialog() }

        recycler_view.apply {
            setHasFixedSize(true)
            adapter = viewAdapter
            layoutManager = viewManager
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, position: Int) {
                val items = Preferences().getAllSubs(applicationContext)
                val selected = Preferences().getSelectedSubs(applicationContext)
                val item = items.toList()[viewHolder.adapterPosition]
                if (items.size > 1) {
                    if (selected.size > 1 || (selected.size == 1 && !selected.contains(item))) {
                        (viewAdapter as SubAdapter).removeItem(viewHolder)
                    } else {
                        errorDialog(R.string.delete_last_selected, viewHolder)
                    }
                } else {
                    errorDialog(R.string.delete_last, viewHolder)
                }
            }

            override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                if (dX > 0) {
                    swipeBackground.setBounds(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                    deleteIcon.setBounds(itemView.left + iconMargin, itemView.top + iconMargin, itemView.left + iconMargin + deleteIcon.intrinsicWidth, itemView.bottom - iconMargin)
                } else {
                    swipeBackground.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    deleteIcon.setBounds(itemView.right - iconMargin - deleteIcon.intrinsicWidth, itemView.top + iconMargin, itemView.right - iconMargin, itemView.bottom - iconMargin)
                }
                swipeBackground.draw(c)

                if (dX > 0) {
                    c.clipRect(itemView.left, itemView.top, dX.toInt(), itemView.bottom)
                } else {
                    c.clipRect(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                }
                deleteIcon.draw(c)

                c.restore()
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recycler_view)
    }

    private fun openAddDialog() {
        val containerSub = container_sub
        MaterialDialog(this).show {
            title(R.string.add_sub)
            message(R.string.add_sub_description)
            input(maxLength = 32, waitForPositiveButton = true) { _, text ->
                val value = text.toString().toLowerCase(Locale.US).capitalize(Locale.ROOT)
                if (!Preferences().getAllSubs(context).contains(value)) {
                    Preferences().setSub(context, value)
                    (viewAdapter as SubAdapter).addItem(value)
                }
                Snackbar.make(containerSub, "$value added.", Snackbar.LENGTH_LONG).show()
            }
            positiveButton(R.string.ok)
            negativeButton(R.string.cancel)
        }
    }

    private fun errorDialog(msg: Int, vh: RecyclerView.ViewHolder) {
        MaterialDialog(this).show {
            title(R.string.bitmap_error_title)
            message(msg)
            positiveButton(R.string.ok)
            onDismiss { (viewAdapter as SubAdapter).notifyItemChanged(vh.adapterPosition) }
        }
    }
}