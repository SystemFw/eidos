package eidos

object id {

  trait Label[A] {
    def label: String
  }

  object Label {
    def getFor[A: Label] = implicitly[Label[A]].label

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

  sealed abstract case class Id[A](value: String) {
    protected def label: Label[A]

    override def toString =
      s"${label.label}${productPrefix}(${value})"
  }

  object Id {
    def of[A](v: String)(implicit ev: Label[A] = Label.default[A]): Id[A] =
      new Id[A](v) {
        override def label = ev
      }
  }
}
