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
import com.github.jchipmunk.strelastik.command.ClearCommand
import com.github.jchipmunk.strelastik.command.StartCommand

private const val START_COMMAND = "start"
private const val CLEAR_COMMAND = "clear"

fun main(args: Array<String>) {
    val commander = JCommander()
    commander.programName = "strelastik"
    val startCommand = StartCommand()
    commander.addCommand(START_COMMAND, startCommand)
    val clearCommand = ClearCommand()
    commander.addCommand(CLEAR_COMMAND, clearCommand)
    commander.addCommand("help", Any())
    commander.parse(*args)
    val command = when {
        commander.parsedCommand == START_COMMAND -> startCommand
        commander.parsedCommand == CLEAR_COMMAND -> clearCommand
        else -> {
            commander.usage(START_COMMAND)
            commander.usage(CLEAR_COMMAND)
            null
        }
    }
    if (command != null) {
        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                command.shutdown()
            }
        })
        command.execute()
    }
}