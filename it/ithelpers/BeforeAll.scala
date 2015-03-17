package ithelpers

import org.specs2.specification.BaseSpecification
import org.specs2.specification.{ Fragments, Step }

// http://stackoverflow.com/questions/16936811/execute-code-before-and-after-specification
trait BeforeAll extends BaseSpecification {

  override def map(fragments: => Fragments) =
    Step(beforeAll) ^ fragments

  protected def beforeAll()
}
