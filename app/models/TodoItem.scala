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

package models

import com.roundeights.hasher.Implicits._
import java.lang.{ System => Sys }
import scala.util.{ Random => Rand }

case class TodoItem(description: String, completed: Boolean = false)
    extends Persistable {

  private[this] var id: String =
    new Rand(Sys.currentTimeMillis()).nextString(ID_LENGTH).md5.hex

  def this(id: String, description: String, completed: Boolean) = {
    this(description, completed)
    this.id = id
  }

  def update(desc: Option[String], compl: Option[Boolean]): TodoItem = {
    new TodoItem(
      this.id,
      desc.getOrElse(this.description),
      compl.getOrElse(this.completed)
    )
  }

  override def id(): String = this.id

  override def toString: String = {
    s"TodoItem(${this.id},${this.description},${this.completed})"
  }
}
