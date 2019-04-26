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
package com.github.jchipmunk.strelastik.task.elasticsearch

import com.codahale.metrics.Counter
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskContext
import io.searchbox.action.Action
import io.searchbox.client.JestClient
import io.searchbox.client.JestResult
import io.searchbox.core.Doc
import org.apache.http.impl.execchain.RequestAbortedException
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap

abstract class ElasticsearchTask(final override val name: String,
                                 private val client: JestClient,
                                 executionRegistry: ExecutionRegistry,
                                 metricRegistry: MetricRegistry) : Task {
    protected val docs = executionRegistry.get("docs") { ConcurrentHashMap<String, Doc>() }
    protected val meter: Meter = metricRegistry.meter("$name.meter")
    protected val requestTotalCounter: Counter = metricRegistry.counter("$name.request.total.counter")
    protected val requestFailureCounter: Counter = metricRegistry.counter("$name.request.failure.counter")
    protected val documentFailureCounter: Counter = metricRegistry.counter("$name.document.failure.counter")

    abstract fun logger(): Logger

    private fun handleThrowable(t: Throwable) {
        requestFailureCounter.inc()
        logger().error("> Got unexpected exception while executing!", t)
    }

    protected fun <T : JestResult> execute(context: TaskContext, request: Action<T>, resultHandler: (T) -> Unit) {
        if (!context.isRunning()) return
        try {
            val result = client.execute(request)
            resultHandler.invoke(result)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (e: RequestAbortedException) {
            if (e.cause !is InterruptedException) handleThrowable(e) else Thread.currentThread().interrupt()
        } catch (t: Throwable) {
            handleThrowable(t)
        } finally {
            requestTotalCounter.inc()
        }
    }
}