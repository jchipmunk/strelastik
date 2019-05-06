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
package com.github.jchipmunk.strelastik.task.zookeeper

import com.codahale.metrics.Counter
import com.codahale.metrics.Meter
import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.model.zookeeper.ZPath
import com.github.jchipmunk.strelastik.model.zookeeper.startClient
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskContext
import org.apache.curator.framework.CuratorFramework
import org.slf4j.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

abstract class ZooKeeperTask(
        final override val name: String,
        protected val client: CuratorFramework,
        executionRegistry: ExecutionRegistry,
        metricRegistry: MetricRegistry) : Task {
    protected val zpaths = executionRegistry.get("zpaths") { ConcurrentHashMap<String, ZPath>() }
    protected val zpathCounter = executionRegistry.get("zpathCounter") { AtomicLong(0) }
    protected val meter: Meter = metricRegistry.meter("$name.meter")
    protected val operationTotalCounter: Counter = metricRegistry.counter("$name.operation.total.counter")
    protected val operationFailureCounter: Counter = metricRegistry.counter("$name.operation.failure.counter")

    abstract fun logger(): Logger

    override fun start() {
        startClient(client)
    }

    protected fun execute(context: TaskContext, action: () -> Unit) {
        if (!context.isRunning()) return
        try {
            action.invoke()
            meter.mark()
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
        } catch (t: Throwable) {
            operationFailureCounter.inc()
            logger().error("> Got unexpected exception while executing!", t)
        } finally {
            operationTotalCounter.inc()
        }
    }

    override fun stop() {
        client.close()
    }
}