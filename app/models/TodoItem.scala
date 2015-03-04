package models

import play.api.Play.current
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.Future

case class TodoItem(
  id: Option[String],
  description: String,
  completed: Option[Boolean] = Some(false)
)

object TodoItem {

  implicit val todoItemReads: Reads[TodoItem] = (
    (JsPath \ "id").readNullable[String] and
    (JsPath \ "description").read[String] and
    (JsPath \ "completed").readNullable[Boolean]
  )(TodoItem.apply _)

  implicit val todoItemWrites = Json.writes[TodoItem]

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("todos")

  def find(id: String): Future[Option[TodoItem]] =
    collection.find(Json.obj("id" -> id)).cursor[TodoItem].headOption

  def findAll(): Future[List[TodoItem]] =
    collection.find(Json.obj()).cursor[TodoItem].collect[List]()

  def save(todoItem: TodoItem) = collection.insert(todoItem)
}

