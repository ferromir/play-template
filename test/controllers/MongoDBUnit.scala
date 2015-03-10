package controllers

import helpers.WithMongoDB

import play.api.libs.json.{ Reads, Json, JsValue }
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

trait MongoDBUnit extends WithMongoDB {

  def loadJson(filePath: String, collection: String) = ???

  def loadJson(collection: String, json: JsValue)(implicit ec: ExecutionContext): Future[String] = {
    db.collection[JSONCollection](collection).insert(json) map { lastError =>
      println(lastError.elements)

      "DONE"
    }
  }

  def findInMongoDB[T](collection: String, params: JsValue)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[List[T]] =
    db.collection[JSONCollection](collection).find(params).cursor[T].collect[List]()

  def removeFromMongoDB(id: String, collection: String)(implicit ec: ExecutionContext) =
    db.collection[JSONCollection](collection).remove(Json.obj("_id" -> Json.obj("$oid" -> id)))

}
