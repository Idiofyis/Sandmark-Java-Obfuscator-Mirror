package sandmark.wizard.modeling.dfa;

public class WeightMap
{

    private static java.util.HashMap myWeights;
    private static java.util.HashMap myDegrade;

    static {
        myWeights = new java.util.HashMap();
        myDegrade = new java.util.HashMap();

    try {
        java.io.InputStream inStream =
        WeightMap.class.getResourceAsStream
        ("/sandmark/wizard/modeling/dfa/Weights.txt");
        java.io.BufferedReader inFile = new java.io.BufferedReader
        (new java.io.InputStreamReader(inStream));

        //file has format ShortName:number
        while(inFile.ready()){
        String entry = inFile.readLine();
        int breakPt = entry.indexOf(':');
        int breakPt2 = entry.indexOf(':', breakPt+1);
        //System.out.println(entry + " " + breakPt + " " + breakPt2);

        String shortname = entry.substring(0, breakPt);
        Float weight = new Float(entry.substring(breakPt+1, breakPt2));
        Float degrade = new Float(entry.substring(breakPt2+1, entry.length()));
        myWeights.put(shortname, weight);
        myDegrade.put(shortname, degrade);
        }
    } catch(java.io.IOException e) {
        throw new RuntimeException("no weightmap found");
    }
    }
    
    public static float getWeightForAlg(sandmark.Algorithm obf)
    {
        Float weight = (Float)myWeights.get(obf.getShortName());
        if(weight == null)
            return 1;
        else
            return weight.floatValue();
    }

    public static float getDegradationForAlg(sandmark.Algorithm obf)
    {
        Float weight = (Float)myDegrade.get(obf.getShortName());
        if(weight == null)
            return 1;
        else
            return weight.floatValue();
    }
}

