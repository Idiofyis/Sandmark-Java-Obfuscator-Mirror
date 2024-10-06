BEGIN {
   ALIAS["sandmark.util.newgraph.codec.RadixGraph"] = "radix"
   ALIAS["sandmark.util.newgraph.codec.PermutationGraph"] = "perm"
   ALIAS["sandmark.util.newgraph.codec.ReduciblePermutationGraph"] = "reducible"
   ALIAS["sandmark.util.newgraph.codec.PlantedPlaneCubicTree"] = "PPCT"

   linetype[0]="linetype none"
   linetype[1]="linetype solid"
   linetype[2]="linetype dotted" 
   linetype[3]="linetype dashed" 
   linetype[4]="linetype longdash" 
   linetype[5]="linetype dotdash"
   linetype[6]="linetype dotdotdash"
   linetype[7]="linetype dotdotdashdash"

   marktype[0]="marktype none"
   marktype[1]="marktype circle"
   marktype[2]="marktype box"
   marktype[3]="marktype cross"
   marktype[4]="marktype triangle"
   marktype[5]="marktype diamond"
   marktype[6]="marktype x"
   marktype[7]="marktype ellipse"

   JAVA_OBJECT_OVERHEAD = 8
}

/^CODEC/ {
   CODEC=ALIAS[$2]; 
   CYCLE=$3;
   next}
/^COMPONENTS/ {
   COMPONENTS=$2+0; next}
/^WATERMARK/ {
   WATERMARK=$2; next}
/^NODECOUNT/ {
   NODECOUNT=$2+0; 
   HEAPSIZE = NODECOUNT * (JAVA_OBJECT_OVERHEAD + 2*4)
   next}
/^EDGECOUNT/ {
   EDGECOUNT=$2+0; next}
/^METHODSIZES/ {
   METHODSIZE=0;
   MIN_METHODSIZE=$2+0;
   MAX_METHODSIZE=$2+0;
   for(i=2;i<=NF;i++) {
     METHODSIZE+=$i+0
     if ($i > MAX_METHODSIZE) MAX_METHODSIZE = $i
     if ($i < MIN_METHODSIZE) MIN_METHODSIZE = $i
   }
   AVG_METHODSIZE=METHODSIZE/COMPONENTS;

   if ((CYCLE == "plain") && (COMPONENTS==1))  {
      bcSize[CODEC] = bcSize[CODEC] ":" WATERMARK " " METHODSIZE
      graphSize[CODEC] = graphSize[CODEC] ":" WATERMARK " " HEAPSIZE
   }

   if ((CYCLE == "plain") && (COMPONENTS>1) && (CODEC == "radix")) {
      bcSizeRadix[COMPONENTS] = bcSizeRadix[COMPONENTS] ":" WATERMARK " " METHODSIZE
   }

   if ((CYCLE == "plain") && (CODEC == "radix")) {
      bcSizeRadixAvg[COMPONENTS] = bcSizeRadixAvg[COMPONENTS] ":" WATERMARK " " AVG_METHODSIZE " " MIN_METHODSIZE " " MAX_METHODSIZE
   }

   if ((CYCLE == "cycle") && (COMPONENTS==1) && (CODEC == "radix"))  {
      graphSizeCycle[CODEC] = graphSizeCycle[CODEC] ":" WATERMARK " " HEAPSIZE
   }

   if ((CYCLE == "cycle") && (CODEC == "radix")) {
      bcSizeRadixCycle[COMPONENTS] = bcSizeRadixCycle[COMPONENTS] ":" WATERMARK " " METHODSIZE
   }

}

function hashLabels() {
   S = ""
   S = S "no_auto_hash_labels\n"
   S = S "hash_label at 16 : 2^4\n"
   # S = S "hash_label at 32 : 2^5\n"
    S = S "hash_label at 64 : 2^6\n"
   # S = S "hash_label at 128 : 2^7\n"
    S = S "hash_label at 256 : 2^8\n"
   # S = S "hash_label at 512 : 2^9\n"
    S = S "hash_label at 1024 : 2^10\n"
   # S = S "hash_label at 2048 : 2^11\n"
    S = S "hash_label at 4096 : 2^12\n"
   # S = S "hash_label at 8192 : 2^13\n"
    S = S "hash_label at 16384 : 2^14\n"
   # S = S "hash_label at 32768 : 2^15\n"
    S = S "hash_label at 65536 : 2^16\n"
   # S = S "hash_label at 131072 : 2^17\n"
    S = S "hash_label at 262144 : 2^18\n"
   # S = S "hash_label at 524288 : 2^19\n"
    S = S "hash_label at 1048576 : 2^20\n"
   # S = S "hash_label at 2097152 : 2^21\n"
    S = S "hash_label at 4194304 : 2^22\n"
   # S = S "hash_label at 8388608 : 2^23\n"
    S = S "hash_label at 16777216 : 2^24\n"
   # S = S "hash_label at 33554432 : 2^25\n"
    S = S "hash_label at 67108864 : 2^26\n"
   # S = S "hash_label at 134217728 : 2^27\n"
    S = S "hash_label at 268435456 : 2^28\n"
   # S = S "hash_label at 536870912 : 2^29\n"
    S = S "hash_label at 1073741824 : 2^30\n"
   # S = S "hash_label at 2147483648 : 2^31\n"
    S = S "hash_label at 4294967296 : 2^32\n"
   # S = S "hash_label at 8589934592 : 2^33\n"
    S = S "hash_label at 17179869184 : 2^34\n"
   # S = S "hash_label at 34359738368 : 2^35\n"
    S = S "hash_label at 68719476736 : 2^36\n"
   # S = S "hash_label at 137438953472 : 2^37\n"
    S = S "hash_label at 274877906944 : 2^38\n"
   # S = S "hash_label at 549755813888 : 2^39\n"
    S = S "hash_label at 1099511627776 : 2^40\n"
   # S = S "hash_label at 2199023255552 : 2^41\n"
    S = S "hash_label at 4398046511104 : 2^42\n"
   # S = S "hash_label at 8796093022208 : 2^43\n"
    S = S "hash_label at 17592186044416 : 2^44\n"
   # S = S "hash_label at 35184372088832 : 2^45\n"
    S = S "hash_label at 70368744177664 : 2^46\n"
   # S = S "hash_label at 140737488355328 : 2^47\n"
    S = S "hash_label at 281474976710656 : 2^48\n"
   # S = S "hash_label at 562949953421312 : 2^49\n"
    S = S "hash_label at 1125899906842624 : 2^50\n"
   # S = S "hash_label at 2251799813685248 : 2^51\n"
    S = S "hash_label at 4503599627370496 : 2^52\n"
   # S = S "hash_label at 9007199254740992 : 2^53\n"
    S = S "hash_label at 18014398509481984 : 2^54\n"
   # S = S "hash_label at 36028797018963968 : 2^55\n"
    S = S "hash_label at 72057594037927936 : 2^56\n"
   # S = S "hash_label at 144115188075855872 : 2^57\n"
    S = S "hash_label at 288230376151711744 : 2^58\n"
   # S = S "hash_label at 576460752303423488 : 2^59\n"
    S = S "hash_label at 1152921504606846976 : 2^60\n"
   # S = S "hash_label at 2305843009213693952 : 2^61\n"
    S = S "hash_label at 4611686018427387904 : 2^62\n"
   # S = S "hash_label at 9223372036854775808 : 2^63\n"
    S = S "hash_label at  18446744073709551616 : 2^64\n"
   return S
}

function newgraph (xlabel, ylabel, legendx, legendy, comment) {
   S = "\n" comment "\n\n"
   S = S "newgraph" "\n"
   S = S "xaxis log log_base 2 size 10cm label fontsize 12 : " xlabel "\n"
   S = S hashLabels()
   S = S "yaxis size 5cm label : " ylabel "\n"
   S = S "legend x " legendx " y " legendy " linelength 0.5\n"
   S = S "border" "\n"
   return S
}

function newcurve (line,mark,label,pts) {
   S = "newcurve " marktype[mark] " " linetype[line] "\n" 
   S = S "   label fontsize 12 : " label "\n"
   S = S "   pts \n"
   n = split(pts, A, ":")
   for(i=2;i<=n;i++) {
      l = 30 - length(A[i])
      b = substr("                                 ",1,l)
      S = S b A[i] "\n"
   }
   return S
}

END {
   X = "(* wm2bytecode: map size of watermark to size of bytecode. *)"
   S = newgraph("watermark", "size(bytes)", 3000, 1000, X) 
   S = S newcurve(1, 1, "PPCT graph bytecode size", bcSize["PPCT"])
   S = S newcurve(1, 2, "reducible permutation graph bytecode size", bcSize["reducible"])
   S = S newcurve(1, 3, "permutation graph bytecode size", bcSize["perm"])
   S = S newcurve(1, 4, "radix graph bytecode size", bcSize["radix"])
   print S > "wm2bytecode.jgr"

   X = "(* wm2heap: map size of watermark to size of heap graph. *)"
   S = newgraph("watermark", "size(bytes)", 3000, 800, X) 
   S = S newcurve(1, 1, "PPCT graph heap size", graphSize["PPCT"])
   S = S newcurve(1, 2, "reducible permutation graph heap size", graphSize["reducible"])
   S = S newcurve(1, 3, "permutation graph heap size", graphSize["perm"])
   S = S newcurve(1, 4, "radix graph heap size", graphSize["radix"])
   print S > "wm2heap.jgr"

   X = "(* wm2bytecode2components: map size of watermark to size of generated\n"
   X = X "   bytecode for different number of graph components, for the radix encoding. *)"
   S = newgraph("watermark", "size(bytes)", 1000, 600, X) 
   S = S newcurve(2, 0, "1 component", bcSize["radix"])
   S = S newcurve(1, 1, "2 components", bcSizeRadix[2])
   S = S newcurve(1, 2, "3 components", bcSizeRadix[3])
   S = S newcurve(1, 3, "4 components", bcSizeRadix[4])
   S = S newcurve(1, 4, "5 components", bcSizeRadix[5])
   print S > "wm2bytecode2components.jgr"

   X = "(* wm2cycled: map the bytecode size of the graph wrt the watermark\n"
   X = X "    size and number of components for cycled graphs. *)"
   S = newgraph("watermark", "size(bytes)", 1024, 4000, X) 
   S = S newcurve(2, 0, "cycled radix graph heap size", graphSizeCycle["radix"])
   S = S newcurve(1, 1, "bytecode size of cycled radix graph with 1 component", bcSizeRadixCycle[1])
   S = S newcurve(1, 2, "bytecode size of cycled radix graph with 2 components", bcSizeRadixCycle[2])
   S = S newcurve(1, 3, "bytecode size of cycled radix graph with 3 components", bcSizeRadixCycle[3])
   S = S newcurve(1, 4, "bytecode size of cycled radix graph with 4 components", bcSizeRadixCycle[4])
   S = S newcurve(1, 5, "bytecode size of cycled radix graph with 5 components", bcSizeRadixCycle[5])
   print S > "wm2cycled.jgr"
}
