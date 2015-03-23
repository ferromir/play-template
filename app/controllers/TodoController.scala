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
import helpers.Helpers._

import scala.concurrent.Future

case class CreateTodo(description: String)

trait TodoController { this: Controller with DataStore =>

  implicit val todoItemFormat = Json.format[TodoItem]
  implicit val todoItemWrite = Json.writes[TodoItem]
  implicit val createTodoItemReads = Json.reads[CreateTodo]

  def get(id: String) = Action.async {
    find[TodoItem](id) map { item =>
      item match {
        case Some(persisted) => Ok(Json.toJson(persisted))
        case None => NotFound
      }
    }
  }

  def all = Action.async {
    findAll[TodoItem] map { items =>
      Ok(Json.toJson(items))
    }
  }

  def save = Action.async(BodyParsers.parse.json) { implicit request =>
    request.body.validate[CreateTodo].fold(
      errors => {
        Future(BadRequest(toJsonErrors(errors)))
      },
      createTodo => {
        val newTodo = TodoItem(description = createTodo.description)

        persist[TodoItem](newTodo) map { _ =>
          Created(Json.toJson(newTodo))
        }
      }.recover {
        case e: Exception => InternalServerError
      }
    )
  }

  def update(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
    request.body.validate[TodoItem].fold(
      errors => {
        Future(BadRequest(toJsonErrors(errors)))
      },
      updatedTodo => {
        modify[TodoItem](updatedTodo) map { _ =>
          Ok(Json.toJson(updatedTodo))
        }
      }.recover {
        case e: Exception => InternalServerError
      }
    )
  }

  def delete(id: String) = Action.async { request =>
    find[TodoItem](id) flatMap { itemOption =>

      itemOption match {
        case None => {
          val error = Json.obj("error" -> s"Invalid Item ID: '$id'")
          Future(BadRequest(error))
        }
        case Some(item) => {
          remove[TodoItem](item) map (_ => Ok)
        }.recover {
          case e: Exception => InternalServerError
        }
      }
    }
  }

}

object TodoController extends Controller
  with TodoController with MongoDBDataStore
