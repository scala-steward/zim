package org.bitlap.zim.infrastructure.repository

import org.bitlap.zim.BaseData
import org.bitlap.zim.domain.model.GroupList
import org.bitlap.zim.infrastructure.repository.TangibleGroupRepositorySpec.TangibleGroupRepositoryConfigurationSpec
import org.bitlap.zim.repository.TangibleGroupRepository
import org.bitlap.zim.repository.TangibleGroupRepository.ZGroupRepository
import scalikejdbc._
import zio.{ ULayer, ZLayer }

/**
 * t_group表操作的单测
 *
 * @author 梦境迷离
 * @since 2022/1/2
 * @version 1.0
 */
final class TangibleGroupRepositorySpec extends TangibleGroupRepositoryConfigurationSpec {

  behavior of "Tangible Group Repository"

  it should "findGroupById by id" in {
    val actual: Option[GroupList] = unsafeRun(
      (for {
        id <- TangibleGroupRepository.createGroupList(mockGroupList)
        dbGroup <- TangibleGroupRepository.findGroupById(id.toInt)
      } yield dbGroup).runHead
        .provideLayer(env)
    )
    actual.map(u => u.id -> u.groupname) shouldBe Some(mockGroupList.id -> mockGroupList.groupname)
  }

  it should "findGroup by groupname" in {
    val actual: Option[GroupList] = unsafeRun(
      (for {
        id <- TangibleGroupRepository.createGroupList(mockGroupList)
        dbGroup <- TangibleGroupRepository.findGroup(Some(mockGroupList.groupname))
      } yield dbGroup).runHead
        .provideLayer(env)
    )
    actual.map(u => u.id -> u.groupname) shouldBe Some(mockGroupList.id -> mockGroupList.groupname)
  }

  // TODO 需要表t_group_members的repository
//  it should "findGroupsById by uid" in {
//    val actual: Option[GroupList] = unsafeRun(
//      (for {
//        id <- TangibleGroupRepository.createGroupList(mockGroupList)
//        dbGroup <- TangibleGroupRepository.findGroupsById(mockGroupList.createId)
//      } yield dbGroup).runHead
//        .provideLayer(env)
//    )
//    actual.map(u => u.id -> u.groupname) shouldBe Some(mockGroupList.id -> mockGroupList.groupname)
//  }

  it should "countGroup by groupname" in {
    val actual: Option[Int] = unsafeRun(
      (for {
        id <- TangibleGroupRepository.createGroupList(mockGroupList)
        dbGroup <- TangibleGroupRepository.countGroup(Some(mockGroupList.groupname))
      } yield dbGroup).runHead
        .provideLayer(env)
    )
    actual shouldBe Some(1)
  }

  it should "deleteGroup by id" in {
    val actual: Option[GroupList] = unsafeRun(
      (for {
        id <- TangibleGroupRepository.createGroupList(mockGroupList)
        _ <- TangibleGroupRepository.deleteGroup(id.toInt)
        dbGroup <- TangibleGroupRepository.findGroupById(id.toInt)
      } yield dbGroup).runHead
        .provideLayer(env)
    )
    actual shouldBe None
  }
}

object TangibleGroupRepositorySpec {

  trait TangibleGroupRepositoryConfigurationSpec extends BaseData {

    override val table: SQL[_, NoExtractor] =
      sql"""
            DROP TABLE IF EXISTS `t_group`;
            CREATE TABLE `t_group` (
              `id` int(20) NOT NULL AUTO_INCREMENT,
              `group_name` varchar(64) NOT NULL COMMENT '群组名称',
              `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '群组图标',
              `create_id` int(20) NOT NULL COMMENT '创建者id',
              `create_time` timestamp NOT NULL DEFAULT '1971-01-01 00:00:00' ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
              PRIMARY KEY (`id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4;
         """

    val env: ULayer[ZGroupRepository] = ZLayer.succeed(h2ConfigurationProperties.databaseName) >>>
      TangibleGroupRepository.live

  }

}
