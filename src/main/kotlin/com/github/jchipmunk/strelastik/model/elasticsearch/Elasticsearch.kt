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
package com.github.jchipmunk.strelastik.model.elasticsearch

import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.jchipmunk.strelastik.model.context
import com.github.mustachejava.Mustache
import java.io.StringWriter

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

class Document(
        val index: String,
        val type: String,
        private val template: Mustache) {

    fun generate(): String {
        return StringWriter().use { template.execute(it, context).toString() }
    }
}