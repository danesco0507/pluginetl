package graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class WeightedGraph<T> {
	// an enum for the three stated used by the Depth first search
    public enum State { UNVISITED, VISITED, COMPLETE };

    // a list to hold all the vertices
    private ArrayList<Vertex> vertexList;

    // list to hold the edges 
    // not really used for anything
    // but display purposes
    private ArrayList<Edge> edgeList;

    public WeightedGraph()
    {
        vertexList = new ArrayList<>();
        edgeList = new ArrayList<>();
    }

    public Set<T> getVerticesValues(){
    	Set<T> values = new HashSet<T>();
    	for(Vertex v: vertexList){
    		values.add(v.getValue());
    	}
    	return values;
    }
    
    public void removeKMostWeightedEdges(int k){
    	if (k < edgeList.size()){
    		while(k>0){
    			removeMostWeightedEdge();
    		}
    	}
    	else{
    		for(Edge e: edgeList){
    			edgeList.remove(e);
    		}
    	}
    }
    
    public void removeMostWeightedEdge(){
    	Edge e = edgeList.get(0);
    	for(Edge ej:edgeList){
    		if(ej.getCost()>e.getCost()){
    			e = ej;
    		}
    	}
    	edgeList.remove(e);
    }

    public void addEdge(T x, T y, double cost)
    {
        Edge e = new Edge(x, y, cost);
        edgeList.add(e);
    }
    
    public void addVertex(T x){
    	if(findVertex(x)==null){
	    	Vertex v = new Vertex(x);
	    	vertexList.add(v);
    	}
    }

    public Vertex findVertex(T v)
    {
        for (Vertex each : vertexList)
        {
            if (each.getValue().equals(v))
                return each;
        }
        return null;
    }

    public String toString()
    {
        String retval = "";
        for (Vertex each : vertexList)
        {
            retval += each.toString() + "\n";
        }
        return retval;
    }

    public String edgesToString()
    {
        String retval = "";
        for (Edge each : edgeList)
        {
            retval += each;
        }
        return retval;
    }

    // get first node and call recursive method
    // check if is graph is connected
    public boolean DepthFirstSearch()
    {
        if (vertexList.isEmpty()) return false;

        // get first node
        Vertex root = vertexList.get(0);
        if (root==null) return false;

        // call recursive function
        DepthFirstSearch(root);
        return isConnected();
    }

    // recurse through nodes
    private void DepthFirstSearch(Vertex v)
    {
        v.setState(State.VISITED);

        // loop through neighbors
        for (Vertex each : v.getAdjacentList())
        {
            if (each.getState()==State.UNVISITED)
            {
                DepthFirstSearch(each);
            }
        }
        v.setState(State.COMPLETE);
    }

    // test if DFS returned a connected graph
    public boolean isConnected()
    {
        for (Vertex each : vertexList)
        {
            if (each.getState() != State.COMPLETE)
                return false;
        }
        return true;
    }

    // vertex class
    class Vertex
    {
        T value;
        ArrayList<Vertex> adjacent;
        State state;

        public Vertex(T v)
        {
            value = v;
            adjacent = new ArrayList<>();
            state = State.UNVISITED;
        }

        public State getState()
        {
            return state;
        }

        public void setState(State s)
        {
            state = s;
        }

        public T getValue()
        {
            return value;
        }

        public void addNeighbor(Vertex n)
        {
            adjacent.add(n);
        }

        public ArrayList<Vertex> getAdjacentList()
        {
            return adjacent;
        }

        public String toString()
        {
            String retval = "";
            retval += "Vertex: " + value + ":";
            for (Vertex each : adjacent)
            {
                retval += each.getValue() + " ";
            }
            return retval;
        }


    }

    // edge class
    class Edge
    {
        private Vertex x;
		private Vertex y;
        private double cost;
        
        public Vertex getX() {
			return x;
		}


		public void setX(Vertex x) {
			this.x = x;
		}


		public Vertex getY() {
			return y;
		}


		public void setY(Vertex y) {
			this.y = y;
		}


		public double getCost() {
			return cost;
		}


		public void setCost(double cost) {
			this.cost = cost;
		}

        public Edge(T v1, T v2, double cost)
        {
            // check to see if first vertex exists
            x = findVertex(v1);
            if (x == null) 
            {
                // doesn't exist, add new
                x = new Vertex(v1);
                // and add to master list
                vertexList.add(x);
            }
            // same for second vertex
            y = findVertex(v2);
            if (y == null) 
            {
                y = new Vertex(v2);
                vertexList.add(y);
            }
            // add each vertex to the adjacent list for the other
            x.addNeighbor(y);
            y.addNeighbor(x);
            this.cost = cost;

        }


        public String toString()
        {
            return "Edge X:" + x.getValue() + " Y:" + y.getValue() + "\n";
        }


    }
    
    public Set<WeightedGraph<T>> primSubGraphs(){
    	
    	Set<WeightedGraph<T>> primGraphs = new HashSet<WeightedGraph<T>>();
    	ArrayList<Vertex> vs = this.vertexList;
    	ArrayList<Edge> es = this.edgeList;
    	while(!vs.isEmpty()){
    		Vertex initial = vs.get(0);
    		WeightedGraph<T> wg = new WeightedGraph<T>();
    		wg.addVertex(initial.getValue());
    		vs.remove(initial);
    		Edge e = getLeastCostEdge(es, initial);
    		while(e!=null){
    			wg.addEdge(e.getX().getValue(), e.getY().getValue(), e.getCost());
    			es.remove(e);
    			if( e.getX().equals(initial)){
    				initial = e.getY();
    				vs.remove(e.getY());
    			}
    			else{
					initial = e.getX();
					vs.remove(e.getX());
				}
    			e = getLeastCostEdge(es, initial);
    		}
    		primGraphs.add(wg);
    	}
		return primGraphs;
    }
    
    public ArrayList<Edge> getVertexEdges(ArrayList<Edge> edges, Vertex v){
    	ArrayList<Edge> es = new ArrayList<Edge>();
    	for(Edge e:edges){
    		if(e.getX().equals(v) || e.getY().equals(v)){
    			es.add(e);
    		}
    	}
    	return es;
    }
    
    public Edge getLeastCostEdge(ArrayList<Edge> edges, Vertex v ){
    	
    	ArrayList<Edge> ves = getVertexEdges(edges, v);
    	
    	if (ves.isEmpty()) return null;
    	
    	Edge e = ves.get(0);
    	for(Edge ej:ves){
    		if(ej.getCost()<e.getCost()){
    			e = ej;
    		}
    	}
    	
    	return e;
    }
}
