package eidos
package id

trait Label[A] {
  def label: String
}

object Label {
  private[id] def default[A] = new Label[A] {
    def label = ""
  }

  // SI-9689 affects repl usage, fixed in scala 2.12
  trait MakeLabel extends Product {
    implicit def l = new Label[this.type] {
      def label = productPrefix
    }
  }
}
