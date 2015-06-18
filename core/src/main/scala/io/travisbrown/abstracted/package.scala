package io.travisbrown

package object abstracted {
  implicit class Abstracted[A](val a: A) {
    def abstracted: Empty[A] = Empty(a)
  }
}
