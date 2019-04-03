package syntheticfibergenerator;

import java.io.File;


/**
 * Contains the "main" method.
 */
public class EntryPoint {

    /**
     * If one or more command-line arguments were passed, the first is interpreted as the path to the params file.
     * Generation is run using the provided params file and results are written to the "output" folder. If no arguments
     * were passed the program runs in GUI mode.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            IOManager IOManager = new IOManager();
            try {
                ImageCollection.Params params = IOManager.readParamsFile(args[0]);
                ImageCollection collection = new ImageCollection(params);
                collection.generateImages();
                IOManager.writeResults(params, collection, "output" + File.separator);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            new MainWindow();
        }
    }
}
