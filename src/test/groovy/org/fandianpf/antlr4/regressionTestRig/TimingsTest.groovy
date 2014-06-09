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
 * Unit tests for the Timings class.
 */
class TimingsTest {
 
  /**
   * Test the loading of values from a comma delimited string.
   * <p>
   * The first four values, min, mean, stdDev, and max, are ignored.
   * <p>
   * All subsequent values are added to the timings ArrayList.
   * <p>
   * Values which are empty (white space between a pair of commas) 
   * or not in a long or double format are ignored and given the internal value
   * of -1L (which denotes a non-value).
   */
  @Test
  void loadValuesTest() {
    Timings timings = new Timings();
    timings.loadValues("0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    timings.computeStats();
    assert timings.getNumValues() == 8;
    assert timings.timings.size() == 10;
    assert timings.timings.get(0) == 5;
    assert timings.timings.get(1) == 6;
    assert timings.timings.get(5) == 10;
    assert timings.timings.get(6) == -1;
    assert timings.timings.get(7) == 12;
    assert timings.timings.get(8) == -1;
    assert timings.timings.get(9) == 14;
  }
  
  /**
   * Test the saving of timings values (and min, mean, stdDev, and max) into a
   * PrintStream.
   * <p>
   * Values which are negative are output as white space.
   */
  @Test
  void saveIntoFileTest() {
    Timings timings = new Timings();
    timings.loadValues("0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    timings.computeStats();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    PrintStream ps = new PrintStream(baos);
    timings.saveIntoFile("testDocName", "testType", ps);
    String content = baos.toString("UTF-8");
    assert content.startsWith("\"testDocName\",\"testType\",5,8.875,3.");
    assert content.endsWith(",14,5,6,7,8,9,10,,12,,14\n");
  }
  
  /**
   * Test the computation of the (simple) statistics.
   */
  @Test
  void computeStatsTest() {
    Timings timings = new Timings();
    timings.loadValues("0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    timings.computeStats();
    assert timings.getMin() == 5;
    assert timings.getMean() == 8.875;
    assert Math.abs(timings.getStdDev()-3.04431) < 0.001;
    assert timings.getMax() == 14;
  }
  
  /**
   * Test combining two sets of timings into a set of totals timings.
   * <p>
   * The set of timings in the base timings are totaled with the corresponding 
   * timings in the other timings. If there are more base timings than other 
   * timings, the base timings with no corresponding other timings are copied as
   * is to the combined timings. If there are more other timings than base
   * timings the extra other timings are ignored.
   */ 
  @Test
  void combineTest() {
    Timings base  = new Timings();
    Timings other = new Timings();
    base.loadValues("0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12,nonLongValue,14");
    other.loadValues("0.1,0.2,0.3,0.4,5.0,6,7,8,9,10,,12");
    Timings combined = base.combine(other);
    assert combined.timings.get(0) == 10;
    assert combined.timings.get(6) == -1;
    assert combined.timings.get(7) == 24;
    assert combined.timings.get(8) == -1;
    assert combined.timings.get(9) == 14;
  }
}
