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
package com.github.jchipmunk.strelastik

import com.beust.jcommander.JCommander
import com.github.jchipmunk.strelastik.command.Command
import com.github.jchipmunk.strelastik.command.elasticsearch.ElasticsearchClearCommand
import com.github.jchipmunk.strelastik.command.elasticsearch.ElasticsearchStartCommand
import com.github.jchipmunk.strelastik.command.zookeeper.ZooKeeperClearCommand
import com.github.jchipmunk.strelastik.command.zookeeper.ZooKeeperStartCommand

fun main(args: Array<String>) {
    val commander = JCommander()
    commander.programName = "strelastik"
    val commands = HashMap<String, Command>()
    addCommand(commands, ElasticsearchStartCommand())
    addCommand(commands, ElasticsearchClearCommand())
    addCommand(commands, ZooKeeperStartCommand())
    addCommand(commands, ZooKeeperClearCommand())
    commands.forEach { name, command -> commander.addCommand(name, command) }
    commander.addCommand("help", Any())
    commander.parse(*args)
    val command = commands[commander.parsedCommand]
    if (command == null) {
        commands.keys.forEach { commander.usage(it) }
    } else {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                command.interrupt()
            }
        })
        command.execute()
    }
}

private fun addCommand(commands: MutableMap<String, Command>, command: Command) {
    commands[command.name()] = command
}