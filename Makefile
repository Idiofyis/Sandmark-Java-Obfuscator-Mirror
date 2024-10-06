#  Makefile for SandMark v3
#
#  Configuration options are defined in ./Makedefs (q.v.).


include Makedefs

SMARK_ROOT = .
PACKAGE_DIR = .

HT = sandmark/html
FREE_DIRS=sandmark
API_DIR = /cs/www/sandmark/API

default:  classfiles $(SMJAR)

all:      classfiles $(SMJAR) 
freejar: clean free-classes $(SMJAR)

$(SMJAR):  FORCE
	rm -f Algorithms.txt
	zip -d $(SMJAR) Algorithms.txt >/dev/null 2>&1 | echo ok > /dev/null
	CLASSPATH=$(ALLJARS) $(JAVA) -DSMARK_PATH=sandmark.jar \
	    sandmark.util.classloading.BuildClassList Algorithms.txt
	zip -u $(SMJAR) Algorithms.txt
	$(JAR) uvf $(SMJAR) sandmark/wizard/modeling/dfa/Weights.txt
	chmod +x $(SMJAR)

jar: classfiles $(SMJAR)

unjared:	
	mkdir unjared ; cd unjared ; \
	for i in $(subst :, ,$(LIBIMPORTS)); do jar xf $$i ; done ; \
	rm -rf META-INF/

superjar: jar unjared
	$(JAR) uf $(SMJAR) -C unjared/ .
	$(JAR) i $(SMJAR)

classfiles::
	$(MAKE) -C sandmark all
	$(MAKE) -C junit all

free-classes::
	$(MAKE) -C sandmark free
	$(MAKE) -C junit all

.newweb:  FORCE
	cp -p `ls -t $(HT)/*.html $(HT)/*.gif $(HT)/*.jpg | sed 1q` .newweb

run:
	CLASSPATH=$(ALLJARS) $(JAVA) -Xmx100m sandmark.gui.SandMarkFrame

runlite:
	$(JAVA) -cp $(ALLJARS) -Xmx100m sandmark.gui.SandMarkLiteFrame

docs:
	cd doc; $(MAKE)

BINDIR = $(INSTALLDIR)/SandMark$(VERSION)

ifeq ($(strip $(VERSION)),)
   VERSION = $(shell date "+%Y%m%d")
endif

install-dir:
	mkdir -p $(BINDIR)

install-source: install-dir clean
	rm -f $(BINDIR)/SandMark-source-$(VERSION).zip
	( cd .. ; zip -r $(BINDIR)/SandMark-source-$(VERSION).zip smark3 )

install-bin: install-dir jar
	cp -f $(SMJAR) $(BINDIR)/SandMark-$(VERSION).jar

install-support: install-dir
	cp -f README $(BINDIR)
	cp -f ../smbin3/smark.std $(BINDIR)/smark

install: install-source install-bin install-support


# build ("make jdoc") and install ("make install-jdoc") Javadoc pages

LOGO = <img src=http://www.cs.arizona.edu/sandmark/logo.jpg>
LABEL = SandMark

version: $(SMJAR)
	CLASSPATH=$(ALLJARS) $(JAVA) -Xmx100m \
	    sandmark.Constants version > version

jdoc: FORCE version
	CLASSPATH=.:$(ALLJARS) $(JDK)/bin/javadoc -d jdoc \
	    -quiet -linksource -breakiterator -author \
	    -source 1.4 \
	    -overview copyright.html \
	    -subpackages sandmark \
	    -exclude sandmark.eclipse \
	    -exclude sandmark.watermark.gtw.eigen \
	    -link http://java.sun.com/j2se/1.4/docs/api \
	    -link http://jakarta.apache.org/bcel/apidocs \
	    -stylesheetfile stylesheet.css \
	    -windowtitle "$(LABEL)" \
	    -header "$(LOGO)<p>$(LABEL) `cat version`" \
	    -footer "$(LABEL)<p>Generated `date +%-d-%b-%Y` by `whoami`"

install-jdoc:
	test -d jdoc || $(MAKE) jdoc
	-test -d $(API_DIR)/`cat version` && rm -rf $(API_DIR)/`cat version`
	mkdir -p $(API_DIR)/`cat version`
	cp -r jdoc/* $(API_DIR)/`cat version`
	chmod -R g+rwx $(API_DIR)/`cat version`
	chmod -R o+rx $(API_DIR)/`cat version`

TAGS:	FORCE
	etags --members `find . -name '*.java'`

clean::
	rm -rf jdoc version $(SMJAR) .newweb Algorithms.txt unjared
	$(MAKE) -C sandmark clean
	$(MAKE) -C junit clean

FORCE:

count:
	wc `find . -name \*.java`

################################# CD BURNING ###############################
SCRATCH=/scratch
EXPORT=export
PSDOCS = $(HOME)/smark3/doc/hackingsm.ps \
          $(HOME)/smark3/doc/smalgs.ps  \
          $(HOME)/smark3/doc/userguide.ps 
PDFDOCS = $(HOME)/smark3/doc/hackingsm.pdf \
          $(HOME)/smark3/doc/smalgs.pdf  \
          $(HOME)/smark3/doc/userguide.pdf
DOCS = $(PSDOCS) $(PDFDOCS)

API.zip:
	-/bin/rm -r ${SCRATCH}/API
	cp -r ${API_DIR} ${SCRATCH}/API
	-/bin/rm ${SCRATCH}/API/.htaccess
	cd ${SCRATCH}; zip -r -9 API.zip API
	mv ${SCRATCH}/API.zip .

# Create export.zip and export.tgz which contain all sources
# (except doc), sandmark.jar, TTT.jar, and documentation (in pdf).
export: superjar docs API.zip
	-/bin/rm -rf $(EXPORT)
	cvs export -D now -d $(EXPORT) -N smark3
	/bin/cp sandmark.jar $(EXPORT) 
	cp $(BCEL) $(BLOAT) $(EXPORT)
	cp ../smapps3/TTT.jar $(EXPORT)
	cp API.zip $(EXPORT)
	/bin/rm -rf $(EXPORT)/smark3/doc
	/bin/mv $(EXPORT)/smark3/README \
                $(EXPORT)/smark3/smarkstd.bat \
                $(EXPORT)/smark3/smark.std \
                $(EXPORT)
	cp $(PDFDOCS) $(EXPORT)
	zip -r -9 export.zip $(EXPORT) 
	tar cvfz export.tgz  $(EXPORT) 

burn_cvs:
	-/bin/rm -rf $(SCRATCH)/wmark.iso
	mkisofs -T -l -o $(SCRATCH)/wmark.iso -r /cs/cvs/wmark
	cdrecord -v speed=4 dev=0,0 $(SCRATCH)/wmark.iso

burn_src:
	-/bin/rm -rf $(SCRATCH)/smark.iso 
	-/bin/rm -rf $(SCRATCH)/sm*.zip 
	cd $(HOME); zip -r $(SCRATCH)/smark3.zip smark3 -x '*/CVS/*'
	cd $(HOME); zip -r $(SCRATCH)/smextern3.zip smextern3 -x '*/CVS/*'
	cd $(HOME); zip -r $(SCRATCH)/smapps3.zip smapps3 -x '*/CVS/*'
	cd $(HOME); zip -r $(SCRATCH)/smtest3.zip smtest3 -x '*/CVS/*'
	cd $(HOME); zip -r $(SCRATCH)/smbin3.zip smbin3 -x '*/CVS/*'
	cd $(HOME); cp API.zip $(SCRATCH)
	cp $(HOME)/smark3/README-CD-SRC $(SCRATCH)/README
	cp $(HOME)/smark3/README-AFRL $(SCRATCH)
	cp $(DOCS) $(SCRATCH)/
	cp $(HOME)/smark3/sandmark.jar $(SCRATCH)/
	mkisofs -J -T -l -m CVS -o $(SCRATCH)/smark.iso -r \
                $(SCRATCH)/sandmark.jar \
                $(SCRATCH)/hackingsm.ps \
                $(SCRATCH)/smalgs.ps  \
                $(SCRATCH)/userguide.ps \
                $(SCRATCH)/hackingsm.pdf \
                $(SCRATCH)/smalgs.pdf  \
                $(SCRATCH)/userguide.pdf \
                $(SCRATCH)/README \
                $(SCRATCH)/README-AFRL \
                $(SCRATCH)/smark3.zip \
                $(SCRATCH)/smextern3.zip \
                $(SCRATCH)/smapps3.zip \
                $(SCRATCH)/smtest3.zip \
                $(SCRATCH)/API.zip \
                $(SCRATCH)/smbin3.zip
	cdrecord -v speed=4 dev=0,0 $(SCRATCH)/smark.iso
