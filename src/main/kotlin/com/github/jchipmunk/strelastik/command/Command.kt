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
package com.github.jchipmunk.strelastik.command

import com.beust.jcommander.Parameter
import com.github.jchipmunk.strelastik.storage.FileStorage
import com.github.jchipmunk.strelastik.storage.Storage
import io.searchbox.client.JestClient
import io.searchbox.client.JestClientFactory
import io.searchbox.client.config.HttpClientConfig
import io.searchbox.indices.DeleteIndex
import io.searchbox.indices.IndicesExists
import org.slf4j.LoggerFactory
import java.nio.file.Paths

abstract class Command {
    companion object {
        private val LOGGER = LoggerFactory.getLogger(Command::class.java)
    }

    @Parameter(names = ["-h", "--host"], description = "Elasticsearch hosts", required = true)
    private var hosts: List<String> = emptyList()
    @Parameter(names = ["--username"], description = "Elasticsearch username")
    private var username: String? = null
    @Parameter(names = ["--password"], description = "Elasticsearch password", password = true)
    private var password: String? = null
    @Parameter(names = ["-p", "--profile-file"], description = "Workload profile file", required = true)
    protected var profileFile: String? = null

    abstract fun execute()

    abstract fun shutdown()

    protected fun createClient(): JestClient {
        val factory = JestClientFactory()
        val clientConfigBuilder = HttpClientConfig.Builder(hosts)
                .connTimeout(5000)
                .readTimeout(5000)
                .multiThreaded(true)
        if (username != null && password != null) {
            clientConfigBuilder.defaultCredentials(username, password)
        }
        factory.setHttpClientConfig(clientConfigBuilder.build())
        return factory.`object`
    }

    protected fun createStorage(): Storage {
        val parent = Paths.get(profileFile)?.parent ?: throw IllegalArgumentException("parent directory isn't defined")
        return FileStorage(parent)
    }

    protected fun indexExists(index: String, client: JestClient): Boolean {
        val indicesExists = IndicesExists.Builder(index).build()
        val result = client.execute(indicesExists)
        return result.isSucceeded
    }

    protected fun deleteIndex(index: String, client: JestClient) {
        LOGGER.debug("Deleting index: {}", index)
        val deleteIndex = DeleteIndex.Builder(index).build()
        val result = client.execute(deleteIndex)
        if (!result.isSucceeded) throw IllegalStateException("Could not delete index: $index, reason: ${result.errorMessage}")
        LOGGER.debug("Index: {} deleted", index)
    }
}