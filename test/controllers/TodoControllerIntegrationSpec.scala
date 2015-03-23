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

package controllers

import com.github.athieriot.EmbedConnection
import de.flapdoodle.embed.mongo.distribution.Version
import helpers.MongoTestHelpers
import models.TodoItem
import org.specs2.execute.Success
import play.api.libs.json.{ JsObject, Json, JsValue, JsArray }
import play.api.test._

import scala.concurrent._
import scala.concurrent.duration._

object TodoControllerIntegrationSpec extends PlaySpecification
    with EmbedConnection
    with MongoTestHelpers {

  override def embedConnectionPort(): Int = { 27017 }
  override def embedMongoDBVersion(): Version.Main = { Version.Main.V2_7 }

  // TODO: To be removed to  DRY (it's app code also)
  implicit val todoItemFormat = Json.format[TodoItem]

  sequential

  "TodoController" should {

    "#all" should {
      "return all todo items" in new WithApplication(FakeApplication()) {
        val todos = for (n <- 1 until 5) yield TodoItem(newId, s"Task $n for GET: /todos", false)
        Await.result(loadDocuments[TodoItem](todos), 10 seconds)

        val uri = routes.TodoController.all().url
        val req = FakeRequest("GET", uri)
        val Some(result) = route(req)

        status(result) must be equalTo OK
        contentAsString(result) must contain(todos(0).id)
        contentAsJson(result) must beAnInstanceOf[JsArray]

        Await.result(Future.sequence(todos map { t => clean[TodoItem](Map("id" -> t.id)) }), 10 seconds)
      }
    }

    "#get" should {
      "return the todo item by its id" in new WithApplication(FakeApplication()) {
        val todo = TodoItem(newId, "Task for GET: /todos/:id", false)
        Await.result(loadDocument[TodoItem](todo), 10 seconds)

        val uri = routes.TodoController.get(todo.id).url
        val req = FakeRequest("GET", uri)
        val Some(result) = route(req)

        status(result) must be equalTo OK
        contentAsString(result) must contain(todo.id)
        contentAsJson(result) must beAnInstanceOf[JsValue]

        Await.result(clean[TodoItem](Map("id" -> todo.id)), 10 seconds)
      }
    }

    "#save" should {
      "store a new todo item" in new WithApplication(FakeApplication()) {
        val newTodoDescription = "New Task  for POST: /todos"

        val uri = routes.TodoController.save().url
        val req = FakeRequest("POST", uri) withJsonBody (Json.obj("description" -> newTodoDescription))
        val Some(result) = route(req)

        status(result) must be equalTo 201

        val newTodoAsJson = contentAsJson(result)
        newTodoAsJson must beAnInstanceOf[JsObject]
        (newTodoAsJson \ "description").as[String] must be equalTo newTodoDescription
        (newTodoAsJson \ "completed").as[Boolean] must be equalTo false
        (newTodoAsJson \ "id").as[String] must not beEmpty

        Await.result(findOne[TodoItem](Map("description" -> newTodoDescription)), 10 seconds) must beSome[TodoItem]
      }
    }

    "#update" should {
      "update a todo item" in new WithApplication(FakeApplication()) {
        val todo = TodoItem(newId, "Task for PUT: /todos/:id", false)
        Await.result(loadDocument[TodoItem](todo), 10 seconds)

        val uri = routes.TodoController.update(todo.id).url
        val req = FakeRequest("PUT", uri) withJsonBody (Json.toJson(todo.copy(completed = true)))
        val Some(result) = route(req)

        status(result) must be equalTo 200

        Await.result(findOne[TodoItem](Map("id" -> todo.id)), 10 seconds) match {
          case Some(item) => item.completed must beTrue
          case _ => failure(s"Couldn't verify the updated item with id $todo.id")
        }
      }
    }

    "#delete" should {
      "delete a todo item" in new WithApplication(FakeApplication()) {
        val todo = TodoItem(newId, "Task for DELETE: /todos/:id", false)
        Await.result(loadDocument[TodoItem](todo), 10 seconds)

        val uri = routes.TodoController.delete(todo.id).url
        val req = FakeRequest("DELETE", uri)
        val Some(result) = route(req)

        status(result) must be equalTo 200

        Await.result(findOne[TodoItem](Map("id" -> todo.id)), 10 seconds) match {
          case Some(_) => failure(s"Expecting todo item id $todo.id to be removed from the database")
          case _ => Success
        }
      }
    }
  }

}
