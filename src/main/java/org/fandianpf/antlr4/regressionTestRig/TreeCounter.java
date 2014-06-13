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

/** TreeCounter computes complexity metrics for a parse tree. */
public class TreeCounter {
  
  /**
   * The rule names used in the {@link #printTree} output. Can be null.
   */
  protected List<String> ruleNames = null;

  /** The length of the longest branch in this tree. */
  protected long treeDepth = 0L;
  
  /** The number of nodes (including terminal nodes) in this tree. */ 
  protected long numNodes  = 0L;
  
	/** Base call to compute the complexity of this tree. */ 
	public void countTree(@Nullable Tree t) {
	  treeDepth = 0L;
	  numNodes  = 0L;
	  if (t != null) countTree(t, 1L);
  }
  
	/** Recursive call to compute the complexity of this (sub)tree. */
	protected void countTree(@NotNull Tree t, Long depth) {
	  if (treeDepth < depth) treeDepth = depth;
		for (int i = 0; i<t.getChildCount(); i++) {
			countTree(t.getChild(i), depth+1);
			numNodes++;
		}
	}

	/** Get the length of the longest branch in the tree. */
	public long getTreeDepth() { return treeDepth; }
	
	/** Get the total number of nodes (including terminal nodes) in this tree. */
	public long getNumberOfNodes() { return numNodes; }
	
	/** Constructor. */
	public TreeCounter() { }
}
