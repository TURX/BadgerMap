import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

public class BackendPlaceholder implements IBackend {
    Hashtable<Integer, IVertex> vertices;
    Hashtable<Integer, IEdge> edges;
    IDataLoader dataLoader;
    File currentFile;
    AEPlaceholder graph;

    public BackendPlaceholder() {
        vertices = new Hashtable<>();
        edges = new Hashtable<>();
        dataLoader = new DataLoaderPlaceholder();
        currentFile = null;
        graph = new AEPlaceholder();
    }

    @Override
    public void saveFile(File file) throws IOException {
        List<IVertex> vertices = getVertices();
        List<IEdge> edges = getEdges();
        dataLoader.writeVertices(vertices, file.getAbsolutePath());
        dataLoader.writeEdges(edges, file.getAbsolutePath());
        currentFile = file;
    }

    @Override
    public void openFile(File file) throws IOException {
        this.clear();
        List<IVertex> verticesList = dataLoader.loadVertices(file.getAbsolutePath());
        List<IEdge> edgesList = dataLoader.loadEdges(file.getAbsolutePath());
        vertices.clear();
        for (IVertex v : verticesList)
            vertices.put(v.getId(), v);
        edges.clear();
        for (IEdge e : edgesList)
            edges.put(e.getId(), e);
        currentFile = file;
    }

    @Override
    public void addVertex(IVertex v) {
        vertices.put(v.getId(), v);
        graph.insertVertex(v);
    }

    @Override
    public void addEdge(IEdge e) {
        edges.put(e.getId(), e);
        graph.insertEdge(vertices.get(e.getFrom()), vertices.get(e.getTo()));
    }

    @Override
    public void removeVertex(int id) {
        graph.removeVertex(vertices.get(id));
        vertices.remove(id);
    }

    @Override
    public void removeEdge(int id) {
        graph.removeEdge(vertices.get(edges.get(id).getFrom()), vertices.get(edges.get(id).getTo()));
        edges.remove(id);
    }

    @Override
    public void changeVertex(int id, double x, double y) {
        if (vertices.get(id).getX() == x && vertices.get(id).getY() == y)
            return;
        vertices.get(id).setX(x);
        vertices.get(id).setY(y);
        graph.vertices.get(vertices.get(id)).setX(x);
        graph.vertices.get(vertices.get(id)).setY(y);
    }

    @Override
    public void changeVertex(int id, String s) {
        vertices.get(id).setLabel(s);
        graph.vertices.get(vertices.get(id)).setLabel(s);
    }

    @Override
    public List<Integer> searchByVertex(String label) {
        List<Integer> result = new java.util.LinkedList<>();
        for (IVertex vertex : vertices.values())
            if (vertex.getLabel().equals(label))
                result.add(vertex.getId());
        return result;
    }

    @Override
    public List<Integer> searchByEdge(double weight) {
        List<Integer> result = new java.util.LinkedList<>();
        for (IEdge edge : edges.values())
            if (String.format("%.2f", edge.getWeight()).equals(String.format("%.2f", weight)))
                result.add(edge.getId());
        return result;
    }

    @Override
    public double getShortestByDistance(int from, int to) {
        return graph.getPathCostD(vertices.get(from), vertices.get(to));
    }

    @Override
    public List<Integer> getShortestByEdge(int from, int to) {
        List<IVertex> path = graph.shortestPath(vertices.get(from), vertices.get(to));
        List<Integer> result = new java.util.LinkedList<>();
        if (path.size() < 2)
            return result;
        IVertex prev = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            IVertex curr = path.get(i);
            for (IEdge edge : edges.values())
                if (edge.getFrom() == prev.getId() && edge.getTo() == curr.getId())
                    result.add(edge.getId());
            prev = curr;
        }
        return result;
    }

    @Override
    public List<Integer> getShortestByVertex(int from, int to) {
        List<Integer> result = new java.util.LinkedList<>();
        List<IVertex> path = graph.shortestPath(vertices.get(from), vertices.get(to));
        for (IVertex v : path)
            result.add(v.getId());
        return result;
    }

    @Override
    public List<IEdge> getEdges() {
        return new java.util.LinkedList<>(edges.values());
    }

    @Override
    public List<IVertex> getVertices() {
        return new java.util.LinkedList<>(vertices.values());
    }

    @Override
    public boolean containsVertex(int id) {
        return vertices.containsKey(id);
    }

    @Override
    public boolean containsEdge(int id) {
        return edges.containsKey(id);
    }

    @Override
    public void clear() {
        vertices.clear();
        edges.clear();
        currentFile = null;
        graph = new AEPlaceholder();
    }
}
