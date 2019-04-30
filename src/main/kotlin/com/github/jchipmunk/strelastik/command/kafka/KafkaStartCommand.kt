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
package com.github.jchipmunk.strelastik.command.kafka

import com.github.jchipmunk.strelastik.model.kafka.Message
import com.github.jchipmunk.strelastik.model.kafka.Profile
import com.github.jchipmunk.strelastik.step.StepRunner
import com.github.jchipmunk.strelastik.step.kafka.KafkaStepFactory
import com.github.mustachejava.Mustache
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KafkaStartCommand : KafkaCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(KafkaStartCommand::class.java)
    }

    private val stepRunner = StepRunner()

    override fun name(): String {
        return "kafka-start"
    }

    override fun execute() {
        val storage = createStorage()
        val profile = storage.profile(profileFile, Profile::class.java)
                ?: throw IllegalArgumentException("profile: $profileFile isn't found")
        val topic = profile.topic
        if (topic.definitions.isEmpty()) throw IllegalArgumentException("list of topic definitions are empty")
        val messages = arrayListOf<Message>()
        for (definition in topic.definitions) {
            val template = storage.template(definition.messageFile, Mustache::class.java)
                    ?: throw IllegalArgumentException("message file: ${definition.messageFile} isn't found")
            messages.add(Message(topic.prefix + definition.name, template))
        }
        if (topic.cleanup) {
            val topics = messages.map { it.name }.toSet()
            clear(topics)
        }
        val stepFactory = KafkaStepFactory(getConfig(), messages.toTypedArray())
        stepRunner.start(stepFactory, profile.steps)
    }

    override fun logger(): Logger {
        return LOGGER
    }
}
