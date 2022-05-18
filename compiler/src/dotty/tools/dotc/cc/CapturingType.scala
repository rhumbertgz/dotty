package dotty.tools
package dotc
package cc

import core.*
import Types.*, Symbols.*, Contexts.*

/** A capturing type. This is internally represented as an annotated type with a `retains`
 *  annotation, but the extractor will succeed only at phase CheckCaptures.
 */
object CapturingType:

  def apply(parent: Type, refs: CaptureSet, boxed: Boolean = false)(using Context): Type =
    if refs.isAlwaysEmpty then parent
    else AnnotatedType(parent, CaptureAnnotation(refs, boxed)(defn.RetainsAnnot))

  def unapply(tp: AnnotatedType)(using Context): Option[(Type, CaptureSet)] =
    if ctx.phase == Phases.checkCapturesPhase && tp.annot.symbol == defn.RetainsAnnot then
      EventuallyCapturingType.unapply(tp)
    else None

end CapturingType

/** An extractor for types that will be capturing types at phase CheckCaptures. Also
 *  included are types that indicate captures on enclosing call-by-name parameters
 *  before phase ElimByName.
 */
object EventuallyCapturingType:

  def unapply(tp: AnnotatedType)(using Context): Option[(Type, CaptureSet)] =
    val sym = tp.annot.symbol
    if sym == defn.RetainsAnnot || sym == defn.RetainsByNameAnnot then
      tp.annot match
        case ann: CaptureAnnotation =>
          Some((tp.parent, ann.refs))
        case ann =>
          try Some((tp.parent, ann.tree.toCaptureSet))
          catch case ex: IllegalCaptureRef => None
    else None

end EventuallyCapturingType


