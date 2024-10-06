DIRS=$(NONFREE_DIRS) $(FREE_DIRS)
CLASSES=$(NONFREE_CLASSES) $(FREE_CLASSES)
SHELL=/bin/bash

HT=sandmark/html
SMJAR=sandmark.jar

ifeq ($(strip $(SMARK_ROOT)),)
   ifneq ($(strip $(PACKAGE_DIR)),)
      SMARK_ROOT = $(shell echo $(PACKAGE_DIR) | sed 's/[^\/][^\/]*/../g')
   endif
endif

all: $(CLASSES) doc
ifneq ($(strip $(CLASSES)),)
	( cd $(SMARK_ROOT) ; \
	   zip -u $(SMJAR) $(PACKAGE_DIR)/*.class || echo "nothing to zip" )
endif
ifneq ($(strip $(DIRS)),)
	set -e ; for i in $(DIRS); do $(MAKE) -C $$i ; done
endif

free: $(FREE_CLASSES) doc
ifneq ($(strip $(FREE_CLASSES)),)
	( cd $(SMARK_ROOT) ; \
	   zip -u $(SMJAR) $(PACKAGE_DIR)/*.class || echo "nothing to zip" )
endif
ifneq ($(FREE_DIRS),)
	set -e ; for i in $(FREE_DIRS); do $(MAKE) -C $$i free ; done
endif

doc: sandmark-jar
	( cd $(SMARK_ROOT) ; \
	   zip -u $(SMJAR) `find $(PACKAGE_DIR)/doc -type f -maxdepth 1` || \
	   echo "no new docs" )

%.class: %.java
	set -e ; CLASSPATH=$$CLASSPATH:$(SMARK_ROOT):$(IMPORTS) $(JC) $^

sandmark-jar::
	set -e ; \
	if [ -e $(SMARK_ROOT)/$(SMJAR) ]; then \
	   echo ; \
	else \
	   cd $(SMARK_ROOT) ; \
	   $(JAR) cfm $(SMJAR) mainclass.mft \
	      $(HT)/*.html $(HT)/*.gif \
	      $(HT)/*.jpg ; \
	   cd $(PACKAGE_DIR) ; \
	fi

clean:
	rm -f *.class
ifneq ($(strip $(DIRS)),)
	for i in $(DIRS); do $(MAKE) -C $$i clean ; done
endif
