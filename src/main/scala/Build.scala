package eidos
package id

sealed trait Build[A] {
  type Out
  def build(s: String, l: Label[A]): Out
}

object Build {
  // Inferred type: default[A]: Build[A]{type Out = Id[A]}
  private[id] def default[A] = new Build[A] {
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
