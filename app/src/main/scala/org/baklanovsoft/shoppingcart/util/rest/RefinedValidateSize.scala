package org.baklanovsoft.shoppingcart.util.rest

import eu.timepit.refined.api.Validate
import eu.timepit.refined.collection.Size

/** Unfortunately, the Circe Refined module doesn’t come with instances for Size[N], where N is an arbitrary literal
  * number. Yet, that’s easy to fix by making the following instance available.
  *
  *   - from PFP
  *
  * https://github.com/fthomas/refined/issues/1155
  */
trait RefinedValidateSize {

  implicit def validateSizeN[N <: Int, R](implicit
      w: ValueOf[N]
  ): Validate.Plain[R, Size[N]] =
    Validate.fromPredicate[R, Size[N]](
      _.toString.length == w.value,
      _ => s"Must have ${w.value} digits",
      Size[N](w.value)
    )

}
