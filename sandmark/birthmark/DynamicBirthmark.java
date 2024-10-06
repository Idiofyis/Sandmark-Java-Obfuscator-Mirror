package sandmark.birthmark;

public abstract class DynamicBirthmark extends GeneralBirthmark{
   abstract public double calculate(DynamicBirthMarkParameters params)
      throws Exception;
}
