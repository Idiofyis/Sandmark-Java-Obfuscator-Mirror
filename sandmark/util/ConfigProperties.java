package sandmark.util;

/**
 * A ConfigProperties object allows properties to be queried and
 * manipulated by name.  Each property has a name,
 * a value, a description, a type, and a phase.  Properties of ConfigProperties
 * objects are generally designed to be manipulated by an end-user.  The type
 * associated with an object is one of TYPE_* integers.  The value of a property
 * is an instance of the class corresponding to a given TYPE_* constant.  For example,
 * if a property has type TYPE_BOOLEAN, then the property's value is an instance of
 * java.lang.Boolean.  A property's phase is a bitwise OR of all the
 * phases (as defined by the PHASE_* constants) in which it is relevant.  For example,
 * currently defined phases include
 * PHASE_OBFUSCATE and PHASE_DYNAMIC_TRACE.  Both phases require an input file that
 * can be changed by the user.  Therefore, there might be a property called "Input File"
 * with phase PHASE_OBFUSCATE|PHASE_DYNAMIC_TRACE.
 *
 * A ConfigProperties object may have a parent.  If so, a query for a property on the
 * child ConfigProperties object will query the parent object for that property
 * if the property is not found on the child.
 *
 * @author Andrew Huntwork <ash@cs.arizona.edu>
 */
public class ConfigProperties {

    /**
     * These constants indicate the types known to ConfigProperties.  Any
     * type that may be a value of a property must have a corresponding
     * TYPE_* constant here.
     */
    public static final int TYPE_FILE = 0;
    public static final int TYPE_JAR = 1;
    public static final int TYPE_PERCENT = 2;
    public static final int TYPE_INTEGER = 3;
    public static final int TYPE_RANDOM_INT = 4;
    public static final int TYPE_BOOLEAN = 5;
    public static final int TYPE_DOUBLE = 6;
    public static final int TYPE_STRING = 7;
    public static final int TYPE_METHOD = 8;
    public static final int TYPE_CLASS = 9;
    
    /* sTypeClasses[0] is the class that every value of a property
     * whose type is 0 (TYPE_FILE) must be assignable from.
     */
    private static Class sTypeClasses[] = {
            java.io.File.class,java.io.File.class,
            Double.class,Integer.class,Integer.class,
            Boolean.class,Double.class,String.class,
            new sandmark.program.Method[0].getClass(),
            new sandmark.program.Class[0].getClass(),
    };

    /**
     * These strings allow the types of properties to be specified in the
     * String[] constructors to ConfigProperties.  They appear in the same
     * order as the TYPE_* constants to which they correspond.  For example,
     * "F" corresponds to TYPE_FILE because both are first in their lists.
     */
    private static String sTypeDescs[] = { "F","J","P","I","R","B","D","S","M","C" };
    private static java.util.Hashtable sTypeDescToVal;
    static {
        sTypeDescToVal = new java.util.Hashtable();
        for(int i = 0 ; i < sTypeDescs.length ; i++)
            sTypeDescToVal.put(sTypeDescs[i],new Integer(i));
    }

    public static final long PHASE_ALL = 1 << 0; // Gets converted to ~0
    public static final long PHASE_NONE = 1 << 1; // Gets converted to 0
    public static final long PHASE_OBFUSCATE = 1 << 2;
    public static final long PHASE_DYNAMIC_TRACE = 1 << 3;
    public static final long PHASE_DYNAMIC_EMBED = 1 << 4;
    public static final long PHASE_DYNAMIC_RECOGNIZE = 1 << 5;
    public static final long PHASE_STATIC_EMBED = 1 << 6;
    public static final long PHASE_STATIC_RECOGNIZE = 1 << 7;
    public static final long PHASE_OPTIMIZE = 1 << 8;
    public static final long PHASE_STATIC_BIRTHMARK = 1 << 9;
    public static final long PHASE_DYNAMIC_BIRTHMARK = 1 << 10;

    /**
     * These strings allow the phases of properties to be specified in the String[]
     * constructors to ConfigProperties.  They appear in the same order as the
     * PHASE_* constants to which they correspond.
     */
    private static String sPhaseDescs[] = {"A","N","O","DT","DE","DR","SE","SR","OPT","SB","DB"};
    private static java.util.Hashtable sPhaseDescToVal;
    static {
        sPhaseDescToVal = new java.util.Hashtable();
        for(int i = 0 ; i < sPhaseDescs.length ; i++)
            sPhaseDescToVal.put(sPhaseDescs[i],new Long(1<<i));
    }

    class PropSpec{
        public String name;
        public String description;
        public int type;
        public Object value;
        public long phases;
        private java.util.ArrayList listeners;
        boolean exclusive;
        java.util.List choices;
        PropSpec(String name,String description,String dflt,int type,String phases)
            throws ConfigPropertyException {
            this.name = name;
            this.description = description;
            this.type = type;
            exclusive = false;

            String phaseDescs[] = phases.split(",");
            for(int i = 0 ; i < phaseDescs.length ; i++) {
                this.phases |= ((Long)sPhaseDescToVal.get(phaseDescs[i])).longValue();
            }
            if((this.phases & PHASE_NONE) != 0)
                this.phases = 0;
            if((this.phases & PHASE_ALL) != 0)
                this.phases = ~(0L);

            listeners = new java.util.ArrayList();
            setValue(dflt);
        }

        public void setValue(String value) {
            try {
                Object newValue = null;
                if(value == null || (type != TYPE_STRING && value.equals(""))) {
                    this.value = null;
                } else {
                    switch(type) {
                    case TYPE_FILE:
                    case TYPE_JAR:
                        newValue = new java.io.File(value);
                        break;
                    case TYPE_PERCENT:
                        newValue = new Double(value);
                        break;
                    case TYPE_INTEGER:
                    case TYPE_RANDOM_INT:
                        newValue = new Integer(value);
                        break;
                    case TYPE_BOOLEAN:
                        newValue = new Boolean(value);
                        break;
                    case TYPE_DOUBLE:
                        newValue = new Double(value);
                        break;
                    case TYPE_STRING:
                        newValue = value;
                        break;
                    case TYPE_METHOD:
                        if(!value.equals(""))
                            throw new ConfigPropertyException
                            ("Can't convert String to MethodID[]");
                        newValue = null;
                        break;
                    case TYPE_CLASS:
                        if(!value.equals(""))
                            throw new ConfigPropertyException
                            ("Can't convert String to String[]");
                        newValue = null;
                        break;
                    default:
                        throw new ConfigPropertyException();
                    }
                }
                setValue(newValue);
            } catch(Exception e) {
                e.printStackTrace();
                throw new ConfigPropertyException("got " + e + " while setting " + name + " to " + value);
            }
        }
        public void setValue(Object value) {
            Object oldValue = this.value;
            Object newValue = value;
            Class correctClass = sTypeClasses[type];
            Class valueClass = value == null ? correctClass : value.getClass();
            if(!correctClass.isAssignableFrom(valueClass))
                throw new ConfigPropertyException
                ("Invalid type for property " + name);
            if((exclusive && choices == null) || 
               (exclusive && choices != null && !choices.contains(value)))
               throw new ConfigPropertyException
               (value + " is not a valid choice for property " + name);
            if((this.value == null && newValue == null) ||
               (newValue != null && newValue.equals(this.value)))
                return;
            this.value = newValue;
            for(java.util.Iterator listenerIt = listeners.iterator() ;
            listenerIt.hasNext() ; ) {
                Listener l = (Listener)listenerIt.next();
                l.listener.propertyChanged
                	(l.configProps,name,oldValue,this.value);
            }
        }
        public String toString() {
            return name + " = " + value;
        }
        public void addListener(ConfigPropertyChangeListener listener,
                                ConfigProperties cp) {
            listeners.add(new Listener(cp,listener));
        }
        public void removeListener(ConfigPropertyChangeListener listener,
                                   ConfigProperties cp) {
            listeners.remove(new Listener(cp,listener));
        }
        private class Listener {
            sandmark.util.ConfigProperties configProps;
            ConfigPropertyChangeListener listener;
            Listener(ConfigProperties cp,ConfigPropertyChangeListener l) {
                configProps = cp;
                listener = l;
            }
            public int hashCode() { 
                return System.identityHashCode(configProps) +
                    listener.hashCode();
            }
            public boolean equals(Object o) {
                Listener l = (Listener) o;
                return l.configProps == configProps &&
                    l.listener == listener;
            }
        }
    }

    class LocalPropIter implements java.util.Iterator {
        java.util.Iterator psIter;
        LocalPropIter() {
            psIter = mPropSpecs.iterator();
        }
        public boolean hasNext() {
            return psIter.hasNext();
        }
        public Object next() {
            return ((PropSpec)psIter.next()).name;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    class PropIter implements java.util.Iterator {
        java.util.Iterator mCurrentIterator;
        boolean mIsParentIter;
        PropIter() {
            if(mParent != null) {
                mCurrentIterator = mParent.properties();
                mIsParentIter = true;
            }
            if(mCurrentIterator == null || !mCurrentIterator.hasNext()) {
                mCurrentIterator = mPropSpecs.iterator();
                mIsParentIter = false;
            }
        }
        public boolean hasNext() {
            return mCurrentIterator.hasNext();
        }
        public Object next() {
            Object o = mCurrentIterator.next();
            //System.out.println(" first : " + mIsParentIter + " ; class : " + o.getClass() + " ; value : " + o);
            if(mIsParentIter) {
                if(!mCurrentIterator.hasNext()) {
                    mCurrentIterator = mPropSpecs.iterator();
                    mIsParentIter = false;
                }
                return o;
            }
            return ((PropSpec)o).name;
        }
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    public static class ConfigPropertyException extends java.lang.RuntimeException {
        ConfigPropertyException() {}
        ConfigPropertyException(String a) { super(a); }
    }

    private java.util.Vector mPropSpecs;
    private java.util.Hashtable mPropNameToIndex;
    private ConfigProperties mParent;

    public ConfigProperties(ConfigProperties parent) {
        this(null,parent);
    }

    /**
     * Constructs a ConfigProperties object with the indicated property names,
     * types, values, descriptions, and phases (optional).  See addProps for
     * format details for args.
     */
    public ConfigProperties(String[][] args,ConfigProperties parent) {
        mPropSpecs = new java.util.Vector();
        mPropNameToIndex = new java.util.Hashtable();
        mParent = parent;

        if(args != null)
            addProps(args);
    }
    /**
     * Adds properties to this ConfigProperites object based on the following
     * format:
     * arg[0] : property Name
     * arg[1] : default property Value
     * arg[2] : description
     * arg[3] : ignored (backward compatibility)
     * arg[4] : type.  see sTypeDescs for possible values
     * arg[5] : optional comma separated list of relevant phases.  see sPhaseDescs for possible values
     */
    private void addProps(String[][] args) {

        for(int i=0; i<args.length; i++) {
           String[] ithArgs = args[i];
           if (ithArgs.length < 5)
               throw new RuntimeException("bad property format");

           java.lang.String key = ithArgs[0];
           java.lang.String def =  ithArgs[1];
           java.lang.String des = ithArgs[2];
           int type = ((Integer)sTypeDescToVal.get(ithArgs[4])).intValue();
           PropSpec ps = new PropSpec(key,des,def,type,
                                      ithArgs.length >= 6 ? ithArgs[5] : "A");
           mPropSpecs.add(ps);
           mPropNameToIndex.put(key,new Integer(mPropSpecs.size() - 1));
        }

        /*
          This parses a possibly nicer format for specifying properties:
          "Type:Name=Default","Description"
          Someone else can rewrite the whole tree to get it into this format.  I already did it once...

          if(args.length % 2 != 0)
          throw new ConfigPropertyException();

          for(int i = 0 ; args != null && i < args.length ; i+= 2) {
          String parts[] = args[i].split("[:=]");

          if(parts.length != 2 && parts.length != 3)
          throw new ConfigPropertyException();

          if(sTypeDescToVal.get(parts[0]) == null)
          throw new ConfigPropertyException();

          mPropSpecs.add(
          new PropSpec(parts[1],args[i + 1],
          parts.length == 3 ? parts[2] : null,
          ((Integer)sTypeDescToVal.get(parts[0])).intValue()));
          mPropNameToIndex.put
          (args[1],new Integer(mPropSpecs.size() - 1));
          }
        */
    }
    /**
     * @return An iterator containing String's that are the names of properties
     * in this ConfigProperties object and its ancestors
     */
    public java.util.Iterator properties() {
        return new PropIter();
    }
    /**
     * @return An iterator containing String's that are the names of properties
     * in this ConfigProperties object
     */
    public java.util.Iterator localProperties() {
        return new LocalPropIter();
    }
    /**
     * @return A bitmask with a bit set if this property is relevant to the
     * the phase represented by that bit as described by the PHASE_* constants
     */
    public long getPhases(String property) {
        return getPS(property).phases;
    }
    /**
     * @return One of the TYPE_* constants indicating the type of this property
     */
    public int getType(String property) {
        return getPS(property).type;
    }
    /**
     * @return A String description of this property
     */
    public String getDescription(String property) {
        return getPS(property).description;
    }
    /**
     * @return An Object whose concrete type is compatible with
     * the type of this property.  For example, if this property's
     * type is TYPE_BOOLEAN, Object is an instance of Boolean
     */
    public Object getValue(String property) {
        return getPS(property).value;
    }
    public void setValue(String property,Object value) {
        getPS(property).setValue(value);
    }
    /**
     * @return getValue() converted to a string
     */
    public String getProperty(String key) {
        Object o = getValue(key);
        return o == null ? "" : o.toString();
    }
    public String getProperty(String key,String value) {
        return getProperty(key);
    }
    public void setProperty(String key,String value) {
        getPS(key).setValue(value);
    }

    protected PropSpec getPS(String property) {
        Integer ndx = (Integer)mPropNameToIndex.get(property);

        if(ndx == null) {
	    if(mParent == null)
		throw new ConfigPropertyException("Unknown property " + property);
            return mParent.getPS(property);
	}

        PropSpec ps = (PropSpec)mPropSpecs.get(ndx.intValue());

        if(ps == null)
            throw new ConfigPropertyException();

        return ps;
    }

    /**
       Returns the PropSpec for the given property, or null if
       the property does not exist. This method does not assume
       that the ConfigProp has a parent.
    */
    protected PropSpec getOrphanPS(String property){
        Integer ndx = (Integer)mPropNameToIndex.get(property);

        if(ndx != null)
            return (PropSpec)mPropSpecs.get(ndx.intValue());

        if(mParent != null)
            return mParent.getOrphanPS(property);

        return null;
    }

    /**
     * Adds an observer to a specific property.  This listener will be notified when
     * the specified property is set to a value not equal() to its previous value
     */
    public void addPropertyChangeListener(String property,ConfigPropertyChangeListener listener) {
        getPS(property).addListener(listener,this);
    }
    public void removePropertyChangeListener(String property,ConfigPropertyChangeListener listener) {
        getPS(property).removeListener(listener,this);
    }
    
    public void setChoices(String property,boolean exclusive,
          						java.util.List choices) {
       PropSpec ps = getPS(property);
       ps.choices = choices;
       ps.exclusive = exclusive;
    }
    
    public boolean getExclusive(String property) {
       return getPS(property).exclusive;
    }
    
    public java.util.List getChoices(String property) {
       return getPS(property).choices;
    }    
}



