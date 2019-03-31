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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.github.jchipmunk.strelastik.model.elasticsearch.Document
import com.github.jchipmunk.strelastik.model.elasticsearch.Profile
import com.github.jchipmunk.strelastik.step.StepRunner
import com.github.jchipmunk.strelastik.step.elasticsearch.ElasticsearchStepFactory
import com.github.mustachejava.Mustache
import io.searchbox.client.JestClient
import io.searchbox.indices.CreateIndex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ElasticsearchStartCommand : ElasticsearchCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticsearchStartCommand::class.java)
    }

    private val stepRunner = StepRunner()

    override fun name(): String {
        return "elasticsearch-start"
    }

    override fun execute() {
        val storage = createStorage()
        val profile = storage.profile(profileFile, Profile::class.java)
                ?: throw IllegalArgumentException("profile: $profileFile isn't found")
        val index = profile.index
        if (index.definitions.isEmpty()) throw IllegalArgumentException("list of index definitions are empty")
        val client = createClient()
        val documents = arrayListOf<Document>()
        for (definition in index.definitions) {
            val template = storage.template(definition.documentFile, Mustache::class.java)
                    ?: throw IllegalArgumentException("document file: ${definition.documentFile} isn't found")
            val indexName = index.prefix + definition.name
            var indexExists = indexExists(indexName, client)
            if (indexExists && index.cleanup) {
                deleteIndex(indexName, client)
                indexExists = false
            }
            if (!indexExists) {
                documents.add(Document(indexName, index.type, template))
                val settings = index.settings?.toString()
                val mapping = storage.template(definition.mappingFile, JsonNode::class.java)
                val mappings =
                        if (mapping == null) null
                        else JsonNodeFactory.instance.objectNode().set(index.type, mapping).toString()
                createIndex(indexName, settings, mappings, client)
            }
        }
        val stepFactory = ElasticsearchStepFactory(client, documents.toTypedArray())
        stepRunner.start(stepFactory, profile.steps)
    }

    private fun createIndex(indexName: String, settings: String?, mappings: String?, client: JestClient) {
        logger().debug("> Creating index: {}", indexName)
        val createIndex = CreateIndex.Builder(indexName).settings(settings).mappings(mappings).build()
        val result = client.execute(createIndex)
        if (!result.isSucceeded)
            throw IllegalStateException("Could not createStep index: $indexName, reason: ${result.errorMessage}")
        logger().debug("> Index: {} created", indexName)
    }

    override fun interrupt() {
        stepRunner.stop()
    }

    override fun logger(): Logger {
        return LOGGER
    }
}
