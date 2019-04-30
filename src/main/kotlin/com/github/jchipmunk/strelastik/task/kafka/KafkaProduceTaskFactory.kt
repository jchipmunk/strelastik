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
import com.github.jchipmunk.strelastik.model.kafka.Message
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskFactory
import org.apache.kafka.clients.producer.KafkaProducer
import java.util.*

class KafkaProduceTaskFactory(
        private val messages: Array<Message>,
        private val config: Properties) : TaskFactory {
    companion object {
        const val OPERATION = "produce"
    }

    override fun createTask(executionRegistry: ExecutionRegistry, metricRegistry: MetricRegistry): Task {
        val producer = KafkaProducer<ByteArray, ByteArray>(config)
        return KafkaProduceTask(OPERATION, messages, producer, metricRegistry)
    }

    override fun getOperation(): String {
        return OPERATION
    }
}