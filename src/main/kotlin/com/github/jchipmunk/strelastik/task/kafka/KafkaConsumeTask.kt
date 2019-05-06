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

import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.task.TaskContext
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.internals.NoOpConsumerRebalanceListener
import org.apache.kafka.common.TopicPartition
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class KafkaConsumeTask(
        name: String,
        private val consumer: KafkaConsumer<ByteArray, ByteArray>,
        private val topics: Set<String>,
        metricRegistry: MetricRegistry) : KafkaTask(name, metricRegistry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(KafkaConsumeTask::class.java)
    }

    override fun logger(): Logger {
        return LOGGER
    }

    override fun start() {
        val assignmentLatch = CountDownLatch(1)
        consumer.subscribe(topics, object : NoOpConsumerRebalanceListener() {
            override fun onPartitionsAssigned(partitions: Collection<TopicPartition>?) {
                consumer.seekToBeginning(partitions)
                assignmentLatch.countDown()
            }
        })
        assignmentLatch.await(5, TimeUnit.SECONDS)
    }

    override fun execute(context: TaskContext) {
        execute(context) {
            val records = consumer.poll(1000L)
            meter.mark(records.count().toLong())
        }
    }

    override fun stop() {
        consumer.close()
    }
}