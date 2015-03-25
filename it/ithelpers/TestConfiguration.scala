// Copyright (C) 2015 Jose Saldana.
// See the LICENCE.txt file distributed with this work for additional
// information regarding copyright ownership.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package ithelpers

import de.flapdoodle.embed.mongo.distribution.Version

import collection.JavaConversions._

import com.typesafe.config.{ ConfigFactory, ConfigResolveOptions, Config }

import java.io.File

trait TestConfiguration {

  // Test configuration
  def configFile(): String = {
    val configFileOpt = Option[String](System.getProperty("config.file"))

    configFileOpt.getOrElse("conf/test.application.conf")
  }

  def testConfiguration: Map[String, _] = {
    val cFile = new File(configFile())
    val config: Config = ConfigFactory.parseFile(cFile).resolve(ConfigResolveOptions.defaults())

    config.entrySet().map(e => (e.getKey, e.getValue().unwrapped())).toMap
  }

  // Test MongoDB database
  val mongoInLocalhost = "localhost:27017"

  def TEST_MONGO_SERVERS = {
    val sRegex = """[(*.?)]""".r
    val servers = testConfiguration.get("mongodb.servers")

    (sRegex findFirstIn servers.getOrElse(mongoInLocalhost).toString).getOrElse(mongoInLocalhost)
  }

  def TEST_MONGO_DB: String = {
    testConfiguration.getOrElse("mongodb.db", "todolist_test").toString
  }

  // Embedded Mongo
  val EMBEDDED_MONGODB_PORT = 27017
  val EMBEDDED_MONGODB_VERSION: Version.Main = Version.Main.V2_7

}
