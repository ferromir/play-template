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

import play.api.test.PlaySpecification

object TodoItemSpec extends PlaySpecification {

  "TodoItem" should {
    "describe a description of task and the completion status" in {
      val todo = TodoItem("Wash dishes", true)

      todo must not be null
      todo.description must be equalTo "Wash dishes"
      todo.completed must beTrue
    }

    "has a convinient way to be initialized with a given identifier" in {
      val todo = new TodoItem("THE_ID", "A todo item", false)

      todo must not be null
      todo.id must be equalTo "THE_ID"
      todo.description must be equalTo "A todo item"
      todo.completed must beFalse
    }

    "provide a way to update the description of the task and/or the completion status" in {
      val todo = TodoItem("Wash dishes", true)

      val updatedTodo = todo.update(Some("Clean room"), Some(true))

      updatedTodo must not be null
      updatedTodo.description must be equalTo "Clean room"
      updatedTodo.completed must beTrue
    }
  }

}
