public class VertexGeneral implements IVertex {
    public int id;
    public String label;
    public double x;
    public double y;

    public VertexGeneral(int id, String label, double x, double y) {
        this.id = id;
        this.label = label;
        this.x = x;
        this.y = y;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

    @Override
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public double getX() {
        return this.x;
    }

    @Override
    public void setX(double x) {
        this.x = x;
    }

    @Override
    public double getY() {
        return this.y;
    }

    @Override
    public void setY(double y) {
        this.y = y;
    }
}
