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
import scala.annotation.meta.field

import com.wordnik.swagger.annotations._

@ApiModel
case class CreateTodo(description: String)
case class UpdateTodo(
  description: Option[String],
  @(ApiModelProperty @field)(
    dataType = "boolean", position = 2
  ) completed: Option[Boolean]
)

@Api(value = "/todos", description = "TODO List Management API")
trait TodoController { this: Controller with DataStore =>

  implicit val createTodoReads = Json.reads[CreateTodo]
  implicit val updateTodoReads = Json.reads[UpdateTodo]

  @ApiOperation(
    value = "Get a TODO by its id",
    response = classOf[models.TodoItem],
    httpMethod = "GET",
    produces = "application/json",
    position = 2
  )
  @ApiResponses(Array(new ApiResponse(code = 404, message = "TODO not found")))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "id",
      value = "The TODO's id",
      required = true,
      paramType = "path"
    )
  ))
  def get(id: String): Action[AnyContent] = Action.async {
    find[TodoItem](id) map { item =>
      item match {
        case Some(persisted) => Ok(Json.toJson(persisted))
        case None => NotFound
      }
    }
  }

  @ApiOperation(
    value = "Get all TODOs",
    response = classOf[models.TodoItem],
    responseContainer = "Array",
    httpMethod = "GET",
    produces = "application/json",
    position = 1
  )
  @ApiResponses(Array(
    new ApiResponse(
      code = 200, message = "A list of TODOs or empty if there is none"
    ),
    new ApiResponse(code = 404, message = "TODO not found")
  ))
  def all: Action[AnyContent] = Action.async {
    findAll[TodoItem] map { items =>
      Ok(Json.toJson(items))
    }
  }

  @ApiOperation(
    value = "Creates a new TODO",
    response = classOf[models.TodoItem],
    httpMethod = "POST",
    consumes = "application/json",
    produces = "application/json",
    position = 3
  )
  @ApiResponses(Array(
    new ApiResponse(code = 201, message = "Todo Item Created"),
    new ApiResponse(code = 400, message = "Invalid request"),
    new ApiResponse(code = 500, message = "Internal Server Error")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "The details for a new TODO",
      required = true,
      paramType = "body",
      dataType = "controllers.CreateTodo"
    )
  ))
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

  @ApiOperation(
    value = "Updates an existing TODO",
    response = classOf[models.TodoItem],
    httpMethod = "PUT",
    consumes = "application/json",
    produces = "application/json",
    position = 4
  )
  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "TODO item updated"),
    new ApiResponse(code = 400, message = "Invalid UpdateTodo request information"),
    new ApiResponse(code = 404, message = "Invalid TODO item"),
    new ApiResponse(code = 500, message = "Internal Server Error")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      value = "The details to update a TODO",
      required = true,
      paramType = "body",
      dataType = "controllers.UpdateTodo"
    ),
    new ApiImplicitParam(
      name = "id",
      value = "The TODO item's id",
      required = true,
      paramType = "path"
    )
  ))
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

  @ApiOperation(
    value = "Deletes a TODO",
    httpMethod = "DELETE",
    response = classOf[Void],
    position = 5
  )
  @ApiResponses(Array(
    new ApiResponse(code = 204, message = "TODO item has been deleted"),
    new ApiResponse(code = 404, message = "TODO to be deleted not found"),
    new ApiResponse(code = 500, message = "Internal Server Error")
  ))
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "id",
      value = "ID of the TODO to be deleted",
      required = true,
      paramType = "path"
    )
  ))
  def delete(id: String): Action[AnyContent] = Action.async {
    implicit request =>
      find[TodoItem](id) flatMap { itemOption =>
        itemOption match {
          case None => {
            Future(NotFound(Json.obj("error" -> s"Invalid Item ID: '$id'")))
          }
          case Some(item) => {
            remove[TodoItem](item) map (_ => NoContent)
          }.recover {
            case e: Exception => InternalServerError
          }
        }
      }
  }

}

object TodoController extends Controller
  with TodoController with MongoDBDataStore
