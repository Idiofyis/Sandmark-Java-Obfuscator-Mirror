all: main.ps embed.ps recognize.ps trace.ps

main.ps: main.ps.gz
	gunzip -c main.ps.gz > main.ps
trace.ps: trace.ps.gz
	gunzip -c trace.ps.gz > trace.ps
recognize.ps: recognize.ps.gz
	gunzip -c recognize.ps.gz > recognize.ps
embed.ps: embed.ps.gz
	gunzip -c embed.ps.gz > embed.ps

