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
package com.github.jchipmunk.strelastik.task.elasticsearch

import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.model.elasticsearch.Document
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.TaskContext
import io.searchbox.client.JestClient
import io.searchbox.core.Bulk
import io.searchbox.core.Doc
import io.searchbox.core.Index
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ElasticsearchIndexTask(
        private val config: Config,
        name: String,
        client: JestClient,
        executionRegistry: ExecutionRegistry,
        metricRegistry: MetricRegistry) : ElasticsearchTask(name, client, executionRegistry, metricRegistry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ElasticsearchIndexTask::class.java)
    }

    override fun execute(context: TaskContext) {
        val bulkBuilder = Bulk.Builder()
        var i = 0
        while (i < config.batchSize) {
            for (document in config.documents) {
                if (!context.isRunning()) return
                val index = Index.Builder(document.generate()).index(document.index).type(document.type).build()
                bulkBuilder.addAction(index)
                i += 1
                if (i == config.batchSize) {
                    break
                }
            }
        }
        execute(context, bulkBuilder.build()) {
            val items = it.items
            meter.mark(items.size.toLong())
            for (item in items) {
                if (item.error == null) {
                    val doc = Doc(item.index, item.type, item.id)
                    docs[doc.id] = doc
                } else {
                    documentFailureCounter.inc()
                    logger().info("> Got error: {} of type: {} with reason: {} for document: {} from index: {}!",
                            item.error, item.errorType, item.errorReason, item.id, item.index)
                }
            }
        }
    }

    override fun logger(): Logger {
        return LOGGER
    }

    class Config(val batchSize: Int, val documents: Array<Document>)
}