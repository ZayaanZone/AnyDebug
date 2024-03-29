package com.hhvvg.anydebug.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.hhvvg.anydebug.R
import com.hhvvg.anydebug.utils.createModuleContext
import com.hhvvg.anydebug.utils.moduleLayoutInflater
import com.hhvvg.anydebug.utils.startGithubPage
import com.hhvvg.anydebug.view.ActivityPreviewWindow
import com.hhvvg.anydebug.view.SettingContent
import com.hhvvg.anydebug.view.SettingsFactory
import com.hhvvg.anydebug.view.SettingsFactoryManager
import java.util.function.Consumer

class MainPanelFragment(private val window: ActivityPreviewWindow, private var targetView: View?) :
    Fragment() {

    private var factory: SettingsFactory? = null
    private var onCommitListener: Runnable? = null
    private var onViewRemoveListener: Consumer<View>? = null

    private lateinit var okButton: View
    private lateinit var settingsContainer: ViewGroup
    private lateinit var githubLinkBtn: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_panel, container, false)
        okButton = view.findViewById(R.id.ok_button)
        settingsContainer = view.findViewById(R.id.settings_container)
        githubLinkBtn = view.findViewById(R.id.about_proj_link)

        okButton.setOnClickListener {
            factory?.commit()
            onCommitListener?.run()
        }
        githubLinkBtn.setOnClickListener {
            requireContext().startGithubPage()
        }
        return view
    }

    override fun getContext(): Context? {
        return targetView?.context?.createModuleContext()
    }

    fun removeTargetView() {
        targetView?.let { onViewRemoveListener?.accept(it) }
        targetView?.parent?.let {
            if (it is ViewGroup) {
                it.removeView(targetView)
            }
        }
    }

    fun setOnCommitListener(listener: Runnable) {
        onCommitListener = listener
    }

    fun setOnViewRemoveListener(listener: Consumer<View>) {
        onViewRemoveListener = listener
    }

    fun setTargetView(target: View) {
        targetView = target
        recreateSettings()
    }

    private fun clearSettings() {
        settingsContainer.removeAllViews()
    }

    private fun createTitle(titleText: CharSequence): TextView {
        val title = requireContext().moduleLayoutInflater()
            .inflate(R.layout.layout_title, settingsContainer, false) as TextView
        title.text = titleText
        return title
    }

    private fun recreateSettings() {
        clearSettings()
        val target = targetView ?: return
        val views = mutableListOf<SettingContent>()
        factory = SettingsFactoryManager.createFactory(window, target).apply {
            onCreate(target, settingsContainer, views)
        }
        views.forEach {
            settingsContainer.addView(createTitle(it.title))
            settingsContainer.addView(it.view)
        }
    }
}