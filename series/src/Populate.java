import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.tdb.lib.StringAbbrev;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.RDFS;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class Populate {
    public static void main(String[] args) {
        Model model;

        byte[] raw;
        try {
            raw = Files.readAllBytes(Paths.get("series.json"));
        } catch (IOException e) {
            return;
        }
        String content = new String(raw);
        JSONArray jsonArray = new JSONArray(content);

        Dataset dataset = TDBFactory.createDataset("series");
        model = dataset.getDefaultModel();
        FileManager.get().readModel(model, "series.rdf", "RDF/XML");

        dataset.begin(ReadWrite.WRITE);

        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        for(Object object : jsonArray) {
            JSONObject series = (JSONObject) object;
            Resource aux = model.createResource("series:Series/".concat(series.getJSONObject("ids").getString("slug")))
                    .addLiteral(model.getProperty("series:name"), series.getString("title"))
                    .addLiteral(model.getProperty("series:imdb"),      series.getJSONObject("ids").isNull("imdb")?"":series.getJSONObject("ids").getString("imdb"))
                    .addLiteral(model.getProperty("series:slug"),      series.getJSONObject("ids").isNull("slug")?"":series.getJSONObject("ids").getString("slug"))
                    .addLiteral(model.getProperty("series:tmdb"),      series.getJSONObject("ids").isNull("tmdb")?"":""+series.getJSONObject("ids").getInt("tmdb"))
                    .addLiteral(model.getProperty("series:trakt"),     series.getJSONObject("ids").isNull("trakt")?"":""+series.getJSONObject("ids").getInt("trakt"))
                    .addLiteral(model.getProperty("series:tvdb"),      series.getJSONObject("ids").isNull("tvdb")?"":""+series.getJSONObject("ids").getInt("tvdb"))
                    .addLiteral(model.getProperty("series:tvrage"),    series.getJSONObject("ids").isNull("tvrage")?"":""+series.getJSONObject("ids").getInt("tvrage"))
                    .addLiteral(model.getProperty("series:rating"),       Math.ceil(series.getDouble("rating")))
                    .addLiteral(model.getProperty("series:year"),         series.getInt("year"))
                    .addLiteral(model.getProperty("series:runtime"),      series.getInt("runtime"))
                    .addLiteral(model.getProperty("series:overview"),   series.isNull("overview")?"":series.getString("overview"))
                    .addLiteral(model.getProperty("series:picture"),    series.isNull("poster")?"":series.getString("poster"))
                    .addLiteral(model.getProperty("rdfs:label"), "Series");
                    ;

            String queryString = "SELECT ?x WHERE { ?x <series:label> 'Network' . ?x <series:name> \""+series.getString("network")+"\" }";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                if (!results.hasNext()) {
                    Resource tmp = model.createResource("series:Network/".concat(series.getString("network").replaceAll("\\s+","")))
                            .addProperty(model.getProperty("series:broadcasts"), aux)
                            .addLiteral(model.getProperty("series:name"), series.getString("network"))
                            .addLiteral(model.getProperty("rdfs:label"), "Network");
                    aux.addProperty(model.getProperty("series:isFromNetwork"), tmp);
                } else {
                    Resource asd = results.nextSolution().getResource("x");
                    aux.addProperty(model.getProperty("series:isFromNetwork"), asd);
                    asd.addProperty(model.getProperty("series:broadcasts"), aux);
                }
            }

            for(Object object2 : series.getJSONArray("genres")) {
                String genre = (String) object2;
                String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Genre' .?x <series:name> \""+genre+"\" }";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                    ResultSet results = qexec.execSelect();
                    if (!results.hasNext()) {
                        Resource tmp = model.createResource("series:Genre/".concat(genre.replaceAll("\\s+","")))
                                .addProperty(model.getProperty("series:isGenreOf"), aux)
                                .addLiteral(model.getProperty("series:name"), genre)
                                .addLiteral(model.getProperty("rdfs:label"), "Genre");
                        aux.addProperty(model.getProperty("series:hasGenre"), tmp);
                    } else {
                        Resource asd = results.nextSolution().getResource("x");
                        aux.addProperty(model.getProperty("series:hasGenre"), asd);
                        asd.addProperty(model.getProperty("series:isGenreOf"), aux);
                    }
                }
            }

            for(Object object2 : series.getJSONArray("cast")) {
                JSONObject actor = (JSONObject) object2;
                String name = (String)actor.getJSONObject("person").getString("name");
                String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \""+name+"\" }";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                    ResultSet results = qexec.execSelect();
                    if (!results.hasNext()) {
                        Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+","")))
                                .addProperty(model.getProperty("series:isActorOn"), aux)
                                .addLiteral(model.getProperty("series:name"), name)
                                .addLiteral(model.getProperty("rdfs:label"), "Person");
                        aux.addProperty(model.getProperty("series:hasActor"), tmp);
                    } else {
                        Resource asd = results.nextSolution().getResource("x");
                        aux.addProperty(model.getProperty("series:hasActor"), asd);
                        asd.addProperty(model.getProperty("series:isActorOn"), aux);

                    }
                }
            }

            if(!series.isNull("crew")) {
                JSONObject crew = series.getJSONObject("crew");
                if(!crew.isNull("production")) {
                    for (Object object2 : crew.getJSONArray("production")) {
                        JSONObject producer = (JSONObject) object2;
                        String name = (String) producer.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isProducerOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasProducer"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasProducer"), asd);
                                asd.addProperty(model.getProperty("series:isProducerOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("art")) {
                    for (Object object2 : crew.getJSONArray("art")) {
                        JSONObject artist = (JSONObject) object2;
                        String name = (String) artist.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isArtDirectorOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasArtDirector"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasArtDirector"), asd);
                                asd.addProperty(model.getProperty("series:isArtDirectorOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("costume & make-up")) {
                    for (Object object2 : crew.getJSONArray("costume & make-up")) {
                        JSONObject costume = (JSONObject) object2;
                        String name = (String) costume.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isCostumeAndMakeUpArtistOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasCostumeAndMakeupArtist"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasCostumeAndMakeupArtist"), asd);
                                asd.addProperty(model.getProperty("series:isCostumeAndMakeUpArtistOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("directing")) {
                    for (Object object2 : crew.getJSONArray("directing")) {
                        JSONObject director = (JSONObject) object2;
                        String name = (String) director.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isDirectorOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasDirector"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasDirector"), asd);
                                asd.addProperty(model.getProperty("series:isDirectorOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("writing")) {
                    for (Object object2 : crew.getJSONArray("writing")) {
                        JSONObject writer = (JSONObject) object2;
                        String name = (String) writer.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isWriterOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasWriter"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasWriter"), asd);
                                asd.addProperty(model.getProperty("series:isWriterOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("sound")) {
                    for (Object object2 : crew.getJSONArray("sound")) {
                        JSONObject sounder = (JSONObject) object2;
                        String name = (String) sounder.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isSoundEngineerOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasSoundEngineer"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasSoundEngineer"), asd);
                                asd.addProperty(model.getProperty("series:isSoundEngineerOn"), aux);
                            }
                        }
                    }
                }
                if(!crew.isNull("camera")) {
                    for (Object object2 : crew.getJSONArray("camera")) {
                        JSONObject camera = (JSONObject) object2;
                        String name = (String) camera.getJSONObject("person").getString("name");
                        String queryString2 = "SELECT ?x WHERE { ?x <series:label> 'Person' .?x <series:name> \"" + name + "\" }";
                        try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                            ResultSet results = qexec.execSelect();
                            if (!results.hasNext()) {
                                Resource tmp = model.createResource("series:Person/".concat(name.replaceAll("\\s+", "")))
                                        .addProperty(model.getProperty("series:isCameraDirectorOn"), aux)
                                        .addLiteral(model.getProperty("series:name"), name)
                                        .addLiteral(model.getProperty("rdfs:label"), "Person");
                                aux.addProperty(model.getProperty("series:hasCameraDirector"), tmp);
                            } else {
                                Resource asd = results.nextSolution().getResource("x");
                                aux.addProperty(model.getProperty("series:hasCameraDirector"), asd);
                                asd.addProperty(model.getProperty("series:isCameraDirectorOn"), aux);
                            }
                        }
                    }
                }
            }

        }

        model.close();
        dataset.commit();
        dataset.end();
        dataset.close();
    }
}
