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

import ithelpers._
import ithelpers.BSONFormatters._

import models.TodoItem

import play.api.libs.json.{JsObject, Json, JsArray}
import play.api.test.{WithServer, PlaySpecification}
import play.api.libs.ws._

class TodoControllerITSpec extends PlaySpecification
                              with EmbeddedMongoHelper
                              with TestConfiguration
                              with TestHelpers
                              with MongoTestHelpers
                              with BeforeAfterAll {

  val port = 9000
  val url = s"http://localhost:$port/todos"

  override def mongoServers() = TEST_MONGO_SERVERS
  override def mongoDbName() = TEST_MONGO_DB
  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  // Might be an issue here.  Need to revise this
  def TodoItemDBH = dbHelperFor("todoitem")

  def beforeAll = {
    startMongo();
    TodoItemDBH.cleanCollection
  }

  def afterAll = {
    TodoItemDBH.closeDB;
    stoptMongo()
  }

  sequential

  "GET to /todos" should {
    "return all todos" in new WithServer(fakeApp, port) {
      val todos = TodoItemDBH.create[TodoItem] {
        for (n <- 0 until 5)
          yield TodoItem(s"Task $n for GET: /todos", false)
      }

      val response = await(WS.url(url).get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsArray]
      //(response.json \\ "id").size must be equalTo 5
      (response.json \\ "description").size must be equalTo 5

      todos foreach { todo => TodoItemDBH.remove("id" -> todo.id) }
    }
  }

  "GET to /todos/:id" should {
    "return the expected todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem("Task for GET: /todos/:id", false)
      }

      val response = await(WS.url(s"$url/${todo.id}").get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsObject]
      //(response.json \ "id").as[String] must be equalTo todo.id

      TodoItemDBH.remove[TodoItem] ("id" -> todo.id)
    }
  }

  "POST to /todos" should {
    "store a new todo" in new WithServer(fakeApp, port) {
      val todoDescription = "New Task  for POST: /todos"

      val jsonRequest = Json.parse(
        s"""{"description": "$todoDescription"}"""
      )

      val response = await(WS.url(url).post(jsonRequest))

      response.status must be equalTo 201
      response.json must beAnInstanceOf[JsObject]
      //(response.json \ "id").as[String] must not beNull
      (response.json \ "description").as[String] must be equalTo todoDescription
      (response.json \ "completed").as[Boolean] must beFalse

      val qTodoCreated = "description" -> todoDescription
      TodoItemDBH.findOne[TodoItem](qTodoCreated) must beSome[TodoItem]
    }
  }

  "PUT to /todos/:id" should {
    "update a todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem("Task for PUT: /todos/:id", false)
      }

      val jsonRequest = Json.parse(
        s"""
          {
            "description": "${todo.description} - Updated",
            "completed": true
          }
        """.trim
      )

      val response = await(
        WS.url(s"$url/${todo.id}").put(jsonRequest)
      )

      response.status must be equalTo(OK)
      //(response.json \ "id").as[String] must equalTo todo.id
      (response.json \ "completed").as[Boolean] must beTrue

      val updatedDescription = (response.json \ "description").as[String]
      updatedDescription must be equalTo s"${todo.description} - Updated"

      TodoItemDBH.findOne[TodoItem]("id" -> todo.id) match {
        case Some(item: TodoItem) => {
          item.description must be equalTo s"${todo.description} - Updated"
          item.completed must beTrue
        }
        case None => {
          throw new Exception(s"Couldn't find the updated TODO ${todo.id}")
        }
      }
    }
  }

  "DELETE to /todos/:id" should {
    "delete a todo item" in new WithServer(fakeApp, port) {
      val todo = TodoItemDBH.create[TodoItem] {
        TodoItem("Task for DELETE: /todos/:id", false)
      }

      val response = await(WS.url(s"$url/${todo.id}").delete())

      response.status must be equalTo(OK)

      TodoItemDBH.findOne[TodoItem]("id" -> todo.id) must beNone
    }
  }

}
