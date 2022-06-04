import java.io.IOException;
import java.util.List;
import java.io.FileNotFoundException;

/**
 * Instances of classes that implement this interface can be used to load a 
 * list of shows from a specified json source file.
 */
public interface IDataLoader {
    /**
     * This method loads the list of vertices and edges described within a json file.
     * @param path is relative to executable's working directory
     * @return a list of IVertex objects that were read from specified file
     */
    List<IVertex> loadVertices(String path) throws IOException;

    /**
     * This method loads the list of vertices and edges described within a json file.
     * @param path is relative to executable's working directory
     * @return a list of IEdge objects that were read from specified file
     */
    List<IEdge> loadEdges(String path) throws IOException;

    /**
     * This method create a new file based on current Vertex objects.
     *
     */
    void writeVertices(List<IVertex> vertices, String path) throws IOException;

    /**
     * This method create a new file based on current Edge objects.
     *
     */
    void writeEdges(List<IEdge> edges, String path) throws IOException;
}
