#  Makefile for GIF figures derived from FIG source
#
#  Run this makefile after updating any .fig files.
#  Note that it needs a version of fig2dev recent enough to understand GIF.

FIGS = trace.gif embed.gif recognize.gif \
      Extract.gif \
      TraceTab.gif EmbedTab.gif  \
      RecognizeTab.gif annotate.gif

GIFS=$(FIGS:.fig=.gif)


.SUFFIXES:	.fig .gif

.fig.gif:	; fig2dev -Lgif -m 0.8 $< $@


#  Build all the GIFs

all:	$(GIFS)

EmbedTab.fig:   	embed-tab.gif
RecognizeTab.fig:       recognize-tab.gif
TraceTab.fig:   	trace-tab.gif


#  Run this only if you're going to rebuild the GIFs

clean:	
	rm -f $(FIGS)
