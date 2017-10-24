package object eidos {
  type Id[A] = id.Id[A]
  // Labels
  type MakeLabel = id.Label.MakeLabel
  type CustomLabel = id.Label.CustomLabel
  // Formats
  type NonBlank = id.strings.NonBlank
  type AlphaNum = id.strings.AlphaNum
  type Num = id.strings.Num
  type UUID = id.strings.UUID
  type Regex = id.strings.Regex

  val Id = id.Id

  trait Default {
    implicit def b: Id.Carrier.Simple[this.type, String] =
      new Id.Carrier.Simple[this.type, String] {}
  }
}
