package org.baklanovsoft.shoppingcart.util.rest

import io.circe.refined.CirceCodecRefined
import sttp.tapir.codec.newtype.TapirCodecNewType
import sttp.tapir.codec.refined.TapirCodecRefined

trait RestCodecs
    extends CoercibleCodecs
    with CirceCodecRefined
    with RefinedValidateSize
    with TapirCodecNewType
    with TapirCodecRefined
    with SquantsCodec

// mark this as always used import in IDEA if needed
object RestCodecs extends RestCodecs
