package sandmark.util;

/**
 * Visualize a graph using the 'dot' tool.
 *  Just do
 * <PRE>
 *     sandmark.util.Misc.writeToFile("file.dot", dot-prog);
 * </PRE>
 * run dot on the resulting file
 * <PRE>
 *     dot -Tps file.dot > file.ps
 * </PRE>
 *  and then view the postscript file
 * <PRE>
 *    gv file.ps
 * </PRE>
 *  <P>
 */
public class Dot {

   /* Edge and node colors: */
   public static final String TURQUOISE = "turquoise";
   public static final String ANTIQUEWHITE = "antiquewhite";
   public static final String CORAL = "coral";
   public static final String DARKGOLDENROD = "darkgoldenrod";
   public static final String AZURE = "azure";
   public static final String CRIMSON = "crimson";
   public static final String GOLD = "gold";
   public static final String BISQUE = "bisque";
   public static final String DARKSALMON = "darksalmon";
   public static final String GOLDENROD = "goldenrod";
   public static final String ALICEBLUE = "aliceblue";
   public static final String BLANCHEDALMOND = "blanchedalmond";
   public static final String DEEPPINK = "deeppink";
   public static final String GREENYELLOW = "greenyellow";
   public static final String BLUE = "blue";
   public static final String CORNSILK = "cornsilk";
   public static final String FIREBRICK = "firebrick";
   public static final String LIGHTGOLDENROD = "lightgoldenrod";
   public static final String BLUEVIOLET = "blueviolet";
   public static final String FLORALWHITE = "floralwhite";
   public static final String HOTPINK = "hotpink";
   public static final String LIGHTGOLDENRODYELLOW = "lightgoldenrodyellow";
   public static final String CADETBLUE = "cadetblue";
   public static final String GAINSBORO = "gainsboro";
   public static final String INDIANRED = "indianred";
   public static final String LIGHTYELLOW = "lightyellow";
   public static final String CORNFLOWERBLUE = "cornflowerblue";
   public static final String GHOSTWHITE = "ghostwhite";
   public static final String LIGHTPINK = "lightpink";
   public static final String PALEGOLDENROD = "palegoldenrod";
   public static final String DARKSLATEBLUE = "darkslateblue";
   public static final String HONEYDEW = "honeydew";
   public static final String LIGHTSALMON = "lightsalmon";
   public static final String YELLOW = "yellow";
   public static final String DEEPSKYBLUE = "deepskyblue";
   public static final String IVORY = "ivory";
   public static final String MAROON = "maroon";
   public static final String YELLOWGREEN = "yellowgreen";
   public static final String DODGERBLUE = "dodgerblue";
   public static final String LAVENDER = "lavender";
   public static final String INDIGO = "indigo";
   public static final String LAVENDERBLUSH = "lavenderblush";
   public static final String ORANGERED = "orangered";
   public static final String LIGHTBLUE = "lightblue";
   public static final String LEMONCHIFFON = "lemonchiffon";
   public static final String CHARTREUSE = "chartreuse";
   public static final String LIGHTSKYBLUE = "lightskyblue";
   public static final String LINEN = "linen";
   public static final String PINK = "pink";
   public static final String DARKGREEN = "darkgreen";
   public static final String LIGHTSLATEBLUE = "lightslateblue";
   public static final String MINTCREAM = "mintcream";
   public static final String RED = "red";
   public static final String DARKOLIVEGREEN = "darkolivegreen";
   public static final String MEDIUMBLUE = "mediumblue";
   public static final String MISTYROSE = "mistyrose";
   public static final String SALMON = "salmon";
   public static final String DARKSEAGREEN = "darkseagreen";
   public static final String MEDIUMSLATEBLUE = "mediumslateblue";
   public static final String MOCCASIN = "moccasin";
   public static final String TOMATO = "tomato";
   public static final String FORESTGREEN = "forestgreen";
   public static final String MIDNIGHTBLUE = "midnightblue";
   public static final String NAVAJOWHITE = "navajowhite";
   public static final String VIOLETRED = "violetred";
   public static final String GREEN = "green";
   public static final String NAVY = "navy";
   public static final String OLDLACE = "oldlace";
   public static final String NAVYBLUE = "navyblue";
   public static final String PAPAYAWHIP = "papayawhip";
   public static final String BROWNS = "Browns";
   public static final String LAWNGREEN = "lawngreen";
   public static final String POWDERBLUE = "powderblue";
   public static final String PEACHPUFF = "peachpuff";
   public static final String BEIGE = "beige";
   public static final String LIGHTSEAGREEN = "lightseagreen";
   public static final String ROYALBLUE = "royalblue";
   public static final String SEASHELL = "seashell";
   public static final String BROWN = "brown";
   public static final String LIMEGREEN = "limegreen";
   public static final String SKYBLUE = "skyblue";
   public static final String SNOW = "snow";
   public static final String BURLYWOOD = "burlywood";
   public static final String MEDIUMSEAGREEN = "mediumseagreen";
   public static final String SLATEBLUE = "slateblue";
   public static final String THISTLE = "thistle";
   public static final String CHOCOLATE = "chocolate";
   public static final String MEDIUMSPRINGGREEN = "mediumspringgreen";
   public static final String STEELBLUE = "steelblue";
   public static final String WHEAT = "wheat";
   public static final String DARKKHAKI = "darkkhaki";
   public static final String WHITE = "white";
   public static final String KHAKI = "khaki";
   public static final String OLIVEDRAB = "olivedrab";
   public static final String WHITESMOKE = "whitesmoke";
   public static final String PERU = "peru";
   public static final String PALEGREEN = "palegreen";
   public static final String ROSYBROWN = "rosybrown";
   public static final String SEAGREEN = "seagreen";
   public static final String DARKORCHID = "darkorchid";
   public static final String SADDLEBROWN = "saddlebrown";
   public static final String SPRINGGREEN = "springgreen";
   public static final String DARKVIOLET = "darkviolet";
   public static final String DARKSLATEGRAY = "darkslategray";
   public static final String SANDYBROWN = "sandybrown";
   public static final String MAGENTA = "magenta";
   public static final String DIMGRAY = "dimgray";
   public static final String SIENNA = "sienna";
   public static final String MEDIUMORCHID = "mediumorchid";
   public static final String TAN = "tan";
   public static final String MEDIUMPURPLE = "mediumpurple";
   public static final String GRAY = "gray";
   public static final String AQUAMARINE = "aquamarine";
   public static final String MEDIUMVIOLETRED = "mediumvioletred";
   public static final String LIGHTGRAY = "lightgray";
   public static final String CYAN = "cyan";
   public static final String ORCHID = "orchid";
   public static final String LIGHTSLATEGRAY = "lightslategray";
   public static final String DARKORANGE = "darkorange";
   public static final String DARKTURQUOISE = "darkturquoise";
   public static final String PALEVIOLETRED = "palevioletred";
   public static final String SLATEGRAY = "slategray";
   public static final String ORANGE = "orange";
   public static final String LIGHTCYAN = "lightcyan";
   public static final String PLUM = "plum";
   public static final String MEDIUMAQUAMARINE = "mediumaquamarine";
   public static final String PURPLE = "purple";
   public static final String MEDIUMTURQUOISE = "mediumturquoise";
   public static final String VIOLET = "violet";
   public static final String BLACK = "black";
   public static final String PALETURQUOISE = "paleturquoise";

   /* Edge styles: */
   public static final String SOLID     = "solid";
   public static final String DASHED    = "dashed";
   public static final String DOTTED    = "dotted";
   public static final String BOLD      = "bold";
   public static final String INIVS     = "invis";
}

