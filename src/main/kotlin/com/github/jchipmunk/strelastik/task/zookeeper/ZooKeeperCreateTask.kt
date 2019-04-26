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
import org.apache.curator.utils.ZKPaths
import org.apache.zookeeper.CreateMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZooKeeperCreateTask(
        private val config: Config,
        name: String,
        client: CuratorFramework,
        executionRegistry: ExecutionRegistry,
        metricRegistry: MetricRegistry) : ZooKeeperTask(name, client, executionRegistry, metricRegistry) {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ZooKeeperCreateTask::class.java)
    }

    override fun execute(context: TaskContext) {
        startClient(client)
        for (znode in config.znodes) {
            execute(context) {
                val data = znode.generate()
                val path = ZKPaths.makePath(znode.name, zpathCounter.incrementAndGet().toString())
                val creator = client.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                if (data == null) creator.forPath(path) else creator.forPath(path, data.toByteArray())
                zpaths[znode.name] = ZPath(znode.name, path)
            }
        }
    }

    override fun logger(): Logger {
        return LOGGER
    }

    class Config(val znodes: Array<ZNode>)
}
