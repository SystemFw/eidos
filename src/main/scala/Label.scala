package eidos
package id

trait Label[A] {
  def label: String
}

object Label {
  private[id] def default[A] = new Label[A] {
    def label = ""
  }

  private[id] sealed trait LabelDefinitionConflict

  trait MakeLabel { self =>
    // See eidos.id.Format.UUID for an explanation of this
    // format: off
    final def `"In Eidos, you can only extend one of MakeLabel or CustomLabel"`
        : LabelDefinitionConflict = null

    implicit final def l(implicit ev: self.type <:< Product): Label[this.type] =
      new Label[this.type] {
        def label = self.productPrefix
      }
  }

  trait CustomLabel {
    final def `"In Eidos, you can only extend one of MakeLabel or CustomLabel"`
        : LabelDefinitionConflict = null
    // format: on
    def label: String

    private def customLabel = label

    implicit final def l: Label[this.type] = new Label[this.type] {
      def label = customLabel
    }
  }
}
