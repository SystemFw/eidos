package eidos
package id

// sealed abstract case class to build newtypes
// https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
sealed abstract case class Id[Tag]() {
  protected def label: String

  override def toString =
    s"$label$productPrefix(${v})"

  protected def v: Any

  def value(implicit ev: Id.Carrier[Tag]): ev.V = v.asInstanceOf[ev.V]

}

object Id {
  private[id] def unsafeCreate[V, Tag](contents: V, l: Label[Tag]) = new Id[Tag]() {
    override def label = l.label
    override def v = contents
  }

  class Curried[Tag, V, Out](val carrier: Carrier.Aux[Tag, V, Out]) {
    def of(value: V)(implicit ev: IsCaseObject[Tag], l: Label[Tag] = Label.default[Tag]): Out = carrier(value, l)
  }

  def apply[Tag](implicit ev: Carrier[Tag]) = new Curried[Tag, ev.V, ev.Out](ev)


  @annotation.implicitNotFound(
    """Please specify an explicit type for the tag of your Id, e.g. Id[Foo].of("foo")""")
  private sealed trait ErrorCall
  def of[V](value:V)(implicit ev: ErrorCall): Unit = ()
  
  @annotation.implicitNotFound(
    "${A} is not a valid Eidos Tag. Declare it to be a case object to fix this error")
  private sealed trait IsCaseObject[A]
  private object IsCaseObject {
    implicit def ev[A <: Singleton with Product]: IsCaseObject[A] = null
  }


  // TODO better error message
@annotation.implicitNotFound(
    "${Tag} is not a valid Eidos Tag. It should extend a format trait")
sealed trait Carrier[Tag] {
  type V
  type Out

  def apply(v: V, l: Label[Tag]): Out
}

object Carrier {
  type Aux[Tag, V_, O] = Carrier[Tag] { type V = V_ ; type Out = O }

  trait Simple[Tag, Contents] extends Carrier[Tag] {
    final override type V = Contents
    final override type Out = Id[Tag]

    final override def apply(v: V, l: Label[Tag]) = Id.unsafeCreate(v, l)
  }

  trait Validated[Tag, Contents] extends Carrier[Tag] {
    final override type V = Contents
    final override type Out = Option[Id[Tag]]

    final override def apply(v: V, l: Label[Tag]): Out =
      validate(v).map(valid => Id.unsafeCreate(valid, l))

    def validate(v: V): Option[V]
  }
}
}
