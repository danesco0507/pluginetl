package clusterer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import subkdm.kdmObjects.CodeItem;
import subkdm.kdmRelations.ClassLevelRelation;
import subkdm.kdmRelations.TypeRelation;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class EJBClusterer {
	
	DirectedGraph<CodeItem, ClassLevelRelation> graph;
	
	public EJBClusterer(){
		graph = new DirectedSparseMultigraph<CodeItem, ClassLevelRelation>();
	}
	
	public void constructGraph(Set<CodeItem> classes, Set<ClassLevelRelation> relations){
		for(CodeItem c : classes){
			graph.addVertex(c);
		}
		
		for(ClassLevelRelation clr : relations){
			graph.addEdge(clr, clr.getFrom(), clr.getTo());
		}
	}
	
	public Set<Set<CodeItem>> makeCluster(Set<CodeItem> ejbs, Set<CodeItem> entities){
		
		Set<Set<CodeItem>> firtsclusters = new HashSet<Set<CodeItem>>();
		
		DijkstraShortestPath<CodeItem, ClassLevelRelation> sp = 
				new DijkstraShortestPath<CodeItem, ClassLevelRelation>(graph, true);
		
		Set<CodeItem> allNodes = new HashSet<CodeItem>(graph.getVertices());
		
		Set<Set<CodeItem>> allEjbHt = constructHeritageTree(ejbs);
		Set<Set<CodeItem>> allEntityHt = constructHeritageTree(entities);
		
		for (Set<CodeItem> ejbHt : allEjbHt)
		{
			Set<CodeItem> cluster = new HashSet<CodeItem>();
			cluster.addAll(ejbHt);
			//ESTO PUEDE SER MUY LENTO
			for(CodeItem ejbHtElem : ejbHt)
			{		
				for(Set<CodeItem> entityHt : allEntityHt)
				{
					for(CodeItem entHtElem : entityHt)
					{
						if(!sp.getPath(ejbHtElem, entHtElem).isEmpty()){
							cluster.addAll(entityHt);
							for(ClassLevelRelation clr : sp.getPath(ejbHtElem, entHtElem)){
								cluster.add(clr.getFrom());
								cluster.add(clr.getTo());
							}
						}
					}
				}
			}
			firtsclusters.add(cluster);
		}
		
		for(Set<CodeItem> ht : allEjbHt){
			allNodes.removeAll(ht);
		}
		
		Set<Set<CodeItem>> lastclusters = new HashSet<Set<CodeItem>>();
		
		for (Set<CodeItem> subCluster : firtsclusters){
			Set<CodeItem> cluster = new HashSet<CodeItem>();
			cluster.addAll(subCluster);
			
			for(CodeItem ci : subCluster){
				for(CodeItem item : allNodes)
				{
					for(ClassLevelRelation clr : sp.getPath(ci, item)){
						cluster.add(clr.getFrom());
						cluster.add(clr.getTo());
					}
					
					for(ClassLevelRelation clr : sp.getPath(item, ci)){
						cluster.add(clr.getFrom());
						cluster.add(clr.getTo());
					}
				}
			}
			
			lastclusters.add(cluster);
		}
		
		return lastclusters;
	}
	
	public Set<Set<CodeItem>> constructHeritageTree(Set<CodeItem> ejbs){
		Set<Set<CodeItem>> heritageTrees = new HashSet<Set<CodeItem>>();
		for(CodeItem ejb : ejbs){
			Set<CodeItem> ht = new HashSet<CodeItem>();
			ht = getHeritageTree(ejb);
			heritageTrees.add(ht);
		}
		return heritageTrees;
	}
	
	public Set<CodeItem> getHeritageTree(CodeItem item){
		Set<CodeItem> set = new HashSet<CodeItem>();
		Collection<ClassLevelRelation> allclr = graph.getOutEdges(item);
		
		for(ClassLevelRelation clr: allclr){
			if(isExtensionOrImplementation(clr)){
				set.add(clr.getFrom());
				set.addAll(getHeritageTree(clr.getTo()));
			}
		}
		return set;
	}
	
	public boolean isExtensionOrImplementation(ClassLevelRelation clr){
		for(TypeRelation tr: clr.getTypeRelations()){
			if(tr.getName().equalsIgnoreCase("Extends") || tr.getName().equalsIgnoreCase("Implements")){
				return true;
			}
		}
		return false;
	}
	
}
