package eidos
package id

// TODO better error message
// @annotation.implicitNotFound(
//     "${Tag} is not a valid Eidos Tag. It should extend a format trait")
// sealed trait Carrier[Tag] {
//   type V
//   type Out

//   def apply(v: V, l: Label[Tag]): Out
// }

// object Carrier {
//   type Aux[Tag, V_, O] = Carrier[Tag] { type V = V_ ; type Out = O }
//   // type Simple[Tag, Contents] = Aux[Tag, Contents, Id[Tag]]
//   // type Wrapped[F[_], Tag, Contents] = Aux[Tag, Contents, F[Id[Tag]]]

//   trait Simple[Tag, Contents] extends Carrier[Tag] {
//     final override type V = Contents
//     final override type Out = Id[Tag]

//     final override def apply(v: V, l: Label[Tag]) = Id.unsafeCreate(v, l)
//   }

//   trait Validated[Tag, Contents] extends Carrier[Tag] {
//     final override type V = Contents
//     final override type Out = Option[Id[Tag]]

//     final override def apply(v: V, l: Label[Tag]): Out =
//       validate(v).map(valid => Id.unsafeCreate(valid, l))

//     def validate(v: V): Option[V]
//   }

  // private[id] def default[A]: Aux[A, Id[A]] = new Build[A] {
  //   type Out = Id[A]
  //   override def build(v: String, l: Label[A]): Out = Id.unsafeCreate(v, l)
  // }

  // trait Validated[A] extends Build[A] {
  //   final override type Out = Option[Id[A]]
  //   final override def build(s: String, l: Label[A]): Out =
  //     validate(s).map(valid => Id.unsafeCreate(valid, l))

  //   def validate(s: String): Option[String]
  // }
//}
