package com.hhvvg.anydebug.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
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
    private lateinit var propertiesSearchView: EditText
    private val items = mutableListOf<ViewExportedProperty>()
    private val adapter = PropertiesAdapter(items)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_view_properties, container, false)
        propertiesRv = view.findViewById<RecyclerView>(R.id.properties_rv).apply {
            this.adapter = this@ViewPropertiesFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        propertiesSearchView = view.findViewById(R.id.type_to_search)
        propertiesSearchView.doOnTextChanged { text, _, _, _ ->
            updatePropertiesList(text)
        }
        updatePropertiesList()
        return view
    }

    override fun getContext(): Context? {
        return targetView.context
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updatePropertiesList(filter: CharSequence? = null) {
        val props = targetView
            .dumpView()
            .formatToExportedProperties().values.flatten()
            .filter {
                if (TextUtils.isEmpty(filter)) {
                    true
                } else {
                    it.name.contains(filter!!, true)
                }
            }
            .sortedBy { it.name }
        items.clear()
        items.addAll(props)
        adapter.notifyDataSetChanged()
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