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
package com.github.jchipmunk.strelastik.step.elasticsearch

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.model.elasticsearch.Document
import com.github.jchipmunk.strelastik.step.Step
import com.github.jchipmunk.strelastik.step.StepFactory
import com.github.jchipmunk.strelastik.task.elasticsearch.ElasticsearchIndexTask
import com.github.jchipmunk.strelastik.task.elasticsearch.ElasticsearchIndexTaskFactory
import com.github.jchipmunk.strelastik.task.elasticsearch.ElasticsearchMixedTask
import com.github.jchipmunk.strelastik.task.elasticsearch.ElasticsearchMixedTaskFactory
import io.searchbox.client.JestClient

class ElasticsearchStepFactory(
        private val client: JestClient,
        private val documents: Array<Document>) : StepFactory {
    override fun createStep(number: Int, item: ObjectNode): Step {
        val operationNode = item.get("operation") ?: throw IllegalArgumentException("operation field isn't found")
        val durationMsNode = item.get("durationMs") ?: throw IllegalArgumentException("durationMs field isn't found")
        val threadsNode = item.get("threads") ?: throw IllegalArgumentException("threads field isn't found")
        val operation = operationNode.textValue()
        val taskFactory = when (operation) {
            ElasticsearchIndexTaskFactory.OPERATION -> {
                val taskConfig = ElasticsearchIndexTask.Config(bulkSizeNode(item).intValue(), documents)
                ElasticsearchIndexTaskFactory(taskConfig, client)
            }
            ElasticsearchMixedTaskFactory.OPERATION -> {
                val taskConfig = ElasticsearchMixedTask.Config(bulkSizeNode(item).intValue(), toMap(documents))
                ElasticsearchMixedTaskFactory(taskConfig, client)
            }
            else -> throw UnsupportedOperationException("$operation operation isn't supported")
        }
        val name = "${taskFactory.getOperation()}-$number"
        return Step(name, taskFactory, durationMsNode.longValue(), threadsNode.intValue())
    }

    private fun bulkSizeNode(item: ObjectNode): JsonNode {
        return item.get("bulkSize") ?: throw IllegalArgumentException("bulkSize field isn't found")
    }

    private fun toMap(documents: Array<Document>): Map<String, Document> {
        val result = hashMapOf<String, Document>()
        documents.forEach { result[it.index] = it }
        return result
    }
}