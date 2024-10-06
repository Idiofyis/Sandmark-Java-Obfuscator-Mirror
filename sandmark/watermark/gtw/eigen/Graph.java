import java.util.HashSet;

public class Graph {
    private int vertices;
    private HashSet [] adjacencyLists;

    public Graph(int _vertices) {
	vertices = (_vertices >= 0) ? _vertices : 0;
	adjacencyLists = new HashSet[vertices];
	for (int i = 0; i < adjacencyLists.length; i++)
	    adjacencyLists[i] = new HashSet();
    }

    private boolean checkRange(int v) {
	return v >= 0 && v < vertices;
    }

    public void addEdge(int v1, int v2) {
	if (checkRange(v1) && checkRange(v2) && v1 != v2) {
	    Edge e = new Edge(v1, v2);
	    adjacencyLists[v1].add(e);
	    adjacencyLists[v2].add(e);
	}
    }

    public boolean containsEdge(int v1, int v2) {
	if (checkRange(v1) && checkRange(v2) && v1 != v2) {
	    return adjacencyLists[v1].contains(new Edge(v1, v2));
	}
	else {
	    return false;
	}
    }

    public int degree(int v) {
	if (checkRange(v))
	    return adjacencyLists[v].size();
	else
	    return -1;
    }

    public int numVertices() {
	return vertices;
    }

    private class Edge {
	private int v1, v2;

	public Edge(int _v1, int _v2) {
	    v1 = _v1;
	    v2 = _v2;
	}

	public boolean equals(Object o) {
	    Edge e = (Edge)o;
	    if (v1 == e.v1)
		return v2 == e.v2;
	    else if (v1 == e.v2)
		return v2 == e.v1;
	    else
		return false;
	}

	public int hashCode() {
	    return v1 ^ v2;
	}
    }    
}

