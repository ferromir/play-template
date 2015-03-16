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

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import reactivemongo.api.{DB, MongoDriver}
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

import reactivemongo.api.collections.default._

import helpers.BSONFormatters._

trait MongoTestHelpers {

  def mongoUri(): String = "localhost:27017/todolist_test"

  lazy val db:DB = {
    val driver = MongoDriver()

    val s = mongoUri().split("/")
    for(host <- Try(s(0));
        database <- Try(s(1));
        connection = driver.connection(List(host));
        db = connection.db(database)) yield db
  }.get

  def newId(): String = BSONObjectID.generate.stringify

  def saveDocument[T](d: T, col: String)(implicit writer: BSONDocumentWriter[T]): Future[T] = {
    db.collection[BSONCollection](col).insert(d).map { lastError =>
      if (lastError.inError) throw new Exception("Couldn't save the document")
      else d
    }
  }

  def saveDocuments[T](d: Seq[T], col: String)(implicit writer: BSONDocumentWriter[T]): Future[Seq[T]] = {
    db.collection[BSONCollection](col).bulkInsert(Enumerator.enumerate(d)).map { n =>
      if (n != d.length) throw new Exception("Couldn't save the document")
      else d
    }
  }

  def saveJSON(col: String, json: JsValue*): Future[Seq[JsValue]] = ???

  def removeDocument[T](q: Map[String, String], col: String)(implicit writer: BSONDocumentWriter[T]): Future[Boolean] = {
    val query = q map { field => BSONDocument(field._1 -> field._2) }

    db.collection[BSONCollection](col).remove(query.head) map { lastError =>
      if(lastError.inError) throw new Exception("Couldn't delete the document")
      else true
    }
  }

  def find[T](q: Map[String, String], col: String)(implicit reader: BSONDocumentReader[T]): Future[List[T]] = {
    val query = q map { field => BSONDocument(field._1 -> field._2) }
    db.collection[BSONCollection](col).find(query.head).cursor[T].collect[List]()
  }

  def findOne[T](q: Map[String, String], col: String)(implicit reader: BSONDocumentReader[T]): Future[Option[T]] = {
    val query = BSONDocument()
    db.collection[BSONCollection](col).find(query).cursor[T].headOption
  }

  def cleanCollectionFor[T](col: String) = {
    val query = BSONDocument()

    db.collection[BSONCollection](col).remove(query) map { lastError =>
      if(lastError.inError) throw new Exception("Couldn't clean the collection")
      else true
    }
  }

}

