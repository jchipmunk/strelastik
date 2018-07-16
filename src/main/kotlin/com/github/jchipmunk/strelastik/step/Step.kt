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

import com.codahale.metrics.ConsoleReporter
import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.data.ExecutionContext
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskFactory
import com.google.common.util.concurrent.ThreadFactoryBuilder
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Step(
        private val name: String,
        private val taskFactory: TaskFactory,
        private val durationMs: Long,
        private val threads: Int) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Step::class.java)
    }

    private val executor = Executors.newFixedThreadPool(threads, ThreadFactoryBuilder()
            .setNameFormat("strelastik-$name-thread-%d")
            .build())
    private val registry = MetricRegistry()

    fun take(context: ExecutionContext) {
        LOGGER.info("Starting step: {}", name)
        try {
            val task = taskFactory.create(context, registry)
            for (i in 0 until threads) {
                executor.submit(TaskRunnable(i, task))
            }
            LOGGER.info("Waiting for {} milliseconds...", durationMs)
            Thread.sleep(durationMs)
            LOGGER.info("Timeout!")
        } catch (e: InterruptedException) {
            LOGGER.error("Got interrupted while running!")
            Thread.currentThread().interrupt()
        } finally {
            LOGGER.info("Stopping step: {}", name)
            print(registry)
            if (stop()) LOGGER.info("Step: {} stopped", name) else LOGGER.info("Stop timeout expired!")
        }
    }

    private fun print(registry: MetricRegistry) {
        LOGGER.info("Print metrics:")
        val reporter = ConsoleReporter.forRegistry(registry).convertRatesTo(TimeUnit.SECONDS).build()
        reporter.report()
    }

    fun abort() {
        LOGGER.info("Aborting step: {}", name)
        print(registry)
        if (stop()) LOGGER.info("Step: {} aborted", name) else LOGGER.info("Abort timeout expired!")
    }

    private fun stop(): Boolean {
        if (executor.isShutdown) {
            LOGGER.info("Step: {} already stopped", name)
            return true
        }
        executor.shutdownNow()
        try {
            return executor.awaitTermination(5L, TimeUnit.SECONDS)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            LOGGER.error("Got interrupted while shutting down!")
        }
        return false
    }

    private class TaskRunnable(private val number: Int, private val task: Task) : Runnable {
        override fun run() {
            LOGGER.debug("Starting {} thread #{}", task.name, number)
            while (!Thread.currentThread().isInterrupted) {
                task.execute()
            }
            LOGGER.debug("Stopping {} thread #{}", task.name, number)
        }
    }
}