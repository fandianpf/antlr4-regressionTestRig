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
 * Manages a simple CSV formated table of various lexer and parser metrics.
 */
public class MetricsTable {
  
  protected TreeMap<String, MetricsHistory> metricsTable = 
    new TreeMap<String, MetricsHistory>();  
  
  public MetricsTable() { }
  
  public void clearMetricsTable() {
    metricsTable.clear();
  }

  public void addTestDocName(String testDocName) {
    if (!metricsTable.containsKey(testDocName)) 
      metricsTable.put(testDocName, new MetricsHistory());
  }
  
  public void appendMetrics(String testDocName, Metrics someMetrics) {
    addTestDocName(testDocName);
    
    MetricsHistory metricsHistory = metricsTable.get(testDocName);
    metricsHistory.appendMetrics(someMetrics);
  }
  
  /**
   * Load this metrics table from the BufferedReader provided.
   * <p>
   * All metrics will be read from the same BufferedReader. Each line is labled
   * with the testDocName, and the metricsType. The metricsTypes which are to be 
   * loaded are t0Lexer, and t1Parser. Any metricsTypes which are not recognized
   * are ignored.
   * <p>
   * @param metricsBuffer the buffered reader from which to load the metrics 
   *                      table.
   */
  protected void loadMetricsTable(BufferedReader metricsBuffer)  
                                  throws IOException {
    
    metricsBuffer.readLine(); // ignore the first (header) line    
    for (String curLine = metricsBuffer.readLine(); 
         curLine!=null; curLine = metricsBuffer.readLine()) {
    
      // ignore this line if it does not start with the name of a testDoc
      if (!curLine.startsWith("\"")) continue;
      MetricsTableLineParser lp = new MetricsTableLineParser(curLine);
      addTestDocName(lp.testDocName);
      metricsTable.get(lp.testDocName).loadValues(lp.metricsType, lp.restOfLine);
    }
  }

  /**
   * Load this metrics table from the filesystem file named metricsTableFileName.
   * See: {@link #loadMetricsTable(BufferedReader)} for details.
   * <p>
   * @param metricsTableFileName the path to the filesystem file from which to 
   *                             load the metrics.
   */
  public void loadMetricsTable(String metricsTableFileName) throws FileNotFoundException,
    IOException {
    FileInputStream metricsFile  = new FileInputStream(metricsTableFileName);
    InputStreamReader metricsReader;
    try {
      metricsReader = new InputStreamReader(metricsFile, "UTF-8");
    } catch ( UnsupportedEncodingException usee) {
      metricsReader = new InputStreamReader(metricsFile);
    }
    BufferedReader metricsBuffer  = new BufferedReader(metricsReader);

    loadMetricsTable(metricsBuffer);

    metricsBuffer.close();
    metricsReader.close();
    metricsFile.close();
  }

  /**
   * Save this metrics table into the PrintStream provided.
   * <p>
   * All metrics are saved into the same PrintStream, ordered by 
   * testDocName and metricsType. This ensures that a modern spreadsheet
   * is capable of re-sorting the metrics to suit a user's particular interest.
   * <p>
   * The metricsTypes are t0Lexer, t1Parser, and t2Totals so that a naive
   * alphabetical sort will sort the lexer metrics before the parser metrics,
   * which will in turn sort before the totals metrics. All metricsTypes start
   * with the character t (for type) to ensure the field is interpreted as a 
   * string.
   * <p>
   * @param metricsFile PrintStream used to save the metrics.
   */
  public void saveMetricsTable(PrintStream metricsFile) {
    MetricsHistory.saveHeaderIntoFile(metricsFile);
    
    Iterator metricsIter = metricsTable.keySet().iterator();
    while(metricsIter.hasNext()) {
      String metricsKey = (String)metricsIter.next();
      MetricsHistory metricsHistory = metricsTable.get(metricsKey);
      metricsHistory.saveIntoFile(metricsKey, metricsFile);
    }
  }
  
  /**
   * Save this metrics table into the filesystem file metricsTableFileName.
   * See: {@link #saveMetricsTable(PrintStream)} for details.
   * <p>
   * @param metricsTableFileName the path to the filesystem file in which to
   *                             save the metrics.
   */
  public void saveMetricsTable(String metricsTableFileName) throws FileNotFoundException {
    PrintStream metricsFile;
    try { 
      metricsFile = new PrintStream(metricsTableFileName, "UTF-8");
    } catch (UnsupportedEncodingException usee) {
      metricsFile = new PrintStream(metricsTableFileName);
    }

    saveMetricsTable(metricsFile);
    
    metricsFile.close();
  }
  
}
