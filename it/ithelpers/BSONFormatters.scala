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

import models.TodoItem
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

object BSONFormatters {

  implicit object TodoItemReader extends BSONDocumentReader[TodoItem] {
    def read(doc: BSONDocument): TodoItem = {
      val id = doc.getAs[String]("id").get
      val description = doc.getAs[String]("description").get
      val completed = doc.getAs[Boolean]("completed").get

      new TodoItem(id, description, completed)
    }
  }

  implicit object TodoItemWriter extends BSONDocumentWriter[TodoItem] {
    def write(item: TodoItem): BSONDocument = {
      BSONDocument(
        "id" -> item.id,
        "description" -> item.description,
        "completed" -> item.completed
      )
    }
  }

}
