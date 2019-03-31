/*
 * Copyright 2018 Andrey Pustovetov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.jchipmunk.strelastik.step

import com.fasterxml.jackson.databind.node.ObjectNode
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StepRunner {
    private val lock = ReentrantLock()
    private var running: Boolean = false
    private var currentStep: Step? = null

    fun start(stepFactory: StepFactory, steps: List<ObjectNode>) {
        lock.withLock {
            if (running) throw IllegalStateException("Already running") else running = true
        }
        try {
            val executionRegistry = ExecutionRegistry()
            for (i in 0 until steps.size) {
                val item = steps[i]
                lock.withLock {
                    if (!running) {
                        return
                    }
                    currentStep = stepFactory.createStep(i, item)
                }
                currentStep!!.take(executionRegistry)
            }
        } finally {
            lock.withLock {
                running = false
                currentStep = null
            }
        }
    }

    fun stop() {
        lock.withLock {
            if (running) {
                running = false
                currentStep?.stop()
            }
        }
    }
}