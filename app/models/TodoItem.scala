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

import scala.util.Random

case class TodoItem(
    override val id: String = new Random(System.currentTimeMillis()).nextString(12),
    description: String,
    completed: Boolean = false
) extends Persistable {

  def update(_description: Option[String], _completed: Option[String]): TodoItem = {
    this.copy(
      description = _description.getOrElse(description),
      completed = _completed.map(compl => compl.toBoolean).getOrElse(completed)
    )
  }

}
