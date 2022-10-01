package com.zuyatna.groceries.adapters

import androidx.recyclerview.selection.ItemKeyProvider

class ItemsKeyProvider(private val adapter: MainAdapter) : ItemKeyProvider<Long>(SCOPE_CACHED) {

  override fun getKey(position: Int): Long =
      adapter.items[position].timeStamp

  override fun getPosition(key: Long): Int =
      adapter.items.indexOfFirst { it.timeStamp == key }
}