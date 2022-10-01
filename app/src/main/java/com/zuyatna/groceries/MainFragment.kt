package com.zuyatna.groceries

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.zuyatna.groceries.adapters.ItemsDetailsLookup
import com.zuyatna.groceries.adapters.ItemsKeyProvider
import com.zuyatna.groceries.adapters.MainAdapter
import com.zuyatna.groceries.databinding.FragmentMainBinding
import com.zuyatna.groceries.model.Item

class MainFragment : Fragment(), ActionMode.Callback {

    private lateinit var binding: FragmentMainBinding
    private lateinit var tracker: SelectionTracker<Long>

    private var actionMode: ActionMode? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUiComponents()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("inflater.inflate(R.menu.menu_main, menu)"))
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_shuffle) {
            val adapter = binding.rvGroceries.adapter as MainAdapter

            val items = adapter.items.toMutableList()
            if (items.size > 3) {
                items.subList(1, items.size).shuffle()
                adapter.setListItems(items)
            } else {
                Snackbar.make(binding.clContainer, R.string.item_add_more, Snackbar.LENGTH_SHORT).show()
            }

            return true
        }

        return false
    }

    @Suppress("DEPRECATION")
    private fun setupToolbar() {
        val appCompatActivity = activity as AppCompatActivity
        appCompatActivity.setSupportActionBar(binding.toolbar)
        appCompatActivity.setTitle(R.string.app_name)

        setHasOptionsMenu(true)
    }

    private fun setupUiComponents() {
        val mainAdapter = MainAdapter { items: MutableList<Item>, changed: Item, isChecked: Boolean ->

            var element = items.first { it.timeStamp == changed.timeStamp }
            val index = items.indexOf(element)

            element = if (index == 0) {
                Snackbar.make(binding.clContainer, R.string.item_more_cookies, Snackbar.LENGTH_SHORT).show()
                element.copy(done = false)

            } else {
                element.copy(done = isChecked)
            }

            items[index] = element
            updateAndSave(items)
        }

        binding.rvGroceries.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            adapter = mainAdapter
        }

        binding.etNewItem.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                // Do nothing
            }

            override fun afterTextChanged(text: Editable?) {
                binding.ivAddToCart.isEnabled = text.toString().isNotEmpty()
            }
        })

        binding.ivAddToCart.isEnabled = false
        binding.ivAddToCart.setOnClickListener {
            val list = mainAdapter.items.toMutableList()
            list.add(
                    Item(
                        list.size,
                        binding.etNewItem.text.toString(),
                        System.currentTimeMillis(),
                        false
                    )
            )

            binding.etNewItem.text?.clear()

            updateAndSave(list)
        }

        tracker = SelectionTracker.Builder(
            getString(R.string.item_selection),
            binding.rvGroceries,
            ItemsKeyProvider(mainAdapter),
            ItemsDetailsLookup(binding.rvGroceries),
            StorageStrategy.createLongStorage()
        ).withSelectionPredicate(
            SelectionPredicates.createSelectAnything()
        ).build()

        tracker.addObserver(
            object : SelectionTracker.SelectionObserver<Long>() {
                override fun onSelectionChanged() {
                    super.onSelectionChanged()

                    if (actionMode == null) {
                        val currentActivity = activity as MainActivity
                        actionMode = currentActivity.startSupportActionMode(this@MainFragment)

                        binding.etNewItem.clearFocus()
                        binding.etNewItem.isEnabled = false
                    }

                    val items = tracker.selection.size()
                    if (items > 0) {
                        actionMode?.title = getString(R.string.action_selected, items)
                    } else {
                        actionMode?.finish()
                    }
                }
            })

        mainAdapter.tracker = tracker
        mainAdapter.setListItems(getGroceriesList(requireContext()))
    }

    private fun updateAndSave(list: List<Item>) {
        (binding.rvGroceries.adapter as MainAdapter).setListItems(list)
        saveGroceriesList(requireContext(), list)
    }

    // region ActionMode

    override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        mode?.menuInflater?.inflate(R.menu.menu_actions, menu)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?) = true

    override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
        return when (item!!.itemId) {
            R.id.action_delete -> {
                val mainAdapter = binding.rvGroceries.adapter as MainAdapter

                var selected = mainAdapter.items.filter {
                    tracker.selection.contains(it.timeStamp)
                }

                val groceries = mainAdapter.items.toMutableList()
                if (groceries[0] == selected[0]) {
                    Snackbar.make(binding.clContainer, R.string.item_prohibited, Snackbar.LENGTH_SHORT).show()
                    selected = selected.subList(1, selected.size)
                }

                groceries.removeAll(selected)

                updateAndSave(groceries)
                actionMode?.finish()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode?) {
        tracker.clearSelection()
        actionMode = null

        binding.etNewItem.isEnabled = true
    }
}