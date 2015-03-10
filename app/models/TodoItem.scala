package models

case class TodoItem(description: String, completed: Boolean = false) extends Persistable {

  def update(description: String, completed: Boolean): TodoItem = {
    this.copy(description = description, completed = completed)
  }

  def unapply(t: TodoItem): Option[(String, String, Boolean)] =
    Some(t.id, t.description, t.completed)

}
