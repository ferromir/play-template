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

trait TodoController { this: Controller with DataStore =>

  val CreatedOrUpdated = Status(201)

  implicit val todoItemWrites = Json.format[TodoItem]

  def get(id: String) = Action.async {
    find[TodoItem](id) map { item =>
      Ok(Json.toJson(item))
    }
  }

  def all = Action.async {
    findAll[TodoItem] map { items =>
      println(s"items: $items")
      Ok(Json.toJson(items))
    }
  }

  def save = Action(BodyParsers.parse.json) { request =>
    val modelValidation = request.body.validate[TodoItem]

    modelValidation.fold(
      errors => { BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))) },
      todoItem => {
        //save[TodoItem](todoItem) map (_ => CreatedOrUpdated)
        CreatedOrUpdated
      }
    )
  }

  def update(id: String) = Action.async { request =>
    val reqBody = request.body.asFormUrlEncoded

    find[TodoItem](id) map { persisted =>
      persisted match {
        case Some(item) => {
          val uDescription = reqBody flatMap (m => m.get("description")) flatMap (_.headOption) getOrElse item.description
          val uCompletionStatus = reqBody flatMap (m => m.get("completed")) flatMap (_.headOption) getOrElse item.completed

          val updatedItem = TodoItem(item.id, uDescription, uCompletionStatus.toString.toBoolean)

          //save[TodoItem](updatedItem)

          CreatedOrUpdated
        }
        case _ => BadRequest(Json.obj("status" -> "KO", "message" -> s"INVALID_ITEM_ID: '$id'"))
      }
    }
  }

}

object TodoController extends Controller with TodoController with MongoDBDataStore
