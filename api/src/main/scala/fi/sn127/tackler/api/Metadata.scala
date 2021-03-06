/*
 * Copyright 2016-2018 Jani Averbach
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
 *
 */
package fi.sn127.tackler.api

import io.circe._
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed abstract trait MetadataItem {
  def text(): String
}

final case class Metadata(git: Option[GitInputReference]) {

  def text(): String = {
    git.map(_.text()).getOrElse("")
  }
}

object Metadata {
  implicit val decodeBalanceItem: Decoder[Metadata] = deriveDecoder[Metadata]
  implicit val encodeBalanceItem: Encoder[Metadata] = deriveEncoder[Metadata]
}

/**
 * Metadata of used Git commit.
 *
 * @param ref if this transaction set was defined by git-ref.
 * @param commit this commitid (sha1) of used git tree
 * @param message commit's short message (one-line format)
 */
final case class GitInputReference(commit: String, ref: Option[String], message: String)
  extends MetadataItem {

  override def text(): String = {
    "" +
      "Git storage:\n" +
      "   commit:  " + commit + "\n" +
      "   ref:     " + ref.getOrElse("FIXED by commit") + "\n" +
      "   message: " + message + "\n"
  }
}

object GitInputReference {
  implicit val decodeBalanceItem: Decoder[GitInputReference] = deriveDecoder[GitInputReference]
  implicit val encodeBalanceItem: Encoder[GitInputReference] = deriveEncoder[GitInputReference]
}
