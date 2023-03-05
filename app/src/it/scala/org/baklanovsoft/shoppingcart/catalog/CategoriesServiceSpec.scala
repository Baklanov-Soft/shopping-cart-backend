package org.baklanovsoft.shoppingcart.catalog

import cats.effect.IO
import cats.effect.kernel.Resource
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.model.CategoryName
import skunk.Session
import weaver.{GlobalRead, IOSuite, LowPriorityImplicits}

class CategoriesServiceSpec(global: GlobalRead) extends IOSuite with LowPriorityImplicits {

  override type Res = Resource[IO, Session[IO]]

  override def sharedResource: Resource[IO, Res] =
    global.getOrFailR[Res](None)(classBasedInstance)

  test("Categories service works") { pool =>
    val service = CategoriesService.make[IO](pool)

    val names =
      Range
        .inclusive(1, 5)
        .view
        .map(i => s"CategoriesServiceSpec-test-$i")
        .map(CategoryName.apply)
        .toList

    for {
      _    <- names.traverse(service.create)
      read <- service.findAll

    } yield assert(names.forall(n => read.exists(_.name == n)))

  }

}
