/**
 * A standard edge object to be stored as JSON
 *
 * @author Ruixuan Tu
 */
public interface IEdge {
    int getId();

    void setId(int id);

    double getWeight();

    void setWeight(double weight);

    int getFrom();

    void setFrom(int id);

    int getTo();

    void setTo(int id);
}
