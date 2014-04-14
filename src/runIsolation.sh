rm *.class
javac *.java
java ApproxPageRank "/Users/daniel/Documents/Codes/ML605/HW6ApproximatePageRank/data/wikiGraph.adj" "Topographic_isolation" 0.3 0.0002 > outputIsolation.txt
python output2gdf.py ../data/wikiGraph.adj outputIsolation.txt 0.0002 > graph_isolation_final.gdf
