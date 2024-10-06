include ../../Makedefs

SMARK_ROOT = ../..
PACKAGE_DIR = sandmark/smash

FREE_CLASSES = Smash.class \
	SandmarkCLI.class \

default:  all
	( cd $(SMARK_ROOT) ; \
	   zip -u $(SMJAR) $(PACKAGE_DIR)/*.smash || echo "nothing to zip" )

include ../../rules.inc
