package models

import scala.util.Random

case class TodoItem(
  id: String = new Random().nextString(32), // TODO: To complete
  description: String,
  completed: Boolean = false
)
