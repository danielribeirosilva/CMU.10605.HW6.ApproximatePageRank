import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;



public class ApproxPageRank {
	
	// from a set of selected nodes, get their neighbors from file 
	// and add them as a list to the provided HashMap
	public static void cacheNodeNeighborsFromFile(String inputPath, HashSet<String> nodesToBeCached, HashMap<String,LinkedList<String>> neighbors){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			String line;
			
			while( (line = br.readLine()) != null){
				
				String nodeAndNeighbors[] = line.split("[\\t]",2);
				String node = nodeAndNeighbors[0];
				
				if(nodesToBeCached.contains(node)){
					String[] splitNeighbors = nodeAndNeighbors[1].split("[\\t]");
					LinkedList<String> neighborsList = new LinkedList<String>(Arrays.asList(splitNeighbors));
					neighbors.put(node, neighborsList);
					nodesToBeCached.remove(node);
				}
			}
			
			br.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}//cache function end

	
	public static double computeConductance(HashSet<String> S, HashMap<String,LinkedList<String>> neighbors){
		
		//compute volume
		double volume = 0D;
		for(String node : S){
			volume += neighbors.get(node).size();
		}
		
		//compute boundary
		double boundary = 0D;
		for(String node : S){
			LinkedList<String> nodeNeighbors = neighbors.get(node); 
			for(String neighbor : nodeNeighbors){
				if(!S.contains(neighbor)){
					boundary++;
				}
			}
		}
		
		// return conductance
		return boundary / volume;
		
	}
	
	
	
	public static void main(String[] args) {
		
		//check args
		if(args.length != 4){
			System.err.println("Not all arguments provided");
			System.exit(0);
		}
		//get args
		String inputPath = args[0];
		String seed = args[1];
		double alpha = Double.parseDouble(args[2]);
		double epsilon = Double.parseDouble(args[3]);
		
		//initialize structures
		HashMap<String,LinkedList<String>> neighbors = new HashMap<String,LinkedList<String>>(); 
		HashMap<String,Double> pageRank = new HashMap<String,Double>(); 
		HashMap<String,Double> residual = new HashMap<String,Double>();
		HashSet<String> nodesToBeCached = new HashSet<String>();
		HashSet<String> nodesToPushFrom = new HashSet<String>();
		
		//add seed 
		residual.put(seed, 1D);
		pageRank.put(seed, 0D);
		nodesToPushFrom.add(seed);
		nodesToBeCached.add(seed);
		cacheNodeNeighborsFromFile(inputPath, nodesToBeCached, neighbors);
		nodesToBeCached.clear();
		
		//MAIN LOOP
		//stop when no more nodes have r/d > epsilon
		while(!nodesToPushFrom.isEmpty()){
			
			//push for each node in the list till all have r/d < epsilon
			while(!nodesToPushFrom.isEmpty()){
				
				//push from all nodes in list
				for(String n : nodesToPushFrom){
					
					double nodePR = pageRank.containsKey(n) ? pageRank.get(n) : 0D;
					double nodeR = residual.get(n);
					
					//PUSH OPERATION
					//update node's page rank
					pageRank.put(n, nodePR + alpha * nodeR);
					//update node's residue
					residual.put(n, (1D - alpha) * nodeR / 2D);
					//update neighbors' residue
					for(String neighbor : neighbors.get(n)){
						double neighborR = residual.containsKey(neighbor) ? residual.get(neighbor) : 0D;
						residual.put(neighbor, neighborR + (1D - alpha) * nodeR / (2D * neighbors.get(n).size()) );
					}
				}
				
				//clear set to prepare for update
				nodesToPushFrom.clear();
				
				//update list using cached nodes
				for(String n : neighbors.keySet()){
					//compute r/d
					Double rdRatio = residual.get(n) / neighbors.get(n).size();
					//remove if below epsilon
					if(rdRatio >= epsilon){
						nodesToPushFrom.add(n);
					}
				}
			}
			
			//read file for uncached candidates of having r/d > epsilon
			//criterion: if r > epsilon, cache node
			for(Entry<String, Double> pair : residual.entrySet()){
				if(pair.getValue() >= epsilon && !neighbors.containsKey(pair.getKey())){
					nodesToBeCached.add(pair.getKey());
				}
			}
			
			//cache candidates
			cacheNodeNeighborsFromFile(inputPath, nodesToBeCached, neighbors);
			nodesToBeCached.clear();
			System.out.println("reading file");
			
			//update list using cached nodes
			for(String n : neighbors.keySet()){
				//compute r/d
				Double rdRatio = residual.get(n) / neighbors.get(n).size();
				//remove if below epsilon
				if(rdRatio >= epsilon){
					nodesToPushFrom.add(n);
				}
			}
			
		}
		
		//print result
		for(String n : pageRank.keySet()){
			System.out.println(n+"\t"+pageRank.get(n).toString());
		}
		
		System.out.println("\n------------------------------------------------------------\n");
		
		
		//BUILD LOW-CONDUCTANCE SUBGRAPH
		//cache all pages on the PageRank list
		nodesToBeCached.clear();
		neighbors.clear();
		for(String n : pageRank.keySet()){
			if(!neighbors.containsKey(n)){
				nodesToBeCached.add(n);
			}
		}
		cacheNodeNeighborsFromFile(inputPath, nodesToBeCached, neighbors);
		
		//initialize sets
		HashSet<String> S = new HashSet<String>();
		HashSet<String> SStar = new HashSet<String>();
		
		//seed
		S.add(seed);
		SStar.addAll(S);
		
		for(String n : neighbors.keySet()){
			
			// disregard seed
			if(n.equals(seed)){
				continue;
			}
			
			// add node to S
			S.add(n);
			
			// if Phi(S) < Phi(S*) => S* <- S
			double conductanceS = computeConductance(S,neighbors);
			double conductanceSStar = computeConductance(SStar,neighbors);
			if(conductanceS < conductanceSStar){
				SStar.addAll(S);
			}
			System.out.println("S: " + String.valueOf(S.size()));
			System.out.println("S*: " + String.valueOf(SStar.size()));
			
		}
		
		//print S*
		for( String n : SStar){
			System.out.println(n+"\t"+pageRank.get(n).toString());
		}
		System.out.println("Total nodes before: " + String.valueOf(pageRank.keySet().size()));
		System.out.println("Total nodes after: " + String.valueOf(SStar.size()));
		
		
		
	}//main end


}//class end
