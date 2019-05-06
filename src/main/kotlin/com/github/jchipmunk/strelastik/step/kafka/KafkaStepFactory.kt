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
package com.github.jchipmunk.strelastik.step.kafka

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.model.kafka.Message
import com.github.jchipmunk.strelastik.step.Step
import com.github.jchipmunk.strelastik.step.StepFactory
import com.github.jchipmunk.strelastik.task.kafka.KafkaConsumeTaskFactory
import com.github.jchipmunk.strelastik.task.kafka.KafkaProduceTaskFactory
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.ByteArrayDeserializer
import org.apache.kafka.common.serialization.ByteArraySerializer
import java.util.*

class KafkaStepFactory(
        private val config: Properties,
        private val messages: Array<Message>) : StepFactory {
    override fun createStep(number: Int, item: ObjectNode): Step {
        val operationNode = item.get("operation") ?: throw IllegalArgumentException("operation field isn't found")
        val durationMsNode = item.get("durationMs") ?: throw IllegalArgumentException("durationMs field isn't found")
        val threadsNode = item.get("threads") ?: throw IllegalArgumentException("threads field isn't found")
        val taskFactory = when (val operation = operationNode.textValue()) {
            KafkaProduceTaskFactory.OPERATION -> {
                val producerConfig = Properties()
                producerConfig.putAll(config)
                producerConfig[KEY_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
                producerConfig[VALUE_SERIALIZER_CLASS_CONFIG] = ByteArraySerializer::class.java.name
                KafkaProduceTaskFactory(messages, producerConfig)
            }
            KafkaConsumeTaskFactory.OPERATION -> {
                val groupIdNode = item.get("groupId") ?: throw IllegalArgumentException("groupId field isn't found")
                val consumerConfig = Properties()
                consumerConfig.putAll(config)
                consumerConfig[GROUP_ID_CONFIG] = groupIdNode.textValue()
                consumerConfig[KEY_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
                consumerConfig[VALUE_DESERIALIZER_CLASS_CONFIG] = ByteArrayDeserializer::class.java.name
                val topics = messages.map { it.name }.toSet()
                KafkaConsumeTaskFactory(topics, consumerConfig)
            }
            else -> throw UnsupportedOperationException("$operation operation isn't supported")
        }
        val name = "${taskFactory.getOperation()}-$number"
        return Step(name, taskFactory, durationMsNode.longValue(), threadsNode.intValue())
    }
}