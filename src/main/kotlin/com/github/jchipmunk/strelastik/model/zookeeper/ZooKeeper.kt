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
package com.github.jchipmunk.strelastik.model.zookeeper

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.model.context
import com.github.mustachejava.Mustache
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.imps.CuratorFrameworkState
import java.io.StringWriter
import java.util.concurrent.TimeUnit

class Definition(
        val name: String,
        val znodeFile: String?)

class ZTree(
        val namespace: String,
        val cleanup: Boolean,
        val definitions: List<Definition>
)

class Profile(
        val ztree: ZTree,
        val steps: List<ObjectNode>)

class ZNode(
        val name: String,
        private val template: Mustache?) {

    fun generate(): String? {
        return if (template == null) null else StringWriter().use { template.execute(it, context).toString() }
    }
}

class ZPath(
        val name: String,
        val path: String)

fun startClient(client: CuratorFramework) {
    if (client.state == CuratorFrameworkState.STARTED) {
        return
    }
    client.start()
    try {
        client.blockUntilConnected(5, TimeUnit.SECONDS)
    } catch (e: InterruptedException) {
        Thread.currentThread().interrupt()
    }
}