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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.jchipmunk.strelastik.data.Document
import com.github.jchipmunk.strelastik.data.ExecutionContext
import com.github.jchipmunk.strelastik.data.Index
import com.github.jchipmunk.strelastik.step.Step
import com.github.jchipmunk.strelastik.step.StepFactory
import com.github.jchipmunk.strelastik.storage.Storage
import io.searchbox.client.JestClient
import io.searchbox.indices.CreateIndex
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class StartCommand : Command() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(StartCommand::class.java)
    }

    private val lock = ReentrantLock()
    private var running: Boolean = false
    private var currentStep: Step? = null

    override fun execute() {
        lock.withLock {
            if (running) throw IllegalStateException("Already running") else running = true
        }
        try {
            val client = createClient()
            val stepFactory = StepFactory(client)
            val storage = createStorage()
            val profile = storage.profile(profileFile) ?: throw IllegalArgumentException("profile: $profileFile isn't found")
            val documents = prepareDocuments(profile.index, client, storage)
            val context = ExecutionContext()
            for (i in 0 until profile.steps.size) {
                val item = profile.steps[i]
                lock.withLock {
                    if (!running) {
                        return
                    }
                    currentStep = stepFactory.create(i, item, documents)
                }
                currentStep!!.take(context)
            }
        } finally {
            lock.withLock {
                running = false
                currentStep = null
            }
        }
    }

    override fun shutdown() {
        lock.withLock {
            if (running) {
                running = false
                currentStep?.abort()
            }
        }
    }

    private fun prepareDocuments(index: Index, client: JestClient, storage: Storage): Array<Document> {
        if (index.definitions.isEmpty()) throw IllegalArgumentException("list of index definitions are empty")
        val result = arrayListOf<Document>()
        for (definition in index.definitions) {
            val template = storage.document(definition.documentFile)
                    ?: throw IllegalArgumentException("document file: ${definition.documentFile} isn't found")
            val indexName = index.prefix + definition.name
            var indexExists = indexExists(indexName, client)
            if (indexExists && index.cleanup) {
                deleteIndex(indexName, client)
                indexExists = false
            }
            if (!indexExists) {
                result.add(Document(indexName, index.type, template))
                val mapping = storage.mapping(definition.mappingFile)
                val settings = createSettings(index, mapping)
                createIndex(indexName, settings, client)
            }
        }
        return result.toTypedArray()
    }

    private fun createIndex(indexName: String, settings: String, client: JestClient) {
        LOGGER.debug("Creating index: {}", indexName)
        val createIndex = CreateIndex.Builder(indexName).settings(settings).build()
        val result = client.execute(createIndex)
        if (!result.isSucceeded) throw IllegalStateException("Could not create index: $indexName, reason: ${result.errorMessage}")
        LOGGER.debug("Index: {} created", indexName)
    }

    private fun createSettings(index: Index, mapping: JsonNode?): String {
        val indexSource = JsonNodeFactory.instance.objectNode()
        if (index.settings?.size() != 0) {
            indexSource.set("settings", index.settings).toString()
        }
        if (mapping != null) {
            val mappingNode = JsonNodeFactory.instance.objectNode().set(index.type, mapping)
            indexSource.set("mappings", mappingNode)
        }
        return if (indexSource.size() != 0) indexSource.toString() else ""
    }
}
