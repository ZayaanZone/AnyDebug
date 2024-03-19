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

package com.hhvvg.libinject.instances

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.database.ContentObserver
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.hhvvg.libinject.Instance
import com.hhvvg.libinject.configurations.AllSettings
import com.hhvvg.libinject.configurations.CONTENT_URI
import com.hhvvg.libinject.modules.ActivityModule
import com.hhvvg.libinject.utils.Logger
import com.hhvvg.libinject.utils.doAfter
import com.hhvvg.libinject.view.ActivityPreviewWindow

class ActivityInstance(private val activity: Activity) : Instance, ActivityLifecycleCallbacks {

    companion object {
        private val TAG = ActivityModule::class.java.simpleName
    }

    private val observer by lazy {
        object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                onDisplayWindowChanged(windowShouldDisplay)
            }
        }
    }
    private val windowShouldDisplay: Boolean
        get() = AllSettings.editEnabled.value
    private val previewWindow by lazy {
        ActivityPreviewWindow(activity)
    }

    init {
        Logger.log(TAG, "activity $activity created.")
        activity.registerActivityLifecycleCallbacks(this)
        Activity::class.doAfter("onWindowFocusChanged", Boolean::class.java) {
            if (it.args[0] as Boolean) {
                onDisplayWindowChanged(windowShouldDisplay)
            }
        }
    }

    private fun onDisplayWindowChanged(show: Boolean) {
        if (show) {
            previewWindow.show()
        } else {
            previewWindow.hide()
        }
    }

    override fun onActivityPreDestroyed(activity: Activity) {
        super.onActivityPreDestroyed(activity)
        activity.contentResolver.unregisterContentObserver(observer)
        onDisplayWindowChanged(false)
    }

    override fun onActivityDestroyed(activity: Activity) {}

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        activity.contentResolver.registerContentObserver(
            CONTENT_URI,
            true,
            observer
        )
    }

    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}