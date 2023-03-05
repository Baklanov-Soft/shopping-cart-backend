package org.baklanovsoft.shoppingcart.catalog

import cats.implicits._
import cats.effect.MonadCancelThrow
import cats.effect.kernel.Resource
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.util.GenUUID
import skunk.Session

trait CategoriesService[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[CategoryId]
}

object CategoriesService {
  import org.baklanovsoft.shoppingcart.sql.CategoriesSQL._

  def make[F[_]: GenUUID: MonadCancelThrow](
      sessionR: Resource[F, Session[F]]
  ): CategoriesService[F] = new CategoriesService[F] {

    // we assume there are not much brands in database
    override def findAll: F[List[Category]] =
      sessionR.use(_.execute(selectAll))

    override def create(name: CategoryName): F[CategoryId] =
      sessionR.use { session =>
        for {
          cmd <- session.prepare(insert)
          id  <- GenUUID[F].make.map(CategoryId.apply)
          _   <- cmd.execute(Category(id, name))
        } yield id
      }
  }
}
