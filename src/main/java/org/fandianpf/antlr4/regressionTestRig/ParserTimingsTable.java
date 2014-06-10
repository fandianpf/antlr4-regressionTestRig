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

import java.lang.Math;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Manages a simple CSV formated table of lexer, parser and total timings.
 */
public class ParserTimingsTable {
  
  /** A simple class to manage the Lexer, Parser and Total timings. */
  protected class LexerParserTimings {
    Timings lexerTimings  = new Timings();
    Timings parserTimings = new Timings();
    Timings totalTimings  = new Timings();
    
    protected LexerParserTimings() {}
    
    protected Timings getLexerTimings() {
      return lexerTimings;
    }
    
    protected Timings getParserTimings() {
      return parserTimings; 
    }
    
    protected Timings getTotalTimings() {
      return totalTimings;
    }
    
    protected void computeTotals() {
      totalTimings = lexerTimings.combine(parserTimings);
    }
    
    protected void computeStats() {
      lexerTimings.computeStats();
      parserTimings.computeStats();
      totalTimings.computeStats();
    }
  }
  
  protected TreeMap<String, LexerParserTimings> timingsTable;  
  
  public ParserTimingsTable() {
    timingsTable = new TreeMap<String, LexerParserTimings>();
  }
  
  public void clearTimingsTable() {
    timingsTable.clear();
  }

  public void addTestDocName(String testDocName) {
    if (!timingsTable.containsKey(testDocName)) 
      timingsTable.put(testDocName, new LexerParserTimings());
  }
  
  public void addLexerTiming(String testDocName, Long lexerTimingMilliSeconds) {
    addTestDocName(testDocName);
    
    Timings timings = timingsTable.get(testDocName).getLexerTimings();
    timings.addTiming(lexerTimingMilliSeconds);
  }
  
  public void addParserTiming(String testDocName, Long parserTimingMilliSeconds) {
    addTestDocName(testDocName);
    
    Timings timings = timingsTable.get(testDocName).getParserTimings();
    timings.addTiming(parserTimingMilliSeconds);
  }

  /**
   * Load this timings table from the BufferedReader provided.
   * <p>
   * All timings will be read from the same BufferedReader. Each line is labled
   * with the testDocName, and the timingsType. The timingsTypes which are to be 
   * loaded are t0Lexer, and t1Parser. Any timingsTypes which are not recognized
   * are ignored.
   * <p>
   * @param timingsBuffer the buffered reader from which to load the timings 
   *                      table.
   */
  protected void loadTimingsTable(BufferedReader timingsBuffer)  
                                  throws IOException {
    
    timingsBuffer.readLine(); // ignore the first (header) line    
    for (String curLine = timingsBuffer.readLine(); 
         curLine!=null; curLine = timingsBuffer.readLine()) {
    
      // ignore this line if it does not start with the name of a testDoc
      if (!curLine.startsWith("\"")) continue;
      TimingsTableLineParser lp = new TimingsTableLineParser(curLine);
      String testDocName = lp.testDocName;
      String timingsType = lp.timingsType;
      addTestDocName(testDocName);
      if (timingsType.equalsIgnoreCase("t0Lexer")) { 
        timingsTable.get(testDocName).getLexerTimings().loadValues(lp.restOfLine);
      } else if (timingsType.equalsIgnoreCase("t1Parser")) {
        timingsTable.get(testDocName).getParserTimings().loadValues(lp.restOfLine);
      } else {
        // ignore any lines which are not t0Lexer or t1Parser
      }
    }
  }

  /**
   * Load this timings table from the filesystem file named timingsTableFileName.
   * See: {@link #loadTimingsTable(BufferedReader)} for details.
   * <p>
   * @param timingsTableFileName the path to the filesystem file from which to 
   *                             load the timings.
   */
  public void loadTimingsTable(String timingsTableFileName) throws FileNotFoundException,
    IOException {
    FileInputStream timingsFile  = new FileInputStream(timingsTableFileName);
    InputStreamReader timingsReader;
    try {
      timingsReader = new InputStreamReader(timingsFile, "UTF-8");
    } catch ( UnsupportedEncodingException usee) {
      timingsReader = new InputStreamReader(timingsFile);
    }
    BufferedReader timingsBuffer  = new BufferedReader(timingsReader);

    loadTimingsTable(timingsBuffer);

    timingsBuffer.close();
    timingsReader.close();
    timingsFile.close();
  }

  /**
   * Save this timings table into the PrintStream provided.
   * <p>
   * All timings are saved into the same PrintStream, ordered by 
   * testDocName and timingsType. This ensures that a modern spreadsheet
   * is capable of re-sorting the timings to suit a user's particular interest.
   * <p>
   * The timingsTypes are t0Lexer, t1Parser, and t2Totals so that a naive
   * alphabetical sort will sort the lexer timings before the parser timings,
   * which will in turn sort before the totals timings. All timingsTypes start
   * with the character t (for type) to ensure the field is interpreted as a 
   * string.
   * <p>
   * @param timingsFile PrintStream used to save the timings.
   */
  public void saveTimingsTable(PrintStream timingsFile) {
    Timings.saveHeaderIntoFile(timingsFile);
    
    Iterator timingsIter = timingsTable.keySet().iterator();
    while(timingsIter.hasNext()) {
      String timingsKey = (String)timingsIter.next();
      LexerParserTimings lpTimings = timingsTable.get(timingsKey);
      lpTimings.computeTotals();
      lpTimings.computeStats();
      lpTimings.getLexerTimings().saveIntoFile(timingsKey, "t0Lexer",  timingsFile);
      lpTimings.getParserTimings().saveIntoFile(timingsKey,"t1Parser", timingsFile);
      lpTimings.getTotalTimings().saveIntoFile(timingsKey, "t2Totals", timingsFile);
    }
  }
  
  /**
   * Save this timings table into the filesystem file timingsTableFileName.
   * See: {@link #saveTimingsTable(PrintStream)} for details.
   * <p>
   * @param timingsTableFileName the path to the filesystem file in which to
   *                             save the timings.
   */
  public void saveTimingsTable(String timingsTableFileName) throws FileNotFoundException {
    PrintStream timingsFile;
    try { 
      timingsFile = new PrintStream(timingsTableFileName, "UTF-8");
    } catch (UnsupportedEncodingException usee) {
      timingsFile = new PrintStream(timingsTableFileName);
    }

    saveTimingsTable(timingsFile);
    
    timingsFile.close();
  }
  
}
