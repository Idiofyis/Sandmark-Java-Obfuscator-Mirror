package sandmark;

/**
 *  The Algorithm class encapsulates common characteristics of all
 *  Sandmark algorithms.  Most of this information is about the
 *  description of the algorithm, while some of the information
 *  is about the algorithm's interaction with other Sandmark algorithms
 *  (dependencies and interferrence).
 *  @author Kelly Heffner (<a href="mailto:kheffner@cs.arizona.edu">kheffner@cs.arizona.edu</a>)
 */
public abstract class Algorithm
{
    /**
     *  Specifies this algorithm's short name. A short name should be no
     *  longer than 20 characters, and contain capitalized space delimited
     *  words (i.e. title case).  For example, an obfuscation that sets
     *  all of the fields in a jar file to public scoping should have a
     *  short name like "Set Fields Public".
     *  @return the short name for this algorithm
     */
    public abstract String getShortName();

    /**
     *  Specifies this algorithm's long name.  There is no limit on the
     *  size of the long name, but it should be in title case.
     *  @return the long name for this algorithm
     */
    public abstract String getLongName();

    /**
     *  Gives a description of what the algorithm does.  This should not
     *  be a long description of what your code does, rather a brief
     *  description as to what effect it has on the program.  For example,
     *  a good algorithm description would be "Set Fields Public changes
     *  the scope of all static and non-static fields to public."; a bad
     *  algorithm description would be "Set Fields Public creates a BCEL
     *  object for each class and makes modifications to it, then stores
     *  it back into the jar file."
     *  @return an HTML formatted description of what the algorithm does.
     */
    public abstract java.lang.String getAlgHTML();

    /**
     *  Specifies the URL of where the user can find more information about
     *  the algorithm.  This URL should point to the help.html file in
     *  the doc directory where the algorithm resides, starting from
     *  the sandmark directory.  For example, the URL for the static_template
     *  watermarking sample would be
     *  sandmark/watermark/static_template/doc/help.html
     *  @return a URL to the help.html file for the algorithm
     */
    public abstract java.lang.String getAlgURL();

    /**
     *  Gets the ConfigProperties object which specifies the parameters
     *  for this run of the algorithm.  It must be non-null and properly
     *  parented.  See ConfigProperties for details.
     *  @return the parameters for this run of the algorithm
     */

    public sandmark.util.ConfigProperties getConfigProperties() {
        return null;
    }

    /**
     *  Specifies the author of the algorithm.
     *  @return the name of the author of the algorithm
     */
    public abstract String getAuthor();

    /**
     *  Specifies the email address author of the algorithm.
     *  @return the email address of the author of the algorithm
     */
    public abstract String getAuthorEmail();

    /**
     *  Specifies what this algorithm does, briefly.  This description
     *  is displayed to the user in the log of execution in Sandmark.
     *  @return a brief description of the algorithm
     */
    public abstract String getDescription();

    /**
     *  Specifies any references that discuss the ideas in this algorithm.
     *  @return a list of references
     */
    public String[] getReferences()
    {
        return new String[]{};
    }

    /**
     *  Specifies the types of modifications that the algorithm makes. For
     *  more information see <code>ModificationProperty</code>.
     *  @return a list of mutations that this algorithm may do on the code
     */
    public abstract sandmark.config.ModificationProperty[] getMutations();


    /**
     *  Specifies a list of properties of algorithms that must be run on the
     *  target code before this algorithm is run.  See
     *  <code>RequisiteProperty</code> for more details.  This method should
     *  only be overridden if the algorithm has prerequisites.
     *  @return a list of prerequisites for this algorithm
     */
    public sandmark.config.RequisiteProperty[] getPrerequisites()
    {
        return null;
    }

    /**
     *  Specifies a list of properties of algorithms that must be run on the
     *  target code after this algorithm is run.  See
     *  <code>RequisiteProperty</code> for more details.  This method should
     *  only be overridden if the algorithm has postrequisites.
     *  @return a list of postrequisites for this algorithm
     */
    public sandmark.config.RequisiteProperty[] getPostrequisites()
    {
        return null;
    }

    /**
     *  Specifies a list of properties of algorithms that should be run on the
     *  target code before this algorithm is run, <i>but are not necessary</i>.
     *  See <code>RequisiteProperty</code> for more details.
     *  This method should only be overridden if the algorithm has a nonempty
     *  set of pre-suggestions.
     *  @return a list of suggested algorithm properties to run before this
     *  algorithm.
     */
    public sandmark.config.RequisiteProperty[] getPresuggestions()
    {
        return null;
    }

    /**
     *  Specifies a list of properties of algorithms that should be run on the
     *  target code after this algorithm is run, <i>but are not necessary</i>.
     *  See <code>RequisiteProperty</code> for more details.
     *  This method should only be overridden if the algorithm has a nonempty
     *  set of post-suggestions.
     *  @return a list of suggested algorithm properties to run after this
     *  algorithm.
     */
    public sandmark.config.RequisiteProperty[] getPostsuggestions()
    {
        return null;
    }

    /**
     *  Specifies a list of properties of algorithms that cannot be run on the
     *  target code before this algorithm is run.  See
     *  <code>RequisiteProperty</code> for more details.  This method should
     *  only be overridden if there is a nonempty set of prohibited algorithms.
     *  @return a list of prohibited algorithms to run before this algorithm
     */
    public sandmark.config.RequisiteProperty[] getPreprohibited()
    {
        return null;
    }

    /**
     *  Specifies a list of properties of algorithms that cannot be run on the
     *  target code after this algorithm is run.  See
     *  <code>RequisiteProperty</code> for more details.  This method should
     *  only be overridden if there is a nonempty set of prohibited algorithms.
     *  @return a list of prohibited algorithms to run after this algorithm
     */
    public sandmark.config.RequisiteProperty[] getPostprohibited()
    {
        return null;
    }
    public final String toString() {
        return getShortName();
    }
}


