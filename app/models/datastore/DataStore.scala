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

import com.roundeights.hasher.Implicits._

import java.lang.{ System => Sys }

import play.api.libs.json.{ Writes, Reads }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag
import scala.util.{ Random => Rand }

trait DataStore {

  /*
   * Find a model of the provided type by its ID
   */
  def find[T](id: String)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Option[T]]

  /*
   * Find a collection of models of the provided type given parameter query
   */
  def find[T](fields: (String, String)*)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Seq[T]]

  /*
   * Find all models of the provided type
   */
  def findAll[T](implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Seq[T]]

  /*
   * Persist a model of the specified type
   */
  def persist[T](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    writer: Writes[T],
    ec: ExecutionContext): Future[Boolean]

  /*
   * Modify/Update a model record of the specified type
   */
  def modify[T <: Persistable](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    writer: Writes[T],
    ec: ExecutionContext): Future[Boolean]

  /*
   * Remove a model of the specified type by its ID
   */
  def remove[T <: Persistable](obj: T)(implicit
    ct: ClassTag[T],
    reader: Reads[T],
    ec: ExecutionContext): Future[Boolean]
}
