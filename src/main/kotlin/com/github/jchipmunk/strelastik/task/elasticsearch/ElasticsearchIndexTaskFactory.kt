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
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskFactory
import io.searchbox.client.JestClient

class ElasticsearchIndexTaskFactory(
        private val taskConfig: ElasticsearchIndexTask.Config,
        private val client: JestClient) : TaskFactory {
    companion object {
        const val OPERATION = "index"
    }

    override fun createTask(executionRegistry: ExecutionRegistry, metricRegistry: MetricRegistry): Task {
        return ElasticsearchIndexTask(taskConfig, OPERATION, client, executionRegistry, metricRegistry)
    }

    override fun getOperation(): String {
        return OPERATION
    }
}