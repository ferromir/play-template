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

package helpers

import play.api.Play.current
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.{ Json, JsValue, Writes, Reads }
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import reactivemongo.bson.BSONObjectID

import scala.concurrent.{ Future, ExecutionContext }
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

trait MongoTestHelpers {

  def db() = { ReactiveMongoPlugin.db }

  def newId(): String = BSONObjectID.generate.stringify

  def loadDocument[T](d: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    writer: Writes[T],
    ec: ExecutionContext): Future[T] = {
    collectionOf[T].insert(d) map { lastError =>
      if (lastError.inError) throw new Exception("Couldn't load the document")
      else d
    }
  }

  def loadDocuments[T](d: Seq[T])(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T], ec: ExecutionContext): Future[Seq[T]] =
    collectionOf[T].bulkInsert(Enumerator.enumerate(d)) map { n =>
      if (n != d.length) throw new Exception("Couldn't load the document")
      else d
    }

  def loadDocument(collection: String, json: JsValue*): Future[Seq[JsValue]] =
    db.collection[JSONCollection](collection).bulkInsert(Enumerator.enumerate(json)) map { n =>
      if (n != json.length) throw new Exception("Couldn't load the document")
      else json
    }

  def clean[T](q: Map[String, String])(implicit ct: ClassTag[T]): Future[Boolean] =
    collectionOf[T].remove(q) map (_ => true)

  def find[T](q: Map[String, String])(implicit ct: ClassTag[T], reader: Reads[T]): Future[List[T]] =
    collectionOf[T].find(Json.toJson[Map[String, String]](q)).cursor[T].collect[List]()

  def findOne[T](q: Map[String, String])(implicit ct: ClassTag[T], reader: Reads[T]): Future[Option[T]] =
    collectionOf[T].find(Json.toJson[Map[String, String]](q)).cursor[T].headOption

  def cleanCollectionFor[T](implicit ct: ClassTag[T]) = collectionOf[T].remove(Json.obj())

  private[helpers] def collectionOf[T](implicit ct: ClassTag[T]) = {
    val collection = ct.runtimeClass.getSimpleName.toLowerCase
    db.collection[JSONCollection](collection)
  }

}

