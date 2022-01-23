package org.bitlap.zim.server.application.impl

import org.bitlap.zim.domain._
import org.bitlap.zim.domain.input.{ UserInput, UserSecurity }
import org.bitlap.zim.domain.model.User
import org.bitlap.zim.server.application.{ ApiApplication, UserApplication }
import org.bitlap.zim.server.util.SecurityUtil
import zio.crypto.hash.MessageDigest
import zio.stream.ZStream
import zio.{ stream, Has }
import org.bitlap.zim.server.util.LogUtil

/**
 * @author 梦境迷离
 * @since 2022/1/8
 * @version 1.0
 */
private final class ApiService(userApplication: UserApplication) extends ApiApplication {

  override def findById(id: Long): stream.Stream[Throwable, User] = userApplication.findById(id)

  override def existEmail(email: String): stream.Stream[Throwable, Boolean] = userApplication.existEmail(email)

  override def findUserById(id: Int): stream.Stream[Throwable, User] = userApplication.findUserById(id)

  override def updateInfo(user: UserInput): stream.Stream[Throwable, Boolean] = {
    def check(): Boolean =
      user.password == null || user.password.trim.isEmpty || user.oldpwd == null || user.oldpwd.trim.isEmpty

    for {
      u <- userApplication.findUserById(user.id)
      pwdCheck <- ZStream.fromEffect(SecurityUtil.matched(user.oldpwd, u.password))
      newPwd <- ZStream.fromEffect(SecurityUtil.encrypt(user.password))
      sex = if (user.sex.equals("nan")) 1 else 0
      checkAndUpdate <-
        if (check()) {
          userApplication.updateUserInfo(u.copy(sex = sex, sign = user.sign, username = user.username))
        } else if (pwdCheck) {
          ZStream.succeed(false)
        } else {
          userApplication
            .updateUserInfo(
              u.copy(
                password = newPwd.value,
                sex = sex,
                sign = user.sign,
                username = user.username
              )
            )

        }
    } yield checkAndUpdate
  }

  override def login(user: UserSecurity.UserSecurityInfo): stream.Stream[Throwable, User] =
    userApplication.matchUser(User(user.id, user.email, user.password))

  override def init(userId: Int): stream.Stream[Throwable, FriendAndGroupInfo] = {
    val ret = for {
      user <- userApplication.findUserById(userId).runHead
      _ <- LogUtil.info(s"init user=>$user")
      friends <- userApplication.findFriendGroupsById(userId).runCollect
      _ <- LogUtil.info(s"init friends=>$friends")
      groups <- userApplication.findGroupsById(userId).runCollect
      _ <- LogUtil.info(s"init groups=>$groups")
      resp = FriendAndGroupInfo(
        // 怎么区分主动刷新？这样如果主动刷新会将隐式重置为在线
        mine = user.fold[User](null)(u => u.copy(status = SystemConstant.status.ONLINE)),
        friend = friends.toList,
        group = groups.toList
      )
      _ <- LogUtil.info(s"init ret=>$resp")
    } yield resp
    ZStream.fromEffect(ret)
  }
}

object ApiService {

  type ZApiApplication = Has[ApiApplication]

  def apply(userApplication: UserApplication): ApiApplication = new ApiService(userApplication)

}
