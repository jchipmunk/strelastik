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
import com.github.jchipmunk.strelastik.model.zookeeper.ZNode
import com.github.jchipmunk.strelastik.model.zookeeper.ZPath
import com.github.jchipmunk.strelastik.model.zookeeper.startClient
import com.github.jchipmunk.strelastik.step.ExecutionRegistry
import com.github.jchipmunk.strelastik.task.TaskContext
import org.apache.curator.framework.CuratorFramework
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZooKeeperMixedTask(
        private val config: Config,
        name: String,
        client: CuratorFramework,
        executionRegistry: ExecutionRegistry,
        metricRegistry: MetricRegistry) : ZooKeeperTask(name, client, executionRegistry, metricRegistry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ZooKeeperCreateTask::class.java)
    }

    private var iterator: Iterator<ZPath>? = null

    override fun execute(context: TaskContext) {
        startClient(client)
        if (iterator == null) iterator = zpaths.values.iterator()
        while (iterator!!.hasNext()) {
            if (!context.isRunning()) return
            val zpath = iterator!!.next()
            execute(context) { client.data.forPath(zpath.path) }
            execute(context) {
                val znode = config.znodes[zpath.name]
                val data = znode!!.generate()
                val setter = client.setData()
                if (data == null) setter.forPath(zpath.path) else setter.forPath(zpath.path, data.toByteArray())
            }
            if (!iterator!!.hasNext()) iterator = zpaths.values.iterator()
        }
    }

    override fun logger(): Logger {
        return LOGGER
    }

    class Config(val znodes: Map<String, ZNode>)
}