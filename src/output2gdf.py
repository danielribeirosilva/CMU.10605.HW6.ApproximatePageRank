import sys
import math

if len(sys.argv) != 4:
	print "python output2gdf.py [path to wiki graph] [output of ApproxPageRank] [epsilon]"
	exit(0)

epsilon = float(sys.argv[3])
relevantNodes = set([l.split()[0] for l in open(sys.argv[2]).readlines()])

pageRank = {}
for line in open(sys.argv[2]):
	nodeAndRank = line.split()
	pageRank[nodeAndRank[0]] = float(nodeAndRank[1])

nodes = []
for line in open(sys.argv[1]):
	node = line.split()
	if node[0] in relevantNodes:
		nodes.append([n for n in node if n in relevantNodes])

print "nodedef>name VARCHAR,label VARCHAR,width DOUBLE"
i = 0
for node in pageRank:
	score = max(1, math.log(pageRank[node]/epsilon))
	print "\"{0}\",\"{1}\",\"{2}\"".format(node,node,score)
	i += 1
sys.stderr.write("Should have printed {0} nodes\n".format(i))

print "edgedef>node1 VARCHAR,node2 VARCHAR"
i = 0
for node in nodes:
	for outlink in node[1:]:
		print "\"{0}\",\"{1}\"".format(node[0], outlink)
		i += 1
sys.stderr.write("Should have printed {0} edges\n".format(i))