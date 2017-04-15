/*
 * Copyright 2016-2017 Jani Averbach
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
package fi.sn127.tackler.report

import fi.sn127.tackler.core.Settings
import fi.sn127.tackler.model.Txns

trait ExportLike {
  val settings: Settings

  protected def txnWriter(writer: Writer, rows: Seq[String]): Unit = {
    rows.foreach(row => {
      writer.write(row)
      writer.write("\n")
    })
    writer.flush()
  }

  def doExport(writer: Writer, txns: Txns): Unit
}
