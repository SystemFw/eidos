package eidos

import org.specs2.mutable.Specification
import org.specs2.execute.Typecheck._
import org.specs2.matcher.TypecheckMatchers

class EidosSpec extends Specification with TypecheckMatchers {
  "In Eidos:".br.tab(1)

  import id._

  "IDs" should {
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

    "be created thorugh an explicitly typed call to `of`" in {
      // for documentation purposed only
      // limitation due to  SI-7371 and SI-7234
      object A
      type A = A.type

      object B {
        implicit def v: Validate[B] = null
      }
      type B = B.type

      { typecheck("""val a: Id[A] = Id.of[A]("")""") must succeed }
      { typecheck("""val b: Option[Id[B]] = Id.of[B]("")""") must succeed }
      { typecheck("""val a: Id[A] = Id.of("")""") must not succeed }
      { typecheck("""val b: Option[Id[B]] = Id.of("")""") must not succeed }
    }

    "have an optional label attached to it" in {
      object B
      type B = B.type

      object C {
        implicit def ev = new Label[C] {
          def label = "Custom"
        }
      }
      type C = C.type

      { Id.of[B]("simple").toString must beEqualTo("Id(simple)") }
      { Id.of[C]("custom").toString must beEqualTo("CustomId(custom)") }
    }

    "be validated against an optional schema on creation" in {
      object NoValidation
      type NoValidation = NoValidation.type

      case object ValidationRequired {
        implicit def validator = new Validate[ValidationRequired] {
          def validate(v: String) =
            if (v == "nonvalid") None else Some(v)
        }
      }
      type ValidationRequired = ValidationRequired.type

      // format: off
      { typecheck("""val a: Id[NoValidation] = Id.of[NoValidation]("whatever")""") must succeed }
      { Id.of[ValidationRequired]("valid").map(_.toString) should beSome("Id(valid)") }
      { Id.of[ValidationRequired]("nonvalid") should beNone }
      // format: on
    }
  }

  "Labels" should {
    "be derivable from the tag name" in {
      case object Device extends MakeLabel
      type Device = Device.type

      { Id.of[Device]("gtx9018").toString must beEqualTo("DeviceId(gtx9018)") }
    }
  }
}
