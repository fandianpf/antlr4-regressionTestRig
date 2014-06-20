# Version history

## V0.3 2014/06/20

* use DecimalFormat to output StdDev to two decimal places in metrics csv file.
* added PrintStreamTraceListener to capture trace information in results files.
* improved PrintTree output to provide more helpful indenting strings which
  assist the visual lining up of the parse tree levels.
* improved PrintTree output to list line and character numbers for each symbol.
* improved PrintTree output to ensure ErrorNodes are noticed as ERRORs.
* added output of the RegressionTestRig arguments to the result files.

## V0.2 2014/06/13

* duplicate the directory hierarchy of the testDocs in the output directory.
* refactored timings table into a more general metrics table.
* changed the "-timings timingsTablePath" option to "-metrics metricsTablePath"
* removed TotalTimings metric.
* increased the number of grammar errors and warnings counts as part of the
  collection of metrics. We now collect the number of syntax errors, as well as 
  ambiguity, weak context, and strong context warnings. These metrics are 
  reported in the metricsTable. See PrintStreamErrorListenern in the htmlDocs
  for more detail.
* compute the number of lexer tokens, parser tree nodes, as well as the depth
  of the parse tree. These metrics are reported in the metricsTable

## V0.1 2014/06/12

* initial version
