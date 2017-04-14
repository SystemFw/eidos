package eidos
package id

// sealed abstract case class to build newtypes
// https://gist.github.com/tpolecat/a5cb0dc9adeacc93f846835ed21c92d2
sealed abstract case class Id[A](value: String) {
  protected def label: Label[A]

  override def toString =
    s"${label.label}${productPrefix}(${value})"
}

object Id {
  private[id] def unsafeCreate[A](v: String, l: Label[A]) = new Id[A](v) {
    override def label = l
  }

  // Due to the use of dependent types, `of` requires explicit type application,
  // merely adding a type signature to the returned value is not enough:
  // one should instead always use Id.of[TypeOfTheTag]
  def of[A](v: String)(implicit l: Label[A] = Label.default[A],
                       b: Build[A] = Build.default[A],
                       ev: IsCaseObject[A]): b.Out =
    b.build(v, l)

  @annotation.implicitNotFound(
    "${A} is not a valid Eidos Tag. Declare it to be a case object to fix this error")
  private sealed trait IsCaseObject[A]
  private object IsCaseObject {
    implicit def ev[A <: Singleton with Product]: IsCaseObject[A] = null
  }
}
