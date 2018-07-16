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
package com.github.jchipmunk.strelastik.data

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.mustachejava.Mustache
import com.google.common.net.InetAddresses
import io.searchbox.core.Doc
import java.io.StringWriter
import java.time.Clock
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_DATE_TIME
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Definition(
        val name: String,
        val documentFile: String,
        val mappingFile: String?)

class Index(
        val settings: ObjectNode?,
        val type: String,
        val prefix: String,
        val cleanup: Boolean,
        val definitions: List<Definition>)

class Profile(
        val index: Index,
        val steps: List<ObjectNode>)

class RandomData {
    private val random = Random()
    val int: Int
        get() = random.nextInt()
    val long: Long
        get() = random.nextLong()
    val boolean: Boolean
        get() = random.nextBoolean()
    val float: Float
        get() = random.nextFloat()
    val double: Double
        get() = random.nextDouble()
    val timestamp: String
        get() = LocalDateTime.now(Clock.systemUTC()).format(ISO_DATE_TIME)
    val string5: String
        get() = getString(5)
    val string10: String
        get() = getString(10)
    val string15: String
        get() = getString(15)
    val string20: String
        get() = getString(20)
    val string25: String
        get() = getString(25)
    val string30: String
        get() = getString(30)
    val string35: String
        get() = getString(35)
    val uuid: String
        get() = UUID.randomUUID().toString()
    val ip: String
        get() = InetAddresses.fromInteger(int).hostAddress

    private fun getString(length: Int): String {
        val value = uuid
        return when {
            length > value.length -> throw IllegalArgumentException("Could not create random string of size $length")
            length == value.length -> value
            else -> value.substring(0, length)
        }
    }
}

class Document(
        val index: String,
        val type: String,
        private val template: Mustache) {
    companion object {
        private val context = mapOf("random" to RandomData())
    }

    fun generate(): String {
        return StringWriter().use { template.execute(it, context).toString() }
    }
}

class ExecutionContext {
    private val docs = ConcurrentHashMap<String, Doc>()

    fun add(doc: Doc) {
        docs[doc.id] = doc
    }

    fun iterator(): MutableIterator<Doc> {
        return docs.values.iterator()
    }
}