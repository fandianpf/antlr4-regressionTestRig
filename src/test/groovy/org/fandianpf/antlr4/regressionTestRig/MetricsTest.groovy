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
 * Unit tests for the Metrics class.
 */
class MetricsTest {
  
  /** Map the various Metrics.METRIC_TYPEs to the correct int. */
  @Test
  void strType2intTest() {
    assert Metrics.strType2int("t0LexerTimes")     == 0;
    assert Metrics.strType2int("t1ParserTimes")    == 1;
    assert Metrics.strType2int("t2LexerErrors")    == 2;
    assert Metrics.strType2int("t3ParserErrors")   == 3;
    assert Metrics.strType2int("t4Ambiguities")    == 4;
    assert Metrics.strType2int("t5WeakContexts")   == 5;
    assert Metrics.strType2int("t6StrongContexts") == 6;
  }
  
  /**
   * A test of the encodings while mapping the various Metrics.METRIC_TYPEs to
   * the correct int.
   */
  @Test
  void strType2intEncodingTest() {
    String[] strTypes = [ 
      "t0LexerTimes",
      "t1ParserTimes",
      "t2LexerErrors",
      "t3ParserErrors",
      "t4Ambiguities",
      "t5WeakContexts",
      "t6StrongContexts"
    ]
    String strTypesStr =
      strTypes[0]+"\n"+
      strTypes[1]+"\n"+
      strTypes[2]+"\n"+
      strTypes[3]+"\n"+
      strTypes[4]+"\n"+
      strTypes[5]+"\n"+
      strTypes[6]+"\n";
      
    StringReader   strTypesReader = new StringReader(strTypesStr);
    BufferedReader strTypesBuffer = new BufferedReader(strTypesReader);
    
    for( int metricType = 0; metricType < Metrics.NUM_METRICS; metricType++) {
      String aLine = strTypesBuffer.readLine()
      assert aLine == strTypes[metricType];
      System.err.println("strType2intEncodingTest ["+aLine+"] should be ["+strTypes[metricType]+"]");
      if (aLine!=null) assert Metrics.strType2int(aLine) == metricType;
    }
  }
}
