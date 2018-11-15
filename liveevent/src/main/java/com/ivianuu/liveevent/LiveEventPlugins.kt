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

/**
 * Global config
 */
object LiveEventPlugins {
    /**
     * The default active state of a [LiveEvent]
     */
    var defaultActiveState = Lifecycle.State.STARTED
        set(value) {
            value.validateState()
            field = value
        }

    /**
     * The default max size of a [LiveEvent]
     */
    var defaultMaxSize = Int.MAX_VALUE
        set(value) {
            value.validateMaxSize()
            field = value
        }
}