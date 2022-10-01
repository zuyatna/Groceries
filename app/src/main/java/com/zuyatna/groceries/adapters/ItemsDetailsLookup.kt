package com.zuyatna.groceries.adapters

import android.view.MotionEvent
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.widget.RecyclerView

class ItemsDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Long>() {

  override fun getItemDetails(event: MotionEvent): ItemDetails<Long>? {
    val view = recyclerView.findChildViewUnder(event.x, event.y)
    if (view != null) {
      return (recyclerView.getChildViewHolder(view) as MainAdapter.ItemViewHolder).getItem()
    }
    return null
  }
}