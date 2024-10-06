#  Makefile for GIF figures derived from FIG source
#
#  Run this makefile after updating any .fig files.
#  Note that it needs a version of fig2dev recent enough to understand GIF.

FIGS = trace.gif embed.gif obfuscate.gif recognize.gif \
      Piracy.gif Watermarking.gif Obfuscation.gif \
      Fingerprint.gif Collusion.gif Extract.gif \
      TraceTab.gif EmbedTab.gif ObfuscateTab.gif \
      RecognizeTab.gif annotate.gif \
      yuma1.jpg yuma2.jpg yuma3.jpg slicing.jpg

GIFS=$(FIGS:.fig=.gif)


.SUFFIXES:	.fig .gif

.fig.gif:	; fig2dev -Lgif -m 0.8 $< $@


#  Build all the GIFs

all:	$(GIFS)

EmbedTab.fig:   	embed-tab.gif
ObfuscateTab.fig:       obfuscate-tab.gif
RecognizeTab.fig:       recognize-tab.gif
TraceTab.fig:   	trace-tab.gif


#  Run this only if you're going to rebuild the GIFs

clean:	
	rm -f $(FIGS)
