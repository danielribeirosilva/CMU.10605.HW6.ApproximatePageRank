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
	public static void cacheNodeNeighborsFromFile(String inputPath, HashSet<String> nodesToBeCached, HashMap<String,String[]> neighbors){
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(inputPath));
			String line;
			
			while( (line = br.readLine()) != null){
				
				String nodeAndNeighbors[] = line.split("[\\t]");
				String node = nodeAndNeighbors[0];
				
				if(nodesToBeCached.contains(node)){
					String[] neighborsArray = Arrays.copyOfRange(nodeAndNeighbors,1,nodeAndNeighbors.length);
					neighbors.put(node, neighborsArray);
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
	
	//compute boundary contribution of a node w.r.t. a set S
	public static double computeBoundaryConstribution(String node, HashSet<String> S, HashMap<String,String[]> neighbors){
		
		double boundaryContribution = 0D;
		String[] nodeNeighbors = neighbors.get(node); 
		for(String neighbor : nodeNeighbors){
			if(!S.contains(neighbor)){
				boundaryContribution++;
			}
		}
		
		// return conductance
		return boundaryContribution;
	}
	
	
	
	public static double computeBoundary(HashSet<String> S, HashMap<String, String[]> neighbors){
		
		double boundary = 0D;
		for(String node : S){
			String[] nodeNeighbors = neighbors.get(node); 
			for(String neighbor : nodeNeighbors){
				if(!S.contains(neighbor)){
					boundary++;
				}
			}
		}
		
		// return conductance
		return boundary;
	}
	
	
	
	public static void main(String[] args) {
		
		//long startTime = System.currentTimeMillis();
		
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
		HashMap<String,String[]> neighbors = new HashMap<String,String[]>(); 
		HashMap<String,Double> pageRank = new HashMap<String,Double>(); 
		HashMap<String,Double> residual = new HashMap<String,Double>();
		HashSet<String> nodesToBeCached = new HashSet<String>();
		
		//add seed 
		residual.put(seed, 1D);
		pageRank.put(seed, 0D);
		nodesToBeCached.add(seed);
		cacheNodeNeighborsFromFile(inputPath, nodesToBeCached, neighbors);
		nodesToBeCached.clear();
		
		//MAIN LOOP
		//stop when no more nodes have r/d > epsilon
		boolean hasNodesToBePushed = true;
		while(hasNodesToBePushed){
			
			//push for each node in the list till all have r/d < epsilon
			boolean hasPushed = true;
			while(hasPushed){
				
				hasPushed = false;
				
				//go through cached nodes and try to push
				for(String n : neighbors.keySet()){
					
					//if r/d>=epsilon, push
					Double rdRatio = residual.get(n) / neighbors.get(n).length;
					if(rdRatio >= epsilon){
						hasPushed = true;
					}
					else{
						continue;
					}
					
					double nodePR = pageRank.containsKey(n) ? pageRank.get(n) : 0D;
					double nodeR = residual.get(n);
					
					//PUSH OPERATION
					//update node's page rank
					pageRank.put(n, nodePR + alpha * nodeR);
					//update node's residue
					residual.put(n, (1D - alpha) * nodeR / 2D);
					//update neighbors' residue
					String[] nodeNeighbors = neighbors.get(n);
					double nodeD = neighbors.get(n).length;
					for(String neighbor : nodeNeighbors){
						double neighborR = residual.containsKey(neighbor) ? residual.get(neighbor) : 0D;
						residual.put(neighbor, neighborR + (1D - alpha) * nodeR / (2D * nodeD) );
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
			//System.out.println("reading file");
			
			//check if has candidates to be pushed
			hasNodesToBePushed = false;
			for(String n : neighbors.keySet()){
				//compute r/d
				Double rdRatio = residual.get(n) / neighbors.get(n).length;
				if(rdRatio >= epsilon){
					hasNodesToBePushed = true;
					break;
				}
			}
			
		}
		
		//System.out.println(String.valueOf((double)(System.currentTimeMillis() - startTime)/1000D));
		
		
		//BUILD LOW-CONDUCTANCE SUBGRAPH
		//initialize sets
		HashSet<String> S = new HashSet<String>();
		HashSet<String> SStar = new HashSet<String>();
		HashSet<String> newElements = new HashSet<String>();
		
		//seed
		S.add(seed);
		SStar.addAll(S);
		
		//compute initial conductances
		double totalVolume = neighbors.get(seed).length;
		double totalBoundary = computeBoundary(S, neighbors);
		double conductanceS = totalBoundary / totalVolume;
		double conductanceSStar = conductanceS;
		
		
		for(String n : pageRank.keySet()){
			
			// disregard seed
			if(n.equals(seed)){
				continue;
			}
			
			// add node to S
			S.add(n);
			newElements.add(n);
			
			//update conductance
			totalVolume += neighbors.get(n).length;
			totalBoundary = computeBoundary(S, neighbors);
			conductanceS = totalBoundary / totalVolume;
			
			// if Phi(S) < Phi(S*) => S* <- S
			if(conductanceS < conductanceSStar){
				SStar.addAll(newElements);
				newElements.clear();
				conductanceSStar = conductanceS;
			}
			
		}
		
		//print S*
		for( String n : SStar){
			System.out.println(n+"\t"+pageRank.get(n).toString());
		}
		//System.out.println("Total nodes before: " + String.valueOf(pageRank.keySet().size()));
		//System.out.println("Total nodes after: " + String.valueOf(SStar.size()));
		
		//System.out.println(String.valueOf((double)(System.currentTimeMillis() - startTime)/1000D));
		
	}//main end


}//class end
