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
package com.github.jchipmunk.strelastik.step.zookeeper

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.model.zookeeper.ZNode
import com.github.jchipmunk.strelastik.step.Step
import com.github.jchipmunk.strelastik.step.StepFactory
import com.github.jchipmunk.strelastik.task.zookeeper.ZooKeeperCreateTaskFactory
import com.github.jchipmunk.strelastik.task.zookeeper.ZooKeeperMixedTaskFactory
import org.apache.curator.framework.CuratorFramework

class ZooKeeperStepFactory(
        private val clientFactory: () -> CuratorFramework,
        private val znodes: Array<ZNode>) : StepFactory {
    override fun createStep(number: Int, item: ObjectNode): Step {
        val operationNode = item.get("operation") ?: throw IllegalArgumentException("operation field isn't found")
        val durationMsNode = item.get("durationMs") ?: throw IllegalArgumentException("durationMs field isn't found")
        val threadsNode = item.get("threads") ?: throw IllegalArgumentException("threads field isn't found")
        val taskFactory = when (val operation = operationNode.textValue()) {
            ZooKeeperCreateTaskFactory.OPERATION -> {
                ZooKeeperCreateTaskFactory(znodes, clientFactory)
            }
            ZooKeeperMixedTaskFactory.OPERATION -> {
                ZooKeeperMixedTaskFactory(toMap(znodes), clientFactory)
            }
            else -> throw UnsupportedOperationException("$operation operation isn't supported")
        }
        val name = "${taskFactory.getOperation()}-$number"
        return Step(name, taskFactory, durationMsNode.longValue(), threadsNode.intValue())
    }

    private fun toMap(znodes: Array<ZNode>): Map<String, ZNode> {
        val result = hashMapOf<String, ZNode>()
        znodes.forEach { result[it.name] = it }
        return result
    }
}
