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

import helpers.MockDataStore
import models.TodoItem

import org.specs2.execute.Success

import play.api.libs.json.{ JsObject, Json, JsValue, JsArray }
import play.api.mvc._
import play.api.test._

import scala.concurrent._
import scala.concurrent.duration._

import org.specs2.mock._
import org.specs2.matcher.Matchers._
import org.mockito.Matchers._
import org.mockito.Matchers.{ eq => equalTo, any }

import play.api.libs.json.{ Writes, Reads }
import scala.concurrent.{ ExecutionContext }
import scala.reflect.ClassTag

object TodoControllerSpec extends PlaySpecification with Mockito {

  class TestController extends Controller with TodoController with MockDataStore

  "TodoController" should {

    "#all" should {
      "return all todo items" in {
        val controller = new TestController()

        controller.mock.findAll[TodoItem](
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[ExecutionContext]
        ) returns Future(Seq(TodoItem("desc", false)))

        val result: Future[Result] = controller.all().apply(FakeRequest())

        val jsonResponse = contentAsJson(result)
        jsonResponse must beAnInstanceOf[JsArray]
        (jsonResponse \\ "id").size must be equalTo 1
        (jsonResponse \\ "description").size must be equalTo 1
        (jsonResponse \\ "completed").size must be equalTo 1
      }
    }

    "#get" should {
      "return a todo item by its id" in {
        val controller = new TestController()

        val id: String = "A1231dc32312"

        controller.mock.find[TodoItem](any[String])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[ExecutionContext]
        ) returns Future(Some(new TodoItem(id, "desc", false)))

        val result: Future[Result] = controller.get(id).apply(FakeRequest())

        val jsonResponse = contentAsJson(result)
        jsonResponse must beAnInstanceOf[JsObject]
        (jsonResponse \ "id").as[String] must be equalTo id
        (jsonResponse \ "description").as[String] must be equalTo "desc"
        (jsonResponse \ "completed").as[Boolean] must beFalse
      }
    }

    "#save" should {
      "store a new todo item" in {
        val controller = new TestController()

        controller.mock.persist[TodoItem](any[TodoItem])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[Writes[TodoItem]],
          any[ExecutionContext]
        ) returns Future(true)

        val newTodoDesc = "My TODO"
        val req = FakeRequest(
          method = "POST",
          uri = routes.TodoController.save().url,
          headers = FakeHeaders(
            Seq("Content-type" -> Seq("application/json"))
          ),
          body = Json.obj("description" -> newTodoDesc)
        )

        val result: Future[Result] = controller.save().apply(req)

        val jsonResponse = contentAsJson(result)
        jsonResponse must beAnInstanceOf[JsObject]
        (jsonResponse \ "id").asOpt[String] must beSome
        (jsonResponse \ "description").as[String] must be equalTo newTodoDesc
        (jsonResponse \ "completed").as[Boolean] must beFalse
      }
    }

    "#update" should {
      "update a todo item" in {
        val controller = new TestController()

        val todo = TodoItem("desc", false)

        controller.mock.find[TodoItem](any[String])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[ExecutionContext]
        ) returns Future(Some(todo))

        controller.mock.modify[TodoItem](any[TodoItem])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[Writes[TodoItem]],
          any[ExecutionContext]
        ) returns Future(true)

        val req = FakeRequest(
          method = "PUT",
          uri = routes.TodoController.update(todo.id).url,
          headers = FakeHeaders(
            Seq("Content-type" -> Seq("application/json"))
          ),
          body = Json.obj("completed" -> true)
        )

        val result: Future[Result] = controller.update(todo.id).apply(req)

        val jsonResponse = contentAsJson(result)
        jsonResponse must beAnInstanceOf[JsObject]
        (jsonResponse \ "id").as[String] must be equalTo todo.id
        (jsonResponse \ "description").as[String] must be equalTo todo.description
        (jsonResponse \ "completed").as[Boolean] must beTrue

        got {
          one(controller.mock).find(===(todo.id))(
            any[ClassTag[TodoItem]],
            any[Reads[TodoItem]],
            any[ExecutionContext]
          )

          one(controller.mock).
            modify[TodoItem](===(todo.copy(completed = true)))(
              any[ClassTag[TodoItem]],
              any[Reads[TodoItem]],
              any[Writes[TodoItem]],
              any[ExecutionContext]
            )
        }
      }
    }

    "#delete" should {
      "delete a todo item" in {
        val controller = new TestController()

        val todo = TodoItem("desc", false)

        controller.mock.find[TodoItem](any[String])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[ExecutionContext]
        ) returns Future(Some(todo))

        controller.mock.remove[TodoItem](any[TodoItem])(
          any[ClassTag[TodoItem]],
          any[Reads[TodoItem]],
          any[ExecutionContext]
        ) returns Future(true)

        val req = FakeRequest(
          "DELETE", routes.TodoController.delete(todo.id).url
        )

        val result: Future[Result] = controller.delete(todo.id).apply(req)

        status(result) must be equalTo 200

        got {
          one(controller.mock).find(===(todo.id))(
            any[ClassTag[TodoItem]],
            any[Reads[TodoItem]],
            any[ExecutionContext]
          )

          one(controller.mock).remove[TodoItem](===(todo))(
            any[ClassTag[TodoItem]],
            any[Reads[TodoItem]],
            any[ExecutionContext]
          )
        }
      }
    }
  }

}
