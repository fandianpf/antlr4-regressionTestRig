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

/** A simple class to manage the various Lexer and Parser metrics. */
public class Metrics {
  
  public long[] metric = { -1L, -1L, -1L, -1L, -1L, -1L, -1L };

  public static final int LEXER_TIMINGS   = 0;
  public static final int PARSER_TIMINGS  = 1;
  public static final int LEXER_ERRORS    = 2;
  public static final int PARSER_ERRORS   = 3;
  public static final int AMBIGUITIES     = 4;
  public static final int WEAK_CONTEXTS   = 5;
  public static final int STRONG_CONTEXTS = 6;
  public static final int NUM_METRICS     = 7;
  
  public static final String[] METRIC_TYPE = {
    "t0LexerTimes",  "t1ParserTimes",
    "t2LexerErrors", "t3ParserErrors",
    "t4Ambiguities", "t5WeakContexts", "t6StrongContexts"
  };
  
  public Metrics() {}
  
  public Metrics setMaxValues() {
    for (int i = 0; i < metric.length; i++) metric[i] = Long.MAX_VALUE;
    return this;
  }
  
  public Metrics setMinValues() {
    for (int i = 0; i < metric.length; i++) metric[i] = Long.MIN_VALUE;
    return this; 
  }

  public static int strType2int(String metricStrType) {
    for (int metricType = 0; metricType < NUM_METRICS; metricType++) {
      if (metricStrType.compareToIgnoreCase(METRIC_TYPE[metricType]) == 0) {
        return metricType;
      }
    }
    return 0;
  }
  
  public long getValue(int metricType) {
    if (metricType < 0) return -1L;
    if (Metrics.NUM_METRICS <= metricType) return -1L;
    return metric[metricType];
  }
    
  public void setValue(int metricType, long value) {
    if (metricType < 0) return;  // ignore
    if (Metrics.NUM_METRICS <= metricType) return; // ignore
    metric[metricType] = value;
  }
  
  public MetricsDouble asDoubles() {
    MetricsDouble result = new MetricsDouble();
    for (int metricType = 0; metricType < Metrics.NUM_METRICS; metricType++) {
      result.metric[metricType] = (double) metric[metricType];
    }
    return result;
  }
}
