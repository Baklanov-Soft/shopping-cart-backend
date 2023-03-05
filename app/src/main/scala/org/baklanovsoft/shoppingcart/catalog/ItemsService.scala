package org.baklanovsoft.shoppingcart.catalog

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.util.GenUUID
import skunk.Session
import skunk.implicits._

trait ItemsService[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[ItemId]
  def update(item: UpdateItem): F[Unit]
}

object ItemsService {
  def make[F[_]: Concurrent: GenUUID](
      sessionR: Resource[F, Session[F]]
  ): ItemsService[F] = new ItemsService[F] {
    import org.baklanovsoft.shoppingcart.sql.ItemSQL._

    override def findAll: F[List[Item]] =
      sessionR.use(s => s.execute(selectAll))

    override def findBy(brand: BrandName): F[List[Item]] =
      sessionR.use(s =>
        s.prepare(selectByBrand).flatMap { ps =>
          ps.stream(brand, 1024).compile.toList
        }
      )

    override def findById(itemId: ItemId): F[Option[Item]] =
      sessionR.use(s => s.prepare(selectById).flatMap(ps => ps.option(itemId)))

    override def create(item: CreateItem): F[ItemId] = sessionR.use(s =>
      for {
        cmd <- s.prepare(insertItem)
        id  <- GenUUID[F].make.map(ItemId.apply)
        _   <- cmd.execute(id ~ item)
      } yield id
    )

    override def update(item: UpdateItem): F[Unit] =
      sessionR.use(_.execute(updateItem, item).void)
  }
}
