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

case class TodoItem(override val id: String, description: String, completed: Boolean = false) extends Persistable {

  def update(description: String, completed: Boolean): TodoItem = {
    this.copy(description = description, completed = completed)
  }

  def unapply(t: TodoItem): Option[(String, String, Boolean)] =
    Some(t.id, t.description, t.completed)

}
