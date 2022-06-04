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
import javafx.scene.shape.Circle;

import java.util.Objects;

/**
 * VertexFD is a class that represents a vertex on the graph, and an instance of IVertex
 *
 * @author Ruixuan Tu
 */
public class VertexFD implements IVertex {
    public Group group;
    public Circle circle;
    public int id;
    public MapPoint pos;
    public Label label;
    public TextField textField;
    public ContextMenu contextMenu;
    public boolean selected;
    public boolean dragging;
    public MapController controller;

    /**
     * Constructor for VertexFD
     *
     * @param id         the id of the vertex
     * @param x          the x coordinate of the vertex
     * @param y          the y coordinate of the vertex
     * @param labelText  the label text of the vertex
     * @param controller the controller of the graph
     */
    public VertexFD(int id, double x, double y, String labelText, MapController controller) {
        this.circle = new Circle(x, y, 30);
        this.label = new Label(labelText);
        this.group = new Group(circle, label);
        this.circle.setFill(Color.LIGHTGRAY);
        this.pos = new MapPoint(x, y);
        this.id = id;
        this.textField = new TextField(labelText);
        this.contextMenu = new ContextMenu();
        MenuItem rename = new MenuItem("Rename");
        MenuItem delete = new MenuItem("Delete");
        this.contextMenu.getItems().addAll(rename, delete);
        this.selected = false;
        this.group.setId("vertex-" + id);
        this.controller = controller;
        try {
            this.controller.backend.addVertex(this);
        } catch (Exception ex) {
            this.controller.criticalError("Backend error when adding Vertex " + id + ".", ex);
        }
        updatePositions();

        this.group.setOnMouseClicked(this::handleGroupMouseClicked);
        this.group.setOnContextMenuRequested(e -> {
            this.contextMenu.show(this.group, e.getScreenX(), e.getScreenY());
            e.consume();
        });
        this.group.setOnMouseDragged(this::handleGroupMouseDragged);
        this.group.setOnDragDetected(e -> this.dragging = true);
        this.group.setOnMousePressed(e -> {
            this.dragging = false;
            updatePositions();
        });

        rename.setOnAction(e -> {
            this.group.getChildren().remove(this.label);
            this.textField.setText(this.label.getText());
            this.group.getChildren().add(textField);
            updatePositions();
            e.consume();
        });
        delete.setOnAction(e -> {
            this.controller.removeVertex(this.id);
            e.consume();
        });

        this.textField.setOnKeyPressed(this::handleTextFieldKeyPressed);
    }

    /**
     * Deselects the vertex
     */
    public void deselect() {
        this.circle.setFill(Color.LIGHTGRAY);
        this.label.setTextFill(Color.BLACK);
        this.selected = false;
        this.controller.selectedVertices.remove(this);
    }

    /**
     * Updates the x and y positions of the vertex
     */
    private void updatePositions() {
        this.label.setLayoutX(this.circle.getCenterX() - this.label.getWidth() / 2);
        this.label.setLayoutY(this.circle.getCenterY() - this.label.getHeight() / 2);
        this.textField.setLayoutX(this.circle.getCenterX() - this.textField.getWidth() / 2);
        this.textField.setLayoutY(this.circle.getCenterY() - this.textField.getHeight() / 2);
        this.pos.setPos(this.circle.getCenterX(), this.circle.getCenterY());
        this.controller.backend.changeVertex(this.id, this.pos.getX(), this.pos.getY());
    }

    /**
     * Handles the key pressed event on the text field
     *
     * @param e the key pressed event
     */
    private void handleTextFieldKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            this.label.setText(this.textField.getText());
            this.controller.backend.changeVertex(this.getId(), this.textField.getText());
            this.group.getChildren().remove(this.textField);
            this.group.getChildren().add(this.label);
            updatePositions();
        } else if (e.getCode() == KeyCode.ESCAPE) {
            this.group.getChildren().remove(this.textField);
            this.group.getChildren().add(this.label);
            updatePositions();
        }
        e.consume();
    }

    /**
     * Handles the mouse clicked event on the vertex
     *
     * @param e the mouse clicked event
     */
    public void handleGroupMouseClicked(MouseEvent e) {
        if (e.getButton() != MouseButton.SECONDARY) this.contextMenu.hide();
        if (e.getButton() == MouseButton.PRIMARY && !dragging)
            this.controller.setSelectedVertex(this);
        updatePositions();
        e.consume();
    }

    /**
     * Handles the mouse clicked event on the vertex when shortest path vertices is being selected
     *
     * @param e the mouse clicked event
     */
    public void handleGroupMouseClickedShortestPathSelection(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY && !dragging) {
            this.controller.addSelectedVertex(this);
            if (this.controller.selectedVertices.size() == 1)
                this.controller.statusLabel.setText("(1/2 selected) Select another vertex to find the shortest path to it.");
            else if (this.controller.selectedVertices.size() == 2) {
                this.controller.statusLabel.setText("(2/2 selected) Processing shortest path...");
                this.controller.processShortestPath();
            }
        }
        updatePositions();
        e.consume();
    }

    /**
     * Handles the mouse dragged event on the vertex
     *
     * @param e the mouse dragged event
     */
    private void handleGroupMouseDragged(MouseEvent e) {
        if (!selected) {
            double draggedX = e.getX();
            double draggedY = e.getY();
            this.circle.setCenterX(draggedX);
            this.circle.setCenterY(draggedY);
            updatePositions();
            if (this.controller.edgesConnectedToVertex.containsKey(this))
                for (EdgeFD edge : this.controller.edgesConnectedToVertex.get(this))
                    edge.update();
        } else {
            if (this.controller.selectedVertices.size() == 1)
                // start to draw an edge
                this.controller.drawFromVertex = this;
        }
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
     * Getter for the label of the edge
     *
     * @return the label text of the edge
     */
    @Override
    public String getLabel() {
        return this.label.getText();
    }

    /**
     * Setter for the label of the edge
     *
     * @param label the label text to be set of the edge
     */
    @Override
    public void setLabel(String label) {
        this.label.setText(label);
    }

    /**
     * Getter for the x coordinate of the edge
     *
     * @return the x coordinate of the edge
     */
    @Override
    public double getX() {
        return this.circle.getCenterX();
    }

    /**
     * Setter for the x coordinate of the edge
     *
     * @param x the x coordinate to be set of the edge
     */
    @Override
    public void setX(double x) {
        this.circle.setCenterX(x);
        updatePositions();
    }

    /**
     * Getter for the y coordinate of the edge
     *
     * @return the y coordinate of the edge
     */
    @Override
    public double getY() {
        return this.circle.getCenterY();
    }

    /**
     * Setter for the y coordinate of the edge
     *
     * @param y the y coordinate to be set of the edge
     */
    @Override
    public void setY(double y) {
        this.circle.setCenterY(y);
        updatePositions();
    }

    /**
     * Decides whether another vertex is equal to this vertex
     *
     * @param o the object to be compared
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexFD vertex = (VertexFD) o;
        return id == vertex.id;
    }

    /**
     * Getter of the hash code for this vertex
     *
     * @return a hash code for this vertex
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
