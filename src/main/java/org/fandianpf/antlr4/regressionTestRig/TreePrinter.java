/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  Copyright (c) 2014 FandianPF (Stephen Gaito)
 *    Based on org.antlr.v4.runtime.tree.Trees.java
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

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * TreePrinter prints parse trees in an indented one symbol per line syntax
 * (similar to Python's format).
 * 
 * This makes it easier for diff tools to identify the changes during 
 * regression testing
 */
public class TreePrinter {

  /**
   * The primary collection of characters used to increase the indentation for 
   * each recursive call to {@link #printTree}.
   */
  protected String primaryIndentAmount = ". ";
  
  /**
   * The secondary collection of characters used to increase the indentation for 
   * each recursive call to {@link #printTree}.
   */
  protected String secondaryIndentAmount = ", ";

  /** Every indentCyclePeriod-th indent will use the secondaryIndentAmount. */
  protected int indentCyclePeriod = 5;
  
  /** The length of the indent string after one full cycle. */
  protected int indentCycleLength = 10;
  
  /**
   * The rule names used in the {@link #printTree} output. Can be null.
   */
  protected List<String> ruleNames = null;
  
  /**
   * The {@link StringBuilder} used to build up the printTree output across all 
   * recursive calls to {@link #printTree}.
   */
  protected StringBuilder buf;
  
  /**
   * Build the correct indent string.
   *
   * @param currentIndent the current indent string.
   * @return returns the new indent string.
   */
  public String newIndent(String currentIndent) {
    int remainderLength = currentIndent.length() % indentCycleLength;
    int cycleIndex      = remainderLength / primaryIndentAmount.length();
    if (cycleIndex < indentCyclePeriod-1) return currentIndent + primaryIndentAmount;
    return currentIndent + secondaryIndentAmount;
  }
  
	/** 
	 * Base call to print out a whole parse tree in indented form. 
	 * {@link #appendNodeText} is used on the node payloads to get the text for the
	 * nodes.  Detect parse trees and extract data appropriately.
	 */
	public String printTree(@Nullable Tree t) {
	  if (t == null) return "<No parse tree found>";
	  
	  buf = new StringBuilder();
	  
    printTree(t, newIndent(""));
    return buf.toString();
  }
  
	/** 
	 * Recursive call to print out a sub-tree in indented form.
	 * {@link #appendNodeText} is used on the node payloads to get the text for the
	 * nodes. Detect parse trees and extract data appropriately.
	 */
	protected void printTree(@NotNull Tree t, String indent) {
		buf.append("\n");
		buf.append(indent);
		appendNodeText(t);
		String childIndent = newIndent(indent);
		for (int i = 0; i<t.getChildCount(); i++) {
			printTree(t.getChild(i), childIndent);
		}
	}

	/**
	 * Append the symbol and its position in the input text to the parse tree
	 * buffer.
	 *
	 * @param symbol the symbol which should be appended to the parse tree.
	 * @return boolean true if symbol is not null, false otherwise.
	 */
	protected boolean appendToken(Token symbol) {
	  if (symbol != null) {
		  buf.append("[");
			String s = Utils.escapeWhitespace(symbol.getText(), false);
			buf.append(s);
			buf.append("]    (line ");
			buf.append(String.valueOf(symbol.getLine()));
			buf.append(":");
			buf.append(String.valueOf(symbol.getCharPositionInLine()));
			buf.append(")");
			return true;
		}
		return false;
	}
	
	/** Append the rule name for this rule context. */
	protected boolean appendRule(RuleContext aRuleContext) {
	  if (aRuleContext != null) {
	    buf.append(ruleNames.get(aRuleContext.getRuleIndex()));
	    return true;
	  }
	  return false;
	}
	
	/**
	 * Determine some string representation for the given node and append it to
	 * the parse tree buffer.
	 *
	 * @param t The sub-tree whose current node needs a string representation.
	 */
	protected void appendNodeText(@NotNull Tree t) {
		if ( ruleNames!=null ) {
			if ( t instanceof RuleNode ) {
				if (appendRule(((RuleNode)t).getRuleContext())) return;
			}
			if ( t instanceof ErrorNode) {
			  buf.append("ERROR: ");
			  appendToken(((ErrorNode)t).getSymbol());
			  return;
			}
			if ( t instanceof TerminalNode) {
				if (appendToken(((TerminalNode)t).getSymbol())) return;
			}
		}
		// no recog for rule names
		Object payload = t.getPayload();
		if ( payload instanceof Token ) {
		  if (appendToken((Token)payload)) return;
		}
		if ( payload instanceof RuleContext ) {
		  if (appendRule((RuleContext)payload)) return;
		}
		String s = Utils.escapeWhitespace(payload.toString(), false);
		buf.append("unknown payload <<");
    buf.append(s);
    buf.append(">>");
	}

	/**
	 * Constructor.
	 *
	 * @param anIndentAmount A String containing the characters used to increase
	 *                       the indent for each recursive call to {@link #printTree}.
	 *
	 * @param recog          The lexer/parser used to obtain the rule names to be
	 *                       printed in the printTree output.
	 */
	public TreePrinter(@Nullable String aPrimaryIndentAmount,
	                   @Nullable String aSecondaryIndentAmount,
	                   int anIndentCyclePeriod,
	                   @Nullable Parser recog) {
	  if (aPrimaryIndentAmount != null)   primaryIndentAmount   = aPrimaryIndentAmount;
	  if (aSecondaryIndentAmount != null) secondaryIndentAmount = aSecondaryIndentAmount;
	  if (0 < anIndentCyclePeriod) {
	    indentCyclePeriod = anIndentCyclePeriod;
	    indentCycleLength = primaryIndentAmount.length() * (indentCyclePeriod - 1) + 
	      secondaryIndentAmount.length();
	  }
		String[] ruleNamesArray = (recog != null) ? recog.getRuleNames() : null;
		ruleNames = (ruleNamesArray != null) ? Arrays.asList(ruleNamesArray) : null;

	}
}
