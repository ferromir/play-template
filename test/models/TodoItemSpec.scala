package models

import org.specs2.specification.BeforeAfter

import play.api.Play.current

import play.api.libs.json.Json
import play.api.libs.concurrent.Execution.Implicits._
import play.api.test._

import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

class TodoItemSpec extends PlaySpecification with BeforeAfter {

  def collection: JSONCollection = ReactiveMongoPlugin.db.collection[JSONCollection]("todos")

  val id = "abcdefghijklmn"

  def before = collection.insert(TodoItem(Some(id), "text"))
  def after = collection.remove(Json.obj("id" -> id))

  "Model: TodoItem" should {

    "fetch a TODO item from the data store" in {
      pending("To be implemented")

      running(FakeApplication()) {
        val item = await(TodoItem find id)

        item must beSome
      }
    }
  }

}
