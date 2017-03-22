package eidos
package id

sealed trait Build[A] {
  type Out
  def build(s: String, l: Label[A]): Out
}

object Build {
  private[id] type Aux[I, O] = Build[I] { type Out = O }

  private[id] def default[A]: Aux[A, Id[A]] = new Build[A] {
    type Out = Id[A]
    override def build(v: String, l: Label[A]): Out = Id.unsafeCreate(v, l)
  }

  trait Validated[A] extends Build[A] {
    final override type Out = Option[Id[A]]
    final override def build(s: String, l: Label[A]): Out =
      validate(s).map(valid => Id.unsafeCreate(valid, l))

    def validate(s: String): Option[String]
  }
}
