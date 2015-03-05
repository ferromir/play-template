package models

import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.modules.reactivemongo.json.BSONFormats._

import reactivemongo.bson._
import reactivemongo.api.collections.default.BSONQueryBuilder

import scala.concurrent.Future

case class TodoItem(
  _id: Option[BSONObjectID],
  description: String,
  completed: Boolean = false
)

object TodoItemBuilder {

  def apply(_id: Option[BSONObjectID], description: String, completed: Option[Boolean]) = {
    TodoItem(_id, description, completed getOrElse (false))
  }

  val todoItemReads: Reads[TodoItem] = (
    (JsPath \ "_id").readNullable[BSONObjectID] and
    (JsPath \ "description").read[String] and
    (JsPath \ "completed").readNullable[Boolean]
  )(TodoItemBuilder.apply _)
}

object TodoItem {

  implicit val todoItemReads = TodoItemBuilder.todoItemReads

  implicit val todoItemWrites = Json.writes[TodoItem]

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("todos")

  def find(id: String): Future[Option[TodoItem]] =
    collection.find(Json.obj("_id" -> Json.obj("$oid" -> id.trim))).cursor[TodoItem].headOption

  def findAll(): Future[List[TodoItem]] =
    collection.find(Json.obj()).cursor[TodoItem].collect[List]()

  def save(todoItem: TodoItem) = todoItem._id match {
    case Some(id) => collection.update(Json.obj("_id" -> Json.obj("$oid" -> todoItem._id)), todoItem)
    case None => {
      val newTodo = TodoItem(Some(BSONObjectID.generate), todoItem.description, todoItem.completed)
      collection.insert(newTodo)
    }
  }
}

