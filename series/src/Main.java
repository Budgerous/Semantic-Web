import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 * Created by pedro on 09/12/2016.
 */
public class Main {
    public static void main(String[] args) throws IOException {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();

        dataset.begin(ReadWrite.READ);

        System.out.println(model.getNsPrefixMap());

        model.setNsPrefix("series", "http://www.semanticweb.org/pedro/ontologies/2016/11/series#");

        System.out.println("Insert an option: 1) filter by genre; 2) filter by network; 3) filter by rating; 4) Misc");
        Scanner reader = new Scanner(System.in);
        int n = reader.nextInt();
        switch (n) {
            case 1: {
                System.out.println("Action series");
                String queryString = "SELECT ?z WHERE"
                        +"{ ?x <series:hasGenre> ?y ."
                        +" ?y <series:name> \"action\" ."
                        +" ?x <series:name> ?z.}";
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
                        +"{ ?x <series:isFromNetwork> ?y ."
                        +" ?y <series:name> \"HBO\" ."
                        +" ?x <series:name> ?z.}";
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
                        +"{ ?x <series:rating> \"10.0\" ."
                        +" ?x <series:name> ?z.}";
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
                        +"{ ?x <series:hasGenre> ?y ."
                        +" ?y <series:name> \"crime\" ."
                        +" ?x <series:isFromNetwork> ?w ."
                        +" ?w <series:name> \"Netflix\" ."
                        +" ?x <series:name> ?z.}";
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
        dataset.end();
        dataset.close();
    }
}
