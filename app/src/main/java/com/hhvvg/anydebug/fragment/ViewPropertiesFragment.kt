package com.hhvvg.anydebug.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.utils.ViewExportedProperty
import com.hhvvg.anydebug.utils.dumpView
import com.hhvvg.anydebug.utils.formatToExportedProperties

class ViewPropertiesFragment(private val targetView: View) : Fragment() {

    private lateinit var propertiesRv: RecyclerView
    private val items = mutableListOf<ViewExportedProperty>()
    private val adapter = PropertiesAdapter(items)

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_view_properties, container, false)
        propertiesRv = view.findViewById<RecyclerView>(R.id.properties_rv).apply {
            this.adapter = this@ViewPropertiesFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        items.addAll(
            targetView.dumpView().formatToExportedProperties().values.flatten()
                .sortedBy { it.name })
        adapter.notifyDataSetChanged()
        return view
    }

    override fun getContext(): Context? {
        return targetView.context
    }
}

private class PropertiesAdapter(private val items: List<ViewExportedProperty>) :
    Adapter<PropertiesHolder>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PropertiesHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.value.text = item.value.toString()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PropertiesHolder {
        return PropertiesHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_view_property_item, parent, false)
        )
    }
}

private class PropertiesHolder(view: View) : ViewHolder(view) {
    val name: TextView by lazy { view.findViewById(R.id.name_view) }
    val value: TextView by lazy { view.findViewById(R.id.value_view) }
}