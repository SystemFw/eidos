# User Guide

### Table of contents
<!-- manually generated :( -->
- [Installation](#installation)
- [Motivation](#motivation)
- [Tagged IDs](#tagged-ids)
- [Pretty printing](#pretty-printing)
- [Validation](#validation)
- [Limitations](#limitations)


## Installation
Add the following to your build.sbt. Builds are available for scala
2.11.x and 2.12.x

``` scala
libraryDependencies += "org.systemfw" %% "eidos" % "0.1.1"
```

## Motivation

If you are already onboard with the idea of avoiding *stringly typed*
programming, feel free to skip to the [next section](#tagged-ids).

The following (simplified) data model is derived from a real-world
application whose purpose is to deliver tailored adverts based on the
geographical location of the user:
```scala
case class Event(
 eventType: EventType,
 locationType: LocationType,
 locationId: String,
 customerId: String,
 deviceId: String
)

case class PushNotification(
 campaignId: String,
 customerId: String,
 subscriptionId: String,
 content: Message
)
```
Large enterprise software typically deals with many subsystems, which
often have their own entities. This leads to a somewhat inevitable
proliferation of IDs, and although a `String` representation seems
natural, it has several drawbacks:

- Try removing the names from the data model, and see how much sense
  you can make of it. You will see the `String` fields carry close to
  zero information, whereas the rest is still clear:

  ```scala
  case class Event(
        : EventType,
        : LocationType,
        : String,
        : String,
        : String
  )

  case class PushNotification(
        : String
        : String
        : String
        : Message
  )
  ```

- Given that all IDs have the same type, it's easy to make mistakes:

  ```scala
  def foo(sessionId: String) = ???

  // Somewhere else in a far away land
  val interactionId = "1340afe324e3134"
  foo(interactionId) // compiles
  ```
  These are particularly insidious because most IDs are opaque to your
  system and you only realise when another system reject them in an
  end-to-end test.  Although named parameters may help a bit, they
  still don't solve the problem and it's quite annoying to have to use
  them defensively just in case you make a mistake.  In fact, the
  amount of thought that should ve given to this problem is zero;
  ideally you should be able to just write the code naturally, and
  only get notified (as early as possible) when you make a mistake.

- A `String` representation prevents you from being finely grained
  without losing clarity:

  ```scala
  case class PushNotification(
   campaignId     : String,
   customerId     : String,
   subscriptionId : String,
   content        : Message
  )

  def processCampaignIds : String => Whatever = ???
  def processCampaignIds: PushNotification => Whatever = ???
  ```
  Notice that trying to be more precise and specify that
  `processCampaignIds` only requires `campaignId`s results in a _less
  precise type_ (`String` is less precise than `PushNotification`).

- String allows you to do more operations than an ID admits:

  ```scala
  // Have fun debugging this in your end to end tests
  val customerId: String = "a23ge-13413-1345g".replaceAll("3","E")
  ```

  and accepts anything:

  ```scala
  val customerId: String =
    "no, I'm not a well-formed id, thanks for asking"
  ```

- Logs are unclear:
  ```scala
  PushNotification(
   d43c828e-c82f-4a36-b62b-80d94a2d458d,
   77633cfb-75e3-4496-817d-924183b1709f,
   dd976ed8-2bff-4a7d-a81d-8013d7b8-7d4a-4aab,
   Message("Amazing offer")
  )
  ```
  There's no way of knowing what those numbers are unless you are
  familiar with the data model.  The majority of the people reading
  your logs (e.g. Ops) won't be.

- Statements like _Interaction IDs should end with 4 digits_, or _Device
  IDs should be UUIDs_ are hard to express, reducing validation to a web
  of `if` statements.


The most common solution to this kind of problem is to create custom
value classes encapsulating the different IDs. Eidos builds upon this
idea to let you build tagged IDs with _à la carte_ pretty-printing and
validation, and an emphasis on type safety and correct-by-construction
code.  We can do better than `String`.

## Tagged IDs

> Note: all examples from now on assume the following import at the
> top of the file `import eidos._`

The main entity exposed by Eidos is the `Id` class:
``` scala
class Id[A](value: String)
```
which is a wrapper over `String`, with an additional type parameter
`A`. Notice that there are no corresponding values of type `A`; `A`
is therefore called a **phantom type**.

The purpose of the phantom type parameter is to allow you to build
tagged IDs.  In Eidos, tags are simply case objects:

``` scala
case object Location
```
you can then declare IDs of locations:
```scala
val location: Id[Location.type] = ???
```
It is recommended to create a type synonym to avoid having to spell
`.type` everywhere:

``` scala
case object Location
type Location = Location.type

val location: Id[Location] = ???
```
Tagged IDs already solve most of the problems outlined in the
[Motivation](#motivation) section; in particular, note the informative types, and the
fact that passing an `Id[Foo]` where an `Id[Bar]` is required is now a
type error.


`Id` is an abstract class, meaning that you can't create instances of
it through the constructor, use the method `of` in the companion
object instead:

``` scala
val location: Id[Location] = Id.of[Location]("12345") // or
val location = Id.of[Location]("12345")
// val location: Id[Location] = Id.of("12345") <-- incorrect
```
Note that while you can remove the type signature for `location`, you
**must** specify the type for `of`, even if you have a type signature
on the value (`of` was named this way to sound better with an explicit
type application).

Additionally, if you try to create an `Id` with a tag that is not a
case object, `of` will fail with a **compile time** error:
```Scala
class A
object B; type B = B.type

val a = Id.of[A]("")
val b = Id.of[B]("")
```
```Scala
[error] A is not a valid Eidos Tag.
[error] Declare it to be a case object to fix this error
[error]   val a = Id.of[A]("")
[error]                   ^
[error] B is not a valid Eidos Tag.
[error] Declare it to be a case object to fix this error
[error]   val b = Id.of[B]("")
[error]                        ^
[error] two errors found
```
case objects are required for implementation reasons.

## Pretty printing

One problem which is still not addressed is that of readable logs.
Given this definition of `PushNotification` (the tags are defined elsewhere):

``` scala
case class PushNotification(
 campaignId: Id[Campaign],
 customerId: Id[Customer],
 subscriptionId: Id[Subscription],
 content: Message
)
```
logs will look like this:

``` scala
PushNotification(
 Id(d43c828e-c82f-4a36-b62b-80d94a2d458d),
 Id(77633cfb-75e3-4496-817d-924183b1709f),
 Id(dd976ed8-2bff-4a7d-a81d-8013d7b8-7d4a-4aab),
 Message("Amazing offer")
)
```
Eidos solves this problem with the concept of _smart tags_: you mix
the appropriate traits to your **tags**, and the corresponding instances
of `Id` will change their behaviour accordingly. This change in
behaviour is _type-driven_, and will apply to all `Id`s with the same
tag.

The first example is the trait `MakeLabel`, which allows you to
automatically include the name of the tag:
```scala
type Customer = Customer.type
case object Customer extends MakeLabel
```

```scala
val customer: Id[Customer] = Id.of[Customer]("cn_1234")

scala> println(customer)
CustomerId(cn_1234)
```
Note that this is done without using reflection, code generation or macros.

We can now have readable logs with minimal effort:

``` scala
PushNotification(
 CampaignId(d43c828e-c82f-4a36-b62b-80d94a2d458d),
 CustomerId(77633cfb-75e3-4496-817d-924183b1709f),
 SubscriptionId(dd976ed8-2bff-4a7d-a81d-8013d7b8-7d4a-4aab),
 Message("Amazing offer")
)
```

If you want to customise the prefix, mix in `CustomLabel` instead, and
define `label`:
```scala
type MobileDevice = MobileDevice.type
case object MobileDevice extends CustomLabel {
 def label = "Phone"
}
```

```scala
val device: Id[MobileDevice] = Id.of[MobileDevice]("md1234")

scala> println(device)
PhoneId(md1234)
```

If you try and mix both in, you will get a **compile time** error:
```scala
case object Device extends MakeLabel with CustomLabel {
 def label = "Won't work"
}
```
```scala
[error] overriding method
"In Eidos, you can only extend one of MakeLabel or CustomLabel"
in trait MakeLabel of type => LabelDefinitionConflict;
...
```

Also remember that mixing these traits is optional, if you don't
extend anything your IDs will simply be printed as `Id(value)`

## Validation
`of` returns an `Id[A]`, meaning that ID creation can't fail (Eidos is
a purely functional library, and it will never throw exceptions).
``` scala
 scala> Id.of[Foo]("anything can go here")
 Id("anything can go here")

 scala> Id.of[Foo]("") // including empty strings
 Id("")

 scala> Id.of[Foo]("   ") // and blank strings
 Id("  ")
 ```
This allows you to treat an ID as opaque when its specific format is
unimportant or unknown, without having to fall back to raw strings.

On the other hand, for most IDs you are likely to want validation, and
Eidos provides a solution using, again, *smart tags*. For example, you
can have your tag extend the `NonBlank` trait to ensure that IDs
cannot contain empty or blank strings.
``` scala
case object Client extends MakeLabel with NonBlank
type Client = Client.type

scala> Id.of[Client]("hello")
res0: Option[Id[Client]] = Some(ClientId("hello"))

scala> Id.of[Client](" ")
res1: Option[Id[Client]] = None
```
The most important thing to notice here is that `of` changes its
return type to be `Option[Id[A]]`: specifying a format for your ID means
that you need to account for the possibility of failure, and that is
reflected in the types, as it should be.

Note that no reflection is involved; Scala's support for dependent
types is used instead to detect whether the specified tag extends one
of Eidos validation traits, and determine statically whether `of`
should return an `Id[A]` or an `Option[Id[A]]`.
Just like with labels, this behaviour is _type-driven_, and will apply
to all `Id`s with the same tag.

You can choose to extend one of the following validation traits:
- `NonBlank`, the ID cannot be empty or an empty string
  (all the other traits imply `NonBlank`)
- `Num`, the ID contains only digits
- `AlphaNum`, the ID is alphanumeric
- `UUID`, the ID follows the same format of a `java.util.UUID`
- `Regex`, the ID conforms to a custom regex
  ```scala
    case object Client extends MakeLabel with Regex {
     def pattern = "(abc)+12"
    }
    type A = A.type

    scala> Id.of[Client]("abcabc12")
    res0: Option[Id[Client]] = Some(ClientId("abcabc12"))

    scala> Id.of[Client]("abcd")
    res1: Option[Id[Client]] = None
  ```

Moreover, labels and validation formats can be mixed in to your tags
completely _à la carte_ (or not at all):
``` scala
case object Defaults // default label, no validation

case object Client extends MakeLabel with NonBlank

case object Device extends CustomLabel with Regex {
 def pattern = "(abc)+12"
 def label = "Phone"
}

case object RFID extends AlphaNum // default label

// and so on
```
If you instead try to extend more than one validation format, you will get a **compile time** error:
```scala
case object Device extends Num with AlphaNum
```
```scala
[error] overriding method
"In Eidos, You can only extend one validation format at the time!"`
in trait Num of type => ValidationFormatDefinitionConflict;
...
```

## Limitations
If you are using Scala < 2.12, due
to [SI-9689](https://issues.scala-lang.org/browse/SI-9689), trying to
define both a tag and its type synonym in the repl will fail with an
`AbstractMethodError`:
``` scala
scala> case object B extends MakeLabel
defined object B

scala> type B = B.type
defined type alias B

scala> Id.of[B]("hello")
java.lang.AbstractMethodError: B$.l()Leidos/id/Label;
  ... 42 elided

```
This problem can be solved completely by upgrading to Scala 2.12. If
you can't, you can use the following workarounds:
- Define your tag and type alias in a file, and load them into a repl
  from there
- Don't define a type alias, and write `Id[Foo.type]` and
  `Id.of[Foo.type]("foo")`


