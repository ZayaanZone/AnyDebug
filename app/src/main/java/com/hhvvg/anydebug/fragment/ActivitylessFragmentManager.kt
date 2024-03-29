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

package com.hhvvg.anydebug.fragment

import android.animation.AnimatorSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.view.isVisible
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.fragment.app.Fragment
import com.hhvvg.anydebug.utils.ALPHA
import com.hhvvg.anydebug.utils.SpringAnimationBuilder
import com.hhvvg.anydebug.utils.TRANSLATION_X
import java.util.Stack

private const val FRAGMENT_ANIM_STIFFNESS = 300F
private const val FRAGMENT_ANIM_DAMPING_RATIO = .99f

class ActivitylessFragmentManager(private val container: ActivitylessFragmentContainer) {

    private val fragmentStack = Stack<FragmentItem>()
    private val inflater by lazy { LayoutInflater.from(container.context) }

    fun push(fragment: Fragment) {
        // get prev fragment
        val prevFragment = if (fragmentStack.isNotEmpty()) {
            fragmentStack.peek()
        } else {
            null
        }
        // create current fragment and push
        val view = fragment.onCreateView(inflater, container, null)
        val currFragment = FragmentItem(fragment, view, null)
        fragmentStack.push(currFragment)
        // add current fragment to container
        container.addView(view)
        fragment.onAttach(container.context)
        // pause prev fragment
        prevFragment?.fragment?.onPause()
        // create fragment animations
        prevFragment?.makeExitAnimation(true)?.start()
        currFragment.makeEnterAnimation(false)?.start()
    }

    fun pop(animate: Boolean = true) {
        if (fragmentStack.isEmpty()) {
            return
        }
        // pop current fragment
        val fragmentItem = fragmentStack.pop()
        val fragment = fragmentItem.fragment
        fragmentItem?.makeExitAnimation(false, removed = true)?.apply {
            start()
            if (!animate) end()
        }
        // call destroy
        // Not removing from container because we want the animation
        fragment.onDestroyView()
        fragment.onDestroy()
        fragment.onDetach()
        val peekFragment = if (fragmentStack.isNotEmpty()) {
            fragmentStack.peek()
        } else {
            null
        }
        // call resume on peek fragment
        peekFragment?.apply {
            fragment.onResume()
            makeEnterAnimation(true)?.apply {
                start()
                if (!animate) end()
            }
        }
    }

    fun popIfNotLast(animate: Boolean = true): Boolean {
        return if (fragmentStack.size > 1) {
            pop(animate)
            true
        } else {
            false
        }
    }
}

data class FragmentItem(
    val fragment: Fragment,
    val view: View?,
    var fragmentAnimation: AnimatorSet?
) {
    fun makeEnterAnimation(popAction: Boolean): AnimatorSet? {
        if (view == null) return null
        val parent = view.parent ?: return null
        if (parent !is ViewGroup) return null // already detached

        fragmentAnimation?.cancel()
        val animation = AnimatorSet()
        val fromX = if (popAction) -parent.width.toFloat() else parent.width.toFloat()
        val toX = 0f
        val tranXAnim = SpringAnimationBuilder(view.context)
            .setDampingRatio(FRAGMENT_ANIM_DAMPING_RATIO)
            .setStiffness(FRAGMENT_ANIM_STIFFNESS)
            .setStartValue(fromX)
            .setEndValue(toX)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .build(view, TRANSLATION_X)
        val alphaAnim = SpringAnimationBuilder(view.context)
            .setDampingRatio(FRAGMENT_ANIM_DAMPING_RATIO)
            .setStiffness(FRAGMENT_ANIM_STIFFNESS)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_ALPHA)
            .setStartValue(ALPHA.get(view))
            .setEndValue(1f)
            .build(view, ALPHA)

        animation.play(alphaAnim)
        animation.play(tranXAnim)
        animation.addListener(
            onStart = {
                fragmentAnimation = animation
                view.isVisible = true
            },
            onEnd = {
                fragmentAnimation = null
            }
        )
        return animation
    }

    fun makeExitAnimation(popAction: Boolean, removed: Boolean = false): AnimatorSet? {
        if (view == null) return null
        val parent = view.parent ?: return null
        if (parent !is ViewGroup) return null // already detached

        fragmentAnimation?.cancel()
        val animation = AnimatorSet()
        val fromX = TRANSLATION_X.get(view)
        val toX = if (popAction) -parent.width.toFloat() else parent.width.toFloat()
        val tranXAnim = SpringAnimationBuilder(view.context)
            .setDampingRatio(FRAGMENT_ANIM_DAMPING_RATIO)
            .setStiffness(FRAGMENT_ANIM_STIFFNESS)
            .setStartValue(fromX)
            .setEndValue(toX)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
            .build(view, TRANSLATION_X)
        val alphaAnim = SpringAnimationBuilder(view.context)
            .setDampingRatio(FRAGMENT_ANIM_DAMPING_RATIO)
            .setStiffness(FRAGMENT_ANIM_STIFFNESS)
            .setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_ALPHA)
            .setStartValue(ALPHA.get(view))
            .setEndValue(0f)
            .build(view, ALPHA)

        animation.play(alphaAnim)
        animation.play(tranXAnim)
        animation.addListener(
            onStart = {
                fragmentAnimation = animation
            },
            onEnd = {
                fragmentAnimation = null
                if (removed ) {
                    parent.removeView(view)
                } else {
                    view.isVisible = false
                }
            }
        )
        return animation
    }
}