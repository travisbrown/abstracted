package io.travisbrown.abstracted

/**
 * Wraps a value but doesn't have any other methods.
 */
case class Empty[A](a: A)

private[abstracted] object Empty {
  implicit def convert[A, B](e: Empty[A])(implicit
    converter: Converter[A, B]
  ): B = converter(e)
}
