package models.datastore

import models.Persistable
import play.api.libs.json.{ Writes, Reads, Json }
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.Play.current

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

trait MongoDBDataStore extends DataStore {

  private def db = ReactiveMongoPlugin.db

  override def find[T](id: String)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Option[T]] = {
    val q = Json.obj("_id" -> Json.obj("$oid" -> id))

    collectionOf.find(q).cursor[T].headOption
  }

  override def find[T](fields: (String, String)*)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Seq[T]] = {
    val q = fields.map(f => Json.obj(f._1 -> f._2))

    collectionOf.find(q).cursor[T].collect[List]()
  }

  override def findAll[T](implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Seq[T]] = {
    collectionOf.find(Json.obj()).cursor[T].collect[List]()
  }

  override def persist[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T], ec: ExecutionContext): Future[Boolean] = {
    collectionOf[T].insert(obj) map (_ => true)
  }

  override def modify[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T], ec: ExecutionContext): Future[Boolean] = {
    val q = Json.obj("id" -> obj.id)

    collectionOf[T].update(q, obj) map (_ => true)
  }

  override def remove[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Boolean] = {
    val q = Json.obj("id" -> obj.id)

    collectionOf[T].remove(q) map (_ => true)
  }

  private def collectionOf[T](implicit ct: ClassTag[T]) = {
    val collection = ct.runtimeClass.getSimpleName.toLowerCase

    db.collection[JSONCollection](collection)
  }
}