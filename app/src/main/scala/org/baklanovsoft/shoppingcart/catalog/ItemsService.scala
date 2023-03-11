package org.baklanovsoft.shoppingcart.catalog

import cats.effect.Concurrent
import cats.effect.kernel.Resource
import cats.implicits._
import org.baklanovsoft.shoppingcart.catalog.model._
import org.baklanovsoft.shoppingcart.catalog.sql.{BrandSQL, CategoriesSQL}
import org.baklanovsoft.shoppingcart.error.DomainError
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

  case object CategoryNotFound extends DomainError {
    val code = "CategoryNotFound"; val status = 404; val description = None
  }

  case object BrandNotFound extends DomainError {
    val code = "BrandNotFound"; val status = 409; val description = None
  }

  case object ItemNameExists extends DomainError {
    val code = "ItemNameExists"; val status = 409; val description = None
  }

  case object ItemNotFound extends DomainError {
    val code = "ItemNotFound"; val status = 404; val description = None
  }

  def make[F[_]: Concurrent: GenUUID](
      sessionR: Resource[F, Session[F]]
  ): ItemsService[F] = new ItemsService[F] {
    import org.baklanovsoft.shoppingcart.catalog.sql.ItemSQL._

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

        cats <- s.execute(CategoriesSQL.selectAll)
        _    <- Concurrent[F].whenA(!cats.exists(_.uuid == item.categoryId))(Concurrent[F].raiseError(CategoryNotFound))

        brands <- s.execute(BrandSQL.selectAll)
        _      <- Concurrent[F].whenA(!brands.exists(_.uuid == item.brandId))(Concurrent[F].raiseError(BrandNotFound))

        check <- s.prepare(selectByName).flatMap(_.option(item.name))
        _     <- Concurrent[F].whenA(check.nonEmpty)(Concurrent[F].raiseError(ItemNameExists))

        cmd <- s.prepare(insertItem)
        id  <- GenUUID[F].make.map(ItemId.apply)
        _   <- cmd.execute(id ~ item)
      } yield id
    )

    override def update(item: UpdateItem): F[Unit] = sessionR.use(s =>
      for {
        maybeItem <- s.prepare(selectById).flatMap(_.option(item.id))
        _         <- Concurrent[F].fromOption(maybeItem, ItemNotFound)
        _         <- s.execute(updateItem, item)
      } yield ()
    )
  }
}
