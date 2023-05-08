/*
 * Copyright 2023 bitlap
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bitlap.zim.server.module

import org.bitlap.zim.domain.ws._
import org.bitlap.zim.server.actor._

import zio._
import zio.actors._

/** zio actor configuration
 *
 *  @author
 *    梦境迷离
 *  @since 2021/12/25
 *  @version 2.0
 */
object ZioActorModule {

  lazy val scheduleActor: ZIO[Scope, Throwable, ActorRef[protocol.Command]] =
    live
      .flatMap(_.make(Constants.SCHEDULE_JOB_ACTOR, zio.actors.Supervisor.none, (), ScheduleStateful.stateful))

  lazy val userStatusActor: ZIO[Scope, Throwable, ActorRef[protocol.Command]] =
    live
      .flatMap(
        _.make(Constants.USER_STATUS_CHANGE_ACTOR, zio.actors.Supervisor.none, (), UserStatusStateful.stateful)
      )

  private lazy val live: ZIO[Scope, Throwable, ActorSystem] =
    ZIO.acquireReleaseExit(ActorSystem("zioActorSystem"))((release, _) => release.shutdown.ignore)
}
