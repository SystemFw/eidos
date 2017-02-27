# Eidos
A tiny and principled library for modelling IDs. We can do better than `String`.
```Scala
import eidos._

case object QnD
type QnD = QnD.type

case object Customer extends MakeLabel with UUID
type Customer = Customer.type

case object Device extends CustomLabel with Regex {
  def pattern = "(abc)+12"
  def label = "Phone"
}
type Device = Device.type

// and then

scala > Id.of[QnD]("no validation required!")
res1: Id[QnD] = Id(no validation required!)

scala> Id.of[Customer]("e07fa50f-7ddf-4e7c-acf5-420406e5a7c5")
res2: Option[Id[Customer]] = Some(CustomerId(e07fa50f-7ddf-4e7c-acf5-420406e5a7c5))

scala> Id.of[Customer]("not-a-uuid")
res3: Option[Id[Customer]] = None

scala> Id.of[Device]("abcabc12")
res4: Option[Id[Device]] = Some(PhoneId(abcabc12))
```
