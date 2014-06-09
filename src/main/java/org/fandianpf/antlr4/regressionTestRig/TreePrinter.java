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
   * The characters used to increase the indentation for each recursive call
   * to {@link #printTree}.
   */
  protected String indentAmount = "  ";
  
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
	 * Base call to print out a whole parse tree in indented form. 
	 * {@link #getNodeText} is used on the node payloads to get the text for the
	 * nodes.  Detect parse trees and extract data appropriately.
	 */
	public String printTree(@Nullable Tree t) {
	  if (t == null) return "<No parse tree found>";
	  
	  buf = new StringBuilder();
    printTree(t, indentAmount);
    return buf.toString();
  }
  
	/** 
	 * Recursive call to print out a sub-tree in indented form.
	 * {@link #getNodeText} is used on the node payloads to get the text for the
	 * nodes. Detect parse trees and extract data appropriately.
	 */
	protected void printTree(@NotNull Tree t, String indent) {
		String s = Utils.escapeWhitespace(getNodeText(t), false);
		buf.append("\n");
		buf.append(indent);
		buf.append(s);
		String childIndent = indent+indentAmount;
		for (int i = 0; i<t.getChildCount(); i++) {
			printTree(t.getChild(i), childIndent);
		}
	}

	/**
	 * Determine some string representation for the given node.
	 *
	 * @param t The sub-tree whose current node needs a string representation.
	 */
	protected String getNodeText(@NotNull Tree t) {
		if ( ruleNames!=null ) {
			if ( t instanceof RuleNode ) {
				int ruleIndex = ((RuleNode)t).getRuleContext().getRuleIndex();
				String ruleName = ruleNames.get(ruleIndex);
				return ruleName;
			}
			else if ( t instanceof ErrorNode) {
				return t.toString();
			}
			else if ( t instanceof TerminalNode) {
				Token symbol = ((TerminalNode)t).getSymbol();
				if (symbol != null) {
					String s = symbol.getText();
					return s;
				}
			}
		}
		// no recog for rule names
		Object payload = t.getPayload();
		if ( payload instanceof Token ) {
			return ((Token)payload).getText();
		}
		return payload.toString();
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
	public TreePrinter(@Nullable String anIndentAmount, @Nullable Parser recog) {
	  if (anIndentAmount != null) indentAmount = anIndentAmount;
		String[] ruleNamesArray = (recog != null) ? recog.getRuleNames() : null;
		ruleNames = (ruleNamesArray != null) ? Arrays.asList(ruleNamesArray) : null;

	}
}
