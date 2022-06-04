import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * The role in this project is to coordinate the overall project operation.
 * After the user performs an operation in the front-end,
 * the command is transmitted to the back-end,
 * which then calls the appropriate method to solve the problem and returns the data to the front-end.
 */

public interface IBackend {

    void saveFile(File file) throws IOException;                   // Save file with json files

    void openFile(File file) throws IOException;                   // Open file with json files

    void addVertex(IVertex v) throws IOException;                  // Adding vertices to the database

    void addEdge(IEdge e) throws IOException;                      // Adding edge to the database

    void removeVertex(int id) throws IOException;                  // Remove vertex in the database

    void removeEdge(int id) throws IOException;                    // Remove edge in the database

    void changeVertex(int id, double x, double y);                 // Change vertex position in the database

    void changeVertex(int id, String s);                           // Change vertex label in the database

    List<Integer> searchByVertex(String label);                    // Search by vertex in the database

    List<Integer> searchByEdge(double weight);                     // Search by edge in the database

    double getShortestByDistance(int from, int to);                // Search shortest by edge in the database

    List<Integer> getShortestByEdge(int from, int to);             // Search shortest by edge in the database

    List<Integer> getShortestByVertex(int from, int to);           // Search shortest by vertex in the database

    List<IEdge> getEdges();                                        // Get edges in the database

    List<IVertex> getVertices();                                   // Get vertices in the database

    boolean containsVertex(int id);                                // Check if vertex in the database

    boolean containsEdge(int id);                                  // Check if edge in the database

    void clear();                                                  // Clear the database

}
