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

import de.flapdoodle.embed.mongo.distribution.Version
import ithelpers.{EmbeddedMongoHelper, TestConfigurationAndHelpers, MongoTestHelpers, BeforeAfterAll}
import models.TodoItem

import play.api.libs.json.{JsObject, Json, JsArray}
import play.api.test.{WithServer, PlaySpecification}
import play.api.libs.ws._

import ithelpers.BSONFormatters._

class TodoControllerITSpec extends PlaySpecification
                              with EmbeddedMongoHelper
                              with TestConfigurationAndHelpers
                              with MongoTestHelpers
                              with BeforeAfterAll {

  override def mongoServers() = TEST_MONGO_SERVERS
  override def mongoDbName() = TEST_MONGO_DB
  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  val port = 9000
  val url = s"http://localhost:$port/todos"

  sequential

  def TodoItemDBH = dbHelperFor("todoitem")

  def beforeAll = { startMongo(); TodoItemDBH.cleanCollection }
  def afterAll = { TodoItemDBH.closeDB; stoptMongo() }

  "GET to /todos" should {
    "return all todos" in new WithServer(fakeApp, port) {
      val todos = TodoItemDBH.create[TodoItem] {
        for (n <- 0 until 5)
          yield TodoItem(newId, s"Task $n for GET: /todos", false)
      }

      val response = await(WS.url(url).get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsArray]
      (response.json \\ "id").size must be equalTo 5

      todos foreach { todo => TodoItemDBH.remove("id" -> todo.id) }
    }
  }

  "GET to /todos/:id" should {
    "return the expected todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem(newId, "Task for GET: /todos/:id", false)
      }

      val response = await(WS.url(s"$url/${todo.id}").get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsObject]
      (response.json \ "id").as[String] must be equalTo todo.id

      TodoItemDBH.remove[TodoItem] ("id" -> todo.id)
    }
  }

  "POST to /todos" should {
    "store a new todo" in new WithServer(fakeApp, port) {
      val newTodoDescription = "New Task  for POST: /todos"

      val response = await(
        WS.url(url).post(Json.obj("description" -> newTodoDescription))
      )

      response.status must be equalTo 201

      TodoItemDBH.findOne[TodoItem]("description" -> newTodoDescription) must beSome[TodoItem]
    }
  }

  "PUT to /todos/:id" should {
    "update a todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem(newId, "Task for PUT: /todos/:id", false)
      }

      val jsonRequest = Json.parse(
        s"""
          {
            "id": "${todo.id}",
            "description": "${todo.description}",
            "completed": true
          }
        """.trim
      )

      val response = await(
        WS.url(s"$url/${todo.id}").put(jsonRequest)
      )

      val updatedTodoInDB = TodoItemDBH.findOne[TodoItem]("id" -> todo.id)
      updatedTodoInDB match {
        case Some(item) => item.completed must beTrue
        case None => throw new Exception(s"Couldn't find the updated TODO ${todo.id}")
      }
    }
  }

  "DELETE to /todos/:id" should {
    "delete a todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem(newId, "Task for DELETE: /todos/:id", false)
      }

      val response = await(WS.url(s"$url/${todo.id}").delete())

      TodoItemDBH.findOne[TodoItem]("id" -> todo.id) must not beSome
    }
  }

}
