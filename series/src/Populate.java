import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by pedro on 09/12/2016.
 */
public class Populate {
    public static void main(String[] args) throws IOException{
        byte[] raw = Files.readAllBytes(Paths.get("trakt_series.json"));
        String content = new String(raw);
        JSONArray jsonArray = new JSONArray(content);

        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        FileManager.get().readModel(model, "series.rdf", "RDF/XML");

        for(Object object : jsonArray) {
            JSONObject series = (JSONObject) object;
            Resource aux = model.createResource("http://www.semanticweb.org/pedro/ontologies/2016/11/series#Series/".concat(series.getJSONObject("ids").getString("slug")))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#name"), series.getString("title"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#imdb"),      series.getJSONObject("ids").isNull("imdb")?"":series.getJSONObject("ids").getString("imdb"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#slug"),      series.getJSONObject("ids").isNull("slug")?"":series.getJSONObject("ids").getString("slug"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#tmdb"),      series.getJSONObject("ids").isNull("tmdb")?"":""+series.getJSONObject("ids").getInt("tmdb"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#trakt"),     series.getJSONObject("ids").isNull("trakt")?"":""+series.getJSONObject("ids").getInt("trakt"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#tvdb"),      series.getJSONObject("ids").isNull("tvdb")?"":""+series.getJSONObject("ids").getInt("tvdb"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#tvrage"),    series.getJSONObject("ids").isNull("tvrage")?"":""+series.getJSONObject("ids").getInt("tvrage"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#rating"),       ""+Math.ceil(series.getDouble("rating")))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#year"),         ""+series.getInt("year"))
                    .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#runtime"),      ""+series.getInt("runtime"))
            ;

            String queryString = "SELECT ?x WHERE { ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \""+series.getString("network")+"\" }";
            try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                ResultSet results = qexec.execSelect();
                if (!results.hasNext()) {
                    Resource tmp = model.createResource("http://www.semanticweb.org/pedro/ontologies/2016/11/series#Network/".concat(series.getString("network").replaceAll("\\s+","")))
                            .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#broadcasts"), aux)
                            .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#name"), series.getString("network"));
                    aux.addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#isFromNetwork"), tmp);
                } else {
                    aux.addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#isFromNetwork"), results.nextSolution().getResource("x"));
                }
            }

            for(Object object2 : series.getJSONArray("genres")) {
                String genre = (String) object2;
                String queryString2 = "SELECT ?x WHERE { ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \""+genre+"\" }";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString2, model)) {
                    ResultSet results = qexec.execSelect();
                    if (!results.hasNext()) {
                        Resource tmp = model.createResource("http://www.semanticweb.org/pedro/ontologies/2016/11/series#Genre/".concat(genre.replaceAll("\\s+","")))
                                .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#isGenreOf"), aux)
                                .addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#name"), genre);
                        aux.addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#hasGenre"), tmp);
                    } else {
                        aux.addProperty(model.getProperty("http://www.semanticweb.org/pedro/ontologies/2016/11/series#hasGenre"), results.nextSolution().getResource("x"));
                    }
                }
            }
        }

        System.out.println("Insert an option: 1) filter by genre; 2) filter by network; 3) filter by rating; 4) Misc");
        Scanner reader = new Scanner(System.in);
        int n = reader.nextInt();
        switch (n) {
            case 1: {
                System.out.println("Action series");
                String queryString = "SELECT ?z WHERE"
                        +"{ ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#hasGenre> ?y ."
                        +" ?y <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \"action\" ."
                        +" ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> ?z.}";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                    ResultSet results = qexec.execSelect();
                    while(results.hasNext()) {
                        System.out.println(results.nextSolution().get("z"));
                    }
                }
                break;
            }case 2 : {
                System.out.println("Series by HBO");
                String queryString = "SELECT ?z WHERE"
                        +"{ ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#isFromNetwork> ?y ."
                        +" ?y <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \"HBO\" ."
                        +" ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> ?z.}";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                    ResultSet results = qexec.execSelect();
                    while(results.hasNext()) {
                        System.out.println(results.nextSolution().get("z"));
                    }
                }
                break;
            }case 3: {
                System.out.println("Series with rating bigger than 9.0");
                String queryString = "SELECT ?z WHERE"
                        +"{ ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#rating> \"10.0\" ."
                        +" ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> ?z.}";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                    ResultSet results = qexec.execSelect();
                    while(results.hasNext()) {
                        System.out.println(results.nextSolution().get("z"));
                    }
                }
                break;
            }case 4: {
                System.out.println("Crime series by Netflix");
                String queryString = "SELECT ?z WHERE"
                        +"{ ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#hasGenre> ?y ."
                        +" ?y <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \"crime\" ."
                        +" ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#isFromNetwork> ?w ."
                        +" ?w <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> \"Netflix\" ."
                        +" ?x <http://www.semanticweb.org/pedro/ontologies/2016/11/series#name> ?z.}";
                try (QueryExecution qexec = QueryExecutionFactory.create(queryString, model)) {
                    ResultSet results = qexec.execSelect();
                    while(results.hasNext()) {
                        System.out.println(results.nextSolution().get("z"));
                    }
                }
                break;
            }default: {
                System.out.println("Not an option");
                break;
            }
        }

        model.close();
        dataset.close();
    }
}
