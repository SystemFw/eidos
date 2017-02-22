package eidos

object id {

  sealed abstract case class Id[A](value: String) {
    protected def label: Label[A]

    override def toString =
      s"${label.label}${productPrefix}(${value})"
  }

  object Id {
    // def of[A](v: String)(
    //     implicit ev: Label[A] = Label.default[A],
    //     validator: Validate[A] = Validate.default[A]): Option[Id[A]] =
    //   validator.validate(v) map { s =>
    //     new Id[A](v) {
    //       override def label = ev
    //     }
    //   }
     def unsafeCreate[A](v: String)(implicit ev: Label[A] = Label.default[A]): Id[A] =
      new Id[A](v) {
        override def label = ev
      }

    def of[A](v: String)(implicit l: Label[A] = Label.default[A], validator: Validate[A] = Validate.default[A]): validator.Out = validator.validate(v)
  }

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

  trait Validate[A] {
    type Out
    def validate(s: String)(implicit ev: Label[A]): Out
  }

  object Validate {
    private[id] def default[A] = new Validate[A] {
      type Out = Id[A]
      override def validate(v: String)(implicit ev: Label[A] = Label.default[A]): Out = Id.unsafeCreate(v)

    }
  }

}
