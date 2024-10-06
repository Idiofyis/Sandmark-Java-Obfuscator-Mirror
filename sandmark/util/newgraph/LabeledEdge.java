package sandmark.util.newgraph;

public class LabeledEdge extends EdgeImpl {
    private String label;

    public LabeledEdge(java.lang.Object from, java.lang.Object to,
		       String label) {
	super(from, to);
	if (label == null)
	   throw new java.lang.NullPointerException("null label");
	this.label = label;
    }

    public String getLabel() {
	return label;
    }

    public String toString() {
	return getLabel();
    }

    public boolean equals(java.lang.Object o) {
	if (o instanceof LabeledEdge) {
	    LabeledEdge e = (LabeledEdge)o;
	    return getLabel().equals(e.getLabel())
		&& sourceNode().equals(e.sourceNode())
		&& sinkNode().equals(e.sinkNode());
	}
	else
	    return false;
    }
}
