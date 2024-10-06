package sandmark.gui;

/**
 * This class allows the representation of the JTable data for Statistics objcets.
 * There are three types of representation: one for classes, one for methods and 
 * one for packages.
 */

public class MetricTableModel extends javax.swing.table.AbstractTableModel
{
   private static abstract class MetricGetter {
      abstract Object getValue(sandmark.metric.Metric metric);
   }
   private static class AppMetricGetter extends MetricGetter {
      sandmark.program.Application mApp;
      AppMetricGetter(sandmark.program.Application app) {
         mApp = app;
      }
      Object getValue(sandmark.metric.Metric metric) {
         return new Integer(((sandmark.metric.ApplicationMetric)metric).
                            getMeasure(mApp));
      }
   }
   private static class ClassMetricGetter extends MetricGetter {
      sandmark.program.Class mClass;
      ClassMetricGetter(sandmark.program.Class clazz) {
         mClass = clazz;
      }
      Object getValue(sandmark.metric.Metric metric) {
         return new Integer(((sandmark.metric.ClassMetric)metric).
                            getMeasure(mClass));
      }
   }
   private static class MethodMetricGetter extends MetricGetter {
      sandmark.program.Method mMethod;
      MethodMetricGetter(sandmark.program.Method method) {
         mMethod = method;
      }
      Object getValue(sandmark.metric.Metric metric) {
         return new Integer(((sandmark.metric.MethodMetric)metric).
                            getMeasure(mMethod));
      }
   }

   private MetricGetter mGetter;
   private sandmark.metric.Metric mMetrics[];
   private java.util.Hashtable mMetricValues = new java.util.Hashtable();
   /**
    * Constructs the package level data representation.
    * @param app the application object
    */
   public MetricTableModel(sandmark.program.Application app) {
      mGetter = new AppMetricGetter(app);
      mMetrics = sandmark.newstatistics.Stats.getApplicationMetrics();
   }

    
   /**
    * Constructs the class level data representation.
    */
   public MetricTableModel(sandmark.program.Class clazz) {
      mGetter = new ClassMetricGetter(clazz);
      mMetrics = sandmark.newstatistics.Stats.getClassMetrics();
   }


   /**
    * Constructs the method level data representation.
    */
   public MetricTableModel(sandmark.program.Method method) {
      mGetter = new MethodMetricGetter(method);
      mMetrics = sandmark.newstatistics.Stats.getMethodMetrics();
   }

   /**
    * Returns the number of colmuns.
    * @return returns the number of columns in the table
    */
   public int getColumnCount() {
      return 2;
   }

   /**
    * Returns the current number of rows
    * @return the number of rows in the table.
    */
   public int getRowCount()
   {
      return mMetrics.length;
   }

   /**
    * Returns the value at the cell specified
    * @param row the row in the table
    * @param col the column in the table
    * @return the Object located at row, col in the table.
    */
   public Object getValueAt(int row, int col)
   {
      switch(col) {
      case 0:
         return mMetrics[row].getName();
      case 1:
         Object metricValue;
         synchronized(mMetricValues) {
            metricValue = mMetricValues.get(mMetrics[row]);
         }
         if(metricValue == null) {
            enqueue(mMetrics[row],row);
            return "[Calculating Metric Value]";
         }
         return metricValue;
      default:
         throw new Error("unknown column " + col);
      }
   }
   
   public String getColumnName(int index) {
      switch(index) {
      case 0:
         return "Metric";
      case 1:
         return "Value";
      default:
         throw new Error("unknown column");
      }
   }
   
   private static class MetricCalcTask {
      sandmark.metric.Metric metric;
      int row;
      MetricCalcTask(sandmark.metric.Metric m,int r) {
         metric = m;
         row = r;
      }
   }
   
   private class MetricCalculator implements Runnable {
      java.util.LinkedList queue = new java.util.LinkedList();
      public void run() {
         while(true) {
            MetricCalcTask task;
            synchronized(queue) {
               while(queue.isEmpty())
                  try { queue.wait(); }
               	catch(InterruptedException e) {}
               task = (MetricCalcTask)queue.remove(0);
            }
            Object value = mMetricValues.get(task.metric);
            if(value == null) {
               value = mGetter.getValue(task.metric);
               mMetricValues.put(task.metric,value);
            }
            fireTableCellUpdated(task.row,1);
         }
      }
      void enqueue(MetricCalcTask task) {
         synchronized(queue) {
            queue.add(task);
            queue.notifyAll();
         }
      }
   }
   
   private MetricCalculator mCalculator;
   private void enqueue(sandmark.metric.Metric metric,int row) {
      if(mCalculator == null) {
         mCalculator = new MetricCalculator();
         new Thread(mCalculator).start();
      }
      mCalculator.enqueue(new MetricCalcTask(metric,row));
   }
}
