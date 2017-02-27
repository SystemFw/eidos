package eidos
package id

import org.specs2.mutable.Specification
import org.specs2.execute.Typecheck._
import org.specs2.matcher.TypecheckMatchers
import org.specs2.ScalaCheck
import org.scalacheck.Prop.forAll
import org.scalacheck.Gen.{uuid, alphaNumStr, numStr}

class EidosSpec extends Specification with TypecheckMatchers with ScalaCheck {
  "In Eidos:".br.tab(1)

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
        implicit def v: Build.Validated[B] = null
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
        implicit def validator = new Build.Validated[ValidationRequired] {
          def validate(v: String) =
            if (v == "nonvalid") None else Some(v)
        }
      }
      type ValidationRequired = ValidationRequired.type

      // format: off
      { typecheck("""val a: Id[NoValidation] = Id.of[NoValidation]("whatever")""") must succeed }
      { Id.of[ValidationRequired]("valid").map(_.value) should beSome("valid") }
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

  "Validation formats" should {
    "be mutually exclusive" in {
      // Specs2 cannot currently test this scenario (the compile time
      // error that Eidos produces is not a type error). To test,
      // uncomment the following line and ensure it fails at compile time
      typecheck {
        """
        object A extends UUID with Num
        """
      } must not succeed
    }.pendingUntilFixed(": specs2 can't test this scenario")

    "allow validating a nonempty string" in {
      case object A extends NonBlank
      type A = A.type

      Id.of[A]("") must beNone
      Id.of[A]("    ") must beNone
      Id.of[A]("fff") must beSome
    }

    "allow validating a numeric string" in {
      case object A extends Num
      type A = A.type

      Id.of[A]("134f") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(numStr.nonEmpty) { s =>
        Id.of[A](s).map(_.value) must beSome(s)
      }
    }

    "allow validating an alphanumeric string" in {
      case object A extends AlphaNum
      type A = A.type

      Id.of[A]("!") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(alphaNumStr.nonEmpty) { s =>
        Id.of[A](s).map(_.value) must beSome(s)
      }
    }

    "allow validating a UUID" in {
      case object A extends UUID
      type A = A.type

      Id.of[A]("!") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(uuid) { u =>
        val s = u.toString
        Id.of[A](s).map(_.value) must beSome(s)
      }
    }

    "allow validating with a custom Regex" in {
      case object A extends Regex {
        def pattern = "abc"
      }
      type A = A.type

      Id.of[A]("abc").map(_.value) must beSome("abc")
      Id.of[A]("abcd") must beNone
    }
  }

  implicit class NonEmptyStringGen(g: org.scalacheck.Gen[String]) {
    def nonEmpty = g suchThat (!_.isEmpty)
  }
}
