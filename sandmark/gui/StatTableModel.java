package sandmark.gui;

/**
 * This class allows the representation of the JTable data for Statistics objcets.
 * There are three types of representation: one for classes, one for methods and 
 * one for packages.
 */

public class StatTableModel extends javax.swing.table.AbstractTableModel {
   private abstract static class Stat {
      abstract Object getValue(sandmark.program.Object o);
      String name;
      String getMethodName;
      Class type;
      Stat(String n,String mn,Class t) {
         name = n;
         getMethodName = mn;
         type = t;
      }
   }
   private static class ClassStat extends Stat {
      ClassStat(String n,String mn,Class t) { super(n,mn,t); }
      Object getValue(sandmark.program.Object o) {
         sandmark.newstatistics.Stats stats = 
            o.getApplication().getStatistics();
         sandmark.program.Class clazz = (sandmark.program.Class)o;
         try {
            java.lang.reflect.Method statGetter = 
               stats.getClass().getMethod
               (getMethodName,new Class[] { String.class,String.class });
            return statGetter.invoke(stats,new Object[] {
               clazz.getPackageName(),clazz.getName()
            });
         } catch(Exception e) {
            throw new Error(e.toString());
         }
      }
   }
   private static class MethodStat extends Stat {
      MethodStat(String n,String mn,Class t) { super(n,mn,t); }
      Object getValue(sandmark.program.Object o) {
         sandmark.program.Method method = 
            (sandmark.program.Method)o;
         sandmark.newstatistics.Stats stats = 
            o.getApplication().getStatistics();
         try {
            java.lang.reflect.Method statGetter = 
               stats.getClass().getMethod
               (getMethodName,
                new Class[] { String.class,String.class,String.class });
            return statGetter.invoke(stats,new Object[] {
               method.getEnclosingClass().getPackageName(),
               method.getEnclosingClass().getName(),method.getName()
            });
         } catch(Exception e) {
            throw new Error(e.toString());
         }
      }
   }
   private static final Stat sClassStats[] = new Stat[] {
      new ClassStat("Number of Methods","getNumMethods",Integer.class),
      new ClassStat("Number of Public methods","getNumberOfPublicMethods",Integer.class),
      new ClassStat("Number of instance methods","getNumberOfInstanceMethods",Integer.class),
      new ClassStat("Number of Static fields","getNumberOfInstanceVariables",Integer.class),
      new ClassStat("Number of static fields","getNumberOfStaticFields",Integer.class),
      new ClassStat("Number of non-static fields","getNumNonStaticFields",Integer.class),
      new ClassStat("Number of fields of non-basic type","getNumFieldsNonBasicTypes",Integer.class),
      new ClassStat("Number of conditional Statements","getNumberOfConditionalStatements",Integer.class),
      new ClassStat("Scalars","getNumberOfScalars",Integer.class),
      new ClassStat("Vectors","getNumberOfVectors",Integer.class),
      new ClassStat("Number of API calls","getNumberOfApiCalls",Integer.class),
      new ClassStat("Number of Methods in scope","getNumberOfMethodsInScope",Integer.class),
      new ClassStat("Number of hierarchy level","getClassHierarchyLevel",Integer.class),
      new ClassStat("Number of inherited methods","getNumberOfMethodsInherited",Integer.class),
      new ClassStat("Number of overridden methods","getNumberOfMethodsOverridden",Integer.class),
      new ClassStat("Number of Subclasses","getNumberOfSubClasses",Integer.class),
   };
   private static final Stat sMethodStats[] = new Stat[] {
      new MethodStat("Calls Dynamic Methods","callsDynamicMethods",Boolean.class),
      new MethodStat("Calls Static Methods","callsStaticMethods",Boolean.class),
      new MethodStat("Instruction List Byte Length","getMethodSizeInBytes",Integer.class),
      new MethodStat("Throws or Catches Exception","throwsCatchesExceptions",Boolean.class),
   };
   private sandmark.program.Object mObj;
   private Stat stats[];
   private java.util.Hashtable mStatValues = new java.util.Hashtable();
   StatTableModel(sandmark.program.Class clazz) {
      mObj = clazz;
      stats = sClassStats;
   }
   StatTableModel(sandmark.program.Method method) {
      mObj = method;
      stats = sMethodStats;
   }

   public int getColumnCount() {
      return 2;
   }

   public int getRowCount() {
      return stats.length;
   }

   public Object getValueAt(int rowIndex,int columnIndex) {
      switch(columnIndex) {
      case 0:
         return stats[rowIndex].name;
      case 1:
         Object value;
         synchronized(mStatValues) {
            value = mStatValues.get(stats[rowIndex]);
         }
         if(value == null) {
            enqueue(stats[rowIndex],rowIndex);
            return "[Calculating Statistic Value]";
         }
         return value;
      default:
         throw new Error("unknown column " + columnIndex);
      }
   }
   
   public String getColumnName(int index) {
      switch(index) {
      case 0:
         return "Statistic";
      case 1:
         return "Value";
      default:
         throw new Error("unknown column");
      }
   }
   
   private static class StatCalcTask {
      Stat stat;
      int row;
      StatCalcTask(Stat s,int r) {
         stat = s;
         row = r;
      }
   }
   
   private class StatCalculator implements Runnable {
      java.util.LinkedList queue = new java.util.LinkedList();
      public void run() {
         while(true) {
            StatCalcTask task;
            synchronized(queue) {
               while(queue.isEmpty())
                  try { queue.wait(); }
               	catch(InterruptedException e) {}
               task = (StatCalcTask)queue.remove(0);
            }
            Object value = mStatValues.get(task.stat);
            if(value == null) {
               value = task.stat.getValue(mObj);
               mStatValues.put(task.stat,value);
            }
            fireTableCellUpdated(task.row,1);
         }
      }
      void enqueue(StatCalcTask task) {
         synchronized(queue) {
            queue.add(task);
            queue.notifyAll();
         }
      }
   }
   
   private StatCalculator mCalculator;
   private void enqueue(Stat stat,int row) {
      if(mCalculator == null) {
         mCalculator = new StatCalculator();
         new Thread(mCalculator).start();
      }
      mCalculator.enqueue(new StatCalcTask(stat,row));
   }
}














