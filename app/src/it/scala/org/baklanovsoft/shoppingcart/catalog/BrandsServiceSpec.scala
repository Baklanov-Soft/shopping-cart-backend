package org.baklanovsoft.shoppingcart.catalog

import cats.implicits._
import cats.effect.IO
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.catalog.model.BrandName
import skunk.Session
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class BrandsServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {

  override type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    global.getOrFailR[Res](None)(classBasedInstance)

  test("Brands service works") { pool =>
    val service = BrandsService.make[IO](pool)

    val names =
      Range
        .inclusive(1, 5)
        .view
        .map(i => s"BrandsServiceSpec-test-$i")
        .map(BrandName.apply)
        .toList

    for {
      _    <- names.traverse(service.create)
      read <- service.findAll

    } yield assert(names.forall(n => read.exists(_.name == n)))

  }

}
