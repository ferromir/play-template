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

import models.Persistable
import models.datastore.DataStore
import play.api.libs.json.{ Reads, Writes }

import scala.concurrent.{ ExecutionContext, Future }
import scala.reflect.ClassTag

trait FakeDataStore extends DataStore {

  override def find[T](id: String)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Option[T]] =
    Future.successful(Some(fakeIntanceOfT[T](ct)))

  override def find[T](fields: (String, String)*)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Seq[T]] =
    Future.successful(Seq(fakeIntanceOfT[T](ct)))

  override def findAll[T](implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Seq[T]] =
    Future.successful(Seq(fakeIntanceOfT[T](ct)))

  override def persist[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T], ec: ExecutionContext): Future[Boolean] =
    Future.successful(true)

  override def modify[T <: Persistable](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], writer: Writes[T], ec: ExecutionContext): Future[Boolean] =
    Future.successful(true)

  override def remove[T](obj: T)(implicit ct: ClassTag[T], reader: Reads[T], ec: ExecutionContext): Future[Boolean] = Future.successful(true)

  private def fakeIntanceOfT[T](ct: ClassTag[T]): T = {
    val targetClazz = ct.runtimeClass
    val ctors = targetClazz.getConstructors()
    val parameterTypes = ctors(0).getParameterTypes

    val parameterValues = parameterTypes.map { pClass =>
      if (pClass.getClass == classOf[String]) ""
      else if (pClass.getClass == classOf[Boolean]) true
    }

    val instance = ctors(0).newInstance(parameterValues).asInstanceOf[T]
    instance
  }

}
