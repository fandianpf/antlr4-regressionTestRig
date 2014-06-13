/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  Copyright (c) 2014 FandianPF (Stephen Gaito)
 *    Based on org.antlr.v4.runtime.ConsoleErrorListener.java
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

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.Nullable;

import java.io.PrintStream;
import java.util.BitSet;

/**
 * Provides an {@link ANTLRErrorListener} which places all error messages on the
 * provided {@link PrintStream}. 
 * <p>
 * A PrintSreamErrorListener also keeps counts of the numbers of each message
 * type. 
 *
 * @author Stephen Gaito (based on work by: Sam Harwell)
 */
public class PrintStreamErrorListener extends BaseErrorListener {

  /**
   * The print stream (by default System.err)
   */
  protected PrintStream output = System.err; 
  
  /** The number of times the {@link #syntaxError} method has been called. */
  protected Long numSyntaxErrors = 0L;
  
  /** The number of times the {@link #reportAmbiguity} method has been called. */
  protected Long numAmbiguityWarnings = 0L;
  
  /**
   * The number of times the {@link #reportAttemptingFullContext} method has
   * been called.
   */
  protected Long numStrongContextWarnings = 0L;
  
  /** 
   * The number of times the {@link #reportContextSensitivity} method has been
   * called. 
   */
  protected Long numWeakContextWarnings = 0L;
  
  /**
   * Class constructor.
   *
   * @param anOutput The {@link PrintStream} to used for all 
   *                 {@link org.antlr.v4.runtime.ANTLRErrorListener} output.
   */
  public PrintStreamErrorListener(@Nullable PrintStream anOutput) {
    if (anOutput != null) output = anOutput;
  }
  
  /** Clears the number of error and warnings. */
  public void clearErrorsAndWarnings() {
    numSyntaxErrors = 0L;
    numAmbiguityWarnings = 0L;
    numStrongContextWarnings = 0L;
    numWeakContextWarnings = 0L;
  }
  
  /**
   * Return the number of times the {@link #syntaxError} method has been called
   * less the number of Ambiguity, StrongContext and WeakContext warnings.
   */
  public Long getNumberOfSyntaxErrors() {
    Long numWarnings = 
      numAmbiguityWarnings + 
      numStrongContextWarnings +
      numWeakContextWarnings;
    return numSyntaxErrors - numWarnings; 
     
  }
  
  /**
   * Return the number of times the {@link #reportAmbiguity} method has been
   * called.
   */
  public Long getNumberOfAmbiguityWarnings() { return numAmbiguityWarnings; }
  
  
  /**
   * Return the number of times the {@link #reportAttemptingFullContext} method
   * has been called.
   */
  public Long getNumberOfStrongContextWarnings() { return numStrongContextWarnings; }
  
  /**
   * Return the number of times the {@link #reportContextSensitivity} method
   * has been called.
   */
  public Long getNumberOfWeakContextWarnings() { return numWeakContextWarnings; }
  
	/**
	 * {@inheritDoc}
	 *
	 * <p>
	 * This implementation prints messages to this instance's {@link #output}
	 * containing the values of {@code line}, {@code charPositionInLine}, and
	 * {@code msg} using the following format.</p>
	 *
	 * <pre>
	 * line <em>line</em>:<em>charPositionInLine</em> <em>msg</em>
	 * </pre>
	 * <p>
	 * It also increments the number of syntaxErrors.
	 */
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
							Object offendingSymbol,
							int line,
							int charPositionInLine,
							String msg,
							RecognitionException e)
	{
		output.println("line " + line + ":" + charPositionInLine + " " + msg);
		numSyntaxErrors++;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * We simply increment the number of Ambiguity warnings.
	 */
  @Override
	public void reportAmbiguity(Parser recognizer,
                       DFA dfa,
                       int startIndex,
                       int stopIndex,
                       boolean exact,
                       BitSet ambigAlts,
                       ATNConfigSet configs) {
     numAmbiguityWarnings++;
   }

   
	/**
	 * {@inheritDoc}
	 * <p>
	 * We simply increment the number of Strong Context warnings.
	 */
  @Override
  public void reportAttemptingFullContext(Parser recognizer,
                                    DFA dfa,
                                    int startIndex,
                                    int stopIndex,
                                    BitSet conflictingAlts,
                                    ATNConfigSet configs) {
     numStrongContextWarnings++;
   }

	/**
	 * {@inheritDoc}
	 * <p>
	 * We simply increment the number of Weak Context warnings.
	 */
  @Override
  public void reportContextSensitivity(Parser recognizer,
                                DFA dfa,
                                int startIndex,
                                int stopIndex,
                                int prediction,
                                ATNConfigSet configs) {
     numWeakContextWarnings++;
   }
}
