/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  Copyright (c) 2014 FandianPF (Stephen Gaito)
 *    Based on org.antlr.v4.runtime.Parser.java
 *      (on GitHub antlr/antlr4 project on 2014/06/06)
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

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Utils;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

/**
 * PrintStreamTraceListener provides a ParseTreeListener which can trace
 * the parser's internal event stream. Note that this event stream might not
 * have the same structure as the final parse tree. Hence the usefulness of 
 * tracing, you are provided with all of the trial parses that the parser
 * actually performed while building the final parse tree.
 * <p>
 * This ParseTreeListener is based upon the ANTLR4 Parser::TraceListener class
 * but this listener sends all output to the provided PrintStream, and in our
 * case, to the results file associated with the RegressionTestRig's inputFiles.
 */
public class PrintStreamTraceListener implements ParseTreeListener {

  /** The results file PrintStream associated with a particular inputFile. */
  public PrintStream outputStream;

  /** The parser which is actively doing the parseing. */
  public Parser parser;

  /** 
   * The rule names used in the {@link #enterEveryRule}, {@link #visitTerminal}
   * and {@link #exitEveryRule} output. Can be null.
   */
  protected List<String> ruleNames = null;

  
  /**
   * Constructor.
   *
   * @param aParser the active parser. Used to get the ruleNames as well as
   *                access to the current tokenStream.
   */
  public PrintStreamTraceListener(@NotNull Parser aParser) {
    parser = aParser;
    String[] ruleNamesArray = (parser != null) ? parser.getRuleNames() : null;
    ruleNames = (ruleNamesArray != null) ? Arrays.asList(ruleNamesArray) : null;
  }
    
  /** Set the current print stream used for output. */
  public void setPrintStream(@Nullable PrintStream anOutputStream) {
    outputStream = anOutputStream;
  }
    
  /** Output the current symbol with line and character position. */
  public void outputSymbol(Token symbol) {
    if ( outputStream != null) {
      outputStream.print(", LT(1)=[");
      String s = Utils.escapeWhitespace(symbol.getText(), false);
      outputStream.print(s);
      outputStream.print("]    (line ");
      outputStream.print(String.valueOf(symbol.getLine()));
      outputStream.print(":");
      outputStream.print(String.valueOf(symbol.getCharPositionInLine()));
      outputStream.println(")");
    }
  }
    
  /**
   * {@inheritDoc}
   * <p>
   * Record the entry to a given rule in the results file.
   */
  @Override
  public void enterEveryRule(ParserRuleContext ctx) {
    if (outputStream != null) {
      outputStream.print("enter   ");
      outputStream.print(ruleNames.get(ctx.getRuleIndex()));
      outputSymbol(parser.getCurrentToken());
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * Record the consumption of the current token.
   */
  @Override
  public void visitTerminal(TerminalNode node) {
    if (outputStream != null) { 
      outputStream.print("consume [");
      outputStream.print(node.getSymbol());
      if ( node.getParent() instanceof RuleContext ) {
        outputStream.print("] rule ");
        RuleContext rc = (RuleContext)(node.getParent());
        outputStream.println(ruleNames.get(rc.getRuleIndex()));
      } else {
        outputStream.println("]");
      }
    }
  }

  /**
   * {@inheritDoc}
   * <p>
   * not used.
   */
  @Override
  public void visitErrorNode(ErrorNode node) { }

  /**
   * {@inheritDoc}
   * <p>
   * Record the exit from a rule. 
   */
  @Override
  public void exitEveryRule(ParserRuleContext ctx) {
    if (outputStream != null) { 
      outputStream.print("exit    ");
      outputStream.print(ruleNames.get(ctx.getRuleIndex()));
      outputSymbol(parser.getCurrentToken());
    }
  }
}
