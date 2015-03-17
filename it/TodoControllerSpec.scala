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

import java.io.File

import com.github.athieriot.EmbedConnection
import com.typesafe.config.ConfigFactory
import de.flapdoodle.embed.mongo.distribution.Version
import ithelpers.MongoTestHelpers
import ithelpers.BeforeAll
import models.TodoItem

import play.api.libs.json.{JsObject, Json, JsArray}
import play.api.mvc.Results
import play.api.test.{FakeApplication, WithServer, PlaySpecification}
import play.api.libs.ws._
import play.api.Play.current

object TodoControllerSpec extends PlaySpecification
                              with EmbedConnection
                              with MongoTestHelpers with BeforeAll {

  override def embedConnectionPort(): Int = { 27017 }
  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  val port = 9000
  val url = s"http://localhost:$port/todos"

  import ithelpers.BSONFormatters._

  val TodoItems = mongoDB("todoitem")

  def beforeAll = TodoItems.cleanCollection()

  sequential

  "GET to /todos" should {
    "return all persisted data" in new WithServer(FakeApplication(), port) {
      val todos = TodoItems.create[TodoItem] {
        for (n <- 0 until 5)
          yield TodoItem(newId, s"Task $n for GET: /todos", false)
      }

      val response = await(WS.url(url).get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsArray]
      (response.json \\ "id").size must be equalTo 5

      todos foreach { todo => TodoItems.remove("id" -> todo.id) }
    }
  }

  "GET to /todos/:id" should {
    "return the expected todo item" in new WithServer(FakeApplication(), port) {
      val todo = TodoItem(newId, "Task for GET: /todos/:id", false)
      TodoItems.create[TodoItem](todo)

      val response = await(WS.url(s"$url/${todo.id}").get())

      response.status must equalTo(OK)
      response.json must beAnInstanceOf[JsObject]
      (response.json \ "id").as[String] must be equalTo todo.id

      TodoItems.remove[TodoItem] ("id" -> todo.id)
    }
  }

  "POST to /todos" should {
    "store a new todo" in new WithServer(FakeApplication(), port) {
      val newTodoDescription = "New Task  for POST: /todos"

      val response = await(
        WS.url(url).post(Json.obj("description" -> newTodoDescription))
      )

      response.status must be equalTo 201

      TodoItems.findOne[TodoItem]("description" -> newTodoDescription) must beSome[TodoItem]
    }
  }

  "PUT to /todos/:id" should {
    "update a todo" in new WithServer(FakeApplication(), port) {
      val todo = TodoItems.create[TodoItem] {
        TodoItem(newId, "Task for PUT: /todos/:id", false)
      }

      val response = await(
        WS.url(s"$url/${todo.id}").put(Map("completed" -> Seq("true")))
      )

      val updatedTodoInDB = TodoItems.findOne[TodoItem]("id" -> todo.id)
      println(updatedTodoInDB)
      updatedTodoInDB match {
        case Some(item) => item.completed must beTrue
        case None => throw new Exception(s"Couldn't find the updated TODO ${todo.id}")
      }
    }
  }

  "DELETE to /todos/:id" should {
    "delete a todo item" in new WithServer(FakeApplication(), port) {
      val todo = TodoItems.create[TodoItem] {
        TodoItem(newId, "Task for DELETE: /todos/:id", false)
      }

      println("TODO ID: " + todo.id)

      val response = await(WS.url(s"$url/${todo.id}").delete())

      TodoItems.findOne[TodoItem]("id" -> todo.id) must not beSome
    }
  }

}
