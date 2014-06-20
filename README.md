# antlr4-regressionTestRig

An ANTLR4 tool, derived from the existing ANTLR4 TestRig, which has been
redesigned for long term regression testing of a large collection of example
documents.

## Description

This RegressionTestRig is based upon the existing ANTLR4 TestRig as distributed
in the ANTLR4 runtime jar.  

However this RegressionTestRig has been altered to ensure:

* Multiple input files are *expected*. This allows you to keep track of the
parser's performance over a large collection of example documents.

* As much output as possible can be captured into a results file associated with
each input file. This enables later processing or diffing.

* The Lexer and Parser steps are both timed in milliseconds.

* Various types of metrics, such as lexer and parser timings and error/warnings,
are collected and recorded to a CSV file which has one row per input file for
each type of metric. This allows you to identify how changes in the grammar,
which might, for example, change the pattern of backtracking, impact a large
collection of examples.

* The -tree output has been restructured so that each parse tree rule node is on
its own line. This allows for more convenient differencing of the output against
a "working" example for a given input file. With this new structure, the
standard unix diff command, for example, will more easily identify exactly where
in the parse tree, changes have occured.

## Use

To use this tool you need *both* the [ANTLR
runtime](http://www.antlr.org/download.html) and the [jar file for this
project](http://fandianpf.github.io/antlr/antlr4-regressionTestRig.html).

Place both of these jar files into an appropriate location on your computer (for
example):

    /usr/local/Java/lib
    
Then place the following into a (unix) shell script called
"antlr4regressionTest":

    #!/bin/sh
    LOCAL_JARS=/usr/local/Java/lib
    ANTLR_JAR=$LOCAL_JARS/antlr-runtime-4.2.2.jar
    TEST_RIG_JAR=$LOCAL_JARS/antlr4-regressionTestRig-0.1.jar
    java -cp .:$TEST_RIG_JAR:$ANTLR_JAR:$CLASSPATH \
      org.fandianpf.antlr4.regressionTestRig.RegressionTestRig $*

(or alter the version found in bin/antlr4regressionTest).

Then once you have used the antlr4 and javac tools to create and compile the
grammar, you can run the regressionTestRig tool as follows:

    antlr4regressionTest <<fully qualified grammar name>> <<start rule name>> \
      [options] <<a list of test documents>>

### Options

The first two options MUST be the fully qualified grammar name followed by the
name of the starting rule.

The following options are accepted:

* *-tokens* records the token stream in the output of the lexer

* *-tree* records the parser tree in a "python" format (one rule/node per line
indented to reflect the tree structure).

* *-trace* records the parser's actions as it builds the parse tree. (At the
moment this trace is sent directly to the System.out).

* *-diagnostics* diagnostic warning messages are captured to the result file.

* *-primaryIndent primaryIndentString* the (primary) string used to indent the
parse tree output for each recursive level.

* *-secondaryIndent secondaryIndentString* the (secondary) string used to indent
the parse tree output for each recursive level.

* *-indentCycle indentCyclePeriod* the last indent in each indent cycle will use
the secondary indent string (rather than the primary indent string). This makes 
it easier to determe the recursive depth of the parse tree at any one node.

* *-encoding encodingName* read the input file using the encoding provided by
"encodingname".

* *-metrics metricsTablePath* (version 0.2) load and save the lexer and parser
metrics to/from the filesystem file located at "metricsTablePath".

* *-timings timingsTablePath* (version 0.1) load and save the lexer and parser
metrics to/from the filesystem file located at "timingsTablePath".

* *-sourceDir sourceDirPath* the name of each testDoc in the metricsTable will
have this "sourceDirPath" removed from the begining of the path provided for
each input file.

* *-outputDir outputDirPath* the results file will be the testDoc name prefixed
with the "outputDirPath" and with ".results" appended to the end.

* a list of *input file paths*. Each input file path will be parsed using the
grammar specified above. The path to the corresponding result file will be the
input file path with any "sourceDirPath" prefix removed, and with any
"outputDirPath" prefixed, and with ".results" appended to the end.

If no input file is provided, the System.in will be parsed and the results will
be sent to System.out.

# License

[The "BSD license"]
 Copyright (c) 2014, FandianPF (Stephen Gaito)
   Some files are based on those found in org.antlr.v4.runtime
     (on GitHub antlr/antlr4 project on 2014/06/06)
   these files have been noted and have the additional copyrights: 
     Copyright (c) 2012 Terence Parr
     Copyright (c) 2012 Sam Harwell

 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer.
    
 * Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution.
    
 * The name of the author may not be used to endorse or promote products derived
 from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
 IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
 OF SUCH DAMAGE.


