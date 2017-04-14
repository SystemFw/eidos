package eidos
package id

sealed trait Format {
  def format: String

  val regex = format.r

  implicit final def validator: Build.Validated[this.type] =
    new Build.Validated[this.type] {
      def validate(s: String) = s match {
        case regex(_ *) => Some(s)
        case _ => None
      }
    }

}

object Format {
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

    final override def format =
      "[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}"
  }

  trait NonBlank extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = """[^\s]+"""
  }

  trait AlphaNum extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = """\p{Alnum}+"""
  }

  trait Num extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    final override def format = """\d+"""
  }

  trait Regex extends Format {
    final def `"In Eidos, You can only extend one validation format at the time!"`
        : ValidationFormatDefinitionConflict = null

    def pattern: String

    final override def format = pattern
  } // format: on
}
