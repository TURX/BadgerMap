public class EdgeGeneral implements IEdge {
    public int id;
    public double weight;
    public int from;
    public int to;

    public EdgeGeneral(int id, double weight, int from, int to) {
        this.id = id;
        this.weight = weight;
        this.from = from;
        this.to = to;
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
    public double getWeight() {
        return this.weight;
    }

    @Override
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int getFrom() {
        return this.from;
    }

    @Override
    public void setFrom(int id) {
        this.from = id;
    }

    @Override
    public int getTo() {
        return this.to;
    }

    @Override
    public void setTo(int id) {
        this.to = id;
    }
}
