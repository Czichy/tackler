package fi.sn127.tackler.report

import org.scalatest.{FlatSpec, MustMatchers}

import fi.sn127.tackler.core.Settings

class RegisterSettingsTest extends FlatSpec with MustMatchers {
  val settings = Settings()

  behavior of "RegisterSettings"

  it should "apply with default" in {
    val cfg = RegisterSettings(settings)

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "REGISTER"
    cfg.accounts mustBe List[String]()
  }

  it should "apply" in {
    val cfg = RegisterSettings(settings, Some("unit test"), Some(List("a", "b")))

    cfg.minScale mustBe settings.Reporting.minScale
    cfg.maxScale mustBe settings.Reporting.maxScale

    cfg.title mustBe "unit test"
    cfg.accounts mustBe List("a", "b")
  }
}
