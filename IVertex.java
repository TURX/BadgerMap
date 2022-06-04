/**
 * A standard vertex object to be stored as JSON
 *
 * @author Ruixuan Tu
 */
public interface IVertex {
    int getId();

    void setId(int id);

    String getLabel();

    void setLabel(String label);

    double getX();

    void setX(double x);

    double getY();

    void setY(double y);
}
