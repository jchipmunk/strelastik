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
package com.github.jchipmunk.strelastik.command.elasticsearch

import com.github.jchipmunk.strelastik.model.elasticsearch.Profile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ElasticsearchClearCommand : ElasticsearchCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticsearchClearCommand::class.java)
    }

    override fun name(): String {
        return "elasticsearch-clear"
    }

    override fun execute() {
        val profile = createStorage().profile(profileFile, Profile::class.java)
                ?: throw IllegalArgumentException("profile: $profileFile isn't found")
        val index = profile.index
        if (index.definitions.isEmpty()) throw IllegalArgumentException("list of index definitions are empty")
        val client = createClient()
        logger().info("> Clear Elasticsearch")
        for (definition in index.definitions) {
            val indexName = index.prefix + definition.name
            if (indexExists(indexName, client)) deleteIndex(indexName, client)
        }
        logger().info("> Elasticsearch is cleared of indexes")
    }

    override fun logger(): Logger {
        return LOGGER
    }
}