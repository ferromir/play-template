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

import play.api.Logger
import play.api.libs.json._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits._

import models.TodoItem

import scala.concurrent.Future

trait TodoController { this: Controller =>

  val CreatedOrUpdated = Status(201)

  def get(id: String) = Action.async {
    TodoItem.find(id) map { item =>
      implicit val todoItemFormat = Json.format[TodoItem]
      Ok(Json.toJson(item))
    }
  }

  def all = Action.async {
    TodoItem.findAll() map { items =>
      Ok(Json.toJson(items))
    }
  }

  def save = Action(BodyParsers.parse.json) { request =>
    val modelValidation = request.body.validate[TodoItem]

    modelValidation.fold(
      errors => { BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(errors))) },
      todoItem => { TodoItem.save(todoItem); CreatedOrUpdated }
    )
  }

}

object TodoController extends Controller with TodoController