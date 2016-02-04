package io.travisbrown

package object abstracted {
  implicit final class Abstracted[A](val a: A) {
    final def abstracted: Empty[A] = Empty(a)
  }
}
