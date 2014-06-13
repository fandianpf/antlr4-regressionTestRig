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

import java.util.ArrayList;
import java.io.PrintStream;

/** 
 * The a sequence of lexer/parser metrics. 
 * Includes the ability to compute simple statistics.
 * <p>
 * Since metrics are positive, any negative value represents
 * no measurement was possible for a given run.
 */
public class MetricsHistory {
   
  protected ArrayList<Metrics> metricsHistory = new ArrayList<Metrics>();
   
  protected MetricsDouble mean   = new MetricsDouble();
  protected MetricsDouble stdDev = new MetricsDouble();
  protected Metrics       min    = new Metrics().setMaxValues();
  protected Metrics       max    = new Metrics().setMinValues();
    
  public MetricsHistory() { }

  /**
   * Load metrics values from a comma separated list in a {@link String}.
   * <p>
   * Empty entries (a pair of commas separated by white space) are treated as a 
   * non-value and the negative value -1L is stored instead of zero.
   * <p>
   * The first four values, representing min, mean, stdDev, and max, are ignored.
   */
  public void loadValues(String metricStrType, String valuesLineStr) {
    // do not do anything if the metricStrType can not be recognized
    int metricType = Metrics.strType2int(metricStrType);
    if (-1 < metricType) {
      // get the values as an array of strings split on ','
      String[] values    = valuesLineStr.split(",");
      // ignore the min, mean, stdDev, max
      for ( int i = 4; i < values.length; i++) {
        // get each value in turn (checking for empty values)
        Long value = -1L;
        if (values[i].contains(".")) {
          try { value = Double.valueOf(values[i]).longValue(); }
          catch ( Exception exp) { /* ignore */ }
        } else if (!values[i].isEmpty()) {
          try { value = Long.valueOf(values[i]); }
          catch ( Exception exp) { /* ignore */ }
        }
        int index = i - 4;
        if (metricsHistory.size() <= index) metricsHistory.add(new Metrics());
        metricsHistory.get(index).setValue(metricType, value);
      }
    }
  }    
  
  public static void saveHeaderIntoFile(PrintStream outFile) {
    outFile.println("\"testDocName\",\"metricType\",\"min\",\"mean\",\"stdDev\",\"max\",\"values\"");
  }
  
  /**
   * Save this set of metrics into the given file.
   * <p>
   * @param testDocName the name of the test document for this set of metrics.
   * @param testType    the type (lexer, parser, or totals) of metrics 
   *                    represented by this set of metrics.
   */
  public void saveIntoFile(String testDocName, PrintStream outFile) {
    computeStats();
    for (int metricType = 0; metricType < Metrics.NUM_METRICS; metricType++) {
      outFile.print('"');
      outFile.print(testDocName);
      outFile.print("\",\"");
      outFile.print(Metrics.METRIC_TYPE[metricType]);
      outFile.print("\",");
      outFile.print(min.metric[metricType]);
      outFile.print(',');
      outFile.print(mean.metric[metricType]);
      outFile.print(',');
      outFile.print(stdDev.metric[metricType]);
      outFile.print(',');
      outFile.print(max.metric[metricType]);
      for(int i = 0; i < metricsHistory.size(); i++) {
        outFile.print(',');
        Long value = metricsHistory.get(i).metric[metricType];
        if (-1 < value) { outFile.print(value); }
      }
      outFile.println();
    }
  }
  
  /** Add one metric into the current list of metrics. */    
  public void appendMetrics(Metrics newMetrics) {
    metricsHistory.add(newMetrics);
  }
  /** 
   * Get a specific metric from the ArrayList.
   * Returns -1L if the index is out of range.
   */
  public Metrics getMetrics(int index) {
    if (index < metricsHistory.size()) {
      return metricsHistory.get(index);
    }
    return new Metrics();
  }
    
  /** 
   * Compute the (simple) statistics associated with this collection of metrics.
   * <p>
   * Since metrics are positive, any negative values are ignored while
   * computing these statistics.
   */
  public void computeStats() {
    // have we already computed these stats?
    if (max.metric[0] != Long.MIN_VALUE) return;
      
    // directly compute the stats in the case 
    // where we have a sample size of one
    if (metricsHistory.size() == 0) {
      // nothing to do
    } else if (metricsHistory.size() == 1) {
      Metrics values = metricsHistory.get(0);
      min    = values;
      max    = values;
      mean   = values.asDoubles();
      stdDev = new MetricsDouble();
      return;
    }

    for (int metricType = 0; metricType < Metrics.NUM_METRICS; metricType++) {
      int numValues = 0;
     
      double sum = 0.0;
      for (int i = 0; i < metricsHistory.size(); i++) {
        Long value = metricsHistory.get(i).metric[metricType];
        if (value < 0) continue;
        numValues += 1;
        if (value < min.metric[metricType]) min.metric[metricType] = value;
        if (max.metric[metricType] < value) max.metric[metricType] = value;
        sum += value;
      }
      mean.metric[metricType] = sum;
      if (0 < numValues) mean.metric[metricType] = sum/((double)numValues);

      double sumDevMean = 0.0;
      for (int i = 0; i < metricsHistory.size(); i++) {
        double value = (double)(metricsHistory.get(i).metric[metricType]);
        if (value < 0) continue;
        sumDevMean += (value - mean.metric[metricType])*(value - mean.metric[metricType]);
      }
      stdDev.metric[metricType] = sumDevMean;
      if (1 < numValues) 
        stdDev.metric[metricType] = Math.sqrt( sumDevMean / ((double)(numValues-1)) );
    }
  }
}

