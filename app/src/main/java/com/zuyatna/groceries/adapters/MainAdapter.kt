package com.zuyatna.groceries.adapters

import android.annotation.SuppressLint
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.zuyatna.groceries.R
import com.zuyatna.groceries.databinding.ItemGroceryBinding
import com.zuyatna.groceries.model.Item

class MainAdapter(val action: (items: MutableList<Item>, changed: Item, checked: Boolean) -> Unit) :
    RecyclerView.Adapter<MainAdapter.ItemViewHolder>() {

  private lateinit var binding: ItemGroceryBinding

  var items: List<Item> = emptyList()

  var tracker: SelectionTracker<Long>? = null

  override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): ItemViewHolder {
    binding = ItemGroceryBinding.inflate(LayoutInflater.from(viewGroup.context), viewGroup, false)
    return ItemViewHolder(binding)
  }

  override fun onBindViewHolder(viewHolder: ItemViewHolder, pos: Int) {
    val item = items[pos]
    viewHolder.bind(item)
  }

  override fun getItemCount(): Int {
    return items.size
  }

  @SuppressLint("NotifyDataSetChanged")
  fun setListItems(list: List<Item>) {
    items = list
    notifyDataSetChanged()
  }

  inner class ItemViewHolder(private val itemBinding: ItemGroceryBinding) :
      RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(item: Item) {
      itemBinding.cbItem.text = item.value
      itemBinding.cbItem.setOnCheckedChangeListener(null)
      itemBinding.cbItem.isChecked = item.done
      itemBinding.cbItem.setOnCheckedChangeListener { _, isChecked ->
        if (item.id == 0) {
          itemBinding.cbItem.isChecked = false
          action(items.toMutableList(), item, false)

        } else {
          action(items.toMutableList(), item, isChecked)
        }
      }

      setItemTextStyle(item.done)

      tracker?.let {

        if (it.isSelected(item.timeStamp)) {
          itemBinding.llContainer.setBackgroundColor(
              ContextCompat.getColor(itemBinding.llContainer.context, R.color.colorPrimary60))
        } else {
          itemBinding.llContainer.background = null
        }
      }
    }

    private fun setItemTextStyle(checked: Boolean) {
      val resources = itemBinding.cbItem.context.resources
      if (checked) {
        val color = ResourcesCompat.getColor(resources, R.color.colorDone, null)
        itemBinding.cbItem.setTextColor(color)
        itemBinding.cbItem.paintFlags =
            itemBinding.cbItem.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

      } else {
        val color = ResourcesCompat.getColor(resources, R.color.colorActive, null)
        itemBinding.cbItem.setTextColor(color)
        itemBinding.cbItem.paintFlags =
            itemBinding.cbItem.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
      }
    }

    @Suppress("DEPRECATION")
    fun getItem(): ItemDetailsLookup.ItemDetails<Long> =

        object : ItemDetailsLookup.ItemDetails<Long>() {

          override fun getPosition(): Int = adapterPosition

          override fun getSelectionKey(): Long = items[adapterPosition].timeStamp
        }
  }
}