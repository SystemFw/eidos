package eidos

object id {

  sealed abstract case class Id[A](value: String) {
    protected def label: Label[A]

    override def toString =
      s"${label.label}${productPrefix}(${value})"
  }

  object Id {
    private[id] def unsafeCreate[A](v: String, l: Label[A]) = new Id[A](v) {
      override def label = l
    }

    def of[A](v: String)(implicit l: Label[A] = Label.default[A],
                         b: Build[A] = Build.default[A]): b.Out =
      b.build(v, l)
  }

  trait Label[A] {
    def label: String
  }

  object Label {
    private[id] def default[A] = new Label[A] {
      def label = ""
    }
  }

  // SI-9689 affects repl usage, fixed in scala 2.12
  trait MakeLabel extends Product {
    implicit def l = new Label[this.type] {
      def label = productPrefix
    }
  }

  sealed trait Build[A] {
    type Out
    def build(s: String, l: Label[A]): Out
  }

  object Build {
    private[id] def default[A] = new Build[A] {
      type Out = Id[A]
      override def build(v: String, l: Label[A]): Out = Id.unsafeCreate(v, l)

    }
  }

  trait Validate[A] extends Build[A] {
    final override type Out = Option[Id[A]]
    final override def build(s: String, l: Label[A]): Out =
      validate(s).map(valid => Id.unsafeCreate(valid, l))

    def validate(s: String): Option[String]
  }
}
