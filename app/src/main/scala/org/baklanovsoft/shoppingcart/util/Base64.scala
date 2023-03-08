package org.baklanovsoft.shoppingcart.util

import cats.implicits._
import cats.ApplicativeThrow
import cats.effect.kernel.Sync

import java.nio.charset.StandardCharsets
import java.util.{Base64 => JBase64}

trait Base64[F[_]] {
  def encode(str: String): F[String]
  def encode(bytes: Array[Byte]): F[String]

  def decodeToBytes(strBase64: String): F[Array[Byte]]
  def decode(strBase64: String): F[String]
}

object Base64 {
  def apply[F[_]: Base64]: Base64[F] = implicitly

  implicit def forSync[F[_]: Sync]: Base64[F] =
    new Base64[F] {

      def encode(bytes: Array[Byte]): F[String] =
        Sync[F].delay {
          JBase64.getEncoder.encodeToString(bytes)
        }

      override def encode(str: String): F[String] =
        encode(str.getBytes(StandardCharsets.UTF_8))

      override def decodeToBytes(strBase64: String): F[Array[Byte]] =
        ApplicativeThrow[F].catchNonFatal {
          JBase64.getDecoder.decode(strBase64)
        }

      override def decode(strBase64: String): F[String] =
        decodeToBytes(strBase64).map(b => new String(b, StandardCharsets.UTF_8))
    }

}
