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

package ithelpers

import play.api.libs.iteratee.Enumerator
import play.api.libs.json.JsValue
import reactivemongo.api.{DB, MongoDriver}
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

import reactivemongo.api.collections.default._

trait MongoTestHelpers {

  def mongoUri(): String = "localhost:27017/todolist_dev"

  lazy val db:DB = {
    val driver = MongoDriver()

    val s = mongoUri().split("/")
    for(host <- Try(s(0));
        database <- Try(s(1));
        connection = driver.connection(List(host));
        db = connection.db(database)) yield db
  }.get

  def mongoDB(col: String): DBHelpers = new DBHelpers(db, col)

}

class DBHelpers(db: DB, col: String) {

  val collection = db.collection[BSONCollection](col)

  def newId(): String = BSONObjectID.generate.stringify

  def create[T](d: => T)(implicit writer: BSONDocumentWriter[T]): T = {
    val p = () => {
      collection.insert(d).map { lastError =>
        if (lastError.inError) throw new Exception("Couldn't save the document")
        else d
      }
    }

    Await.result(p(), 10 seconds)
  }

  def create[T](d: => Seq[T])(implicit writer: BSONDocumentWriter[T]): Seq[T] = {
    val p = () => {
      collection.bulkInsert(Enumerator.enumerate(d)).map { n =>
        if (n != d.length) throw new Exception("Couldn't save the document")
        else d
      }
    }

    Await.result(p(), 10 seconds)
  }

  def saveJSON(json: JsValue*): Future[Seq[JsValue]] = ???

  def remove[T](q: (String, String)*)(implicit writer: BSONDocumentWriter[T]): Boolean = {
    val p = () => {
      val query = q map { field => BSONDocument(field._1 -> field._2) }

      collection.remove(query.head) map { lastError =>
        if(lastError.inError) throw new Exception("Couldn't delete the document")
        else true
      }
    }

    Await.result(p(), 10 seconds)
  }

  def find[T](q: => Map[String, String])(implicit reader: BSONDocumentReader[T]): List[T] = {
    val p = () => {
      val query = q map { field => BSONDocument(field._1 -> field._2) }
      collection.find(query.head).cursor[T].collect[List]()
    }

    Await.result(p(), 10 seconds)
  }

  def findOne[T](q: => Map[String, String])(implicit reader: BSONDocumentReader[T]): Option[T] = {
    val p = () => {
      val query = BSONDocument()
      collection.find(query).cursor[T].headOption
    }

    Await.result(p(), 10 seconds)
  }

  def cleanCollection(): Boolean = {
    val p = () => {
      val query = BSONDocument()

      collection.remove(query) map { lastError =>
        if(lastError.inError) throw new Exception("Couldn't clean the collection")
        else true
      }
    }

    Await.result(p(), 10 seconds)
  }
}

