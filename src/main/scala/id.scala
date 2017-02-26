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

    // `of` requires explicit type application due to SI-7371 to SI-7234
    // merely adding a type signature to the returned value is not enough
    // one should instead always use Id.of[TypeOfTheTag]
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
    // Inferred type: default[A]: Build[A]{type Out = Id[A]}
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

  sealed trait Format {
    def format: String

    val regex = format.r

    implicit final def validator = new Validate[this.type] {
      def validate(s: String) = s match {
        case regex(_ *) => Some(s)
        case _ => None
      }
    }

  }

  private[id] sealed trait ValidationFormatDefinitionConflict

  // format: off
  trait UUID extends Format {
    //   Every subclass of Format will have this special value, to
    //   prevent the user from mixing in two different formats at the
    //   same time. The weird name is a to make the error clear for
    //   the user.  Note that it can't be moved to the common super
    //   class (Format), or trait linearisation will kick in, and the
    //   code will compile when it shouldn't.
    //   Furthermore, this special def needs to be the first thing
    //   defined in the trait to appear as the first error message. This
    //   is clearly a trick, but it's the best we can do (short of
    //   macros), given that Scala does not have custom errors
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = ""
  }

  trait AlphaNum extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = ""
  }

  trait Num extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = ""
  }

  trait Regex extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    def pattern: String

    final override def format = pattern
  } // format: on
}
