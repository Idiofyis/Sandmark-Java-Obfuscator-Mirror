include ../../../Makedefs

SMARK_ROOT = ../../..
PACKAGE_DIR = sandmark/watermark/constantstring

FREE_CLASSES = ConstantString.class

include ../../../rules.inc

CP = $(SMEXTERN)/BCEL.jar:$(SMEXTERN)/bloat-1.0.jar:$(SMARK_ROOT)/sandmark.jar

IN = in.jar
OUT = out.jar
WM = 123456

test: Program.class
	jar cvf in.jar Program.class
	java -classpath $(CP) sandmark.watermark.constantstring.Test $(IN) $(OUT) $(WM)
