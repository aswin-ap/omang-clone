package com.omang.app.ui.test.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.omang.app.BR
import com.omang.app.R
import com.omang.app.data.database.test.TestEntity
import com.omang.app.databinding.ItemsUnitTestNewBinding
import java.util.Locale

class UnitTestListAdapter(
    val context: Context,
    newTests: List<TestEntity>,
    val onItemClick: (Int) -> Unit,
) : RecyclerView.Adapter<UnitTestListAdapter.ViewHolder>(), Filterable {
    private var newTests: List<TestEntity> = ArrayList()
    private var filterNewTests: List<TestEntity> = ArrayList()


    init {
        this.newTests = newTests
        filterNewTests = newTests
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DataBindingUtil.inflate<ItemsUnitTestNewBinding>(
            LayoutInflater.from(parent.context),
            R.layout.items_unit_test_new, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val test = filterNewTests[position]
        holder.bind(test)
    }

    override fun getItemCount(): Int {
        return filterNewTests.size
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charstring = charSequence.toString()
                filterNewTests = if (charstring.isEmpty()) {
                    newTests
                } else {
                    val newTest: MutableList<TestEntity> = ArrayList()
                    for (item in newTests) {
                        if (item.name.lowercase(Locale.getDefault())
                                .contains(charstring.lowercase(Locale.getDefault()))
                        ) {
                            newTest.add(item)
                        }
                    }
                    newTest
                }
                val filterResults = FilterResults()
                filterResults.values = filterNewTests
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                filterNewTests = filterResults.values as ArrayList<TestEntity>
                notifyDataSetChanged()
            }
        }
    }

    inner class ViewHolder(var binding: ItemsUnitTestNewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(obj: TestEntity?) {
            binding.setVariable(BR.newTest, obj)
            binding.executePendingBindings()
        }

        init {
            binding.cvNewTest.setOnClickListener {
                onItemClick(filterNewTests[absoluteAdapterPosition].id)
            }
        }
    }
}