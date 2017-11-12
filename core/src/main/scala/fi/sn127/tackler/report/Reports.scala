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
import java.io.{BufferedWriter, OutputStreamWriter}
import java.nio.file.{Files, Path, Paths}

import org.slf4j.{Logger, LoggerFactory}
import resource._

import fi.sn127.tackler.core._
import fi.sn127.tackler.model.TxnData

final case class Reports(settings: Settings) {

  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  /**
   * Do report in selected formats.
   *
   * Selected reporter and its doReport method is called only once
   * with all outputs and formats as parameters.
   * So for reporter it is possible to go through all Txns only once,
   * and it is possible to implement Reporter.doReport in stream/iterator
   * like fashion. This is especially important with RegisterReport which
   * can produce big amount of report rows (as many as there are txns).
   *
   * @param outputBase basepath and basename of output
   * @param txnData to be used for reporting
   * @param reporter actual report producer, reporter.doReport is called only once.
   * @param formats formats to be produced (e.g. text, json, etc)
   */
  def doReport(outputBase: Option[Path], txnData: TxnData,
    reporter: ReportLike, formats: Seq[ReportFormat])
  : Unit = {

    /**
     * Do we have console output?
     */
    val consoles = if (settings.console) {
      List(new BufferedWriter(new OutputStreamWriter(Console.out)))
    } else {
      Nil
    }

    outputBase.fold {
      /**
       * No outputBase-path!
       */
      if (consoles.nonEmpty) {
        val frmts: Formats = List((ReportFormat("txt"), consoles))
        reporter.doReport(frmts, txnData)
      } else {
        log.warn("Reporting: no output at has been defined (no console and no files)!")
      }
    } {
      /**
       * we have outputBase-path!
       */
      outputPath =>

        /**
         * collect (format, output) pairs, and at the same time all streams which must be closed
         * result is ((format, output), closables)
         */
        val outputs = formats.map(frmt => {
          val ostream = Files.newOutputStream(Paths.get(outputPath.toString + "." + reporter.name + "." + frmt.ext))
          val strms: Writers = List(new BufferedWriter(new OutputStreamWriter(ostream, "UTF-8")))
          ((frmt, frmt.consoleOutput(strms, consoles)), strms)
        })

        try {
          val frmts = outputs.map({ case (frmt, closables) => frmt })
          reporter.doReport(frmts, txnData)
        } finally {
          outputs.foreach({ case (frmt, closables: Writers) => closables.foreach(c => c.close()) })
        }
    }

    consoles.foreach(stream => {
      // If we had console output(s), then separate reports by newline
      stream.write("\n")
      stream.flush()
    })
  }

  /**
   * Export Txns data, result is something which can be fed back
   * to Tackler (e.g. it is valid txn-data).
   *
   * Selected exporter and its doExport method is called only once
   * with all outputs and formats as parameters.
   * So for exporter it is possible to go through all Txns only once,
   * and it is possible to implement exporter.doExport in stream/iterator
   * like fashion. This is especially important with IdentityReport which
   * can produce big amount of txn rows (as many as there are txns).
   *
   * @param name of export
   * @param outputBase basepath and basename of output
   * @param txnData to be used for exporting
   * @param exporter actual export producer, exporter.doExport is called only once.
   */
  def doExport(name: String, outputBase: Option[Path], txnData: TxnData,
    exporter: ExportLike)
  : Unit = {
    outputBase.fold {
      log.warn("Report exporting: no output file is defined!")
    } { outputPath =>
      for {
        ostream <- managed(Files.newOutputStream(Paths.get(outputPath.toString + "." + name + ".txn")))
      } {
        val strm: Writer = new BufferedWriter(new OutputStreamWriter(ostream, "UTF-8"))
        exporter.doExport(strm, txnData.txns)
      }
    }
  }

  def doReports(outputBase: Option[Path], txnData: TxnData): Unit ={
    // todo: own set of formats for each report
    val frmts: Seq[ReportFormat] = settings.formats

    settings.reports.foreach {
      case BalanceReportType() =>
        val balReport = new BalanceReport("bal", settings)
        doReport(outputBase, txnData, balReport, frmts)

      case BalanceGroupReportType() =>
        val balgrpReport = new BalanceGroupReport("balgrp", settings)
        doReport(outputBase, txnData, balgrpReport, frmts)

      case RegisterReportType() =>
        val regReport = new RegisterReport("reg", settings)
        doReport(outputBase, txnData, regReport, frmts)

      case EquityReportType() =>
        val eqReport = new EquityReport(settings)
        doExport("equity", outputBase, txnData, eqReport)

      case IdentityReportType() =>
        val idReport = new IdentityReport(settings)
        doExport("identity", outputBase, txnData, idReport)
    }
  }
}
