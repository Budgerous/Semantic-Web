import rdflib
from rdflib.namespace import RDF

g = rdflib.Graph()
g.load("../series/series.rdf", format="application/rdf+xml")

n = rdflib.Namespace("http://www.semanticweb.org/pedro/ontologies/2016/11/series#")

bob = n.Person

g.add( ( bob, RDF.type, n.Person ) )
g.add( ( bob, n.name, rdflib.Literal("Bob") ) )

print g.serialize(format="turtle")

g.close()
