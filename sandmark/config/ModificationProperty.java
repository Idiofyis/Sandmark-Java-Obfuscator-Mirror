package sandmark.config;

/**
 *  A ModificationProperty encapsulates information about dependencies between
 *  each obfuscation and watermarking algorithm.  Specifically, it encapsulates
 *  a code mutation, like <code>I_CHANGE_FIELD_NAMES</code> represents the property
 *  that an algorithm modifies the names of the fields in the constant pool.  It
 *  is essential that this information is consistent across all of sandmark, so
 *  this class has no public constructor. The only available modification
 *  properties are those listed in the static fields of this class.  If there
 *  is a mutation not available that you would like added to
 *  ModificationProperty, please email
 *  <a href="mailto:kheffner@cs.arizona.edu">kheffner@cs.arizona.edu</a>.
 *  @author Kelly Heffner
 */
public class ModificationProperty extends sandmark.config.RequisiteProperty
{

    /**
       Any of the publicizing algorithms should have this property.
    */
    public static final ModificationProperty I_PUBLICIZE_FIELDS =
        new ModificationProperty("prop:I_PUBLICIZE_FIELDS");

    /**
       Any of the publicizing algorithms should have this property.
    */
    public static final ModificationProperty I_PUBLICIZE_METHODS =
        new ModificationProperty("prop:I_PUBLICIZE_METHODS");
    /**
       Any of the publicizing algorithms should have this property.
    */
    public static final ModificationProperty I_PUBLICIZE_CLASSES =
        new ModificationProperty("prop:I_PUBLICIZE_CLASSES");
    public static final ModificationProperty I_CHANGE_CONSTANT_POOL =
        new ModificationProperty("prop:I_CHANGE_CONSTANT_POOL");
    public static final ModificationProperty I_CHANGE_FIELD_TYPES =
        new ModificationProperty("prop:I_CHANGE_FIELD_TYPES");
    public static final ModificationProperty I_CHANGE_FIELD_NAMES =
        new ModificationProperty("prop:I_CHANGE_FIELD_NAMES");

    public static final ModificationProperty I_CHANGE_FIELD_SCOPES =
        new ModificationProperty("prop:I_CHANGE_FIELD_SCOPES");

    public static final ModificationProperty I_CHANGE_METHOD_NAMES =
        new ModificationProperty("prop:I_CHANGE_METHOD_NAMES");
    public static final ModificationProperty I_CHANGE_METHOD_SIGNATURES =
        new ModificationProperty("prop:I_CHANGE_METHOD_SIGNATURES");
    public static final ModificationProperty I_CHANGE_METHOD_SCOPES =
        new ModificationProperty("prop:I_CHANGE_METHOD_SCOPES");

        public static final ModificationProperty I_CHANGE_METHOD_BODIES =
        new ModificationProperty("prop:I_CHANGE_METHOD_BODIES");

    /**
       Any algorithm that inserts code (of any form, whether it
       will ever execute or not) should have this property.
    */
    public static final ModificationProperty I_ADD_METHOD_CODE =
        new ModificationProperty("prop:I_ADD_METHOD_CODE");

    public static final ModificationProperty I_REORDER_INSTRUCTIONS =
        new ModificationProperty("prop:I_REORDER_INSTRUCTIONS");

    /**
        Any algorithm that deletes code from a method should
        have this property.
    */
    public static final ModificationProperty I_REMOVE_METHOD_CODE =
        new ModificationProperty("prop:I_REMOVE_METHOD_CODE");

    /**
       Any algorithm that edits instructions (like the local
       variable number, or which constant number is referenced)
       should have this property.
    */
    public static final ModificationProperty I_MODIFY_METHOD_CODE =
        new ModificationProperty("prop:I_MODIFY_METHOD_CODE");

    /**
       Any algorithm that adds locals to the method(s) it runs on
       should have this property.
    */
    public static final ModificationProperty I_ADD_LOCAL_VARIABLES =
        new ModificationProperty("prop:I_ADD_LOCAL_VARIABLES");

    public static final ModificationProperty I_CHANGE_LOCAL_VARIABLES =
        new ModificationProperty("prop:I_CHANGE_LOCAL_VARIABLES");

    public static final ModificationProperty I_CHANGE_CLASS_NAMES =
        new ModificationProperty("prop:I_CHANGE_CLASS_NAMES");
    public static final ModificationProperty I_CHANGE_CLASS_SCOPES =
        new ModificationProperty("prop:I_CHANGE_CLASS_SCOPES");
        public static final ModificationProperty I_CHANGE_CLASS_CONTENTS =
        new ModificationProperty("prop:I_CHANGE_CLASS_CONTENTS");

    public static final ModificationProperty I_ADD_CLASSES =
        new ModificationProperty("prop:I_ADD_CLASSES");
    public static final ModificationProperty I_REMOVE_CLASSES =
        new ModificationProperty("prop:I_REMOVE_CLASSES");

    public static final ModificationProperty I_ADD_METHODS =
        new ModificationProperty("prop:I_ADD_METHODS");
    public static final ModificationProperty I_REMOVE_METHODS =
        new ModificationProperty("prop:I_REMOVE_METHODS");

    public static final ModificationProperty I_ADD_FIELDS =
        new ModificationProperty("prop:I_ADD_FIELDS");
    public static final ModificationProperty I_REMOVE_FIELDS =
        new ModificationProperty("prop:I_REMOVE_FIELDS");

    public static final ModificationProperty I_OBFUSCATE_IDENTIFIERS =
        new ModificationProperty("prop:I_OBFUSCATE_IDENTIFIERS");

    public static final ModificationProperty THREAD_UNSAFE =
        new ModificationProperty("prop:THREAD_UNSAFE");

    public static final ModificationProperty REFLECTION_UNSAFE =
        new ModificationProperty("prop:REFLECTION_UNSAFE");

    public static final ModificationProperty PERFORMANCE_DEGRADE_HIGH =
        new ModificationProperty("prop:PERFORMANCE_DEGRADE_HIGH");
    public static final ModificationProperty PERFORMANCE_DEGRADE_MED =
        new ModificationProperty("prop:PERFORMANCE_DEGRADE_MED");
    public static final ModificationProperty PERFORMANCE_DEGRADE_LOW =
        new ModificationProperty("prop:PERFORMANCE_DEGRADE_LOW");
    public static final ModificationProperty PERFORMANCE_DEGRADE_NONE =
        new ModificationProperty("prop:PERFORMANCE_DEGRADE_NONE");

    public static final ModificationProperty p1 =
        new ModificationProperty("prop:p1");
    public static final ModificationProperty p2 =
        new ModificationProperty("prop:p2");
    public static final ModificationProperty p3 =
        new ModificationProperty("prop:p3");

    private String myProp;

    /**
     *  Modification properties should have a descriptor string that has
     *  "prop:" as the start, to denote that they are mutation properties,
     *  and not a specific algorithm.
     */
    private ModificationProperty(String desc)
    {
                myProp = desc;
    }

    public String toString()
    {
                return myProp;
        }
}

