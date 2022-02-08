package org.bitlap.zim.domain.model

import io.circe.generic.semiauto._
import io.circe.{ Decoder, Encoder }
import scalikejdbc.{ WrappedResultSet, _ }

/**
 * 添加好友
 * @see table:t_friend_group_friends
 * @param uid  用户ID
 * @param fgid 分组ID
 */
final case class AddFriend(uid: Int, fgid: Int)

object AddFriend extends BaseModel[AddFriend] {

  override lazy val columns: collection.Seq[String] = autoColumns[AddFriend]()

  override def tableName: String = "t_friend_group_friends"

  implicit val decoder: Decoder[AddFriend] = deriveDecoder[AddFriend]
  implicit val encoder: Encoder[AddFriend] = deriveEncoder[AddFriend]

  override def apply(rs: WrappedResultSet)(implicit sp: SyntaxProvider[AddFriend]): AddFriend =
    autoConstruct[AddFriend](rs, sp)
}
