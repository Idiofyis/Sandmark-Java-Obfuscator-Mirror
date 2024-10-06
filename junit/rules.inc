SHELL=/bin/bash

ifeq ($(strip $(JUNIT_ROOT)),)
   ifneq ($(strip $(PACKAGE_DIR)),)
      JUNIT_ROOT = $(shell echo $(PACKAGE_DIR) | sed 's/[^\/][^\/]*/../g')
   endif
endif

SMARK_ROOT = $(JUNIT_ROOT)/..

all: $(CLASSES)
ifneq ($(strip $(DIRS)),)
	set -e ; for i in $(DIRS); do $(MAKE) -C $$i ; done
endif

%.class: %.java
	set -e ; CLASSPATH=$$CLASSPATH:$(SMARK_ROOT):$(IMPORTS):$(JUNIT) $(JC) $^

clean:
	rm -f *.class
ifneq ($(strip $(DIRS)),)
	for i in $(DIRS); do $(MAKE) -C $$i clean ; done
endif
