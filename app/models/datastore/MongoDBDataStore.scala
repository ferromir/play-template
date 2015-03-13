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

  override def find[T](id: String)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Option[T]] =
    collectionOf.find(Json.obj("id" -> id)).cursor[T].headOption

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