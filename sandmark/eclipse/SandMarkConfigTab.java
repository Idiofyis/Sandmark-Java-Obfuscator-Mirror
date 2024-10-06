/*
 * Created on Mar 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package sandmark.eclipse;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.core.runtime.CoreException;

/**
 * @author ash
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class SandMarkConfigTab extends AbstractLaunchConfigurationTab
	implements SelectionListener,ConfigPropertyPanel.ChangeListener {
	public static final String ALG_NAME_KEY = "sandmark.Algorithm";
	public static final String ALG_PROPS_KEY = "sandmark.Algorithm.props";
	public static final String ALG_INPUT_FILE = "sandmark.Algorith.file";
	
	private sandmark.util.ConfigProperties mConfigProps =
		new sandmark.util.ConfigProperties
		(new String[][] {{"Input File","","The Input File","","S","A"}},null);
	
   private static sandmark.Algorithm sAlgs[];
   private ConfigPropertyPanel mCPP;
   private Combo mAlgList;
   private Composite mCPPContainer;
   
   static {
      java.util.ArrayList algs = new java.util.ArrayList();
      java.util.Collection algNames = 
         sandmark.util.classloading.ClassFinder.getClassesWithAncestor
         (sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR);
      for(java.util.Iterator algIt = algNames.iterator() ; algIt.hasNext() ; ) {
         String algName = (String)algIt.next();
         try {
            algs.add((sandmark.Algorithm)Class.forName(algName).newInstance());
         } catch(Exception e) { /* i guess it's not really an obfuscator */ }
      }
      sAlgs = (sandmark.Algorithm [])algs.toArray(new sandmark.Algorithm[0]);
      java.util.Arrays.sort(sAlgs,new java.util.Comparator() {
      	public int compare(Object o1,Object o2) {
      		return ((sandmark.Algorithm)o1).getShortName().compareTo
			   (((sandmark.Algorithm)o2).getShortName());
      	}
      });
   }

   /* (non-Javadoc)
    * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
    */
   public void createControl(Composite arg0) {
   	Composite comp = new Composite(arg0,SWT.NONE);
		setControl(comp);		
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(layout);
		comp.setLayoutData(gridData);
		comp.setFont(arg0.getFont());		
		mAlgList = new Combo(comp,SWT.DROP_DOWN|SWT.READ_ONLY);
		GridData data = new GridData();
		mAlgList.setLayoutData(data);
		mAlgList.setItems(getAlgNames());
		mAlgList.addSelectionListener(this);
		mAlgList.select(0);
		
		mCPPContainer = new Composite(comp,SWT.NONE);
		GridData cppContainerData = new GridData(GridData.FILL_BOTH);
		cppContainerData.grabExcessHorizontalSpace = true;
		cppContainerData.grabExcessVerticalSpace = true;
		mCPPContainer.setLayoutData(cppContainerData);
		GridLayout cppContainerLayout = new GridLayout();
		cppContainerLayout.numColumns = 1;
		mCPPContainer.setLayout(cppContainerLayout);
		
		setAlgorithm();
		createVerticalSpacer(comp, 2);
		Dialog.applyDialogFont(arg0);
   }
   
   private String[] getAlgNames() {
      String algNames[] = new String[sAlgs.length];
      for(int i = 0 ; i < sAlgs.length ; i++)
         algNames[i] = sAlgs[i].getShortName();
      return algNames;
   }
   
   private void setAlgorithm() {
   	  if(mCPP != null) {
   	  	 mCPP.updateProperties();
   	  	 mCPP.removeChangeListener(this);
   	  	 mCPP.dispose();
   	  }
   	  sandmark.Algorithm alg = sAlgs[mAlgList.getSelectionIndex()];
   	  mCPP = new ConfigPropertyPanel
	     (mCPPContainer,new sandmark.util.ConfigProperties[] {
	     		mConfigProps,
	     		alg.getConfigProperties(),
	     }, ~(0L),null);
   	  mCPP.addChangeListener(this);
   	  GridData cppData = new GridData(GridData.FILL_BOTH);
   	  cppData.grabExcessHorizontalSpace = true;
   	  cppData.grabExcessVerticalSpace = true;
   	  mCPP.setLayoutData(cppData);
   	  mCPPContainer.layout(true);
   	  ((Composite)this.getControl()).layout(true);
   }
   
   public void changed() { 
   	System.out.println("changed"); 
   	this.setDirty(true); 
   	updateLaunchConfigurationDialog();
   	}

   /* (non-Javadoc)
    * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
    */
   public void setDefaults(ILaunchConfigurationWorkingCopy arg0) {
   }

   /* (non-Javadoc)
    * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
    */
   public void initializeFrom(ILaunchConfiguration arg0) {
   	try {
   		String inputFile = arg0.getAttribute(ALG_INPUT_FILE,(String)null);
   		mConfigProps.setProperty("Input File",inputFile);
   		String algName = arg0.getAttribute(ALG_NAME_KEY,sAlgs[0].getShortName());
   		int algIndex = mAlgList.indexOf(algName);
   		mAlgList.select(algIndex);
   		sandmark.Algorithm alg = sAlgs[algIndex];
   		sandmark.util.ConfigProperties cp = alg.getConfigProperties();
   		System.out.println(algName + " " + algIndex + " " + alg + " " + 
   	       cp + " " + mAlgList.getSelectionIndex());
   		if(cp == null)
   			return;
   		java.util.Map props = new java.util.HashMap();
   		props = arg0.getAttribute(ALG_PROPS_KEY,props);
   		for(java.util.Iterator propNames = props.keySet().iterator() ; 
   	        propNames.hasNext() ; ) {
   			String propName = (String)propNames.next();
   			System.out.println(propName + " " + props.get(propName));
   			cp.setProperty(propName,(String)props.get(propName));
   		}
   		setAlgorithm();
   	} catch(CoreException e) {
   		//not much to do other than just not initialize
   	}
   }

   /* (non-Javadoc)
    * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
    */
   public void performApply(ILaunchConfigurationWorkingCopy arg0) {
   	String inputFile = mConfigProps.getProperty("Input File");
   	arg0.setAttribute(ALG_INPUT_FILE,inputFile);
   	sandmark.Algorithm alg = sAlgs[mAlgList.getSelectionIndex()];
   	arg0.setAttribute(ALG_NAME_KEY,alg.getShortName());
   	if(alg.getConfigProperties() != null) {
   		java.util.Map props = new java.util.HashMap();
   		for(java.util.Iterator propIt = alg.getConfigProperties().properties() ;
   		propIt.hasNext() ; ) {
   			String propName = (String)propIt.next();
   			props.put(propName,alg.getConfigProperties().getProperty(propName));
   		}
   	}
   }

   /* (non-Javadoc)
    * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
    */
   public String getName() {
      return "SandMark Build Configuration";
   }
   
   public void widgetSelected(SelectionEvent e) {
      System.out.println("non-default");
      setDirty(true);
      setAlgorithm();
      updateLaunchConfigurationDialog();
   }
   
   public void widgetDefaultSelected(SelectionEvent e) {
      System.out.println("default");
      setDirty(true);
      setAlgorithm();
      updateLaunchConfigurationDialog();
   }	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab#getLocationLabel()
	 */
	protected String getLocationLabel() {
		return "SandMarkConfigTab.1"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.externaltools.internal.launchConfigurations.ExternalToolsMainTab#getWorkingDirectoryLabel()
	 */
	protected String getWorkingDirectoryLabel() {
		return "SandMarkBuilder";//$NON-NLS-1$
	}
	
}
