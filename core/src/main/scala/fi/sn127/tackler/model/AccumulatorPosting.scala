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
package fi.sn127.tackler.model

object OrderByAccumulatorPosting extends Ordering[AccumulatorPosting] {
  def compare(before: AccumulatorPosting, after: AccumulatorPosting): Int = {
    before.post.acctn.account.compareTo(after.post.acctn.account)
  }
}

final case class AccumulatorPosting(
  post: Posting,
  runningTotal: BigDecimal
) {
  def account: String = post.acctn.account
  def amount: BigDecimal = post.amount
  def commodity: Option[Commodity] = post.acctn.commodity
}
