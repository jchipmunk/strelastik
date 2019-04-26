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
package com.github.jchipmunk.strelastik.command.zookeeper

import com.beust.jcommander.Parameter
import com.github.jchipmunk.strelastik.command.Command
import com.github.jchipmunk.strelastik.model.zookeeper.startClient
import com.github.jchipmunk.strelastik.storage.FileStorage
import com.github.jchipmunk.strelastik.storage.Storage
import org.apache.curator.framework.CuratorFramework
import org.apache.curator.framework.CuratorFrameworkFactory
import org.apache.curator.retry.RetryForever
import org.slf4j.Logger
import java.nio.file.Paths

abstract class ZooKeeperCommand : Command {
    @Parameter(names = ["-h", "--host"], description = "ZooKeeper hosts", required = true)
    private var hosts: String? = null
    @Parameter(names = ["-p", "--profile-file"], description = "Workload profile file", required = true)
    protected var profileFile: String? = null

    abstract fun logger(): Logger

    protected fun createClient(namespace: String? = null): CuratorFramework {
        val clientBuilder = CuratorFrameworkFactory.builder()
                .namespace(namespace)
                .connectString(hosts)
                .retryPolicy(RetryForever(1000))
        return clientBuilder.build()
    }

    protected fun createStorage(): Storage {
        val parent = Paths.get(profileFile)?.parent ?: throw IllegalArgumentException("parent directory isn't defined")
        return FileStorage(parent)
    }

    protected fun clear(namespace: String) {
        logger().info("> Clear ZooKeeper")
        createClient().use {
            startClient(it)
            val path = "/" + namespace
            if (it.checkExists().forPath(path) != null) {
                it.delete().guaranteed().deletingChildrenIfNeeded().forPath(path)
            }
        }
        logger().info("> ZooKeeper is cleared of znodes")
    }
}
