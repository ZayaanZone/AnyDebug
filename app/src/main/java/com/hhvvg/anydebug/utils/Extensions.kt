/*
 *     Copyright (C) <2024>  <gitofleonardo>
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.hhvvg.anydebug.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.res.Resources
import android.graphics.Rect
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewDebug
import android.view.ViewGroup
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.InjectHookEntry
import com.hhvvg.anydebug.ModuleContext
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.Unhook
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import java.io.BufferedWriter
import java.io.StringWriter
import java.util.regex.Pattern
import kotlin.reflect.KClass

/**
 * Do action before method called.
 */
fun KClass<*>.doBefore(
    methodName: String,
    vararg methodParams: Class<*>,
    callback: (XC_MethodHook.MethodHookParam) -> Unit
): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Do action after method called.
 */
fun KClass<*>.doAfter(
    methodName: String,
    vararg methodParams: Class<*>,
    callback: (XC_MethodHook.MethodHookParam) -> Unit
): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Overrides method call.
 */
fun KClass<*>.override(
    methodName: String,
    vararg methodParams: Class<*>,
    callback: (XC_MethodHook.MethodHookParam) -> Any?
): Unhook {
    val method = XposedHelpers.findMethodBestMatch(this.java, methodName, *methodParams)
    val methodHook = object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam): Any? {
            return callback.invoke(param)
        }
    }
    return XposedBridge.hookMethod(method, methodHook)
}

/**
 * Calls method with specific name
 */
fun Any.call(method: String, vararg args: Any?): Any? {
    return XposedHelpers.callMethod(this, method, *args)
}

/**
 * Guarded call a method
 */
fun Any.guardedCall(method: String, vararg args: Any?) {
    try {
        call(method, *args)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Call a static method
 */
fun Class<*>.call(method: String, vararg args: Any?): Any? {
    return XposedHelpers.callStaticMethod(this, method, *args)
}

/**
 * Regex pattern for parsing padding/margin
 */
val ltrbPattern: Pattern = Pattern.compile("^\\[(-?\\d+),(-?\\d+),(-?\\d+),(-?\\d+)]$")

/**
 * Padding format in string
 */
val View.paddingLtrb: String
    get() = "[${paddingLeft},${paddingTop},${paddingRight},${paddingBottom}]"

/**
 * Margin format in string
 */
val ViewGroup.MarginLayoutParams.ltrb: String
    get() = "[${leftMargin},${topMargin},${rightMargin},${bottomMargin}]"

/**
 * Find ancestor for target view with a specific class.
 */
fun <T : ViewGroup> View.findTargetAncestor(targetClass: Class<T>): T? {
    var currParent = parent
    while (currParent != null && !targetClass.isInstance(currParent)) {
        currParent = currParent.parent
    }
    return if (currParent == null || !targetClass.isInstance(currParent)) null else currParent as T
}

/**
 * Retrieves resources of module app
 */
val Context.moduleResources: Resources
    get() = InjectHookEntry.moduleRes

/**
 * Create a new module context
 */
@SuppressLint("DiscouragedApi")
fun Context.createModuleContext(): Context {
    val themeId = moduleResources.getIdentifier("AppTheme", "style", BuildConfig.APPLICATION_ID)
    val theme = moduleResources.newTheme().apply {
        applyStyle(themeId, true)
    }
    return ModuleContext(topContext(), theme)
}

/**
 * Reverse mapping from key->value to value->key
 */
fun <K, V> Map<K, V>.reverse(): Map<V, K> {
    val result = mutableMapOf<V, K>()
    entries.forEach {
        result[it.value] = it.key
    }
    return result
}

/**
 * Create module layout inflater
 */
fun Context.moduleLayoutInflater(): LayoutInflater {
    return LayoutInflater.from(createModuleContext())
}

/**
 * Get window display frame, ignoring insets
 */
fun View.getWindowDisplayFrame(outRect: Rect) {
    outRect.setEmpty()
    guardedCall("getWindowDisplayFrame", outRect)
    if (outRect.isEmpty) {
        getWindowVisibleDisplayFrame(outRect)
    }
}

/**
 * Return the most top context of this context
 */
fun Context.topContext(): Context {
    var top: Context = this
    while (top is ContextWrapper) {
        top = top.baseContext
    }
    return top
}

/**
 * Dump view to string
 */
fun View.dumpView(): String {
    val sw = StringWriter()
    val bw = BufferedWriter(sw)
    ViewDebug::class.java.call("dumpView", context, this, bw, 0, true)
    bw.flush()
    return sw.toString()
}

/**
 * Pattern for view exported properties output
 */
val propertyPattern = Pattern.compile("^(([\\s\\S]+):)?([\\s\\S]+)=([\\d]+),([\\s\\S]+)$")

val NULL: String = "null"

/**
 * Convert view dump to properties
 */
fun String.formatToExportedProperties(): Map<String, MutableList<ViewExportedProperty>> {
    val result = mutableMapOf<String, MutableList<ViewExportedProperty>>()
    val props = split(" ")
    for (prop in props) {
        val matcher = propertyPattern.matcher(prop)
        if (matcher.find()) {
            val category = matcher.group(2) ?: NULL
            val name = matcher.group(3) ?: continue
            val length = matcher.group(4)?.toIntOrNull() ?: continue
            val value = matcher.group(5) ?: continue
            result.computeIfAbsent(category) { _ -> mutableListOf() }
                .add(ViewExportedProperty(category, name, length, value))
        }
    }
    return result
}

private const val GITHUB_PAGE_URL = "https://github.com/gitofleonardo/AnyDebug"

fun Context.startGithubPage() {
    val intent = Intent().apply {
        action = Intent.ACTION_VIEW
        data = Uri.parse(GITHUB_PAGE_URL)
        flags = flags or FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}