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
package org.aiotrade.lib.securities

import org.aiotrade.lib.math.timeseries.{DefaultMasterTSer, TFreq, TSerEvent, TVal, TItem}
import org.aiotrade.lib.math.timeseries.plottable.Plot

/**
 *
 * @author Caoyuan Deng
 */
class QuoteSer(freq: TFreq) extends DefaultMasterTSer(freq) {
    
  private var _shortDescription: String = ""
  var adjusted: Boolean = false
    
  val open   = TVar[Float]("O", Plot.Quote)
  val high   = TVar[Float]("H", Plot.Quote)
  val low    = TVar[Float]("L", Plot.Quote)
  val close  = TVar[Float]("C", Plot.Quote)
  val volume = TVar[Float]("V", Plot.Volume)
    
  val close_ori = TVar[Float]()
  val close_adj = TVar[Float]()

  override protected def assignValue(tval: TVal) {
    val time = tval.time
    tval match {
      case quote: Quote =>
        open(time)   = quote.open
        high(time)   = quote.high
        low(time)    = quote.low
        close(time)  = quote.close
        volume(time) = quote.volume

        close_ori(time) = quote.close

        val adjuestedClose = if (quote.close_adj != 0 ) quote.close_adj else quote.close
        close_adj(time) = adjuestedClose
      case _ => assert(false, "Should pass a Quote type TimeValue")
    }
  }

  /**
   * @param boolean b: if true, do adjust, else, de adjust
   */
  def adjust(b: Boolean) {
    var i = 0
    while (i < size) {
            
      var prevNorm = close(i)
      var postNorm = if (b) {
        /** do adjust */
        close_adj(i)
      } else {
        /** de adjust */
        close_ori(i)
      }
                        
      high(i)  = linearAdjust(high(i),  prevNorm, postNorm)
      low(i)   = linearAdjust(low(i),   prevNorm, postNorm)
      open(i)  = linearAdjust(open(i),  prevNorm, postNorm)
      close(i) = linearAdjust(close(i), prevNorm, postNorm)

      i += 1
    }
        
    adjusted = b
        
    val evt = TSerEvent.Updated(this, null, 0, lastOccurredTime)
    publish(evt)
  }
    
  /**
   * This function adjusts linear according to a norm
   */
  private def linearAdjust(value: Float, prevNorm: Float, postNorm: Float): Float = {
    ((value - prevNorm) / prevNorm) * postNorm + postNorm
  }

  override def shortDescription_=(symbol: String): Unit = {
    this._shortDescription = symbol
  }
    
  override def shortDescription: String = {
    if (adjusted) {
      _shortDescription + "(*)"
    } else {
      _shortDescription
    }
  }
    
}





