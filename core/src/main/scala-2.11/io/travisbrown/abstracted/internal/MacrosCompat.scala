package io.travisbrown.abstracted.internal

import scala.reflect.ClassTag

private[abstracted] trait MacrosCompat {
  type Context = scala.reflect.macros.whitebox.Context

  def resultType(c: Context)(tpe: c.Type): c.Type = tpe.finalResultType

  def implicitViews(c: Context)(source: c.Type)(implicit
    /**
     * See SI-5143 for discussion of why we need this class tag.
     */
    tag: ClassTag[c.universe.TypeRef]
  ): List[c.Tree] = {
    import c.ImplicitCandidate
    import c.universe.{ EmptyTree, TypeRef }

    c.enclosingImplicits.collect {
      case ImplicitCandidate(_, _, TypeRef(_, _, _ :: target :: _), _) =>
        c.inferImplicitView(EmptyTree, source, target)
    }.filterNot(_.isEmpty)
  }
}
