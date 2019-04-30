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
import com.github.jchipmunk.strelastik.task.TaskContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KafkaProduceTask(
        name: String,
        private val messages: Array<Message>,
        private val producer: KafkaProducer<ByteArray, ByteArray>,
        metricRegistry: MetricRegistry) : KafkaTask(name, metricRegistry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(KafkaProduceTask::class.java)
    }

    override fun logger(): Logger {
        return LOGGER
    }

    override fun execute(context: TaskContext) {
        for (message in messages) {
            execute(context) {
                val data = message.generate()
                val record = ProducerRecord<ByteArray, ByteArray>(message.name, data.toByteArray())
                producer.send(record) { _, exception ->
                    if (exception != null) handleThrowable(exception) else meter.mark()
                }
            }
        }
    }

    override fun stop() {
        producer.close()
    }
}