package models

import scala.util.Random

trait Persistable {

  def id: String = new Random(System.nanoTime).nextString(32)

}
