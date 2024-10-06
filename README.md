# Sandmark-Java-Obfuscator-Mirror
A mirror for the abandonded [Sandmark Java Obfuscator](http://sandmark.cs.arizona.edu/index.html).

## Project Description

SandMark is a tool developed at the University of Arizona for software
watermarking, tamper-proofing, and code obfuscation of Java
bytecode. The ultimate goal of the project is to implement and study
the effectiveness of all known software protection algorithms.

Running Sandmark
----------------

Note that you will need Java 1.4 to run SandMark properly.
Get it from:
   http://java.sun.com/j2se/1.4/

To run Sandmark under Windows, copy smarkstd.bat to smark.bat and edit
it to include the correct path to JDK1.4.  Double click this batch
file or type smark on the command line to run sandmark.

To run Sandmark under Unix, copy smark.std to smark and edit it to
include the correct path to JDK1.4.  Run this script to run sandmark.

Trying it out
-------------

To get started try watermarking the TTT (tic-tac-toe)
application:
   1) In the 'trace' pane enter
         "Jar-file to waternmark:" TTT.jar
         "Main class name:"        TTTApplication
   2) Hit <START>
   3) Click on a few X's and O's.
   4) Hit <DONE>
   5) In the 'embed' pane enter:
         "Watermark value:" 123456
   6) Hit <EMBED>
   7) Go to the 'recognize' pane and hit the <START> button.
   8) Click on the same X's and O's as you did in step 3),
      in the same order.
   9) Hit <DONE>.

You should see the watermark '123456' extracted from the 
watermarked TTT application.

Building Sandmark from source
-----------------------------

Do the following to build sandmark:
> cd smark
> cp Makedefs.std Makedefs
# Make the 'obvious' changes to Makedefs.
# In particular, you should set these variables:
#   JDK =
#   HOME = 

make superjar
java -jar sandmark.jar

## Website Description Mirror

SandMark is a tool developed at the University of Arizona for software watermarking, tamper-proofing, and code obfuscation of Java bytecode. The ultimate goal of the project is to implement and study the effectiveness of all known software protection algorithms. Currently, the tool incorporates several dynamic and static watermarking algorithms (such as those proposed by Venkatesan, Collberg, Stern, and others), a large collection of obfuscation algorithms, a code optimizer, and tools for viewing and analyzing Java bytecode.
* Software watermarking algorithms can be used to embed a customer identification number (a fingerprint) into a Java program in order to trace software pirates. A SandMark software watermarking algorithm consists of two programs:
    * The embedder takes a Java jar-file and a string (the watermark) as input and produces the a new jar-file that embeds the string.
    * The recognizer takes the watermarked jar-file as input and produces the watermark string as output.
Typically, the watermark is a copyright notice or a customer identification number.
* The code obfuscation algorithms in SandMark take a Java jar-file as input and produce an obfuscated jar-file as output. They have many applications:
    * The obfuscations can be used to protect the intellectual property of Java programs (by rendering the code difficult to understand).
    * Obfuscations can protect fingerprinted programs from collusive attacks (by making differently fingerprinted program differ everywhere, not just in the parts where the mark is embedded).
    * Obfuscations can also be used to attack software watermarks (by reorganizing the code such that the mark can no longer be recognized).
SandMark is designed to be simple to use. A graphical user interface allows novices to easily try out watermarking and obfuscation algorithms. Algorithms can be combined, the resulting watermarked and/or obfuscated code can be examined, and attacks can be easily launched. SandMark has been designed using a plugin-style architecture which makes it easy to extend with additional algorithms.

We are currently using SandMark to study which software watermarking algorithms are vulnerable to which code optimizations and code obfuscations. We are also interested in evaluating the effectiveness and performance overhead of obfuscation algorithms.

 

 

The development of SandMark is supported by the NSF under grant CCR-0073483, the AFRL under contract F33615-02-C-1146, and by the New Economy Research Fund of New Zealand under contracts UOAX9906 and UOAX0214.
