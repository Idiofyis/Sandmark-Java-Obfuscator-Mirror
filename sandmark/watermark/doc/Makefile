#  Makefile for GIF figures derived from FIG source
#
#  Run this makefile after updating any .fig files.
#  Note that it needs a version of fig2dev recent enough to understand GIF.

FIGS = Piracy.gif Watermarking.gif Fingerprint.gif Extract.gif

GIFS=$(FIGS:.fig=.gif)


.SUFFIXES:	.fig .gif

.fig.gif:	; fig2dev -Lgif -m 0.8 $< $@


#  Build all the GIFs

all:	$(GIFS)


#  Run this only if you're going to rebuild the GIFs

clean:	
	rm -f $(FIGS)
