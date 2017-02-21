package eidos

object id {

  trait Label[A] {
    def label: String
  }

  object Label {
    def getFor[A: Label] = implicitly[Label[A]].label
  }

  trait MakeLabel extends Product {
    implicit def l = new Label[this.type] {
      def label = productPrefix
    }
  }

  case class Id[A: Label](value: String) {
    override def toString =
      s"${Label.getFor[A]}${productPrefix}(${value})"
  }

  object Id {}
}
