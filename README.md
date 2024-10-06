# Sandmark-Java-Obfuscator-Mirror
A mirror for the abandonded [Sandmark Java Obfuscator](http://sandmark.cs.arizona.edu/index.html).

## Description Mirror

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
