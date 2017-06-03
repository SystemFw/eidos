object Next {
  trait Tag {
    type Out
  }
  abstract class I[A <: Tag] {
    def value: A#Out
  }

  object H extends Tag { type Out = String }
  type H = H.type

  val a = new I[H] { def value = "hello" }

  val b: String = a.value

  def foo(i: I[H]): String = i.value

  trait Tagg[A] { type Out }

  abstract class II[A] {
    type O
    def v: O
    def value(implicit a: Tagg[A]): a.Out = v.asInstanceOf[a.Out]
  }

  object HH {
    implicit def hh: Tagg[HH] { type Out = String } = new Tagg[HH] {
      type Out = String
    }
  }
  type HH = HH.type

  val aa = new II[HH] { type O = String; def v = "hello" }

  val bb: String = aa.value

  def fooo(i: II[HH]): String = i.value

  object TT {
    trait Tagg[A] {
      type Out
      def build(a: Out): II[A] = new II[A] {
        def value(implicit ev: Tagg[A]): ev.Out = a.asInstanceOf[ev.Out]
      }
    }

    object Tagg {
      type Aux[I, O] = Tagg[I] { type Out = O }
      case class Curry[A]() {
        def apply[B](v: B)(implicit ev: Tagg.Aux[A, B]) = ev.build(v)
      }
      def of[A] = new Curry[A]
    }

    abstract class II[A] {
      def value(implicit a: Tagg[A]): a.Out
    }

    object HH {
      implicit def hh: Tagg.Aux[HH, String] = new Tagg[HH] {
        type Out = String
      }
    }
    type HH = HH.type

    val aa = Tagg.of[HH]("hello")

    val bb: String = aa.value

    def fooo(i: II[HH]): String = i.value

  }
}

object err {
  @annotation.implicitNotFound(
    "${A} is not a valid Eidos Tag. Declare it to be a **case** object to fix this error")
  trait Label[A] {
    def name: String
  }

  trait MkLabel { self =>
    implicit def label(implicit ev: self.type <:< Product): Label[this.type] =
      new Label[this.type] {
        def name = self.productPrefix
      }
  }

  case object Foo extends MkLabel
  type Foo = Foo.type

  val a = implicitly[Label[Foo]].name // Foo

  object Bar extends MkLabel
  type Bar = Bar.type

  // val b = implicitly[Label[Bar]]
  // Bar is not a valid Eidos Tag. Declare it to be a case object to fix this error
}
