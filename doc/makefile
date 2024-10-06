SECTIONS = \
   introduction.tex \
   installation.tex \
   scripting.tex\
   codingStandard.tex\
   tools.tex \
   usingCVS.tex\
   sandmarkPackages.tex\
   CollbergThomborson.tex\
   RenameFieldsMethods.tex\
   ConstantString.tex\
   bogus_switch.tex\
   BogusExpression.tex\
   AddMethField.tex\
   HatTrick.tex\
   Degrade.tex\
   OpaquePredicates.tex \
   ROW.tex\
   obfuscation.tex \
   BogusInitializer.tex\
   ClassSplitting.tex \
   BogusPredicates.tex\
   watermarking.tex \
   codec.tex \
   documentation.tex \
   BCEL.tex \
   BLOAT.tex \
   BoolSplitter.tex \
   nodesplitter.tex

FIGS = \
   EEPIC/CTexample2.eepic \
   EEPIC/Embed.eepic \
   EEPIC/cvs.eepic \
   EEPIC/split.eepic \
   EEPIC/CTexample1.eepic \
   EEPIC/CToverview.eepic \
   EEPIC/phases.eepic \
   EEPIC/dep.eepic \
   EEPIC/CTalgorithm.eepic

manuals.ps: \
   getpackagesum \
   userguide.ps \
   hackingsm.ps \
   smalgs.ps \
   userguide.pdf \
   hackingsm.pdf \
   smalgs.pdf 
   

getpackagesum:
	./getPackageInfo

userguide.ps: userguide.dvi 
	dvips  -Pcmz -Pamz -o userguide.ps userguide.dvi

userguide.pdf: userguide.ps 
	ps2pdf userguide.ps userguide.pdf

userguide.dvi: userguide.tex ${SECTIONS} ${FIGS}
	cd PS; $(MAKE)
	latex userguide.tex

hackingsm.ps: hackingsm.dvi
	dvips -Pcmz -Pamz -o hackingsm.ps hackingsm.dvi

hackingsm.pdf: hackingsm.ps 
	ps2pdf hackingsm.ps hackingsm.pdf

hackingsm.dvi: hackingsm.tex ${SECTIONS} ${FIGS}
	cd PS; $(MAKE)
	latex hackingsm.tex

smalgs.pdf: smalgs.ps 
	ps2pdf smalgs.ps smalgs.pdf

smalgs.ps: smalgs.dvi
	dvips  -Pcmz -Pamz -o smalgs.ps smalgs.dvi

smalgs.dvi: smalgs.tex ${SECTIONS} ${FIGS}
	cd PS; $(MAKE)
	latex smalgs.tex

EEPIC/cvs.eepic: EEPIC FIGS/cvs.fig
	fig2dev -L eepic -m 0.6 FIGS/cvs.fig > EEPIC/cvs.eepic

EEPIC/split.eepic: EEPIC FIGS/split.fig
	fig2dev -L eepic -m 0.6 FIGS/split.fig > EEPIC/split.eepic

EEPIC/CTexample1.eepic: EEPIC FIGS/CTexample1.fig
	fig2dev -L eepic -m 0.6 FIGS/CTexample1.fig > EEPIC/CTexample1.eepic

EEPIC/CTexample2.eepic: EEPIC FIGS/CTexample2.fig
	fig2dev -L eepic -m 0.6 FIGS/CTexample2.fig > EEPIC/CTexample2.eepic

EEPIC/Embed.eepic: FIGS/Embed.fig
	fig2dev -Leepic -m 0.8 FIGS/Embed.fig > EEPIC/Embed.eepic

EEPIC/CToverview.eepic: FIGS/CToverview.fig
	fig2dev -Leepic -m 0.8 FIGS/CToverview.fig > EEPIC/CToverview.eepic

EEPIC/CTalgorithm.eepic: FIGS/CTalgorithm.fig
	fig2dev -Leepic -m 0.8 FIGS/CTalgorithm.fig > EEPIC/CTalgorithm.eepic

EEPIC/phases.eepic: FIGS/phases.fig
	fig2dev -Leepic -m 0.8 FIGS/phases.fig > EEPIC/phases.eepic

EEPIC/dep.eepic: FIGS/dep.fig
	fig2dev -Leepic -m 0.8 FIGS/dep.fig > EEPIC/dep.eepic

EEPIC:
	-mkdir EEPIC

again:  clean
	$(MAKE)

clean:
	rm -f *.aux *.log *.toc *.dvi *.ps *.pdf PS/*.ps
	rm -rf EEPIC new-package-list packageSummary.tex
