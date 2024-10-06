package sandmark.util;

public interface ConfigPropertyChangeListener {
    public void propertyChanged(ConfigProperties cp,String propertyName,
            Object oldValue,Object newValue);
}
