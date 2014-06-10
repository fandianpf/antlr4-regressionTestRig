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
 * Unit tests for the ParserTimingsTable class.
 */
class ParserTimingsTableTest {
  
  /** Test the addLexerTiming method. */
  @Test
  void addLexerTimingTest() {
    ParserTimingsTable timingsTable = new ParserTimingsTable();
    timingsTable.addLexerTiming("testDocName", 10L);
    assert timingsTable.timingsTable.containsKey("testDocName");
    Timings lTimings = timingsTable.timingsTable.get("testDocName").getLexerTimings();
    assert lTimings.timings.size() == 1;
    assert lTimings.timings.get(0) == 10L;
  }
  
  /** Test the addParserTiming method. */
  @Test
  void addParserTimingTest() {
    ParserTimingsTable timingsTable = new ParserTimingsTable();
    timingsTable.addLexerTiming("testDocName", 10L);
    timingsTable.addParserTiming("testDocName", 12L);
    assert timingsTable.timingsTable.containsKey("testDocName");
    Timings pTimings = timingsTable.timingsTable.get("testDocName").getParserTimings();
    assert pTimings.timings.size() == 1;
    assert pTimings.timings.get(0) == 12L;
  }

  /**
   * Test the loadTimingsTable method.
   * <p>
   * The timingsTable string has a header line, and one testDoc, with lexer, 
   * parser and totals timings types. The loadTimingsTable must load the 
   * appropriate timingsType into the corresponding timings. It must also
   * load exactly the number of timings given for each timingsType.
   */
  @Test
  void loadTimingsTableTest() {
    String[] timingsTableStrs = [
      "\"testDocName\",\"type\",\"min\",\"mean\",\"stdDev\",\"max\",\"values\"",
      "\"testDocName\",\"t0Lexer\",1,3.0,1.5811388300841898,5,1,2,3,4,5",
      "\"testDocName\",\"t1Parser\",1,3.0,1.5811388300841898,5,2,4,6,8,10,12",
      "\"testDocName\",\"t2Totals\",2,6.0,3.1622776601683795,10,3,6,9,12,15"
    ]
    String timingsTableStr = 
      timingsTableStrs[0]+"\n"+
      timingsTableStrs[1]+"\n"+
      timingsTableStrs[2]+"\n"+
      timingsTableStrs[3]+"\n";

    StringReader   timingsReader = new StringReader(timingsTableStr);
    BufferedReader timingsBuffer = new BufferedReader(timingsReader);
    
    ParserTimingsTable timingsTable = new ParserTimingsTable();
    timingsTable.loadTimingsTable(timingsBuffer);
    Timings lexerTimings  = timingsTable.timingsTable.get("testDocName").getLexerTimings();
    Timings parserTimings = timingsTable.timingsTable.get("testDocName").getParserTimings();
    assert  lexerTimings.timings.get(0) == 1;
    assert parserTimings.timings.get(0) == 2;
    assert  lexerTimings.timings.get(4) == 5;
    assert parserTimings.timings.get(4) == 10;
    assert parserTimings.timings.get(5) == 12;
    assert  lexerTimings.timings.size() == 5;
    assert parserTimings.timings.size() == 6;
  }
  
  /**
   * Test the saveTimingsTable method.
   * <p>
   * The tested timings has one testDoc, and so has 4 lines of output (a header 
   * and a line each for the Lexer, Parser, and Totals timings).
   * <p>
   * Since the lexer and parser timings are the same, the totals timings should 
   * be exactly double.
   */
  @Test
  void saveTimingsTableTest() {
    ByteArrayOutputStream timingsBaos = new ByteArrayOutputStream();
    PrintStream timingsPs             = new PrintStream(timingsBaos);
    
    ParserTimingsTable parserTable = new ParserTimingsTable();
    parserTable.addTestDocName("testDocName");
    parserTable.timingsTable.get("testDocName").getLexerTimings().loadValues("0,0.0,0.0,0,1,2,3,4,5");
    parserTable.timingsTable.get("testDocName").getParserTimings().loadValues("0,0.0,0.0,0,1,2,3,4,5");

    parserTable.saveTimingsTable(timingsPs);
    
    String timingsContent = timingsBaos.toString("UTF-8");
    String[] timingsLines = timingsContent.split("\n");
    
    assert timingsLines.length  == 4;
    
    assert timingsLines[1].startsWith("\"testDocName\",\"t0Lexer\",1,3.0,1.");
    assert timingsLines[1].endsWith(",5,1,2,3,4,5");
    assert timingsLines[2].startsWith("\"testDocName\",\"t1Parser\",1,3.0,1.");
    assert timingsLines[2].endsWith(",5,1,2,3,4,5");
    assert timingsLines[3].startsWith("\"testDocName\",\"t2Totals\",2,6.0,3.");
    assert timingsLines[3].endsWith(",10,2,4,6,8,10");
  }
}
