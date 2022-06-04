import javafx.scene.Group;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.Objects;

/**
 * EdgeFD is a class that represents an edge on the graph, and an instance of IEdge
 *
 * @author Ruixuan Tu
 */
public class EdgeFD implements IEdge {
    public int id;
    public double weight;
    public boolean selected;
    public boolean dragging;
    public double theta; // in radian

    public Group group;
    public Line line;
    public Polygon triangle;
    public Label label;

    public MapPoint fromPos;
    public MapPoint toPos;
    public TextField textField;
    public ContextMenu contextMenu;

    public VertexFD fromVertex;
    public VertexFD toVertex;
    public MapController controller;

    /**
     * Constructor for EdgeFD
     *
     * @param id         the id of the edge
     * @param fromVertex the vertex that the edge starts from
     * @param toVertex   the vertex that the edge ends at
     * @param controller the controller of the graph
     */
    public EdgeFD(int id, VertexFD fromVertex, VertexFD toVertex, MapController controller) {
        this.line = new Line(fromVertex.pos.getX(), fromVertex.pos.getY(), toVertex.pos.getX(), toVertex.pos.getY());
        this.triangle = new Polygon(0, 7.5, 15, -7.5, -15, -7.5);
        this.label = new Label(Double.toString(weight));
        this.group = new Group(line, triangle, label);
        this.line.setStroke(Color.DARKGRAY);
        this.line.setStrokeWidth(2);
        this.triangle.setFill(Color.DARKGRAY);
        this.label.setTextFill(Color.BLACK);
        this.fromPos = fromVertex.pos;
        this.toPos = toVertex.pos;
        this.controller = controller;
        this.id = id;
        this.textField = new TextField(Double.toString(weight));
        this.contextMenu = new ContextMenu();
        MenuItem changeWeight = new MenuItem("Change Weight");
        MenuItem delete = new MenuItem("Delete");
        this.contextMenu.getItems().addAll(changeWeight, delete);
        this.selected = false;
        this.group.setId("edge-" + id);
        this.fromVertex = fromVertex;
        this.toVertex = toVertex;

        try {
            this.controller.backend.addEdge(this);
        } catch (Exception ex) {
            this.controller.criticalError("Backend error when adding Edge " + id + ".", ex);
        }

        update();

        this.group.setOnMouseClicked(this::handleGroupMouseClicked);
        this.group.setOnContextMenuRequested(e -> {
            this.contextMenu.show(this.group, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        this.group.setOnDragDetected(e -> this.dragging = true);
        this.group.setOnMousePressed(e -> {
            this.dragging = false;
            update();
            e.consume();
        });

        changeWeight.setOnAction(e -> {
            this.group.getChildren().remove(this.label);
            this.textField.setText(this.label.getText());
            this.group.getChildren().add(textField);
            update();
            e.consume();
        });

        delete.setOnAction(e -> {
            this.controller.removeEdge(this.id);
            e.consume();
        });

        this.textField.setOnKeyPressed(this::handleTextFieldKeyPressed);
    }

    /**
     * Deletes the edge from the map
     */
    public void deselect() {
        this.line.setStroke(Color.DARKGRAY);
        this.triangle.setFill(Color.DARKGRAY);
        this.label.setTextFill(Color.BLACK);
        this.selected = false;
        this.controller.selectedEdges.remove(this);
    }

    /**
     * Updates the edge
     */
    public void update() {
        updateTheta();
        updatePositions();
        updateArrow();
    }

    /**
     * Updates the position of the edge
     */
    private void updatePositions() {
        double x1a = this.fromPos.getX();
        double y1a = this.fromPos.getY();
        double x2a = this.toPos.getX();
        double y2a = this.toPos.getY();
        this.setWeight(this.controller.distance(x1a, y1a, x2a, y2a));
        double x1b, y1b, x2b, y2b;
        // this.controller.statusLabel.setText("[DEBUG] Theta (deg): " + this.theta * 180 / Math.PI + " Theta (rad): " + this.theta / Math.PI + " pi");
        x1b = x1a - 30 * Math.sin(Math.PI + this.theta);
        y1b = y1a + 30 * Math.cos(Math.PI + this.theta);
        x2b = x2a - 30 * Math.sin(Math.PI + this.theta);
        y2b = y2a + 30 * Math.cos(Math.PI + this.theta);
        this.line.setStartX(x1b);
        this.line.setStartY(y1b);
        this.line.setEndX(x2b);
        this.line.setEndY(y2b);
        MapPoint midPos = new MapPoint((x1b + x2b) / 2, (y1b + y2b) / 2);
        this.label.setLayoutX(midPos.getX() - this.label.getWidth() / 2);
        this.label.setLayoutY(midPos.getY() - this.label.getHeight() / 2);
        this.textField.setLayoutX(midPos.getX() - this.textField.getWidth() / 2);
        this.textField.setLayoutY(midPos.getY() - this.textField.getHeight() / 2);
    }

    /**
     * Updates the angle of the edge
     */
    private void updateTheta() {
        // draw to see the equation
        double deltaX = this.toPos.getX() - this.fromPos.getX();
        double deltaY = this.toPos.getY() - this.fromPos.getY();
        this.theta = Math.atan2(deltaY, deltaX);
    }

    /**
     * Updates the arrow of the edge
     */
    private void updateArrow() {
        double x1 = this.line.getStartX();
        double y1 = this.line.getStartY();
        double x2 = this.line.getEndX();
        double y2 = this.line.getEndY();
        MapPoint toPos = new MapPoint((x1 + x2) / 2 + (x2 - x1) / 2, (y1 + y2) / 2 + (y2 - y1) / 2);
        this.triangle.setRotate((this.theta - Math.PI / 2) * 180 / Math.PI);
        this.triangle.setLayoutX(toPos.getX());
        this.triangle.setLayoutY(toPos.getY());
    }

    /**
     * Getter for the id of the edge
     *
     * @return the id of the edge
     */
    @Override
    public int getId() {
        return this.id;
    }

    /**
     * Setter for the id of the edge
     *
     * @param id the id to be set of the edge
     */
    @Override
    public void setId(int id) {
        this.id = id;
        group.setId(Integer.toString(id));
    }

    /**
     * Getter for the weight of the edge
     *
     * @return the weight of the edge
     */
    @Override
    public double getWeight() {
        return this.weight;
    }

    /**
     * Setter for the weight of the edge
     *
     * @param weight the weight to be set of the edge
     */
    @Override
    public void setWeight(double weight) {
        this.weight = weight;
        this.label.setText(String.format("%.2f", weight));
    }

    /**
     * Getter for the id of the vertex from which the edge starts
     *
     * @return the id of the vertex from which the edge starts
     */
    @Override
    public int getFrom() {
        return this.fromVertex.getId();
    }

    /**
     * Setter for the id of the vertex from which the edge starts
     *
     * @param id the id to be set of the vertex from which the edge starts
     */
    @Override
    public void setFrom(int id) {
        this.fromVertex = this.controller.vertices.get(id);
    }

    /**
     * Getter for the id of the vertex to which the edge ends
     *
     * @return the id of the vertex to which the edge ends
     */
    @Override
    public int getTo() {
        return this.toVertex.getId();
    }

    /**
     * Setter for the id of the vertex to which the edge ends
     *
     * @param id the id to be set of the vertex to which the edge ends
     */
    @Override
    public void setTo(int id) {
        this.toVertex = this.controller.vertices.get(id);
    }

    /**
     * Decides whether another edge is equal to this edge
     *
     * @param o the object to be compared
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeFD edge = (EdgeFD) o;
        return id == edge.id;
    }

    /**
     * Getter of the hash code for this edge
     *
     * @return a hash code for this edge
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Handles the mouse click event on the edge
     *
     * @param e the mouse click event
     */
    private void handleGroupMouseClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.SECONDARY) this.contextMenu.hide();
        if (e.getButton() == MouseButton.PRIMARY && !dragging)
            this.controller.setSelectedEdge(this);
        update();
        e.consume();
    }

    /**
     * Handles the key pressed event on the edge
     *
     * @param e the key pressed event
     */
    private void handleTextFieldKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            double newWeight = Double.parseDouble(this.textField.getText());
            double scale = newWeight / this.weight;
            double dx = this.toPos.getX() - this.fromPos.getX();
            double dy = this.toPos.getY() - this.fromPos.getY();
            this.toVertex.setX(this.fromVertex.getX() + dx * scale);
            this.toVertex.setY(this.fromVertex.getY() + dy * scale);
            this.group.getChildren().remove(this.textField);
            this.group.getChildren().add(this.label);
            for (EdgeFD edge : this.controller.edgesConnectedToVertex.get(this.toVertex))
                edge.update();
            this.controller.backend.changeVertex(this.toVertex.getId(), this.toVertex.getX(), this.toVertex.getY());
        } else if (e.getCode() == KeyCode.ESCAPE) {
            this.group.getChildren().remove(this.textField);
            this.group.getChildren().add(this.label);
            update();
        }
        e.consume();
    }
}
