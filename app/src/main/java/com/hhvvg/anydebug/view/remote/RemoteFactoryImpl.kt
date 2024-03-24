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

package com.hhvvg.anydebug.view.remote

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.hhvvg.anydebug.BuildConfig
import com.hhvvg.anydebug.HookEntry
import com.hhvvg.anydebug.utils.createRemotePackageContext
import com.highcapable.yukihookapi.hook.factory.applyModuleTheme

/**
 * Impl for RemoteFactory
 */
class RemoteFactoryImpl : RemoteFactory {

    @SuppressLint("DiscouragedApi")
    override fun onInflateView(
        context: Context, name: String, root: ViewGroup?,
        attachToRoot: Boolean
    ): View {
        val packageContext = context.createRemotePackageContext()
        val resId = packageContext.resources
            .getIdentifier(name, "layout", BuildConfig.APPLICATION_ID)
        if (resId > 0) {
            return LayoutInflater.from(packageContext).inflate(resId, root, attachToRoot)
        }
        throw RuntimeException("Error inflating remote layout.")
    }

}