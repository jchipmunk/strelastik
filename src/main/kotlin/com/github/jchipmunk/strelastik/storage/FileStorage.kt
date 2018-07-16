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
package com.github.jchipmunk.strelastik.storage

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.github.jchipmunk.strelastik.data.Profile
import com.github.mustachejava.DefaultMustacheFactory
import com.github.mustachejava.Mustache
import com.google.common.io.Files
import java.io.File
import java.io.StringReader
import java.nio.file.Path

class FileStorage(parent: Path) : Storage {
    private val parent = File(parent.toUri())
    private val mapper = ObjectMapper(YAMLFactory()).registerModule(KotlinModule())
    private val mustacheFactory = DefaultMustacheFactory()

    private fun readMustache(file: File): Mustache {
        val source = Files.asCharSource(file, Charsets.UTF_8)
        if (source.isEmpty) throw IllegalArgumentException("${file.name} file is empty")
        val stringBuilder = StringBuilder()
        source.readLines().forEach { stringBuilder.append(it) }
        val value = stringBuilder.toString()
        return mustacheFactory.compile(StringReader(value), file.nameWithoutExtension)
    }

    private fun readString(file: File): String {
        val source = Files.asCharSource(file, Charsets.UTF_8)
        if (source.isEmpty) throw IllegalArgumentException("${file.name} file is empty")
        return source.read()
    }

    private fun file(pathname: String): File {
        val file = File(pathname)
        if (!file.exists()) throw IllegalArgumentException("$pathname file doesn't exist")
        return file
    }

    private fun relativeFile(pathname: String): File {
        val file = parent.resolve(pathname)
        if (!file.exists()) throw IllegalArgumentException("$pathname file doesn't exist")
        return file
    }

    override fun profile(pathname: String?): Profile? {
        return if (pathname == null) null else mapper.readValue(readString(file(pathname)), Profile::class.java)
    }

    override fun document(pathname: String?): Mustache? {
        return if (pathname == null) null else readMustache(relativeFile(pathname))
    }

    override fun mapping(pathname: String?): JsonNode? {
        return if (pathname == null) null else mapper.readTree(readString(relativeFile(pathname)))
    }
}