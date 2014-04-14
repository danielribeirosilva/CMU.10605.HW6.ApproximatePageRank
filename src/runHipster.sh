rm *.class
javac *.java
java ApproxPageRank "/Users/daniel/Documents/Codes/ML605/HW6ApproximatePageRank/data/wikiGraph.adj" "Hipster_%28contemporary_subculture%29" 0.3 0.001 > output_Hipster.txt
python output2gdf.py ../data/wikiGraph.adj outputHipster.txt 0.001 > graph_hispter_final.gdf
