package org.aiotrade.platform.test

import java.util.concurrent.TimeUnit
import org.aiotrade.lib.indicator.Indicator
import org.aiotrade.lib.math.indicator.IndicatorDescriptor
import org.aiotrade.lib.math.indicator.ComputeFrom
import org.aiotrade.lib.math.indicator.SpotIndicator
import org.aiotrade.lib.math.timeseries._
import org.aiotrade.lib.math.timeseries.datasource._
import org.aiotrade.lib.math.timeseries.descriptor._
import org.aiotrade.lib.securities._
import org.aiotrade.lib.securities.model._
import org.aiotrade.lib.securities.dataserver._
import org.aiotrade.lib.indicator.basic._

trait TestHelper {
    
  def createQuoteContract(symbol: String, category: String , sname: String, freq: TFreq, isRefreshable: Boolean, serverClassName: String): QuoteContract = {
    val dataContract = new QuoteContract

    dataContract.active = true
    dataContract.serviceClassName = serverClassName

    dataContract.srcSymbol = symbol

    dataContract.freq = freq
    dataContract.isRefreshable = isRefreshable
    dataContract.refreshInterval = 5

    dataContract
  }

  def createTickerContract(symbol: String, category: String, sname: String, freq: TFreq, serverClassName: String): TickerContract = {
    val dataContract = new TickerContract

    dataContract.active = true
    dataContract.serviceClassName = serverClassName

    dataContract.srcSymbol = symbol

    dataContract.freq = freq
    dataContract.isRefreshable = true
    dataContract.refreshInterval = 5

    dataContract
  }

  def createAnalysisContents(symbol: String, freq: TFreq): AnalysisContents = {
    val contents = new AnalysisContents(symbol)

    contents.addDescriptor(createIndicatorDescriptor(classOf[ARBRIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[BIASIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[BOLLIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[CCIIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[DMIIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[EMAIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[GMMAIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[HVDIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[KDIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[MACDIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[MAIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[MFIIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[MTMIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[OBVIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[ROCIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[RSIIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[SARIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[WMSIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[ZIGZAGFAIndicator], freq))
    contents.addDescriptor(createIndicatorDescriptor(classOf[ZIGZAGIndicator], freq))

    contents
  }

  def createIndicatorDescriptor[T <: Indicator](clazz: Class[T], freq: TFreq): IndicatorDescriptor = {
    val descriptor = new IndicatorDescriptor
    descriptor.active = true
    descriptor.serviceClassName = clazz.getName
    descriptor.freq = freq
    descriptor
  }


  def loadSer(contents: AnalysisContents): Unit = {
    val quoteContract = contents.lookupActiveDescriptor(classOf[QuoteContract]) getOrElse (return)

    val freq = quoteContract.freq
    if (!quoteContract.isFreqSupported(freq)) {
      return
    }

    val sec = contents.serProvider
    var mayNeedsReload = false
    if (sec == null) {
      return
    } else {
      mayNeedsReload = true
    }

    if (mayNeedsReload) {
      sec.resetSers
    }
    val ser = sec.serOf(freq).get

    if (!ser.isLoaded && !ser.isInLoading) {
      sec.loadSer(ser)
    }
  }

  def initIndicators(contents: AnalysisContents, baseSer: BaseTSer): Seq[Indicator] = {
    var indicators: List[Indicator] = Nil
    for (descriptor <- contents.lookupDescriptors(classOf[IndicatorDescriptor])
         if descriptor.active && descriptor.freq.equals(baseSer.freq)
    ) yield {
      descriptor.serviceInstance(baseSer) match {
        case Some(indicator: Indicator) => indicators ::= indicator
        case _ => println("In test: can not init instance of: " + descriptor.serviceClassName)
      }
    }
    indicators
  }

  def computeSync(indicator: Indicator): Unit = {
    indicator match {
      case _: SpotIndicator => // don't compute it right now
      case _ =>
        val t0 = System.currentTimeMillis
        indicator.computeFrom(0)
        println("Computing " + indicator.shortDescription + "(" + indicator.freq + ", size=" + indicator.size +  "): " + (System.currentTimeMillis - t0) + " ms")
    }
  }

  def computeAsync(indicator: Indicator): Unit = {
    indicator match {
      case _: SpotIndicator => // don't compute it right now
      case _ => indicator ! ComputeFrom(0)
    }
  }

  def printValuesOf(indicator: Indicator): Unit = {
    println
    println(indicator.freq)
    println(indicator.shortDescription + ":" + indicator.size)
    for (v <- indicator.vars) {
      print(v.name + ": ")
      v.values.reverse foreach {x => print(x + ",")}
      println
    }
  }

  def printLastValueOf(indicator: Indicator) {
    println
    println(indicator.freq + "-" +indicator.shortDescription + ":" + indicator.size)
    for (v <- indicator.vars if v.size > 0) {
      println(v.name + ": " + v.values.last)
    }
  }

  def reportQuote(sec: Sec) {
    println("\n======= " + new java.util.Date + " size of " + sec.uniSymbol  + " ======")
    sec.serOf(TFreq.DAILY)   foreach {x => println("daily:  "  + x.size)}
    sec.serOf(TFreq.ONE_MIN) foreach {x => println("1 min:  "  + x.size)}
    sec.serOf(TFreq.WEEKLY)  foreach {x => println("weekly: "  + x.size)}
  }

  def reportInds(inds: Seq[Indicator]) {
    inds foreach {x => println(x.toString)}
  }


  // wait for some ms
  def waitFor(ms: Long): Unit = {
    TimeUnit.MILLISECONDS.sleep(ms)
  }

}
