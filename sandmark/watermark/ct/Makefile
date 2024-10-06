include ../../../Makedefs

SMARK_ROOT = ../../..
PACKAGE_DIR = sandmark/watermark/ct

FREE_DIRS = embed encode recognize trace

FREE_CLASSES = CT.class Evaluate.class FindWindows.class

include ../../../rules.inc

evaluate:
	cd ../../..; java -classpath sandmark.jar sandmark.watermark.ct.Evaluate > result

postscript:
	gawk -f eval2jgr.awk ../../../result
	jgraph wm2bytecode.jgr > wm2bytecode.ps
	jgraph wm2heap.jgr > wm2heap.ps
	jgraph wm2bytecode2components.jgr > wm2bytecode2components.ps
	jgraph wm2cycled.jgr > wm2cycled.ps

