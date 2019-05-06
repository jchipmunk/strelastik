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

import com.beust.jcommander.Parameter
import com.github.jchipmunk.strelastik.command.Command
import com.github.jchipmunk.strelastik.storage.FileStorage
import com.github.jchipmunk.strelastik.storage.Storage
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.DeleteTopicsOptions
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException
import org.slf4j.Logger
import java.io.File
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutionException

abstract class KafkaCommand : Command {
    @Parameter(names = ["-h", "--host"], description = "Kafka hosts", required = true)
    private var hosts: String? = null
    @Parameter(names = ["-p", "--profile-file"], description = "Workload profile file", required = true)
    protected var profileFile: String? = null
    @Parameter(names = ["-c", "--config-file"], description = "Kafka configuration file", required = false)
    protected var configFile: String? = null

    abstract fun logger(): Logger

    protected fun getConfig(): Properties {
        val config = Properties()
        if (configFile != null) config.load(File(configFile).inputStream())
        config["bootstrap.servers"] = hosts
        return config
    }

    protected fun createStorage(): Storage {
        val parent = Paths.get(profileFile)?.parent ?: throw IllegalArgumentException("parent directory isn't defined")
        return FileStorage(parent)
    }

    protected fun clear(topics: Set<String>) {
        logger().info("> Clear Kafka")
        AdminClient.create(getConfig()).use {
            logger().debug("> Deleting topics: {}", topics)
            try {
                it.deleteTopics(topics.toMutableSet(), DeleteTopicsOptions().timeoutMs(5000)).all().get()
            } catch (e: ExecutionException) {
                if (e.cause !is UnknownTopicOrPartitionException) {
                    throw e
                }
            }
            logger().debug("> Topics: {} deleted", topics)
        }
        logger().info("> Kafka is cleared of topics")
    }
}