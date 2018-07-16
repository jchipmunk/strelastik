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
import com.github.jchipmunk.strelastik.data.ExecutionContext
import io.searchbox.client.JestClient

class IndexTaskFactory(
        private val taskConfig: IndexTask.Config,
        private val client: JestClient) : TaskFactory {
    companion object {
        const val OPERATION = "index"
    }

    override fun create(context: ExecutionContext, registry: MetricRegistry): Task {
        return IndexTask(OPERATION, taskConfig, client, context, registry)
    }

    override fun getOperation(): String {
        return OPERATION
    }
}