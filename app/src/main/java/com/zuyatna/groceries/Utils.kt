package com.zuyatna.groceries

import android.content.Context
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zuyatna.groceries.model.Item
import java.util.*

private const val GROCERIES_LIST = "groceries_list"

fun saveGroceriesList(context: Context, list: List<Item>) {
  val json = Gson().toJson(list)

  val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  sharedPreferences.edit().putString(GROCERIES_LIST, json).apply()
}

fun getGroceriesList(context: Context): List<Item> {
  val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
  val json = sharedPreferences.getString(GROCERIES_LIST, "")

  if (json.isNullOrEmpty()) {
    return listOf(Item(0, context.getString(R.string.item_default), System.currentTimeMillis()))
  }

  val type = object : TypeToken<ArrayList<Item>>() {}.type
  return Gson().fromJson(json, type)
}