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

import android.util.FloatProperty
import android.view.View

val SCALE = object : FloatProperty<View>("scaleX") {
    override fun get(view: View): Float {
        return view.scaleX
    }

    override fun setValue(view: View, value: Float) {
        view.scaleX = value
        view.scaleY = value
    }
}

val TRANSLATION_X = object : FloatProperty<View>("translationX") {
    override fun setValue(view: View, value: Float) {
        view.translationX = value
    }

    override fun get(view: View): Float {
        return view.translationX
    }
}

val ALPHA = object : FloatProperty<View>("alpha") {
    override fun setValue(view: View, value: Float) {
        view.alpha = value
    }

    override fun get(view: View): Float {
        return view.alpha
    }
}