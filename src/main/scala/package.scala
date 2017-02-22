package object eidos {
  type OptionValidator[A] = id.OptionValidator[A]
  type Id[A] = id.Id[A]
  type Label[A] = id.Label[A]
  type MakeLabel = id.MakeLabel
  type Validate[A] = id.Validate[A]

  val Id = id.Id
}
