package eidos

import org.specs2.mutable.Specification
import org.specs2.execute.Typecheck._
import org.specs2.matcher.TypecheckMatchers

class IdSpec extends Specification with TypecheckMatchers {

  "eidos IDs" should {
    "be parameterised by a tag" in {
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

    "have an optional label attached to it" in {
      case object Device extends MakeLabel
      type Device = Device.type

      object B
      type B = B.type

      object C {
        implicit def ev = new Label[C] {
          def label = "Custom"
        }
      }
      type C = C.type

      { Id.of[Device]("gtx9018").toString must beEqualTo("DeviceId(gtx9018)") }
      { Id.of[B]("simple").toString must beEqualTo("Id(simple)") }
      { Id.of[C]("custom").toString must beEqualTo("CustomId(custom)") }
    }
  }
}
