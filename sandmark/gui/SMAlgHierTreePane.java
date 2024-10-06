package sandmark.gui;

public class SMAlgHierTreePane extends javax.swing.JTree {
    private java.util.Hashtable mKeyToNode;

    public SMAlgHierTreePane(){
	mKeyToNode = new java.util.Hashtable();
	javax.swing.tree.DefaultMutableTreeNode root = createNodes();
	javax.swing.tree.DefaultTreeModel model = 
	    (javax.swing.tree.DefaultTreeModel)getModel();
	model.setRoot(root);
	model.nodeStructureChanged(root);
	getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
	setShowsRootHandles(true);
	setEditable(false);
    }

    private javax.swing.tree.DefaultMutableTreeNode add(
       javax.swing.tree.DefaultMutableTreeNode root,
       String label, 
       String url) {
	SMAlgHierTreeNode dep = new SMAlgHierTreeNode(label,url);
	root.add(dep);
	mKeyToNode.put(label,dep);
        return dep;
    }

    private void addAll(
        javax.swing.tree.DefaultMutableTreeNode root,
	int classKind) {
	java.util.Collection names =
	    sandmark.util.classloading.ClassFinder.getClassesWithAncestor(classKind);
	for(java.util.Iterator it = names.iterator() ; it.hasNext() ; ) {
	    try {
	        sandmark.Algorithm alg = (sandmark.Algorithm)
	            Class.forName((String)it.next()).newInstance();
                add(root, alg.getShortName(),alg.getAlgURL());
	    } catch(InstantiationException e) {
	        // not really a class, ignore it
	    } catch(IllegalAccessException e) {
	        // not really a class, ignore it
	    } catch(ClassNotFoundException e) {
	        // not really a class, ignore it
	    }
	}
    }

    private javax.swing.tree.DefaultMutableTreeNode createNodes(){
	javax.swing.tree.DefaultMutableTreeNode root = new SMAlgHierTreeNode("SandMark",sandmark.Console.getHelpURL());
	mKeyToNode.put("SandMark",root);

        add(root, "running SandMark","sandmark/html/dependencies.html");
        add(root, "release documents","sandmark/html/releasedocs.html");
        add(root, "publications","sandmark/html/publications.html");
        add(root, "bugs","sandmark/html/bugs.html");

	javax.swing.tree.DefaultMutableTreeNode watermark = 
            add(root, "watermark",sandmark.watermark.GeneralWatermarker.getHelpURL());
	
	javax.swing.tree.DefaultMutableTreeNode dynWM = 
            add(watermark, "dynamic",sandmark.watermark.DynamicWatermarker.getHelpURL());
        addAll(dynWM, sandmark.util.classloading.IClassFinder.DYN_WATERMARKER);

	javax.swing.tree.DefaultMutableTreeNode staticWM  = 
            add(watermark, "static",sandmark.watermark.StaticWatermarker.getHelpURL());
        addAll(staticWM, sandmark.util.classloading.IClassFinder.STAT_WATERMARKER);

	javax.swing.tree.DefaultMutableTreeNode obfuscate  = 
            add(root, "obfuscate",sandmark.gui.ObfuscatePanel.getHelpURL());
        addAll(obfuscate, sandmark.util.classloading.IClassFinder.GEN_OBFUSCATOR);
    
	javax.swing.tree.DefaultMutableTreeNode optimize  = 
            add(root, "optimize",sandmark.optimise.Optimise.getHelpURL());
        addAll(optimize, sandmark.util.classloading.IClassFinder.GEN_OPTIMIZER);
   
	javax.swing.tree.DefaultMutableTreeNode hacking  = 
            add(root, "extending SandMark","sandmark/html/extending.html");
        add(hacking, "adding an obfuscator","sandmark/obfuscate/doc/hacking.html");
        add(hacking, "adding a watermarker","sandmark/watermark/doc/hacking.html");
        add(hacking, "adding an optimizer","sandmark/optimise/doc/hacking.html");
        add(hacking, "static analysis","sandmark/analysis/doc/hacking.html");
        add(hacking, "program objects","sandmark/program/doc/hacking.html");
        add(hacking, "graph library","sandmark/util/newgraph/doc/hacking.html");
        add(hacking, "opaque predicates","sandmark/util/opaquepredicatelib/doc/help.html");
        add(hacking, "generating Java","sandmark/util/javagen/doc/hacking.html");
        add(hacking, "expression trees","sandmark/util/newexprtree/doc/hacking.html");
	javax.swing.tree.DefaultMutableTreeNode utils  = 
            add(hacking, "utilities","sandmark/util/doc/hacking.html");
        add(utils, "splitting integers","sandmark/util/splitint/doc/hacking.html");
        add(utils, "class loading","sandmark/util/classloading/doc/hacking.html");
        add(utils, "executing applications","sandmark/util/exec/doc/hacking.html");
        add(hacking, "coding standard","sandmark/html/codingStandard.html");

        add(root, "view",sandmark.view.View.getHelpURL());
        add(root, "decompile",sandmark.decompile.Decompile.getHelpURL());
        add(root, "diff",sandmark.gui.diff.DiffFrame.getHelpURL());
        add(root, "scripting","sandmark/html/scripting.html");
        add(root, "quick protect",QuickProtectPanel.getHelpURL());

	return root;
    }
    public void selectNode(String key) {
	javax.swing.tree.DefaultMutableTreeNode node =
	    (javax.swing.tree.DefaultMutableTreeNode)
	    mKeyToNode.get(key);
	javax.swing.tree.TreePath path =
	    new javax.swing.tree.TreePath(node.getPath());
	setSelectionPath(path);
    }
}

