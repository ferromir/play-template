package models

import play.api.test.PlaySpecification

object TodoItemSpec extends PlaySpecification {

  "TodoItem" should {
    "describe a description of task and the completion status" in {
      val todo = TodoItem("abcdefg", "Wash dishes", true)

      todo must not be null
      todo.completed must beTrue
      todo.description must be equalTo "Wash dishes"
    }

    "provide a way to update the description of the task and/or the completion status" in {
      val todo = TodoItem("abcdefg", "Wash dishes", true)

      val updatedTodo = todo.update(Some("Clean room"), Some("true"))

      updatedTodo must not be null
      updatedTodo.description must be equalTo "Clean room"
      updatedTodo.completed must beTrue
    }
  }

}
