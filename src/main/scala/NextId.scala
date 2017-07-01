package eidos.id
package next
// sealed abstract case class to build newtypes
// https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
sealed abstract case class Id[Tag]() {
  protected def label: Label[Tag]

  override def toString =
    s"${label.label}${productPrefix}(${v})"

  type V

  def v: V

  def value(implicit ev: Build[Tag]): ev.Out = v.asInstanceOf[ev.Out]
}

object Id {
  type Aux[Tag, V_] = Id[Tag] { type V = V_ }

  private [next] def unsafeCreate[Tag, V_](contents: V_, l: Label[Tag]) = new Id[Tag]() {
    type V = V_
    override def label = l
    override def v = contents
  }

  class Curried[Tag]() {
    def apply[V](v: V)(implicit b: Build.Aux[Tag, V] = Build.default[Tag], l: Label[Tag] = Label.default[Tag], ev: IsCaseObject[Tag]) = b.build(v, l)
  }

  def of[Tag] = new Curried[Tag]

  @annotation.implicitNotFound(
    "${A} is not a valid Eidos Tag. Declare it to be a case object to fix this error")
  private sealed trait IsCaseObject[A]
  private object IsCaseObject {
    implicit def ev[A <: Singleton with Product]: IsCaseObject[A] = null
  }
}


sealed trait Build[Tag] {
  type Out
  type V

  def build(v: V, l: Label[Tag]): Out
}
object Build {
  type Aux[Tag, V_] = Build[Tag] { type V = V_ }
  type Aux2[Tag, V_, O] = Build[Tag] { type V = V_ ; type Out = O}

  private[next] def default[Tag]: Build.Aux2[Tag, String, Id.Aux[Tag, String]] = new Build[Tag] {
    type Out = Id.Aux[Tag, String]
    type V = String

    override def build(v: V, l: Label[Tag]): Out = Id.unsafeCreate(v,l)
  }

}

object T {
  case object Foo
  type Foo = Foo.type

  val a: Id[Foo] = Id.of[Foo]("hello")
  //val b = a.value
  // problem: value can't find the implicit build...would need a
  // default argument there, which breaks parametricity
  // def foo[A](id: Id[A]) = id.value will return String 
}
