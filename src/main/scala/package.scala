package object eidos {
  type Id[A] = id.Id[A]
  type MakeLabel = id.Label.MakeLabel
  type NonBlank = id.Format.NonBlank
  type AlphaNum = id.Format.AlphaNum
  type Num = id.Format.Num
  type UUID = id.Format.UUID
  type Regex = id.Format.Regex

  val Id = id.Id
}
