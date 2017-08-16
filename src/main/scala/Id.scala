package eidos
package id

// sealed abstract case class to build newtypes
// https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
sealed abstract case class Id[Tag](value: String) {
  protected def label: String

  override def toString =
    s"${label.label}${productPrefix}(${v})"

  protected def v: Any

  def value(implicit ev: Carrier[Tag]): ev.V = v.asInstanceOf[ev.V]

}

object Id {
  private[id] def unsafeCreate[V, Tag](contents: V, label: Label[Tag]) = new Id[Tag](v) {
    override def label = l.label
    override def v = contents
  }

  class Curried[Tag, V, Out](val ev: Carrier.Aux[Tag, V, Out]) {
    def of(value: V)(implicit ev: IsCaseObject[Tag], l: Label[Tag] = Label.default[Tag]): Out = b.build(value, l)
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
}
