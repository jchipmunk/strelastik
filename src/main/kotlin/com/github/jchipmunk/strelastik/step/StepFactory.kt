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
package com.github.jchipmunk.strelastik.step

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.data.Document
import com.github.jchipmunk.strelastik.task.IndexTask
import com.github.jchipmunk.strelastik.task.IndexTaskFactory
import com.github.jchipmunk.strelastik.task.MixedTask
import com.github.jchipmunk.strelastik.task.MixedTaskFactory
import io.searchbox.client.JestClient

class StepFactory(private val client: JestClient) {
    fun create(number: Int, item: ObjectNode, documents: Array<Document>): Step {
        val operationNode = item.get("operation") ?: throw IllegalArgumentException("operation field isn't found")
        val durationMsNode = item.get("durationMs") ?: throw IllegalArgumentException("durationMs field isn't found")
        val threadsNode = item.get("threads") ?: throw IllegalArgumentException("threads field isn't found")
        val operation = operationNode.textValue()
        val taskFactory = when (operation) {
            IndexTaskFactory.OPERATION -> {
                val bulkSizeNode = item.get("bulkSize") ?: throw IllegalArgumentException("bulkSize field isn't found")
                val taskConfig = IndexTask.Config(bulkSizeNode.intValue(), documents)
                IndexTaskFactory(taskConfig, client)
            }
            MixedTaskFactory.OPERATION -> {
                val bulkSizeNode = item.get("bulkSize") ?: throw IllegalArgumentException("bulkSize field isn't found")
                val taskConfig = MixedTask.Config(bulkSizeNode.intValue(), toMap(documents))
                MixedTaskFactory(taskConfig, client)
            }
            else -> throw UnsupportedOperationException("$operation operation isn't supported")
        }
        val name = "${taskFactory.getOperation()}-$number"
        return Step(name, taskFactory, durationMsNode.longValue(), threadsNode.intValue())
    }

    private fun toMap(documents: Array<Document>): Map<String, Document> {
        val result = hashMapOf<String, Document>()
        documents.forEach { result[it.index] = it }
        return result
    }
}