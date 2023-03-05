package org.baklanovsoft.shoppingcart.catalog

import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.util.GenUUID
import skunk.Session

trait BrandsService[F[_]] {
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[BrandId]
}

object BrandsService {
  import org.baklanovsoft.shoppingcart.sql.BrandSQL._

  def make[F[_]: GenUUID: MonadCancelThrow](
      sessionR: Resource[F, Session[F]]
  ): BrandsService[F] = new BrandsService[F] {

    // we assume there are not much brands in database
    override def findAll: F[List[Brand]] =
      sessionR.use(_.execute(selectAll))

    override def create(name: BrandName): F[BrandId] =
      sessionR.use { session =>
        for {
          cmd <- session.prepare(insert)
          id  <- GenUUID[F].make.map(BrandId.apply)
          _   <- cmd.execute(Brand(id, name))
        } yield id
      }
  }
}
