package object eidos {
  type Id[A] = id.Id[A]
  type Label[A] = id.Label[A]
  type MakeLabel = id.MakeLabel
  type Validate[A] = id.Validate[A]

  val Id = id.Id
}
