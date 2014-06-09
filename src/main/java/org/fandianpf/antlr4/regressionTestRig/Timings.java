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
 * The timings for a single purpose. 
 * Includes the ability to compute simple statistics.
 * <p>
 * Since timings are positive, any negative value represents
 * no measurement was possible for a given run.
 */
public class Timings {
   
  protected ArrayList<Long> timings;
   
  protected double mean      = 0.0;
  protected double stdDev    = 0.0;
  protected Long   min       = Long.MAX_VALUE;
  protected Long   max       = 0L;
  protected int    numValues = 0;
    
  public Timings() {
    timings = new ArrayList<Long>();
  }

  /**
   * Load timings values from a comma separated list in a {@link String}.
   * <p>
   * Empty entries (a pair of commas separated by white space) are treated as a 
   * non-value and the negative value -1L is stored instead of zero.
   * <p>
   * The first four values, representing min, mean, stdDev, and max, are ignored.
   */
  public void loadValues(String valuesStr) {
    // get the values as an array of strings split on ','
    String[] values    = valuesStr.split(",");
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
      timings.add(value);
    }
  }    
  
  public static void saveHeaderIntoFile(PrintStream outFile) {
    outFile.println("\"testDocName\",\"type\",\"min\",\"mean\",\"stdDev\",\"max\",\"values\"");
  }
  
  /**
   * Save this set of timings into the given file.
   * <p>
   * @param testDocName the name of the test document for this set of timings.
   * @param testType    the type (lexer, parser, or totals) of timings 
   *                    represented by this set of timings.
   */
  public void saveIntoFile(String testDocName, String testType, PrintStream outFile) {
    outFile.print('"');
    outFile.print(testDocName);
    outFile.print("\",\"");
    outFile.print(testType);
    outFile.print("\",");
    outFile.print(min);
    outFile.print(',');
    outFile.print(mean);
    outFile.print(',');
    outFile.print(stdDev);
    outFile.print(',');
    outFile.print(max);
    for(int i = 0; i < timings.size(); i++) {
      outFile.print(',');
      Long value = timings.get(i);
      if (-1 < value) { outFile.print(value); }
    }
    outFile.println();
  }
    
  /** Add one timing into the current list of timings. */    
  public void addTiming(Long timingMilliSeconds) {
    timings.add(timingMilliSeconds);
  }
  /** 
   * Get a specific timing from the ArrayList.
   * Returns -1L if the index is out of range.
   */
  public Long getTiming(int index) {
    if (index < timings.size()) {
      return timings.get(index);
    }
    return -1L;
  }
    
  /** 
   * Compute the (simple) statistics associated with this collection of timings.
   * <p>
   * Since timings are positive, any negative values are ignored while
   * computing these statistics.
   */
  public void computeStats() {
    // have we already computed these stats?
    if (max != 0) return;
      
    // directly compute the stats in the case 
    // where we have a sample size of one
    if (timings.size() == 1) {
      Long value = timings.get(0);
      min    = value;
      max    = value;
      mean   = (double)value;
      stdDev = 0.0;
      return;
    }

    numValues = 0;
     
    double sum = 0.0;
    for (int i = 0; i < timings.size(); i++) {
      Long value = timings.get(i);
      if (value < 0) continue;
      numValues += 1;
      if (value < min) min = value;
      if (max < value) max = value;
      sum += value;
    }
    mean = sum/((double)numValues);

    double sumDevMean = 0.0;
    for (int i = 0; i < timings.size(); i++) {
      double value = (double)timings.get(i);
      if (value < 0) continue;
      sumDevMean += (value - mean)*(value - mean);
    }
    stdDev = Math.sqrt( sumDevMean / ((double)(numValues-1)) );
  }

  public double getMean() {
    computeStats();
    return mean;
  }
    
  public double getStdDev() {
    computeStats();
    return stdDev;
  }
    
  public Long getMin() {
    computeStats();
    return min;
  }
    
  public Long getMax() {
    computeStats();
    return max;
  }
    
  /** Returns the total number of *positive* values. */
  public int getNumValues() {
    computeStats();
    return numValues;
  }
    
  /**
   * Add the timings from two Timings structures into a "totalTimings" 
   * structure.
   * <p>
   * The set of timings in the base timings are totaled with the corresponding 
   * timings in the other timings. If there are more base timings than other 
   * timings, the base timings with no corresponding other timings are copied as
   * is to the combined timings. If there are more other timings than base
   * timings the extra other timings are ignored.
   * <p>
   * This combination is careful to interpret negative timings as a 
   * non-measurement.
   */
  public Timings combine(Timings anOther) {
    Timings totalTimings = new Timings();
    for(int i = 0; i < timings.size(); i++) {
      Long totalValue = -1L;

      Long value = timings.get(i);
      if (-1 < value) { totalValue = value; }
        
      if (i < anOther.timings.size()) {
        Long anOtherValue = anOther.timings.get(i);
        if (-1 < anOtherValue) {
          if (-1 < totalValue) { totalValue += anOtherValue; }
          else { totalValue = anOtherValue; }
        }
      }
      totalTimings.addTiming(totalValue);
    }
    return totalTimings;
  }
}

