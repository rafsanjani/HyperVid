package com.foreverrafs.hyperdownloader.util

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.foreverrafs.hyperdownloader.R


/* Created by Rafsanjani on 02/04/2020. */

class ItemTouchCallback(private val adapter: ItemTouchHelperAdapter) :
    ItemTouchHelper.Callback() {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        adapter.onItemMoved(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        adapter.onItemDismiss(viewHolder.adapterPosition)
    }

    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 0.4f
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val swipeFlags = ItemTouchHelper.START
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onChildDraw(
        canvas: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
    ) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            val viewItem = viewHolder.itemView
            SwipeBackgroundHelper.paintDrawCommandToStart(
                canvas, viewItem, R.drawable.ic_delete
                , dX
            )
        }
        super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    interface ItemTouchHelperAdapter {

        /**
         * Called when one item is dragged and dropped into a different position
         */
        fun onItemMoved(fromPosition: Int, toPosition: Int)

        /**
         * Called when one item is swiped away
         */
        fun onItemDismiss(position: Int)
    }
}