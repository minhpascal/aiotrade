/*
 * Copyright (c) 2006-2007, AIOTrade Computing Co. and Contributors
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *    
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *    
 *  o Neither the name of AIOTrade Computing Co. nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.aiotrade.lib.math.timeseries.datasource

import java.util.Calendar
import java.util.Date
import org.aiotrade.lib.math.timeseries.descriptor.AnalysisDescriptor
import org.aiotrade.lib.util.serialization.BeansDocument
import org.aiotrade.lib.util.serialization.JavaDocument
import org.w3c.dom.Element

/**
 * Securities' data source request contract. It know how to find and invoke
 * server by call createBindInstance().
 *
 * We simplely inherit AnalysisDescriptor, we may think the bindClass provides
 * service for descriptor.
 *
 * most fields' default value should be OK.
 *
 * @author Caoyuan Deng
 */
abstract class DataContract[S <: AnyRef: Manifest] extends AnalysisDescriptor[S] {
  @transient var reqId = 0

  /** symbol in source */
  var srcSymbol: String = _
   
  var datePattern: Option[String] = None
  var urlString: String = ""
  var isRefreshable: Boolean = false
  var refreshInterval: Int = 5000 // ms

  private val cal = Calendar.getInstance
  var endDate = cal.getTime
  cal.set(1970, Calendar.JANUARY, 1)
  var beginDate = cal.getTime


  /**
   * All dataserver will be implemented as singleton
   * @param none args are needed.
   */
  override def createServiceInstance(args: Any*): Option[S] = {
    lookupServiceTemplate(m.erasure.asInstanceOf[Class[S]], "DataServers")
  }

  override def toString: String = displayName

  override def writeToBean(doc: BeansDocument): Element = {
    val bean = super.writeToBean(doc)

    doc.valuePropertyOfBean(bean, "symbol", srcSymbol)
    doc.valuePropertyOfBean(bean, "datePattern", datePattern)

    val begDateBean = doc.createBean(beginDate)
    doc.innerPropertyOfBean(bean, "begDate", begDateBean)
    doc.valueConstructorArgOfBean(begDateBean, 0, beginDate.getTime)

    val endDateBean = doc.createBean(endDate)
    doc.innerPropertyOfBean(bean, "endDate", endDateBean)
    doc.valueConstructorArgOfBean(endDateBean, 0, endDate.getTime)

    doc.valuePropertyOfBean(bean, "urlString", urlString)
    doc.valuePropertyOfBean(bean, "refreshable", isRefreshable)
    doc.valuePropertyOfBean(bean, "refreshInterval", refreshInterval)

    bean
  }

  override def writeToJava(id: String): String = {
    super.writeToJava(id) +
    JavaDocument.set(id, "setSymbol", "" + srcSymbol) +
    JavaDocument.set(id, "setDateFormatPattern", "" + datePattern) +
    JavaDocument.create("begDate", classOf[Date], beginDate.getTime.asInstanceOf[AnyRef]) +
    JavaDocument.set(id, "setBegDate", "begDate") +
    JavaDocument.create("endDate", classOf[Date], endDate.getTime.asInstanceOf[AnyRef]) +
    JavaDocument.set(id, "setEndDate", "endDate") +
    JavaDocument.set(id, "setUrlString", urlString) +
    JavaDocument.set(id, "setRefreshable", isRefreshable) +
    JavaDocument.set(id, "setRefreshInterval", refreshInterval)
  }
}

