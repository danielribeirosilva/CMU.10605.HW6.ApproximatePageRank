rm *.class
javac *.java
java ApproxPageRank "/Users/daniel/Documents/Codes/ML605/HW6ApproximatePageRank/data/wikiGraph.adj" "Topographic_isolation" 0.3 0.0002 > outputIsolation.txt
