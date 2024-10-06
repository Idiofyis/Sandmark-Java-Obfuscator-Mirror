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
public class StaticClassBirthMarkParameters {
   public sandmark.program.Class original;
   public sandmark.program.Class suspect;
   private StaticClassBirthMarkParameters() {}
   public static sandmark.util.ConfigProperties buildProperties() {
      String props[][] = new String[][] {
            {"Suspect File","","The suspect input jar-file.",null, "J","SB",},
            {"Original Class", "", "Original class.",null,"S","SB",},
            {"Suspect Class", "", "Suspect class.", null,"S","SB",},
      };
      return new sandmark.util.ConfigProperties(props,null);
   }
   public static StaticClassBirthMarkParameters buildParams
      (sandmark.util.ConfigProperties cp,sandmark.program.Application app) 
      throws Exception {
      StaticClassBirthMarkParameters params =
         new StaticClassBirthMarkParameters();
      String inputClassName = (String)cp.getValue("Original Class");
      params.original = app.getClass(inputClassName);
      String suspectClassName = (String)cp.getValue("Suspect Class");
      params.suspect = new sandmark.program.Application
         ((java.io.File)cp.getValue("Suspect File")).getClass(suspectClassName);
      if(params.suspect == null || params.original == null)
         throw new ClassNotFoundException();
      return params;
   }
}
