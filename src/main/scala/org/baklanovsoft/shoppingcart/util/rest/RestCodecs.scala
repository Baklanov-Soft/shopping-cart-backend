package org.baklanovsoft.shoppingcart.util.rest

import io.circe.refined.CirceCodecRefined
import sttp.tapir.codec.newtype.TapirCodecNewType
import sttp.tapir.codec.refined.TapirCodecRefined

trait RestCodecs
    extends TapirCodecNewType
    with TapirCodecRefined
    with CoercibleCodecs
    with CirceCodecRefined
    with SquantsCodec
