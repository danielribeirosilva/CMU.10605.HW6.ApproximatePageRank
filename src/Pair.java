
public class Pair implements Comparable<Pair>{
	public String node;
	public double pageRank;
	
	public Pair(String node, double pageRank){
		this.node = node;
		this.pageRank = pageRank;
	}

	@Override
	public int compareTo(Pair other) {
		return Double.compare(other.pageRank, this.pageRank);
	}
	
}
