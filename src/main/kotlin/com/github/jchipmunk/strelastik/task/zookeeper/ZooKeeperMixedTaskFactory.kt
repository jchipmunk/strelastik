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
package com.github.jchipmunk.strelastik.task.zookeeper

import com.codahale.metrics.MetricRegistry
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.Task
import com.github.jchipmunk.strelastik.task.TaskFactory
import org.apache.curator.framework.CuratorFramework

class ZooKeeperMixedTaskFactory(
        private val taskConfig: ZooKeeperMixedTask.Config,
        private val clientFactory: () -> CuratorFramework) : TaskFactory {
    companion object {
        const val OPERATION = "mixed"
    }

    override fun createTask(executionRegistry: ExecutionRegistry, metricRegistry: MetricRegistry): Task {
        return ZooKeeperMixedTask(taskConfig, OPERATION, clientFactory.invoke(), executionRegistry, metricRegistry)
    }

    override fun getOperation(): String {
        return OPERATION
    }
}