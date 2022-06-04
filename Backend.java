import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

public class Backend implements IBackend{
    List<IVertex> vertices;
    List<IEdge> edges;
    IGraph graph;
    IDataLoader dataLoader;
    HashMap<Integer, IVertex> idToV;
    HashMap<Integer, IEdge> idToE;

    public Backend(){
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        // graph need to be initialized
        // dataLoader need to be initialized
        idToE = new HashMap();
        idToV = new HashMap();
        dataLoader = new DataLoaderPlaceholder();
        graph = new AEPlaceholder();
    }

    @Override
    public void saveFile(File file) throws IOException {
        String path = file.getAbsolutePath();
        dataLoader.writeEdges(edges, path + File.separator + "edges.json");
        dataLoader.writeVertices(vertices, path + File.separator + "vertices.json");
    }

    @Override
    public void openFile(File file) throws IOException {
        String path = file.getAbsolutePath();
        vertices = dataLoader.loadVertices(path + File.separator + "vertices.json");
        edges = dataLoader.loadEdges(path + File.separator + "edges.json");
        for (IVertex v: vertices){
            graph.insertVertex(v);
            idToV.put(v.getId(), v);
        }

        for (IEdge e: edges) {
            idToE.put(e.getId(), e);
            graph.insertEdge(idToV.get(e.getFrom()), idToV.get(e.getTo()));
        }
    }

    @Override
    public void addVertex(IVertex v) throws IOException {
        if (idToV.containsKey(v.getId())) return;
        idToV.put(v.getId(), v);
        vertices.add(v);
        graph.insertVertex(v);
    }

    @Override
    public void addEdge(IEdge e) throws IOException {
        if (idToE.containsKey(e.getId())) return;
        idToE.put(e.getId(), e);
        edges.add(e);
        graph.insertEdge(idToV.get(e.getFrom()), idToV.get(e.getTo()));
    }

    @Override
    public void removeVertex(int id) throws IOException {
        IVertex v = idToV.get(id);
        graph.removeVertex(v);
        vertices.remove(v);
        idToV.remove(id);
    }

    @Override
    public void removeEdge(int id) throws IOException {
        IEdge e = idToE.get(id);
        graph.removeEdge(idToV.get(e.getFrom()), idToV.get(e.getTo()));
        edges.remove(e);
        idToE.remove(id);
    }

    @Override
    public void changeVertex(int id, double x, double y) {
        if (idToV.get(id).getX() == x && idToV.get(id).getY() == y) return;
        IVertex v = idToV.get(id);
        v.setX(x);
        v.setY(y);
    }

    @Override
    public void changeVertex(int id, String s) {
        if (idToV.get(id).getLabel().equals(s)) return;
        IVertex v = idToV.get(id);
        v.setLabel(s);
    }

    @Override
    public List<Integer> searchByVertex(String label) {
        List<Integer> id = new ArrayList<>();
        for (IVertex v: vertices){
            if (v.getLabel().equals(label))
                id.add(v.getId());
        }
        return id;
    }

    @Override
    public List<Integer> searchByEdge(double weight) {
        List<Integer> id = new ArrayList<>();
        for (IEdge e: edges){
            if (e.getWeight() == weight)
                id.add(e.getId());
        }
        return id;
    }

    @Override
    public double getShortestByDistance(int from, int to) {
        return graph.getPathCostD(idToV.get(from), idToV.get(to));
    }

    @Override
    public List<Integer> getShortestByEdge(int from, int to) {
        IVertex start, end;
        double weight;
        List<Integer> shortV = getShortestByVertex(from, to);
        List<Integer> res = new ArrayList<>();

        for (int i = 0; i < shortV.size(); i++){
            start = idToV.get(from);
            end = idToV.get(to);
            weight = Double.MAX_VALUE;
            IEdge toBeAdd = null;
            for (IEdge e: edges){
                if (e.getFrom() == start.getId() && e.getTo() == end.getId()){
                    if (e.getWeight() <= weight){
                        toBeAdd = e;
                        weight = e.getWeight();
                    }
                }
            }
            if (toBeAdd != null)
                res.add(toBeAdd.getId());
        }

        return res;
    }

    @Override
    public List<Integer> getShortestByVertex(int from, int to) {
        IVertex start = idToV.get(from);
        IVertex end = idToV.get(to);
        List<IVertex> v = graph.shortestPath(start, end);
        List<Integer> res = new ArrayList<>();
        for (IVertex ver: v)
            res.add(ver.getId());
        return res;
    }

    @Override
    public List<IEdge> getEdges() {
        return edges;
    }

    @Override
    public List<IVertex> getVertices() {
        return vertices;
    }

    @Override
    public boolean containsVertex(int id) {
        return idToV.containsKey(id);
    }

    @Override
    public boolean containsEdge(int id) {
        return idToE.containsKey(id);
    }

    @Override
    public void clear() {
        vertices = new ArrayList<>();
        edges = new ArrayList<>();
        // graph = new Graph();
        idToE = new HashMap();
        idToV = new HashMap();
    }
}
