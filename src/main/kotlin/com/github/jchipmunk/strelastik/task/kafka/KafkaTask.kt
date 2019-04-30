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
package com.github.jchipmunk.strelastik.task.kafka

import com.codahale.metrics.Counter
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskContext
import org.slf4j.Logger

abstract class KafkaTask(
        final override val name: String,
        metricRegistry: MetricRegistry) : Task {
    protected val meter: Meter = metricRegistry.meter("$name.meter")
    protected val invocationTotalCounter: Counter = metricRegistry.counter("$name.invocation.total.counter")
    protected val invocationFailureCounter: Counter = metricRegistry.counter("$name.invocation.failure.counter")

    abstract fun logger(): Logger

    protected fun handleThrowable(t: Throwable) {
        invocationFailureCounter.inc()
        logger().error("> Got unexpected exception while executing!", t)
    }

    protected fun execute(context: TaskContext, action: () -> Unit) {
        if (!context.isRunning()) return
        try {
            action.invoke()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (t: Throwable) {
            handleThrowable(t)
        } finally {
            invocationTotalCounter.inc()
        }
    }
}