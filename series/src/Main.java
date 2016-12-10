import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

/**
 * Created by pedro on 09/12/2016.
 */
public class Main {
    public static void main(String[] args) {
        Dataset dataset = TDBFactory.createDataset("series");
        Model model = dataset.getDefaultModel();
        FileManager.get().readModel(model, "series.rdf", "RDF/XML");
        model.close();
        dataset.close();
    }
}
