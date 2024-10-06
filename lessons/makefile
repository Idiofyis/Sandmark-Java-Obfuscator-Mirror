SECTIONS = bcel.tex\
	bloat.tex\
	sm.tex\
	cvs.tex

FIGS = \

lessons.ps: lessons.dvi
	dvips -o lessons.ps lessons.dvi

lessons.dvi: lessons.tex ${SECTIONS} ${FIGS}
	latex lessons.tex

EEPIC:
	-mkdir EEPIC

again:
	-/bin/rm lessons.ps lessons.dvi 
	$(MAKE)

clean:
	-/bin/rm lessons.ps lessons.dvi *.aux 
