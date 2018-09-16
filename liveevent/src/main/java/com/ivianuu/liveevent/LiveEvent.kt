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

import android.os.Looper
import androidx.lifecycle.GenericLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import java.util.*

/**
 * Dispatches events to consumers and buffers them while no one is subscribed
 */
open class LiveEvent<T> {

    private var _consumer: Pair<LifecycleOwner, ((T) -> Unit)>? = null

    private val pendingEvents = LinkedList<T>()

    private val lifecycleObserver = GenericLifecycleObserver { source, _ ->
        d { "state changed" }

        // dispatch events if the consumer reaches the active state
        if (isActive) {
            d { "active dispatch events" }
            dispatchPendingEvents()
        }

        // clean up
        if (source.lifecycle.currentState == Lifecycle.State.DESTROYED) {
            d { "clean up consumer destroyed" }
            clearConsumer()
        }
    }

    private val isActive get() =
        _consumer?.first?.lifecycle?.currentState?.isAtLeast(ACTIVE_STATE) == true

    /**
     * Adds a consumer which will be invoked on events
     */
    fun consume(owner: LifecycleOwner, consumer: (T) -> Unit) {
        d { "consume called" }

        requireMainThread()

        if (_consumer != null) {
            throw IllegalArgumentException("only one consumer at a time allowed")
        }

        _consumer = owner to consumer

        owner.lifecycle.addObserver(lifecycleObserver)
    }

    /**
     * Removes the consumer
     */
    fun clearConsumer() {
        d { "clear consumer" }
        requireMainThread()
        _consumer?.let { (owner) ->
            owner.lifecycle.removeObserver(lifecycleObserver)
        }
        _consumer = null
    }

    /**
     * Dispatches a new event this can be called from any thread
     */
    protected open fun offer(event: T) {
        d { "$event" }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            d { "is main thread" }
            offerInternal(event)
        } else {
            d { "is not main thread" }
            mainThreadHandler.post { offerInternal(event) }
        }
    }

    private fun offerInternal(event: T) {
        requireMainThread()

        d { "offer internal $event" }

        pendingEvents.add(event)
        dispatchPendingEvents()
    }

    private fun dispatchPendingEvents() {
        requireMainThread()

        d { "dispatch pending events" }

        // is there any pending event?
        if (pendingEvents.isEmpty()) return

        d { "pending events available" }

        // is there a consumer?
        val consumer = _consumer?.second ?: return

        d { "consume present" }

        // are we active?
        if (!isActive) return

        d { "consumer active" }

        // dispatch all events to the consumer
        while (pendingEvents.isNotEmpty()) {
            val event = pendingEvents.poll()
            d { "dispatch event -> $event" }
            consumer.invoke(event)
        }
    }

    private companion object {
        private val ACTIVE_STATE = Lifecycle.State.STARTED
    }
}