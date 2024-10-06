package sandmark.program;

/**
 * The superclass of all program objects.
 * For more specific editing operation and access methods,
 * see specific subclasses.
 * For more background, see the {@link sandmark.program} package overview.
 *
 * <P> Program objects maintain an notion of <i>versions</i> to
 * allow for a clean implementation of caching.  The methods listed here
 * keep this information and all internal caching information consistent.
 * After making any change through other means (for example, editing the
 * BCEL instruction list), the user must call the program object's
 * {@link sandmark.program.Object#mark() mark()} method to inform the
 * object that it has changed.
 *
 * <P> There is a cache associated with each program object
 * that stores information that is useful to internal code and external
 * packages  such as the Statistics package.  When available, an application
 * object's getX() method should be called to access information rather than
 * constructing a new X object.   The cache is cleared whenever the
 * object's mark() method is called.
 *
 * <P> Operations on these objects act locally.
 * Changing a method name does not change any of the references to that method.
 * Global editing methods are provided in the
 * {@link sandmark.program.util sandmark.program.util}
 * package.
 *
 * @author Gregg Townsend and Kelly Heffner
 */

public abstract class Object {

   private static final boolean DEBUG = false;

   private int versionNumber = 0;
   private String name;
   private boolean mutable = true;

   private sandmark.program.Application application;
   private sandmark.program.Object parent;
   private java.util.HashMap cacheMap = new java.util.HashMap();
   private java.util.ArrayList members = new java.util.ArrayList();  //mapping from name to object

   private UserObjectConstraints constraints;

   /*package*/ Object() {}
   /*package*/ Object(sandmark.program.Object orig) {
      if(orig != null && orig.hasUserConstraints()) {
         UserObjectConstraints constraints = new UserObjectConstraints();
         constraints.copyFrom(orig.getUserConstraints());
         setUserConstraints(constraints);
      }
   }


   /**
    * Returns the name of this object.
    * @return this objects name
    */
   public String getName() {
      return name;
   }

   /**
       Returns the canonical representation for the application object,
       guaranteed to be unique across the application.  For example,
       a method's canonical name is classname.methodname(signature)
       whereas a class's canonical name is simply classname.
       @return a unique name for the object within the application
   */
   public abstract String getCanonicalName();

   /**
    * Renames this object.  Changing the name of an object is only a local
    * change; the code for the underlying object will change but any
    * references to the object will not be updated.  To make a global
    * change to the name of this object see
    * {@link sandmark.program.util.Renamer sandmark.program.util.Renamer}.
    *
    * @param newName the new name for the object
    */
   public void setName(String newName) {
      if(name != null && name.equals(newName))
	 return;
      
      name = newName;
      mark();
   }



   /**
    * Returns a concise representation for this object.
    * @return a one line representation
    */
   public String toString() {
      if(DEBUG)
      return super.toString() + ":" + name;
      else
          return name;
   }



   /**
    * Marks this object as immutable.
    * This action is irreversible.
    */
   Throwable imStack;
   public void setImmutable() {
      if(DEBUG) {
         try { throw new RuntimeException() ; }
         catch(Exception e) { imStack = e; }
      }
      mutable = false;
   }



   /**
    * Returns <CODE>true</CODE> if this object can be edited.
    * Objects loaded from the CLASSPATH are not editable.
    * @return true if this is a mutable object
    */
   public boolean isMutable() {
      return mutable;
   }



   /**
    * Marks that this object has been modified.
    * The object's cache is invalidated, its version number is incremented,
    * and its parent's {@link sandmark.program.Object#mark() mark} method
    * is called.
    *
    * <P> For the cache to be useful, this method must be called after any
    * change to the object's data.
    *
    * <P> Only mutable objects can be marked.
    * Attempting to mark an immutable object
    * throws <CODE>java.lang.UnsupportedOperationException</CODE>.
    */
   public void mark() {
      if (!mutable) {
         unsupp();      // throws java.lang.UnsupportedOperationException
      }

      versionNumber++;
      cacheMap = new java.util.HashMap();        //flush cache

      if (parent != null) {
         parent.mark();
      }
      
      notifyMarkListeners();
   }



   /**
    * Returns the version number of the object.  The version number of
    * the object is incremented each time a change has occurred within
    * the object either from a call to {@link sandmark.program.Object#mark()
    * mark()} or from one of the editing methods provided by this object.
    * @return the number of mark() calls
    */
   public int getVersion() {
      return versionNumber;
   }



   /**
    * Deletes this component from its parent, and from the jar file.
    * A deleted object is invalid and should not be used further.
    */
   public void delete() {
      if (parent == null) {
         throw new IllegalArgumentException
            ("Cannot delete an object that has no parent.");
      }

      parent.delete(this);
   }



   /**
    * Gets a reference to the Application that this object is part of.
    * @return the associated Application object
    */
   public Application getApplication() {
      return application;
   } // get the associated Application object



   /**
    * Returns the parent of this object, if any.
    * @return this object's parent, or null
    */
   public sandmark.program.Object getParent() {
      return parent;
   }



   /**
    * Returns the number of member (child) objects this object has.
    * @return a count of this object's members
    */
   public int getSize() {
      return members.size();
   }



   /**
    * Gets a member by name (one of possibly many).
    *
    * @param searchName the name of the member
    * @return the program object for the specified member
    */
   public sandmark.program.Object getMember(String searchName) {
      for(int i = 0 ; i < members.size() ; i++) {
         sandmark.program.Object member = (sandmark.program.Object)members.get(i);
         if(member.name.equals(searchName))
            return member;
      }
      return null;
   }



   /**
    * Returns a list of members (children) of this object, if any.
    *
    * @return this object's members, or null
    */
   public sandmark.program.Object[] getMembers() {
      return (sandmark.program.Object[])
         (members.toArray(new sandmark.program.Object[members.size()]));
   }



   /**
    * Returns an iterator over the members of this object.
    *
    * @return the members of this object
    */
   public java.util.Iterator members() {
      return members.iterator();
   }



   /**
    * Stores a value in this object's cache.
    * <b>Note:</b> This cache is flushed
    * on a call to {@link sandmark.program.Object#mark() mark()}.
    *
    * @param key the key for this cache data
    * @param value the value to cache
    */
   public void cache(java.lang.Object key, java.lang.Object value) {
      cacheMap.put(key, value);
   }



   /**
    * Retrieves a value from this object's cache.
    * <b>Note:</b> This cache is flushed
    * on a call to {@link sandmark.program.Object#mark() mark()}.
    *
    * @param key the key used to cache this data
    * @return cached data
    */
   public java.lang.Object retrieve(java.lang.Object key) {
      return cacheMap.get(key);
   }



   /**
    * Adds the given object as a child of this object.
    */
   Throwable addStack;
   /*package*/ void add(sandmark.program.Object object) { add(object,null); }
   /*package*/ void add(sandmark.program.Object object,sandmark.program.Object orig) {
      //check duplicates
       mark(); //kheffner & ash adding to an application should mark it
      if (members.contains(object)) {
         throw new IllegalArgumentException(this +
            " already contains a member " + object.name);
      }

      if(DEBUG) {
         try { throw new RuntimeException(); }
         catch(Exception e) { object.addStack = e; }
      }
      
      object.parent = this;
      object.application = this.application;
      members.add(object);
      
      if(orig == null)
         notifyAddedMember(object);
      else
         notifyCopiedMember(orig,object);
   }


   /**
    * Deletes the given child of this object.
    */
   /*package*/ void delete(sandmark.program.Object subobject) {
      subobject.onDelete();
      notifyDeletingMember(subobject);
      
      if (members.contains(subobject)) {
         members.remove(subobject);
         subobject.parent = null;
      } else {
         throw new RuntimeException(
            "Object " + subobject + " is not a member of " + this);
      }
      subobject.setImmutable();
   }



   /*package*/ void setApplication(sandmark.program.Application a) {
      application = a;
   }

   public UserObjectConstraints getUserConstraints() {
      if(constraints == null)
         constraints = new UserObjectConstraints();
      return constraints;
   }

    /**
        Used for serializing the user preference data, checks to
        see if there has been changes from the default prefrence
        settings.
    */
    /*package*/ boolean hasUserConstraints(){
        //Added by kheffner on 11/18/03
        return constraints != null;
    }

    /*package*/ void setUserConstraints(UserObjectConstraints c){
        constraints = c;
    }

   /**
    * Throws <CODE>java.lang.UnsupportedOperationException</CODE>.
    */
   /*package*/ int unsupp() {
      if(DEBUG) {
         System.err.println("made immutable by\n" + 
                            getStackTrace(imStack.getStackTrace()));
         System.err.println("added by \n" + (addStack == null ? null : 
                            getStackTrace(addStack.getStackTrace())));
      }
      throw new java.lang.UnsupportedOperationException(
         "immutable: " + this.toString());
   }
   
   private static String getStackTrace(StackTraceElement trace[]) {
      String s = "";
      for(int i = 0 ; i < trace.length ; i++)
         s += trace[i].toString() + "\n";
      return s;
   }
   
   protected void onDelete() {
      sandmark.program.Object allMembers[] = getMembers(); 
      for(int i = 0 ; i < allMembers.length ; i++)
         delete(allMembers[i]);
      
   }
   
   private java.util.Set mMemberChangeListeners = new java.util.HashSet();
   public void addObjectMemberChangeListener(ObjectMemberChangeListener l) {
      mMemberChangeListeners.add(l);
   }
   public void removeObjectMemberChangeListener(ObjectMemberChangeListener l) {
      mMemberChangeListeners.remove(l);
   }
   
   private void notifyDeletingMember(sandmark.program.Object member) {
      for(java.util.Iterator listeners = mMemberChangeListeners.iterator() ;
          listeners.hasNext() ; ) {
         ObjectMemberChangeListener l = 
            (ObjectMemberChangeListener)listeners.next();
         l.deletingObject(this,member);
      }
   }
   
   private void notifyAddedMember(sandmark.program.Object member) {
      for(java.util.Iterator listeners = mMemberChangeListeners.iterator() ;
          listeners.hasNext() ; ) {
         ObjectMemberChangeListener l = 
            (ObjectMemberChangeListener)listeners.next();
         l.addedObject(this,member);
      }
   }

   private void notifyCopiedMember(sandmark.program.Object orig,
                                   sandmark.program.Object copy) {
      for(java.util.Iterator listeners = mMemberChangeListeners.iterator() ;
          listeners.hasNext() ; ) {
         ObjectMemberChangeListener l = 
            (ObjectMemberChangeListener)listeners.next();
         l.copiedObject(this,orig,copy);
      }
   }
   
   private java.util.HashSet mMarkListeners = new java.util.HashSet();
   public void addMarkListener(MarkListener m) { mMarkListeners.add(m); }
   public void removeMarkListener(MarkListener m) { mMarkListeners.remove(m); }
   private void notifyMarkListeners() {
      for(java.util.Iterator listeners = mMarkListeners.iterator() ;
          listeners.hasNext() ; ) {
         MarkListener listener = (MarkListener)listeners.next();
         listener.objectMarked(this);
      }
   }
}

