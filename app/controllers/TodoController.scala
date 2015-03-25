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

import models.datastore.{ MongoDBDataStore, DataStore }
import models.TodoItem
import models.JsonFormats._

import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json._
import play.api.mvc._
import helpers.Helpers._

import scala.concurrent.Future

case class CreateTodo(description: String)
case class UpdateTodo(description: Option[String], completed: Option[Boolean])

trait TodoController { this: Controller with DataStore =>

  implicit val createTodoReads = Json.reads[CreateTodo]
  implicit val updateTodoReads = Json.reads[UpdateTodo]

  def get(id: String): Action[AnyContent] = Action.async {
    find[TodoItem](id) map { item =>
      item match {
        case Some(persisted) => Ok(Json.toJson(persisted))
        case None => NotFound
      }
    }
  }

  def all: Action[AnyContent] = Action.async {
    findAll[TodoItem] map { items =>
      Ok(Json.toJson(items))
    }
  }

  def save: Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
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

  def update(id: String): Action[JsValue] = Action.async(BodyParsers.parse.json) {
    implicit request =>
      request.body.validate[UpdateTodo].fold(
        errors => {
          Future(BadRequest(toJsonErrors(errors)))
        },
        todoUpdate => {
          find[TodoItem](id) flatMap { itemOption =>
            itemOption match {
              case None => {
                Future(NotFound(Json.obj("error" -> s"Invalid Item ID: '$id'")))
              }
              case Some(item) => {
                val updatedTodo = item.update(
                  todoUpdate.description, todoUpdate.completed
                )

                modify[TodoItem](updatedTodo) map { _ =>
                  Ok(Json.toJson(updatedTodo))
                }
              }
            }
          }
        }.recover {
          case e: Exception => InternalServerError
        }
      )
  }

  def delete(id: String): Action[AnyContent] = Action.async { request =>
    find[TodoItem](id) flatMap { itemOption =>
      itemOption match {
        case None => {
          Future(NotFound(Json.obj("error" -> s"Invalid Item ID: '$id'")))
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
