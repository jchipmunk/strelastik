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
package com.github.jchipmunk.strelastik.command

import org.slf4j.LoggerFactory

class ClearCommand : Command() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ClearCommand::class.java)
    }

    override fun execute() {
        val storage = createStorage()
        val profile = storage.profile(profileFile) ?: throw IllegalArgumentException("profile: $profileFile isn't found")
        if (profile.index.definitions.isEmpty()) throw IllegalArgumentException("list of index definitions are empty")
        val client = createClient()
        for (definition in profile.index.definitions) {
            val indexName = profile.index.prefix + definition.name
            if (indexExists(indexName, client)) deleteIndex(indexName, client)
        }
        LOGGER.info("Elasticsearch is cleared of indexes")
    }

    override fun shutdown() {
        // do nothing
    }
}