/**
 * Created by pedro on 20/12/2016.
 */
import static spark.Spark.*;

import freemarker.template.Configuration;
import org.apache.jena.atlas.logging.Log;
import org.apache.jena.graph.Node;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import spark.*;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import spark.template.freemarker.FreeMarkerEngine;

public class Web {
    public static void main(String[] args) {
        Configuration configuration = new Configuration();
        try {
            configuration.setDirectoryForTemplateLoading(new File(System.getProperty("user.dir")));
        } catch (Exception e) {}
        FreeMarkerEngine freeMarkerEngine = new FreeMarkerEngine();
        freeMarkerEngine.setConfiguration(configuration);
        staticFileLocation("/assets");

        get("/", (request, response) -> {
            return freeMarkerEngine.render(new ModelAndView(null, "web/index.ftl"));
        });

        get("/search", (request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");

                ArrayList<String> semSearch = semSearch(query);
                attributes.put("query", query);
                if(!semSearch.isEmpty()) {
                    attributes.put("series", semSearch);
                } else {
                    attributes.put("searchSeries", searchSeriesByName(query));
                    attributes.put("searchRatings", searchRatings(query));
                    attributes.put("searchNetworks", searchNetworks(query));
                    attributes.put("searchGenres", searchGenres(query));
                    attributes.put("searchPeople", searchPeople(query));
                }
            } catch (Exception e) {
                Log.warn("/search", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/search.ftl"));
        });

        get("/rating", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");

                attributes.put("searchSeries", searchSeriesByRating(query));
                attributes.put("query", query);
            } catch (Exception e) {
                Log.warn("/rating", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/search.ftl"));
        }));

        get("/network", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");

                attributes.put("searchSeries", searchSeriesByNetwork(query));
                attributes.put("query", query);
            } catch (Exception e) {
                Log.warn("/network", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/search.ftl"));
        }));

        get("/genre", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");

                attributes.put("searchSeries", searchSeriesByGenre(query));
                attributes.put("query", query);
            } catch (Exception e) {
                Log.warn("/genre", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/search.ftl"));
        }));

        get("/people", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");

                attributes.put("searchSeries", searchSeriesByPeople(query));
                attributes.put("query", query);
            } catch (Exception e) {
                Log.warn("/people", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/search.ftl"));
        }));

        get("/series", ((request, response) -> {
            Map<String, Object> attributes = new HashMap<>();

            try {
                String query = request.queryParams("query");
                Series master = getSeries(query);

                attributes.put("series", master);
                attributes.put("suggestions", suggestSeries(master));
                attributes.put("query", query);
            } catch (Exception e) {
                Log.warn("/search", "Error searching query");
            }

            return freeMarkerEngine.render(new ModelAndView(attributes, "web/series.ftl"));
        }));
    }

    public static ArrayList<String> searchSeriesByName(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = "SELECT DISTINCT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . "
                +" ?s <series:name> ?name "
                +" FILTER (regex(?name, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchSeriesByName", "Error searching for series: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchSeriesByRating(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?s <series:rating> ?y FILTER (?y >= " + query.toLowerCase() + ") "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchSeriesByRating", "Error searching for series: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchSeriesByNetwork(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?s <series:isFromNetwork> ?n . ?n <series:name> ?y "
                +" FILTER (regex(?y, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchSeriesByNetwork", "Error searching for series: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchSeriesByGenre(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?s <series:hasGenre> ?g . ?g <series:name> ?y "
                +" FILTER (regex(?y, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchSeriesByGenre", "Error searching for series: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchSeriesByPeople(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?s WHERE { "
                +" { ?s <rdfs:label> 'Series' } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasActor> ?a . ?a <series:name> ?y } }"
                +" UNION "
                +" { OPTIONAL { ?s <series:hasDirector> ?d . ?d <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasArtDirector> ?ad . ?ad <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasCameraDirector> ?cd . ?cd <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasCostumeAndMakeupArtist> ?cama . ?cama <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasProducer> ?p . ?p <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasSoundEngineer> ?se . ?se <series:name> ?y } } "
                +" UNION "
                +" { OPTIONAL { ?s <series:hasWriter> ?w . ?w <series:name> ?y } } "
                +" FILTER (regex(?y, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getResource("s").getProperty(model.getProperty("series:name")).getString());
            }
        } catch (Exception e) {
            Log.fatal("searchSeriesByPeople", "Error searching for series: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchPeople(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?p WHERE { "
                +" ?x <rdfs:label> 'Person' . "
                +" ?x <series:name> ?p "
                +" FILTER (regex(?p, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("p").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchPeople", "Error searching for people: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<Integer> searchRatings(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<Integer> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?r WHERE { "
                +" ?s <rdfs:label> 'Series' . "
                +" ?s <series:rating> ?r "
                +" FILTER ( ?r =  floor("+ query +") ) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("r").getInt());
            }
        } catch (Exception e) {
            Log.fatal("searchRatings", "Error searching for ratings: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchNetworks(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?n WHERE { "
                +" ?x <rdfs:label> 'Network' . "
                +" ?x <series:name> ?n "
                +" FILTER (regex(?n, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("n").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchNetworks", "Error searching for networks: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> searchGenres(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString = " SELECT DISTINCT ?g WHERE { "
                +" ?x <rdfs:label> 'Genre' . "
                +" ?x <series:name> ?g "
                +" FILTER (regex(?g, '^.*" + query.toLowerCase() + ".*$', 'i')) "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("g").getString());
            }
        } catch (Exception e) {
            Log.fatal("searchGenres", "Error searching for genres: "+query);
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static Series getSeries(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        String queryString = " SELECT  ?s WHERE { "
                +" ?s <rdfs:label> 'Series' . "
                +" ?s <series:name> '"+query+"' "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                Series result = new Series(results.nextSolution().getResource("s"), model);
                model.close();
                dataset.end();
                dataset.close();
                return result;
            } else {
                Log.warn("getSeries", "Series not found: "+query);
                model.close();
                dataset.end();
                dataset.close();
                return null;
            }
        } catch (Exception e) {
            Log.fatal("getSeries", "Error getting series: "+query);
            e.printStackTrace();
            model.close();
            dataset.end();
            dataset.close();
            return null;
        }
    }

    public static ArrayList<String> suggestSeries(Series query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        ArrayList<String> result = new ArrayList<>();
        String queryString;

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:hasGenre> ?g . ?g <series:isGenreOf> ?query "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:hasActor> ?g . ?g <series:isActorOn> ?query "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:isFromNetwork> ?g . ?g <series:broadcasts> ?query "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:rating> ?g . ?query <series:rating> ?g "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:hasDirector> ?g . ?g <series:isActorOn> ?query "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        queryString = "SELECT ?name WHERE { "
                +" ?s <rdfs:label> 'Series' . ?s <series:name> ?name . "
                +" ?query <series:name> ?y . "
                +" ?s <series:hasProducer> ?g . ?g <series:isProducerOn> ?query "
                +" FILTER (regex(?y, '^.*" + query.getName().toLowerCase() + ".*$', 'i')) "
                +" FILTER not exists { ?s <series:name> ?y } "
                +" } ";
        try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
            ResultSet results = qexec.execSelect();
            while (results.hasNext()) {
                result.add(results.nextSolution().getLiteral("name").getString());
            }
        } catch (Exception e) {
            Log.fatal("suggestSeries", "Error suggesting series: "+query.getName());
        }

        Log.info("suggestions::before", ""+result.size());

        final Map<String, Integer> counter = new HashMap<>();
        for (String series : result)
            counter.put(series, 1 + (counter.containsKey(series) ? counter.get(series) : 0));

        List<String> list = new ArrayList<>(counter.keySet());
        Collections.sort(list, (x, y) -> counter.get(y) - counter.get(x));

        result = (ArrayList<String>)list;
        Log.info("suggestions::after", ""+result.size());

        int size = result.size();
        if(size > 20) {
            result.subList(20, size).clear();
        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }

    public static ArrayList<String> semSearch(String query) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        dataset.begin(ReadWrite.READ);
        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        Pattern pattern;
        Matcher matcher;
        ArrayList<String> result = new ArrayList<>();

        pattern = Pattern.compile("(\\w+) series.*", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        if(matcher.find()) {
            String find = matcher.group(1);
            System.out.println("FIND " + find);
            String queryString = "SELECT DISTINCT ?s WHERE { "
                    +" { ?s <rdfs:label> 'Series' } "
                    +" UNION "
                    +" { OPTIONAL { ?s <series:hasGenre> ?g . ?g <series:name> ?find } } "
                    +" UNION "
                    +" { OPTIONAL { ?s <series:isFromNetwork> ?n . ?n <series:name> ?find } }  "
                    +" FILTER (regex(?find, '^.*" + find.toLowerCase() + ".*$', 'i')) "
                    +" } ";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    result.add(results.nextSolution().getResource("s").getProperty(model.getProperty("series:name")).getString());
                }
            } catch (Exception e) {
                Log.fatal("semSearch", "Error searching for series: "+query);
            }
        }

        pattern = Pattern.compile(".*series by ((\\w\\s?)+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        if(matcher.find()) {
            String find = matcher.group(1);
            System.out.println("FIND " + find);
            String queryString = "SELECT DISTINCT ?s WHERE { "
                    +" { ?s <rdfs:label> 'Series' } "
                    +" UNION "
                    +" { OPTIONAL { ?s <series:isFromNetwork> ?n . ?n <series:name> ?find } }  "
                    +" UNION "
                    +" { OPTIONAL { ?s <series:hasDirector> ?d . ?d <series:name> ?find } }  "
                    +" UNION "
                    +" { OPTIONAL { ?s <series:hasProducer> ?p . ?p <series:name> ?find } }  "
                    +" FILTER (regex(?find, '^.*" + find.toLowerCase() + ".*$', 'i')) "
                    +" } ";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    result.add(results.nextSolution().getResource("s").getProperty(model.getProperty("series:name")).getString());
                }
            } catch (Exception e) {
                Log.fatal("semSearch", "Error searching for series: "+query);
            }
        }

        pattern = Pattern.compile(".*series.* with ((\\w\\s?)+)", Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(query);

        if(matcher.find()) {
            String find = matcher.group(1);
            System.out.println("FIND " + find);
            String queryString = "SELECT DISTINCT ?s WHERE { "
                    +" ?s <rdfs:label> 'Series' . "
                    +" ?s <series:hasActor> ?a . ?a <series:name> ?find "
                    +" FILTER (regex(?find, '^.*" + find.toLowerCase() + ".*$', 'i')) "
                    +" } ";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                while (results.hasNext()) {
                    result.add(results.nextSolution().getResource("s").getProperty(model.getProperty("series:name")).getString());
                }
            } catch (Exception e) {
                Log.fatal("semSearch", "Error searching for series: "+query);
            }
        }

        if(!result.isEmpty()) {
            final Map<String, Integer> counter = new HashMap<>();
            for (String series : result)
                counter.put(series, 1 + (counter.containsKey(series) ? counter.get(series) : 0));

            List<String> list = new ArrayList<>(counter.keySet());
            Collections.sort(list, (x, y) -> counter.get(y) - counter.get(x));
            ArrayList<String> tmp = (ArrayList<String>)list;
            int max = counter.get(tmp.get(0));
            int aux = max;

            System.out.println("Before: " + result.size());

            result.clear();
            while (result.size() < 4 && aux > 0) {
                for (String cur : tmp) {
                    if (counter.get(cur) == aux) {
                        result.add(cur);
                    }
                    if (result.size() == 16 && aux != max) {
                        break;
                    }
                }
                if(max==3) {
                    break;
                }
                aux--;
            }

        }

        model.close();
        dataset.end();
        dataset.close();
        return result;
    }
}
