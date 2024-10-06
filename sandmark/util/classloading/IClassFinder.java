package sandmark.util.classloading;

/**
 * An interface implemented by all classes suitable for use by
 * sandmark.util.classloading.ClassFinder as sources of class names
 @see sandmark.util.classloading.ClassFinder
 @author Andrew Huntwork
 @version 1.0
*/

public interface IClassFinder {
    int ALGORITHM = 0;
    int GEN_OBFUSCATOR = 1;
    int APP_OBFUSCATOR = 2;
    int METHOD_OBFUSCATOR = 3;
    int CLASS_OBFUSCATOR = 4;
    int DYN_WATERMARKER = 5;
    int GEN_WATERMARKER = 6;
    int STAT_WATERMARKER = 7;
    int GRAPH_CODEC = 8;
    int METHOD_ALGORITHM = 9;
    int CLASS_ALGORITHM = 10;
    int APP_ALGORITHM = 11;
    int APP_METRIC = 12;
    int METHOD_METRIC = 13;
    int CLASS_METRIC = 14;
    int PREDICATE_GENERATOR = 15;
    int WRAPPER_CODEC = 16;
    int GEN_OPTIMIZER = 17;
    int APP_OPTIMIZER = 18;
    int METHOD_OPTIMIZER = 19;
    int CLASS_OPTIMIZER = 20;
    int GEN_BIRTHMARK = 21;
    int STAT_BIRTHMARK = 22;
    int DYN_BIRTHMARK = 23;
    int QUICK_PROTECT = 24;
    int CLASS_COUNT = 25;

    String CLASS_NAMES[] = new String[] {
    "sandmark.Algorithm",
    "sandmark.obfuscate.GeneralObfuscator",
    "sandmark.obfuscate.AppObfuscator",
    "sandmark.obfuscate.MethodObfuscator",
    "sandmark.obfuscate.ClassObfuscator",
    "sandmark.watermark.DynamicWatermarker",
    "sandmark.watermark.GeneralWatermarker",
    "sandmark.watermark.StaticWatermarker",
    "sandmark.util.newgraph.codec.GraphCodec",
    "sandmark.MethodAlgorithm",
    "sandmark.ClassAlgorithm",
    "sandmark.AppAlgorithm",
    "sandmark.metric.ApplicationMetric",
    "sandmark.metric.MethodMetric",
    "sandmark.metric.ClassMetric",
    "sandmark.util.opaquepredicatelib.OpaquePredicateGenerator",
    "sandmark.util.newgraph.codec.WrapperCodec",
    "sandmark.optimise.GeneralOptimizer",
    "sandmark.optimise.AppOptimizer",
    "sandmark.optimise.MethodOptimizer",
    "sandmark.optimise.ClassOptimizer",
    "sandmark.birthmark.GeneralBirthmark",
    "sandmark.birthmark.StaticClassBirthmark",
    "sandmark.birthmark.DynamicBirthmark",
    "sandmark.wizard.quickprotect.QuickProtect"
    };
    String CLASS_IDS[] = new String[] {
    "ALGORITHM",
    "GEN_OBFUSCATOR",
    "APP_OBFUSCATOR",
    "METHOD_OBFUSCATOR",
    "CLASS_OBFUSCATOR",
    "DYN_WATERMARKER",
    "GEN_WATERMARKER",
    "STAT_WATERMARKER",
    "GRAPH_CODEC",
    "METHOD_ALGORITHM",
    "CLASS_ALGORITHM",
    "APP_ALGORITHM",
    "APP_METRIC",
    "METHOD_METRIC",
    "CLASS_METRIC",
    "PREDICATE_GENERATOR",
    "WRAPPER_CODEC",
    "GEN_OPTIMIZER",
    "APP_OPTIMIZER",
    "METHOD_OPTIMIZER",
    "CLASS_OPTIMIZER",
    "GEN_BIRTHMARK",
    "STAT_BIRTHMARK",
    "DYN_BIRTHMARK",
    "QUICK_PROTECT",
    };
    /**
     * Get a Collection of String's containing names
     * of classes that derive from the type specified by
     * ancestor.
     * @param ancestor one of the constants above
     * @return Collection of String's containing names of classes derived from class specified by ancestor
     */
    java.util.Collection getClassesWithAncestor(int ancestor);

    /**
     * Get a string suitable for display to the user that describes className
     * @param className A String returned as a member of a Collection by getClassesWithAncestor
     * @return A short String description of className
     */
    String getClassShortname(String className);
}

