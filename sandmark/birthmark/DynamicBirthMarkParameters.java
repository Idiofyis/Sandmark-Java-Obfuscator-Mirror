/*
 * Created on Apr 12, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.birthmark;


/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class DynamicBirthMarkParameters {
   public sandmark.program.Application suspect;
   public sandmark.program.Application original;
   public java.io.File suspectFile;
   public java.io.File originalFile;
   public String suspectArgv[];
   public String originalArgv[];
   
   private DynamicBirthMarkParameters() {}
   public static sandmark.util.ConfigProperties createConfigProperties() {
      String props[][] = new String [][] {
            {"Suspect File","","The suspect input jar-file.",null, "J","DB",},
            {"Input Main Class", "", 
               "The main class where execution begins in the input file.",null,"S","DB",},
            {"Suspect Main Class", "", 
               "The main class where execution begins in the suspect file.", null,"S","DB",},
            {"Class Path", "", 
               "User defined classpath additions used during tracing.",null,"S","DB",},
            {"Input File Arguments", "",
               "The arguments to the program used to execute the input file.",null,"S","DB",},
            {"Suspect File Arguments","",
               "The arguments to the program used to execute the suspect file.",null,"S","DB",},
      };
      return new sandmark.util.ConfigProperties(props,null);
   }
   public static DynamicBirthMarkParameters buildParameters
      (sandmark.util.ConfigProperties cp,sandmark.program.Application input) 
      throws Exception {
      DynamicBirthMarkParameters params = new DynamicBirthMarkParameters();
      params.original = input;
      params.suspect = 
         new sandmark.program.Application
         ((java.io.File)cp.getValue("Suspect File"));
      params.suspectFile = java.io.File.createTempFile("smk",".jar");
      params.suspectFile.deleteOnExit();
      params.suspect.save(params.suspectFile);
      params.originalFile = java.io.File.createTempFile("smk",".jar");
      params.originalFile.deleteOnExit();
      params.original.save(params.originalFile);
      params.originalArgv = sandmark.watermark.DynamicWatermarker.constructArgv
         (params.originalFile,(String)cp.getValue("Class Path"),
          (String)cp.getValue("Input Main Class"),
          (String)cp.getValue("Input File Arguments"));
      params.suspectArgv = sandmark.watermark.DynamicWatermarker.constructArgv
         (params.suspectFile,(String)cp.getValue("Class Path"),
          (String)cp.getValue("Suspect Main Class"),
          (String)cp.getValue("Suspect File Arguments"));
      return params;
   }
}
