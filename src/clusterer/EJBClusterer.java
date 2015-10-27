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
		
		Set<Set<CodeItem>> allht = constructHeritageTree(ejbs);
		System.out.println("------------------------------------------------------------");
		System.out.println(allht);
		System.out.println("------------------------------------------------------------");
		for (Set<CodeItem> ht : allht){
			Set<CodeItem> cluster = new HashSet<CodeItem>();
			cluster.addAll(ht);
			
			for(CodeItem ci : ht){
				for(CodeItem entity : entities)
				{
					for(ClassLevelRelation clr : sp.getPath(ci, entity)){
						cluster.add(clr.getFrom());
						cluster.add(clr.getTo());
					}
				}
			}
			
			firtsclusters.add(cluster);
		}
		
		for(Set<CodeItem> ht : allht){
			allNodes.removeAll(ht);
		}
		
		Set<Set<CodeItem>> lastclusters = new HashSet<Set<CodeItem>>();
		
		for (Set<CodeItem> ht : firtsclusters){
			Set<CodeItem> cluster = new HashSet<CodeItem>();
			cluster.addAll(ht);
			
			for(CodeItem ci : ht){
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
			System.out.println("-----------------heritage tree-------------------------------------------");
			System.out.println(ht);
			System.out.println("-----------------heritage tree-------------------------------------------");
			heritageTrees.add(ht);
		}
		return heritageTrees;
	}
	
	public Set<CodeItem> getHeritageTree(CodeItem item){
		Set<CodeItem> set = new HashSet<CodeItem>();
		Collection<ClassLevelRelation> allclr = graph.getOutEdges(item);
		
		for(ClassLevelRelation clr: allclr){
			System.out.println("-----------------is impl-------------------------------------------");
			System.out.println(clr);
			System.out.println(isExtensionOrImplementation(clr));
			System.out.println("-----------------is impl-------------------------------------------");
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
