import sys

if len(sys.argv) != 3:
	print "python output2gdf.py [path to wiki graph] [output of ApproxPageRank]"
	exit(0)

relevantNodes = set([l.split()[0] for l in open(sys.argv[2]).readlines()])
nodes = []
for line in open(sys.argv[1]):
	node = line.split()
	if node[0] in relevantNodes:
		nodes.append([n for n in node if n in relevantNodes])

print "nodedef>name VARCHAR"
i = 0
for node in nodes:
	print "\"{0}\"".format(node[0])
	i += 1
sys.stderr.write("Should have printed {0} nodes\n".format(i))

print "edgedef>node1 VARCHAR,node2 VARCHAR"
i = 0
for node in nodes:
	for outlink in node[1:]:
		print "\"{0}\",\"{1}\"".format(node[0], outlink)
		i += 1
sys.stderr.write("Should have printed {0} edges\n".format(i))