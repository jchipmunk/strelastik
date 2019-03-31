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

import com.github.jchipmunk.strelastik.model.zookeeper.Profile
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ZooKeeperClearCommand : ZooKeeperCommand() {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(ZooKeeperClearCommand::class.java)
    }

    override fun name(): String {
        return "zookeeper-clear"
    }

    override fun execute() {
        val storage = createStorage()
        val profile = storage.profile(profileFile, Profile::class.java)
                ?: throw IllegalArgumentException("profile: $profileFile isn't found")
        val ztree = profile.ztree
        if (ztree.definitions.isEmpty()) throw IllegalArgumentException("list of ztree definitions are empty")
        clear(ztree.namespace)
    }

    override fun logger(): Logger {
        return LOGGER
    }
}