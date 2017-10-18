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

  // TODO
  // error messages when
  // no case objects
  // not extending a Build
  // not specifying a type
  // multiple types of IDs
  // type is preserved, also in functions
  // testing Num formats

  "IDs" should {
    "be parameterised by a tag" in {
      case object A
      type A = A.type

      case object B
      type B = B.type

      def a: Id[A] = ???
      def b: Id[B] = ???

      def foo(a: Id[A]) = ???

      { typecheck("foo(b)") must not succeed }
      { typecheck("foo(a)") must succeed }
    }

    "be created through an explicitly typed call to `of`" in {
      // for documentation purposes only
      case object A
      type A = A.type

      case object B {
        implicit def v: Id.Carrier.Validated[B, String] = null
      }
      type B = B.type

      // TODO bottom two tests, we can now test for a specific error msg

      { typecheck("""val a: Id[A] = Id[A].of("")""") must succeed }
      { typecheck("""val b: Option[Id[B]] = Id[B].of("")""") must succeed }
      { typecheck("""val a: Id[A] = Id.of("")""") must not succeed }
      { typecheck("""val b: Option[Id[B]] = Id.of("")""") must not succeed }
    }

    "have an optional label attached to it" in {
      case object B
      type B = B.type

      case object C {
        implicit def ev: Label[C] = new Label[C] {
          def label = "Custom"
        }
      }
      type C = C.type

      { Id[B].of("simple").toString must beEqualTo("Id(simple)") }
      { Id[C].of("custom").toString must beEqualTo("CustomId(custom)") }
    }

    "be validated against an optional schema on creation" in {
      case object NoValidation
      type NoValidation = NoValidation.type

      case object ValidationRequired {
        implicit def validator: Id.Carrier.Validated[ValidationRequired, String] =
          new Id.Carrier.Validated[ValidationRequired, String] {
            def validate(v: String) =
              if (v == "nonvalid") None else Some(v)
          }
      }
      type ValidationRequired = ValidationRequired.type

      // format: off
      { typecheck("""val a: Id[NoValidation] = Id[NoValidation].of("whatever")""") must succeed }
      { Id[ValidationRequired].of("valid").map(_.value) should beSome("valid") }
      { Id[ValidationRequired].of("nonvalid") should beNone }
      // format: on
    }
  }

  "Tag" should {
    "be case objects" in {
      trait Trait

      // MakeLabel is the reason why "case" is required
      object Object extends MakeLabel
      type Object = Object.type

      case object CaseObject
      type CaseObject = CaseObject.type

      def errorMessage(name: String) =
        s"$name is not a valid Eidos Tag. Declare it to be a case object to fix this error"

      // format: off
      { typecheck("""Id[Trait].of("")""") must failWith(errorMessage("Trait")) }
      { typecheck("""Id[Object].of("")""") must failWith(errorMessage("Object")) }
      { typecheck("""Id[CaseObject].of("")""") must succeed }
      // format: on
    }
  }

  "Labels" should {
    "be mutually exclusive" in {
      // Specs2 cannot currently test this scenario (the compile time
      // error that Eidos produces is not a type error). To test,
      // uncomment the following line and ensure it fails at compile time
      // case object A extends CustomLabel with MakeLabel
      typecheck {
        """
        case object A extends CustomLabel with MakeLabel
        """
      } must not succeed
    }.pendingUntilFixed(": specs2 can't test this scenario")

    "be derivable from the tag name" in {
      case object Device extends MakeLabel
      type Device = Device.type

      { Id[Device].of("gtx9018").toString must beEqualTo("DeviceId(gtx9018)") }
    }

    "be customisable" in {
      case object A extends CustomLabel {
        def label = "Device"
      }
      type A = A.type

      { Id[A].of("gtx9018").toString must beEqualTo("DeviceId(gtx9018)") }
    }
  }

  "Validation formats" should {
    "be mutually exclusive" in {
      // Specs2 cannot currently test this scenario (the compile time
      // error that Eidos produces is not a type error). To test,
      // uncomment the following line and ensure it fails at compile time
      // case object A extends UUID with Num
      typecheck {
        """
        case object A extends UUID with Num
        """
      } must not succeed
    }.pendingUntilFixed(": specs2 can't test this scenario")

    "allow validating a nonempty string" in {
      case object A extends NonBlank
      type A = A.type

      Id[A].of("") must beNone
      Id[A].of("    ") must beNone
      Id[A].of("fff") must beSome
    }

    "allow validating a numeric string" in {
      case object A extends Num
      type A = A.type

      Id[A].of("134f") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(numStr.nonEmpty) { s =>
        Id[A].of(s).map(_.value) must beSome(s)
      }
    }

    "allow validating an alphanumeric string" in {
      case object A extends AlphaNum
      type A = A.type

      Id[A].of("!") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(alphaNumStr.nonEmpty) { s =>
        Id[A].of(s).map(_.value) must beSome(s)
      }
    }

    "allow validating a UUID" in {
      case object A extends UUID
      type A = A.type

      Id[A].of("!") must beNone

      // Scalacheck props MUST be the last assertion
      // Multiple props MUST be linked together by && or ||
      forAll(uuid) { u =>
        val s = u.toString
        Id[A].of(s).map(_.value) must beSome(s)
      }
    }

    "allow validating with a custom Regex" in {
      case object A extends Regex {
        def pattern = "(abc)+12"
      }
      type A = A.type

      Id[A].of("abcabc12").map(_.value) must beSome("abcabc12")
      Id[A].of("abcd") must beNone
    }
  }

  implicit class NonEmptyStringGen(g: org.scalacheck.Gen[String]) {
    def nonEmpty = g suchThat (!_.isEmpty)
  }
}
