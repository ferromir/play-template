// Copyright (C) 2015 Fernando Romero.
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

import models.datastore.{ MongoDBDataStore, DataStore }
import models.TodoItem
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.{ Promise, Future }

case class CreateTodo(description: String)

trait TodoController { this: Controller with DataStore =>

  val TodoCreated = Status(201)

  implicit val todoItemFormat = Json.format[TodoItem]
  implicit val createTodoItemReads = Json.reads[CreateTodo]

  def get(id: String) = Action.async {
    find[TodoItem](id) map { item =>
      Ok(Json.toJson(item))
    }
  }

  def all = Action.async {
    findAll[TodoItem] map { items =>
      Ok(Json.toJson(items))
    }
  }

  def save = Action.async(BodyParsers.parse.json) { implicit request =>
    val modelValidation = request.body.validate[CreateTodo]

    modelValidation.fold(
      errors => {
        val result = BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors)))
        Future.successful(result)
      },
      createTodo => {
        val newTodo = TodoItem(description = createTodo.description)

        persist[TodoItem](newTodo) map (_ => TodoCreated)
      }.recover { case e: Exception => InternalServerError(e.getMessage) }
    )
  }

  def update(id: String) = Action.async { implicit request =>
    val reqBody = request.body.asFormUrlEncoded

    val updatePromise = Promise[Result]

    find[TodoItem](id) map { persisted =>
      persisted match {
        case Some(item: TodoItem) => {
          val uDescription = reqBody flatMap (m => m.get("description")) flatMap (_.headOption)
          val uCompletionStatus = reqBody flatMap (m => m.get("completed")) flatMap (_.headOption)

          val updatedItem = item.update(uDescription, uCompletionStatus)

          modify[TodoItem](updatedItem) map (_ => updatePromise.trySuccess(Ok))
        }.recover { case e: Exception => updatePromise.trySuccess(InternalServerError(e.getMessage)) }

        case _ => {
          updatePromise.trySuccess(BadRequest(Json.obj("status" -> "KO", "message" -> s"INVALID_ITEM_ID: '$id'")))
        }
      }
    }

    updatePromise.future
  }

  def delete(id: String) = Action.async { request =>
    val deletePromise = Promise[Result]

    find[TodoItem](id) map { e =>
      e match {
        case Some(item: TodoItem) => {
          remove[TodoItem](item) map (_ => deletePromise.trySuccess(Ok))
        }.recover { case e: Exception => deletePromise.trySuccess(InternalServerError(e.getMessage)) }
        case _ => {
          deletePromise.trySuccess(BadRequest(Json.obj("status" -> "KO", "message" -> s"INVALID_ITEM_ID: '$id'")))
        }
      }
    }

    deletePromise.future
  }

}

object TodoController extends Controller with TodoController with MongoDBDataStore
