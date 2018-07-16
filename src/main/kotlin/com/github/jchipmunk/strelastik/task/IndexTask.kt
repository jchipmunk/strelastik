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
package com.github.jchipmunk.strelastik.task

import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.data.Document
import com.github.jchipmunk.strelastik.data.ExecutionContext
import io.searchbox.client.JestClient
import io.searchbox.core.Bulk
import io.searchbox.core.Doc
import io.searchbox.core.Index
import org.slf4j.LoggerFactory

class IndexTask(
        name: String,
        private val config: Config,
        client: JestClient,
        context: ExecutionContext,
        registry: MetricRegistry) : AbstractTask(name, client, context, registry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(IndexTask::class.java)
    }

    override fun execute() {
        val bulkBuilder = Bulk.Builder()
        var i = 0
        while (i < config.batchSize) {
            for (document in config.documents) {
                val index = Index.Builder(document.generate()).index(document.index).type(document.type).build()
                bulkBuilder.addAction(index)
                i += 1
                if (i == config.batchSize) {
                    break
                }
            }
        }
        executeRequest(bulkBuilder.build()) {
            val items = it.items
            meter.mark(items.size.toLong())
            for (item in items) {
                if (item.error == null) {
                    context.add(Doc(item.index, item.type, item.id))
                } else {
                    documentFailureCounter.inc()
                    LOGGER.info("Got error: {} of type: {} with reason: {} for document: {} from index: {}!",
                            item.error, item.errorType, item.errorReason, item.id, item.index)
                }
            }
        }
    }

    class Config(val batchSize: Int, val documents: Array<Document>)
}