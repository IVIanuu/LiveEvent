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

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle

internal fun Lifecycle.State.validateState() {
    if (this == Lifecycle.State.DESTROYED) {
        throw IllegalArgumentException("destroyed cannot be used a active state.")
    }
}

internal fun Int.validateMaxSize() {
    if (this < 1) {
        throw IllegalArgumentException("size must be at least 1.")
    }
}

internal val isMainThread get() = Looper.myLooper() == Looper.getMainLooper()

internal val mainThreadHandler = Handler(Looper.getMainLooper())

internal fun requireMainThread() {
    if (!isMainThread) {
        throw IllegalStateException("must be called from the main thread")
    }
}