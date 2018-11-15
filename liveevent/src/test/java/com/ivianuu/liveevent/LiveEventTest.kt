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

import androidx.lifecycle.Lifecycle
import com.ivianuu.liveevent.util.TestConsumer
import com.ivianuu.liveevent.util.TestLifecycleOwner
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LiveEventTest {

    private var liveEvent = MutableLiveEvent<Int>()
    private var lifecycleOwner = TestLifecycleOwner()
    private var consumer = TestConsumer<Int>()

    @Test
    fun testSetAndRemoveConsumer() {
        assertFalse(liveEvent.hasConsumer)

        liveEvent.consume(lifecycleOwner, consumer)
        assertTrue(liveEvent.hasConsumer)

        liveEvent.removeConsumer()
        assertFalse(liveEvent.hasConsumer)
    }

    @Test
    fun testWaitsForConsumer() {
        liveEvent.offer(1)

        lifecycleOwner.markState(Lifecycle.State.STARTED)
        liveEvent.consume(lifecycleOwner, consumer)

        assertEquals(listOf(1), consumer.history)
    }

    @Test
    fun testWaitsForActiveConsumer() {
        liveEvent.offer(1)

        liveEvent.consume(lifecycleOwner, Lifecycle.State.RESUMED, consumer)
        assertEquals(emptyList<Int>(), consumer.history)

        lifecycleOwner.markState(Lifecycle.State.STARTED)
        assertEquals(emptyList<Int>(), consumer.history)

        lifecycleOwner.markState(Lifecycle.State.RESUMED)
        assertEquals(listOf(1), consumer.history)
    }

    @Test
    fun testClear() {
        liveEvent.offer(1)
        liveEvent.clear()

        lifecycleOwner.markState(Lifecycle.State.STARTED)
        liveEvent.consume(lifecycleOwner, consumer)

        assertEquals(emptyList<Int>(), consumer.history)
    }

    @Test
    fun testThrowsOnMultipleConsumers() {
        liveEvent.consume(lifecycleOwner, consumer)
        val throwedException = try {
            liveEvent.consume(lifecycleOwner, consumer)
            false
        } catch (e: Exception) {
            true
        }
        assertTrue(throwedException)
    }

    @Test
    fun testMaxSize() {
        liveEvent = MutableLiveEvent(2)
        liveEvent.offer(1)
        liveEvent.offer(2)
        liveEvent.offer(3)

        lifecycleOwner.markState(Lifecycle.State.STARTED)
        liveEvent.consume(lifecycleOwner, consumer)

        assertEquals(listOf(2, 3), consumer.history)
    }

    @Test
    fun testDefaultMaxSize() {
        LiveEventPlugins.defaultMaxSize = 1
        liveEvent = MutableLiveEvent()
        assertEquals(liveEvent.maxSize, 1)
        LiveEventPlugins.defaultMaxSize = Int.MAX_VALUE
    }

    @Test
    fun testDefaultActiveState() {
        LiveEventPlugins.defaultActiveState = Lifecycle.State.RESUMED
        liveEvent = MutableLiveEvent()

        liveEvent.offer(1)

        liveEvent.consume(lifecycleOwner, consumer)
        assertEquals(emptyList<Int>(), consumer.history)

        lifecycleOwner.markState(Lifecycle.State.STARTED)
        assertEquals(emptyList<Int>(), consumer.history)

        lifecycleOwner.markState(Lifecycle.State.RESUMED)
        assertEquals(listOf(1), consumer.history)

        LiveEventPlugins.defaultActiveState = Lifecycle.State.STARTED
    }
}