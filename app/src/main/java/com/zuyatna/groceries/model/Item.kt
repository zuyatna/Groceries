package com.zuyatna.groceries.model

data class Item(val id: Int, val value: String, val timeStamp: Long, var done: Boolean = false)