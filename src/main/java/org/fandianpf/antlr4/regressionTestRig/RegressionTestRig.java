/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  Copyright (c) 2014 FandianPF (Stephen Gaito)
 *    Based on org.antlr.v4.runtime.misc.TestRig.java
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

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import javax.print.PrintException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Run a lexer/parser combo, optionally printing tree string. 
 * Optionally taking input file.
 *
 *  $ java org.fandianpf.antlr4.RegressionTestRig GrammarName startRuleName
 *        [-tree]
 *        [-tokens]
 *        [-trace]
 *        [-diagnostics]
 *        [-SLL]
 *        [-encoding anEncoding]
 *        [-timings aTimingsTablePath]
 *        [input-filename(s)]
 */
public class RegressionTestRig {
  
  /** Default start rule for the lexer. */
	public static final String LEXER_START_RULE_NAME = "tokens";
	
	/** Default bar used in results files to delineate sections. */
	public static final String PRINT_STREAM_BAR =
    "--------------------------------------------------------------------------------";
	
	/** 
	 * Option: Fully qualified root name of the grammar to be tested.
	 * The lexer and parser grammars will be loaded by appending 'Lexer' and 
	 * 'Parser' respectively to 'grammarName'.
	 */
	protected String grammarName;
	
	/** Option: The name of the lexer/parser method used to start parsing. */ 
	protected String startRuleName;
	
	/** Option: List of input files to be tested */
	protected final List<String> inputFiles = new ArrayList<String>();
	
	/** Option: Whether or not to print the parse tree. Default: false */ 
	protected boolean printTree = false;
	
	/** Option: Whether or not to show lexer tokens. Default: false */
	protected boolean showTokens = false;
	
	/** Option: Whether or not to trace parsing execution. Default: false */
	protected boolean trace = false;
	
	/** Option: Whether or not to report diagnostic messages. Default: false */ 
	protected boolean diagnostics = false;
	
	/** Option: The encoding used by all input files. Default: system encoding. */
	protected String encoding = null;
	
	/**
	 * Option: Use the Simple LL(*) (SLL) parsing strategy. This uses a faster 
	 * but slightly weaker parsing strategy instead of the more powerful but
	 * potentially slower ALL(*) parsing strategy.
	 */
	protected boolean SLL = false;
	
	/** 
	 * Option: The path to the CSV structured timingsTable used to store the 
	 * regressionTestRig timings.
	 */
	protected String timingsTablePath = null;

	/**
	 * The lexer used by this grammar to break the input stream into tokens 
	 * recognized by the parser.
	 */
	protected Lexer lexer;
	
	/** The parser used to parse this grammer. */
	protected Parser parser;
	
	/** The parser's Java Class used to locate the requested grammar start rule. */
	protected Class<? extends Parser> parserClass;
	
	/** The TreePrinter intitalized for use with the loaded parser. */
	protected TreePrinter treePrinter;
	
	/**
	 * The (internal) timings table structure used to store the regressionTestRig 
	 * timings.
	 */
	protected ParserTimingsTable timingsTable;
	
	/**
	 * Constructs an instance of RegressionTestRig on the supplied command line
	 * argument strings.
	 * <p>
	 * To actually perform the parsing of the requested input files, invoke the 
	 * {@link #processInputFiles} method on this instance.
	 * <p>
	 * @throws various exceptions thrown by {@link #loadLexer} and {@link #loadParser}.
	 */
	public RegressionTestRig(String[] args) throws Exception {
	  this();
	  if (processArgs(args)) {
 		  loadLexer();
	    loadParser();
	  }
	}
	
	/**
	 * Constructs an instance of RegressionTestRig for testing purposes only.
	 */
	protected RegressionTestRig() {
	  timingsTable = new ParserTimingsTable();
	}
	
	/**
	 * Process the arguments for this use of the regression test rig.
	 *
	 * @param args The list of command line argument strings.
	 * @return     Returns true if there are enough required arguments and, 
	 *             if requested, the encoding is correctly specified.
	 *             Returns false otherwise.
	 */
	protected boolean processArgs(String[] args) {	  
		if ( args.length < 2 ) {
			System.err.println("java org.fandianpf.antlr4.RegressionTestRig GrammarName startRuleName\n" +
							   "  [-tokens] [-tree] [-encoding encodingname]\n"+
							   "  [-trace] [-diagnostics] [-SLL]\n"+
							   "  [-timings timingsTablePath]\n"+
							   "  [input-filename(s)]");
			System.err.println("Use startRuleName='tokens' if GrammarName is a lexer grammar.");
			System.err.println("Omitting input-filename makes rig read from stdin.");
			return false;
		}
		int i=0;
		grammarName = args[i];
		i++;
		startRuleName = args[i];
		i++;
		while ( i<args.length ) {
			String arg = args[i];
			i++;
			if ( arg.charAt(0)!='-' ) { // input file name
				inputFiles.add(arg);
				continue;
			}
			if ( arg.equals("-tree") ) {
				printTree = true;
			}
			if ( arg.equals("-tokens") ) {
				showTokens = true;
			}
			else if ( arg.equals("-trace") ) {
				trace = true;
			}
			else if ( arg.equals("-SLL") ) {
				SLL = true;
			}
			else if ( arg.equals("-diagnostics") ) {
				diagnostics = true;
			}
			else if ( arg.equals("-encoding") ) {
				if ( i>=args.length ) {
					System.err.println("missing encoding on -encoding");
					return false;
				}
				encoding = args[i];
				i++;
			}
			else if ( arg.equals("-timings") ) {
				if ( i>=args.length ) {
					System.err.println("missing timingsTablePath on -timings");
					return false;
				}
				timingsTablePath = args[i];
				i++;
			}
		}
		// If no inputFiles were specificed add our "Standard IN marker"
		if (inputFiles.size() < 1) inputFiles.add(null);
		
		return true;
	}

	/** 
	 * Load the lexer as requested by the command line arguments.
	 * <p>
	 * The {@link #lexer} variable may be null if no lexer could be loaded or
	 * instantiated.
	 * <p>
	 * @throws various exceptions while loading the lexer class and instantiating
	 *                 a lexer instance.
	 */
	protected void loadLexer() throws ClassNotFoundException, NoSuchMethodException, 
	  InstantiationException, IllegalAccessException, InvocationTargetException {
	    
		String lexerName = grammarName+"Lexer";
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Class<? extends Lexer> lexerClass = null;
		try {
			lexerClass = cl.loadClass(lexerName).asSubclass(Lexer.class);
		} catch (ClassNotFoundException anException) {
			// might be pure lexer grammar; no Lexer suffix then
			lexerName = grammarName;
			try {
				lexerClass = cl.loadClass(lexerName).asSubclass(Lexer.class);
			} catch (ClassNotFoundException anOtherException) {
				System.err.println("Can't load "+lexerName+" as lexer or parser");
				throw anOtherException;
			}
		}

		try {
		  Constructor<? extends Lexer> lexerCtor = lexerClass.getConstructor(CharStream.class);
		  lexer = lexerCtor.newInstance((CharStream)null);
		} catch (Exception anException) {
		  System.err.println("Could not create a lexer for "+lexerName);
		  throw anException;
		}
  }

  /**
   * Load the parser as requested by the command line arguments, and then setup
   * the parser for the 'diagnostics', 'printTree' or 'SLL' options.
   * <p>
   * The {@link #parser} and {@link #treePrinter} variables my be null if no 
   * parser was requested (that is, if the start rule name is the 
   * LEXER_START_RULE_NAME), or if the requested parser could not be loaded or
   * instantiated.
   * <p>
   * The {@link #treePrinter} variable may also be null if the printTree option 
   * has not been requested.
   * <p>
   * @throws various exceptions while loading the parser class and instantiating
   *                 a parser instance.
   */
  protected void loadParser() throws ClassNotFoundException, NoSuchMethodException, 
	  InstantiationException, IllegalAccessException, InvocationTargetException {
	    
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if ( !startRuleName.equals(LEXER_START_RULE_NAME) ) {
			String parserName = grammarName+"Parser";
			parserClass = null;
			try {
			  parserClass = cl.loadClass(parserName).asSubclass(Parser.class);
			}	catch (ClassNotFoundException cnfe) {
			  System.err.println("Can't load "+parserName+" as a parser");
			  throw cnfe;
			}

			try {
			  Constructor<? extends Parser> parserCtor = parserClass.getConstructor(TokenStream.class);
			  parser = parserCtor.newInstance((TokenStream)null);
			} catch (Exception anException) {
			  System.err.println("Could not create a parser for "+parserName);
			  throw anException;
			}
			
  		if ( diagnostics ) {
				parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
			}

			if ( printTree ) {
				parser.setBuildParseTree(true);
  			treePrinter = new TreePrinter("  ", parser);
			}

			if ( SLL ) { // overrides diagnostics
				parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			}
		}
  }

  /** The main entry point for the command line use of the regression test rig. */ 
	public static void main(String[] args) throws Exception {
		RegressionTestRig testRig = new RegressionTestRig(args);
 		testRig.processInputFiles();
	}
  
	/** Parse each requested input file in turn. */
	protected void processInputFiles() {

    if (timingsTablePath != null) try {
      timingsTable.loadTimingsTable(timingsTablePath);
    } catch (Exception exp) {
      System.err.println("Could not load the timingsTable from ["+timingsTablePath+"]");
    }
	  
		for (String inputFile : inputFiles) {
		  String outputFile = inputFile+".result";
		  if (inputFile!=null) {
		    System.err.println("RegressionTestRig: parsing ["+inputFile+"]");
		    System.err.println("     with reports going to ["+outputFile+"]");
		  } else {
		    System.err.println("RegressionTestRig: parsing stdin with reports going to stdout");
		  }
		  
			InputStream inputStream = System.in;
			PrintStream outputStream = System.out;
			try {
  			if ( inputFile!=null ) {
	  			inputStream = new FileInputStream(inputFile);
	  			try {
  		  		if (encoding!=null) {
    		  		outputStream = new PrintStream(outputFile, encoding);
		  		  } else {
    	  			outputStream = new PrintStream(outputFile);
	  		  	}
	  		  } catch (UnsupportedEncodingException usee) {
	  		    System.err.println("Could not use encoding: ["+encoding+"] using system default encoding.");
   	  			outputStream = new PrintStream(outputFile);
	  		  }
		  	}
		  } catch (FileNotFoundException fnfe) {
		    System.err.println("Could not open either input or output files");
		    continue;
		  }
		  
			Reader reader;
		  try {
  			if ( encoding!=null ) {
    			reader = new InputStreamReader(inputStream, encoding);
		    }	else {
			    reader = new InputStreamReader(inputStream);
		    }
		  } catch (UnsupportedEncodingException usee) {
		    System.err.println("Could not use encoding: ["+encoding+"] using system default encoding.");
		    reader = new InputStreamReader(inputStream);
		  }
		  try {
  		  Long[] timingResults = processAnInputFile(reader, outputStream);
  		  timingsTable.addLexerTiming(inputFile, timingResults[0]); // lexer
  		  timingsTable.addLexerTiming(inputFile, timingResults[1]); // parser
  		} catch (IOException ioe) {
	  	  System.err.println("Could not read: ["+inputFile+"]");
		  }

  		try {
			  if (reader!=null) reader.close();
  		  if (inputStream!=null && inputStream!= System.in) inputStream.close();
	  		if (outputStream!=null && outputStream!= System.out) outputStream.close();
	  	} catch (Exception anException) {
	  	  // not much more we can do ;-(
	  	}
		}
		
    if (timingsTablePath != null) try {
      timingsTable.saveTimingsTable(timingsTablePath);
    } catch (Exception exp) {
      System.err.println("Could not save the timingsTable into ["+timingsTablePath+"]");
    }
	}

	/** 
	 * Parse a single input file.
	 * <p>
	 * @param reader the {@link Reader} used to read the characters in the input 
	 *               file.
	 * @param writer the {@link PrintStream} used to print out the tokens,
	 *               diagnostic reports, and parse tree structure.
	 */
	protected Long[] processAnInputFile(Reader reader, PrintStream writer)
	  throws IOException { 
	
	  Long[] timingResults = { -1L, -1L };
	  
    PrintStreamErrorListener psErrorListener = new PrintStreamErrorListener(writer);

	  if (lexer==null) return timingResults;
	  if (reader==null) return timingResults;
  	  
    lexer.removeErrorListeners();
    lexer.addErrorListener(psErrorListener);

    ANTLRInputStream input = new ANTLRInputStream(reader);
    lexer.setInputStream(input);
  	CommonTokenStream tokens = new CommonTokenStream(lexer);

  	writer.println(PRINT_STREAM_BAR);
  	writer.println("Lexer tokenizing input");
  	writer.println(PRINT_STREAM_BAR);
  		
  	Long beforeMilliSeconds = System.currentTimeMillis();
	 	tokens.fill();
	 	Long afterMilliSeconds  = System.currentTimeMillis();
	 	timingResults[0] = afterMilliSeconds - beforeMilliSeconds;
 	  	
	  if ( showTokens ) {
   		writer.println(PRINT_STREAM_BAR);
   		writer.println("Lexer token stream");
   		writer.println(PRINT_STREAM_BAR);
    		
		  for (Object tok : tokens.getTokens()) {
   			writer.println(tok);
	 		}
	  }

	  if ( startRuleName.equals(LEXER_START_RULE_NAME) ) return timingResults;
  	if (parser==null) return timingResults;
	 	if (parserClass==null) return timingResults;

  	writer.println(PRINT_STREAM_BAR);
    writer.println("Parser building parse tree");
  	writer.println(PRINT_STREAM_BAR);
	  	
	  parser.removeErrorListeners();
	  parser.addErrorListener(psErrorListener);
  	if ( diagnostics ) {
  		parser.addErrorListener(new DiagnosticErrorListener());
 		}

    parser.setTokenStream(tokens);
  	parser.setTrace(trace);

	 	try {
		  Method startRule = parserClass.getMethod(startRuleName);
		  beforeMilliSeconds = System.currentTimeMillis();
  		ParserRuleContext tree = (ParserRuleContext)startRule.invoke(parser, (Object[])null);
	 	  afterMilliSeconds  = System.currentTimeMillis();
	 	  timingResults[1] = afterMilliSeconds - beforeMilliSeconds;
	  	  
	 		if ( printTree ) {
     		writer.println(PRINT_STREAM_BAR);
     		writer.println("Parser parse tree");
     		writer.println(PRINT_STREAM_BAR);
	  		writer.println(treePrinter.printTree(tree));
		  }
  	}	catch (Exception nsme) {
	 		System.err.println("No method for rule "+startRuleName+" or it has arguments");
	  }
		return timingResults;
	}

}
