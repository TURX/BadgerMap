import java.util.List;

public interface IGraph extends GraphADT<IVertex> {
    boolean insertVertex(IVertex data);
    boolean removeVertex(IVertex data);
    boolean insertEdge(IVertex source, IVertex target, double weight);
    boolean insertEdge(IVertex source, IVertex target);
    boolean removeEdge(IVertex source, IVertex target);
    boolean containsVertex(IVertex data);
    boolean containsEdge(IVertex source, IVertex target);
    int getWeight(IVertex source, IVertex target);
    int getEdgeCount();
    int getVertexCount();
    boolean isEmpty();
    List<IVertex> shortestPath(IVertex start, IVertex end);
    int getPathCost(IVertex start, IVertex end);
    double getPathCostD(IVertex start, IVertex end);
}
