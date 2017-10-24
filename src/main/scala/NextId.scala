package next
// sealed abstract case class to build newtypes
// https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
sealed abstract case class Id[Tag]() {
  protected def label: String

  override def toString =
    s"${label}${productPrefix}(${v})"

  protected def v: Any

  def value(implicit ev: Build[Tag]): ev.V = v.asInstanceOf[ev.V]
}

object Id {
  type Aux[Tag, V_] = Id[Tag] { type V = V_ }

  private[next] def unsafeCreate[Tag, V_](contents: V_, l: Label[Tag]) =
    new Id[Tag]() {
      type V = V_
      override def label = l.label
      override def v = contents
    }

  class Curried[Tag, V, O](val b: Build.Aux2[Tag, V, O]) {
    def of(value: V)(implicit ev: IsCaseObject[Tag],
                     l: Label[Tag] = Label.default[Tag]): O = b.build(value, l)
  }

  def apply[Tag](implicit ev: Build[Tag]) = new Curried[Tag, ev.V, ev.Out](ev)

  def of[V](value: V)(implicit ev: ErrorCall): Unit = ()
  @annotation.implicitNotFound("Specify the type of the tag for your Id")
  private sealed trait ErrorCall

  @annotation.implicitNotFound(
    "${A} is not a valid Eidos Tag. Declare it to be a case object to fix this error")
  private sealed trait IsCaseObject[A]
  private object IsCaseObject {
    implicit def ev[A <: Singleton with Product]: IsCaseObject[A] = null
  }
}
@annotation.implicitNotFound(
  "${A} is not a valid Eidos Tag. It should extend a Label type")
trait Label[A] {
  def label: String
}

object Label {
  private[next] def default[A] = new Label[A] {
    def label = ""
  }

  private[next] sealed trait LabelDefinitionConflict

  trait MakeLabel { self =>
    // See eidos.id.Format.UUID for an explanation of this
    // format: off
    final def `"In Eidos, you can only extend one of MakeLabel or CustomLabel"`
        : LabelDefinitionConflict = null
    // format: on

    implicit final def l(
        implicit ev: self.type <:< Product): Label[this.type] =
      new Label[this.type] {
        def label = self.productPrefix
      }
  }

  trait CustomLabel {
    // format: off
    final def `"In Eidos, you can only extend one of MakeLabel or CustomLabel"`
        : LabelDefinitionConflict = null
    // format: on
    def label: String

    private def customLabel = label

    implicit final def l: Label[this.type] = new Label[this.type] {
      def label = customLabel
    }
  }
}

@annotation.implicitNotFound(
  "${Tag} is not a valid Eidos Tag. It should extend a Build type")
sealed trait Build[Tag] {
  type Out
  type V

  def build(v: V, l: Label[Tag]): Out
}
object Build {
  type Aux[Tag, V_] = Build[Tag] { type V = V_ }
  type Aux2[Tag, V_, O] = Build[Tag] { type V = V_; type Out = O }

  type Simple[Tag, Contents] = Aux2[Tag, Contents, Id.Aux[Tag, Contents]]
  type Wrapped[Tag, Contents] =
    Aux2[Tag, Contents, Option[Id.Aux[Tag, Contents]]]

  trait Default {
    private type Tag = this.type
    implicit def b: Simple[Tag, String] = new Build[Tag] {
      type Out = Id.Aux[Tag, String]
      type V = String

      override def build(v: V, l: Label[Tag]): Out = Id.unsafeCreate(v, l)
    }
  }
  trait Ints {
    private type Tag = this.type
    implicit def b: Simple[Tag, Int] = new Build[Tag] {
      type Out = Id.Aux[Tag, V]
      type V = Int

      override def build(v: V, l: Label[Tag]): Out = Id.unsafeCreate(v, l)
    }
  }
  trait PosInts {
    private type Tag = this.type
    implicit def b: Wrapped[Tag, Int] = new Build[Tag] {
      type Out = Option[Id.Aux[Tag, V]]
      type V = Int

      override def build(v: V, l: Label[Tag]): Out =
        if (v > 0) Option(Id.unsafeCreate(v, l)) else None
    }
  }
}

object Tags {
  trait Default extends Build.Default
}

object T {
  case object Foo extends Tags.Default
  type Foo = Foo.type

  val a: Id[Foo] = Id[Foo].of(value = "hello")
  val b = a.value

  def foo(id: Id[Foo]): String = {
    id.value.toUpperCase()
  }

  case object Bar extends Build.Ints with Label.MakeLabel
  type Bar = Bar.type

  val c = Id[Bar].of(1)
  val d = c.value

  case object Baz extends Build.PosInts
  type Baz = Baz.type

  val e = Id[Baz].of(value = 3)
  val f = Id[Baz].of(-1)
}
