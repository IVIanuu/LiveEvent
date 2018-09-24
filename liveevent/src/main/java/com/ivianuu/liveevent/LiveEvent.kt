/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.liveevent

import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.*

/**
 * Dispatches events to consumers and buffers them while no one is subscribed
 */
open class LiveEvent<T>(private val maxSize: Int = LifeEventPlugins.defaultMaxSize) {

    val hasConsumer get() = _consumer != null

    private var _consumer: OwnerWithConsumer<T>? = null

    private val pendingEvents = LinkedList<T>()

    private val lock = Any()

    init {
        maxSize.validateMaxSize()
    }

    /**
     * Adds a consumer which will be invoked on events
     */
    @MainThread
    fun consume(
        owner: LifecycleOwner,
        consumer: (T) -> Unit
    ) {
        consume(owner, LifeEventPlugins.defaultActiveState, consumer)
    }

    /**
     * Adds a consumer which will be invoked on events
     */
    @MainThread
    fun consume(
        owner: LifecycleOwner,
        activeState: Lifecycle.State,
        consumer: (T) -> Unit
    ) {
        requireMainThread()

        if (hasConsumer) {
            throw IllegalArgumentException("only one consumer at a time allowed")
        }

        _consumer = OwnerWithConsumer(owner, consumer, activeState)
    }

    /**
     * Removes the consumer
     */
    @MainThread
    fun clearConsumer() {
        requireMainThread()
        _consumer?.destroy()
        _consumer = null
    }

    /**
     * Dispatches a new event this can be called from any thread
     */
    @AnyThread
    protected open fun offer(event: T) {
        if (isMainThread) {
            offerInternal(event)
        } else {
            mainThreadHandler.post { offerInternal(event) }
        }
    }

    /**
     * Clears all pending events
     */
    @AnyThread
    protected open fun clear() {
        synchronized(lock) { pendingEvents.clear() }
    }

    private fun offerInternal(event: T) {
        requireMainThread()

        synchronized(lock) {
            pendingEvents.add(event)

            // trim
            while (pendingEvents.size > maxSize) {
                pendingEvents.poll()
            }
        }

        dispatchPendingEvents()
    }

    private fun dispatchPendingEvents() {
        requireMainThread()

        // is there any pending event?
        if (pendingEvents.isEmpty()) return

        // is there a consumer?
        val consumer = _consumer ?: return

        // are we active?
        if (!consumer.isActive) return

        // dispatch all events to the consumer
        synchronized(lock) {
            while (pendingEvents.isNotEmpty()) {
                val event = pendingEvents.poll()
                consumer.action.invoke(event)
            }
        }
    }

    private inner class OwnerWithConsumer<T>(
        val owner: LifecycleOwner,
        val action: (T) -> Unit,
        val activeState: Lifecycle.State
    ) {

        val isActive
            get() =
                owner.lifecycle.currentState.isAtLeast(activeState)

        private val lifecycleObserver = GenericLifecycleObserver { source, _ ->
            // dispatch events if the consumer reaches the active state
            if (isActive) {
                dispatchPendingEvents()
            }

            // clean up
            if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
                clearConsumer()
            }
        }

        init {
            owner.lifecycle.addObserver(lifecycleObserver)
        }

        fun destroy() {
            owner.lifecycle.removeObserver(lifecycleObserver)
        }
    }

}