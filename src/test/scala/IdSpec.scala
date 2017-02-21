package eidos

import org.specs2.mutable.Specification
import org.specs2.execute._
import org.specs2.execute.Typecheck._
import org.specs2.matcher.TypecheckMatchers

class IdSpec extends Specification with TypecheckMatchers {

  "eidos IDs" should {
    "be parameterised by a tag" in {
      import id._

      object A
      type A = A.type

      object B
      type B = B.type

      def a: Id[A] = ???
      def b: Id[B] = ???

      def foo(a: Id[A]) = ???

      { typecheck("foo(b)") must not succeed }
      { typecheck("foo(a)") must succeed }
    }
  }
}
