package helpers

import models.TodoItem
import reactivemongo.bson.{BSONDocumentWriter, BSONDocument, BSONDocumentReader}

object BSONFormatters {

  implicit object TodoItemReader extends BSONDocumentReader[TodoItem] {
    def read(doc: BSONDocument): TodoItem = {
      val id = doc.getAs[String]("id").get
      val description = doc.getAs[String]("description").get
      val completed = doc.getAs[Boolean]("completed").get

      TodoItem(id, description, completed)
    }
  }

  implicit object TodoItemWriter extends BSONDocumentWriter[TodoItem] {
    def write(item: TodoItem): BSONDocument = {
      BSONDocument(
        "id" -> item.id,
        "description" -> item.completed,
        "completed" -> item.completed
      )
    }
  }

}
