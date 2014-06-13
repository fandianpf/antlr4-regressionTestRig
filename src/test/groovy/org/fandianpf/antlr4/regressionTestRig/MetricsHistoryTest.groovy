/*
 * [The "BSD license"]
 *  Copyright (c) 2014 FandianPF (Stephen Gaito)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.fandianpf.antlr4.regressionTestRig;

import org.junit.Test
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Unit tests for the MetricsHistory class.
 */
class MetricsHistoryTest {
 
  /**
   * Test the loading of values from a comma delimited string.
   * <p>
   * The first four values, min, mean, stdDev, and max, are ignored.
   * <p>
   * All subsequent values are added to the metrics ArrayList.
   * <p>
   * Values which are empty (white space between a pair of commas) 
   * or not in a long or double format are ignored and given the internal value
   * of -1L (which denotes a non-value).
   */
  @Test
  void loadValuesTest() {
    MetricsHistory metricsHistory = new MetricsHistory();
    metricsHistory.loadValues(Metrics.METRIC_TYPE[2], "0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    metricsHistory.computeStats();
//    assert metricsHistory.getNumValues() == 8;
    assert metricsHistory.metricsHistory.size() == 10;
    assert metricsHistory.metricsHistory.get(0).metric[2] == 5;
    assert metricsHistory.metricsHistory.get(1).metric[2] == 6;
    assert metricsHistory.metricsHistory.get(5).metric[2] == 10;
    assert metricsHistory.metricsHistory.get(6).metric[2] == -1;
    assert metricsHistory.metricsHistory.get(7).metric[2] == 12;
    assert metricsHistory.metricsHistory.get(8).metric[2] == -1;
    assert metricsHistory.metricsHistory.get(9).metric[2] == 14;
  }
  
  /**
   * Test the saving of metrics values (and min, mean, stdDev, and max) into a
   * PrintStream.
   * <p>
   * Values which are negative are output as white space.
   */
  @Test
  void saveIntoFileTest() {
    MetricsHistory metricsHistory = new MetricsHistory();
    metricsHistory.loadValues(Metrics.METRIC_TYPE[0], "0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    metricsHistory.computeStats();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    metricsHistory.saveIntoFile("testDocName", ps);
    String content = baos.toString("UTF-8");
    assert content.startsWith("\"testDocName\",\"t0LexerTimes\",5,8.875,3.");
    assert content.contains(",14,5,6,7,8,9,10,,12,,14\n");
  }
  
  /**
   * Test the computation of the (simple) statistics.
   */
  @Test
  void computeStatsTest() {
    MetricsHistory metricsHistory = new MetricsHistory();
    metricsHistory.loadValues(Metrics.METRIC_TYPE[0], "0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    metricsHistory.computeStats();
    assert metricsHistory.min.metric[0] == 5;
    assert metricsHistory.mean.metric[0] == 8.875;
    assert Math.abs(metricsHistory.stdDev.metric[0]-3.04431) < 0.001;
    assert metricsHistory.max.metric[0] == 14;
  }
}
