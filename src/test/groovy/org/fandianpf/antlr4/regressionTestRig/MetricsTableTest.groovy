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
import java.io.StringReader;
import java.io.BufferedReader;

/**
 * Unit tests for the MetricsTable class.
 */
class MetricsTableTest {
  
  /** Test the addLexerTiming method. */
  @Test
  void addLexerTimingTest() {
    MetricsTable metricsTable = new MetricsTable();
    Metrics someMetrics = new Metrics();
    someMetrics.metric[0] = 10L;
    metricsTable.appendMetrics("testDocName", someMetrics);
    assert metricsTable.metricsTable.containsKey("testDocName");
    MetricsHistory metricsHistory = metricsTable.metricsTable.get("testDocName");
    assert metricsHistory.metricsHistory.size() == 1;
    assert metricsHistory.metricsHistory.get(0).metric[0] == 10L;
  }
  
  /**
   * Test the loadMetricsTable method.
   * <p>
   * The metricsTable string has a header line, and one testDoc, with lexer, 
   * parser and totals metrics types. The loadMetricsTable must load the 
   * appropriate metricsType into the corresponding metrics. It must also
   * load exactly the number of metrics given for each metricsType.
   */
  @Test
  void loadMetricsTableTest() {
    String[] metricsTableStrs = [
      "\"testDocName\",\"type\",\"min\",\"mean\",\"stdDev\",\"max\",\"values\"",
      "\"testDocName\",\"t00LexerTimes\",1,3.0,1.5811388300841898,5,1,2,3,4,5",
      "\"testDocName\",\"t01ParserTimes\",1,3.0,1.5811388300841898,5,2,4,6,8,10,12",
      "\"testDocName\",\"t20LexerErrors\",2,6.0,3.1622776601683795,10,3,6,9,12,15",
      "\"testDocName\",\"t21ParserErrors\",2,6.0,3.1622776601683795,10,4,8,12,16,20"
    ]
    String metricsTableStr = 
      metricsTableStrs[0]+"\n"+
      metricsTableStrs[1]+"\n"+
      metricsTableStrs[2]+"\n"+
      metricsTableStrs[3]+"\n"+
      metricsTableStrs[4]+"\n";

    StringReader   metricsReader = new StringReader(metricsTableStr);
    BufferedReader metricsBuffer = new BufferedReader(metricsReader);
    
    MetricsTable metricsTable = new MetricsTable();
    metricsTable.loadMetricsTable(metricsBuffer);
    MetricsHistory metricsHistory  = metricsTable.metricsTable.get("testDocName");
    assert metricsHistory.metricsHistory.size() == 6;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.LEXER_TIMINGS]   == 1;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.PARSER_TIMINGS]  == 2;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.LEXER_ERRORS]    == 3;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.PARSER_ERRORS]   == 4;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.AMBIGUITIES]     == -1;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.WEAK_CONTEXTS]   == -1;
    assert metricsHistory.metricsHistory.get(0).metric[Metrics.STRONG_CONTEXTS] == -1;

    assert metricsHistory.metricsHistory.get(4).metric[Metrics.LEXER_TIMINGS]   == 5;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.PARSER_TIMINGS]  == 10;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.LEXER_ERRORS]    == 15;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.PARSER_ERRORS]   == 20;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.AMBIGUITIES]     == -1;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.WEAK_CONTEXTS]   == -1;
    assert metricsHistory.metricsHistory.get(4).metric[Metrics.STRONG_CONTEXTS] == -1;

    assert metricsHistory.metricsHistory.get(5).metric[Metrics.PARSER_TIMINGS] == 12;
  }
  
  /**
   * Test the saveMetricsTable method.
   * <p>
   * The tested metrics has one testDoc, and so has 11 lines of output (a header 
   * and a line each for the 10 metric types).
   */
  @Test
  void saveMetricsTableTest() {
    ByteArrayOutputStream metricsBaos = new ByteArrayOutputStream();
    PrintStream metricsPs             = new PrintStream(metricsBaos);
    
    MetricsTable parserTable = new MetricsTable();
    parserTable.addTestDocName("testDocName");
    parserTable.metricsTable.get("testDocName").loadValues(Metrics.METRIC_TYPE[0],"0,0.0,0.0,0,1,2,3,4,5");
    parserTable.metricsTable.get("testDocName").loadValues(Metrics.METRIC_TYPE[1],"0,0.0,0.0,0,1,2,3,4,5");
    parserTable.metricsTable.get("testDocName").loadValues(Metrics.METRIC_TYPE[2],"0,0.0,0.0,0,1,2,3,4,5");

    parserTable.saveMetricsTable(metricsPs);
    
    String metricsContent = metricsBaos.toString("UTF-8");
    String[] metricsLines = metricsContent.split("\n");
    
    assert metricsLines.length  == 11;
    
    assert metricsLines[1].startsWith("\"testDocName\",\"t00LexerTimes\",1,3.0,1.");
    assert metricsLines[1].endsWith(",5,1,2,3,4,5");
    assert metricsLines[2].startsWith("\"testDocName\",\"t01ParserTimes\",1,3.0,1.");
    assert metricsLines[2].endsWith(",5,1,2,3,4,5");
    assert metricsLines[3].startsWith("\"testDocName\",\"t10LexerTokens\",1,3.0,1.");
    assert metricsLines[3].endsWith(",5,1,2,3,4,5");
  }
}
