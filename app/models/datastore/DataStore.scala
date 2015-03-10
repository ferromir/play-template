package models.datastore

import models.Persistable
import play.api.libs.json.{ Writes, Reads }

import scala.concurrent.Future
import scala.reflect.ClassTag

trait DataStore {

  def find[T](id: String)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Option[T]]

  def find[T](fields: (String, String)*)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Seq[T]]

  def findAll[T](implicit ct: ClassTag[T], reader: Reads[T]): Future[Seq[T]]

  def persist[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T]): Future[Boolean]

  def modify[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T]): Future[Boolean]

  def remove[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T]): Future[Boolean]

}

//TODO a JsonDataStore for databases like MongoDB
