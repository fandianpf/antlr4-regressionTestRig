/*
 * [The "BSD license"]
 *  Parts copyright (c) 2013 Terence Parr 
 *    (The CSV example contained in csvContentStrs array in the 
 *     processAnInputFileTest method below)
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

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;

import org.fandianpf.testParser.CSVLexer;
import org.fandianpf.testParser.CSVParser;

/**
 * Unit tests for the ParserTimingsTable class.
 */
class RegressionTestRigTest {

  /** Test the proceessing of command line arguments. */
  @Test
  void processArgsTest() {
    RegressionTestRig rtr = new RegressionTestRig();
    String[] args = [
      "aGrammarName",
      "aStartRule",
      "-tree",
      "-tokens",
      "-trace",
      "-SLL",
      "-diagnostics",
      "-encoding", "anEncoding",
      "-metrics", "metricsTable.csv",
      "-sourceDir", "aSourceDirPath",
      "-outputDir", "anOutputDirPath",
      "firstFileName",
      "secondFileName"
    ]
    assert rtr.grammarName == null;
    assert rtr.startRuleName == null;
    assert rtr.inputFiles.isEmpty()
    assert !rtr.printTree;
    assert !rtr.showTokens;
    assert !rtr.trace;
    assert !rtr.diagnostics;
    assert !rtr.SLL;
    assert rtr.encoding == null;
    assert rtr.metricsTablePath == null;
    assert rtr.metricsTable != null;
    assert rtr.processArgs(args);
    assert rtr.grammarName == "aGrammarName";
    assert rtr.startRuleName == "aStartRule";
    assert rtr.printTree;
    assert rtr.showTokens;
    assert rtr.trace;
    assert rtr.diagnostics;
    assert rtr.SLL;
    assert rtr.encoding == "anEncoding";
    assert rtr.sourceDir == "aSourceDirPath/";
    assert rtr.sourceDirRegExp.toString() == "^.*aSourceDirPath/";
    assert rtr.outputDir == "anOutputDirPath/";
    assert rtr.metricsTablePath == "metricsTable.csv";
    assert rtr.inputFiles.size() == 2;
    assert rtr.inputFiles.get(0) == "firstFileName";
    assert rtr.inputFiles.get(1) == "secondFileName";
  }
  
  /** Test the loading of the lexer class. */
  @Test
  void loadLexerTest() {
    RegressionTestRig rtr = new RegressionTestRig();
    String[] args = [ "org.fandianpf.testParser.CSV", "aStartRule" ];
    assert rtr.processArgs(args);
    assert rtr.lexer == null;
    rtr.loadLexer();
    assert rtr.lexer != null;
    assert rtr.lexer instanceof Lexer;
    assert rtr.lexer instanceof CSVLexer;
  }
  
  /**
   * Test the loading of the parser class.
   * <p>
   * Ensure that the parser will build a parse tree and that there is a valid
   * treePrinter with which to print the parse tree.
   */
  @Test
  void loadParserTest() {
    RegressionTestRig rtr = new RegressionTestRig();
    String[] args = [ 
      "org.fandianpf.testParser.CSV", "aStartRule",
      "-tree"
    ];
    assert rtr.processArgs(args);
    assert rtr.parser == null;
    assert rtr.parserClass == null;
    assert rtr.treePrinter == null;
    rtr.loadParser();
    assert rtr.parser != null;
    assert rtr.parser instanceof Parser;
    assert rtr.parser instanceof CSVParser;
    assert rtr.parserClass != null;
    assert rtr.parserClass instanceof Class;
    assert rtr.parser.getBuildParseTree();
    assert rtr.printTree;
    assert rtr.treePrinter != null;
    assert rtr.treePrinter instanceof TreePrinter;
  }
  
  /**
   * Test processing of one input file.
   * <p>
   * We build a simple CSV example and then check that the various 
   * RegressionTestRig sections, parse tree nodes as well content from the CSV 
   * example exist in the output.
   * <p>
   * The CSV.g4 grammar and the corresponding example1.csv file have been taken
   * from, 
   * <a href="https://github.com/antlr/grammars-v4/tree/master/csv" target="_blank">GitHub antlr/grammars-v4/csv</a>,
   * and are Copyright (c) 2013 Terence Parr and Licensed under a 3-clause BSD 
   * license.
   */
  @Test
  void processAnInputFileTest() {
    RegressionTestRig rtr = new RegressionTestRig();
    String[] args = [ 
      "org.fandianpf.testParser.CSV", "file",
      "-tokens", "-tree"
    ];
    assert rtr.processArgs(args);
    rtr.loadLexer();
    assert rtr.lexer != null;
    rtr.loadParser();
    assert rtr.parser != null;
    
    String[] csvContentStrs = [
      "\"REVIEW_DATE\",\"AUTHOR\",\"ISBN\",\"DISCOUNTED_PRICE\"",
      "\"1985/01/21\",\"Douglas Adams\",0345391802,5.95",
      "\"1990/01/12\",\"Douglas Hofstadter\",0465026567,9.95",
      "\"1998/07/15\",\"Timothy \"\"The Parser\"\" Campbell\",0968411304,18.99",
      "\"1999/12/03\",\"Richard Friedman\",0060630353,5.95",
      "\"2001/09/19\",\"Karen Armstrong\",0345384563,9.95",
      "\"2002/06/23\",\"David Jones\",0198504691,9.95",
      "\"2002/06/23\",\"Julian Jaynes\",0618057072,12.50",
      "\"2003/09/30\",\"Scott Adams\",0740721909,4.95",
      "\"2004/10/04\",\"Benjamin Radcliff\",0804818088,4.95",
      "\"2004/10/04\",\"Randel Helms\",0879755725,4.50"
    ]
    String csvContentStr =
      csvContentStrs[0]+"\n"+
      csvContentStrs[1]+"\n"+
      csvContentStrs[2]+"\n"+
      csvContentStrs[3]+"\n"+
      csvContentStrs[4]+"\n"+
      csvContentStrs[5]+"\n"+
      csvContentStrs[6]+"\n"+
      csvContentStrs[7]+"\n"+
      csvContentStrs[8]+"\n"+
      csvContentStrs[9]+"\n"+
      csvContentStrs[10]+"\n"
      
    StringReader          csvReader = new StringReader(csvContentStr);
    ByteArrayOutputStream outBaos   = new ByteArrayOutputStream();    
    PrintStream           outStream = new PrintStream(outBaos);
    
    Metrics metricsResults = rtr.processAnInputFile(csvReader, outStream);
    assert 0 < metricsResults.metric[Metrics.LEXER_TIMINGS];
    assert 0 < metricsResults.metric[Metrics.PARSER_TIMINGS];
    assert -1 < metricsResults.metric[Metrics.LEXER_ERRORS];
    assert -1 < metricsResults.metric[Metrics.PARSER_ERRORS];
    assert -1 < metricsResults.metric[Metrics.AMBIGUITIES];
    assert -1 < metricsResults.metric[Metrics.WEAK_CONTEXTS];
    assert -1 < metricsResults.metric[Metrics.STRONG_CONTEXTS];
    
    String testRigContent = outBaos.toString("UTF-8");
    String[] testRigLines = testRigContent.split("\n");
    assert testRigLines.length == 247;
    assert testRigLines[1] == "Lexer tokenizing input";
    assert testRigLines[4] == "Lexer token stream";
    assert testRigContent.contains("[@0,0:12='\"REVIEW_DATE\"',<5>,1:0]");
    assert testRigContent.contains("The Parser");
    assert testRigLines[96] == "Parser building parse tree";
    assert testRigLines[99] == "Parser parse tree";
    assert testRigContent.contains("file");
    assert testRigContent.contains("hdr");
    assert testRigContent.contains("row");
    assert testRigContent.contains("field");
  }
}
