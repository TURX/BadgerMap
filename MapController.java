import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.*;

/**
 * Controller for the map
 *
 * @author Ruixuan Tu
 */
public class MapController {
    public final IBackend backend;
    public LinkedList<VertexFD> selectedVertices;
    public LinkedList<EdgeFD> selectedEdges;
    public Hashtable<Integer, VertexFD> vertices;
    public Hashtable<Integer, EdgeFD> edges;
    public Hashtable<VertexFD, LinkedList<EdgeFD>> edgesConnectedToVertex; // bidirectional
    public VertexFD drawFromVertex;
    public int vertexId;
    public int edgeId;
    public boolean dragging;
    public File currentGraphFile;
    public boolean changed;
    public Stage stage;
    public boolean dialogCancelled;

    @FXML
    public BorderPane borderPane;
    @FXML
    public Pane pane;
    @FXML
    public Label statusLabel;
    @FXML
    public ScrollPane scrollPane;
    @FXML
    public MenuBar menuBar;

    /**
     * Constructor
     */
    public MapController() {
        backend = new BackendPlaceholder();
        selectedVertices = new LinkedList<>();
        selectedEdges = new LinkedList<>();
        vertices = new Hashtable<>();
        edges = new Hashtable<>();
        edgesConnectedToVertex = new Hashtable<>();
        vertexId = 0;
        edgeId = 0;
        drawFromVertex = null;
        currentGraphFile = null;
        dragging = false;
        changed = false;
    }

    /**
     * Initialize the controller
     */
    public void initialize() {
        scrollPane.setOnMouseClicked(this::handleScrollPaneMouseClicked);
        scrollPane.setOnKeyPressed(this::handleScrollPaneKeyPressed);
        scrollPane.setOnDragDetected(e -> this.dragging = true);
        scrollPane.setOnMousePressed(e -> this.dragging = false);
        scrollPane.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::handleChanged);
        scrollPane.addEventFilter(MouseEvent.MOUSE_CLICKED, this::handleChanged);
        scrollPane.addEventFilter(KeyEvent.KEY_PRESSED, this::handleChanged);

        this.statusLabel.setText("New map loaded.");
    }

    /**
     * Add a vertex to be selected
     *
     * @param vertex the vertex to be selected
     */
    public void addSelectedVertex(VertexFD vertex) {
        vertex.circle.setFill(Color.SALMON);
        vertex.label.setTextFill(Color.DARKCYAN);
        vertex.selected = true;
        this.selectedVertices.add(vertex);
    }

    /**
     * Add an edge to be selected
     *
     * @param edge the edge to be selected
     */
    public void addSelectedEdge(EdgeFD edge) {
        edge.line.setStroke(Color.DARKCYAN);
        edge.triangle.setFill(Color.DARKCYAN);
        edge.label.setTextFill(Color.DARKRED);
        edge.selected = true;
        this.selectedEdges.add(edge);
    }

    /**
     * Set a vertex to be selected and unselect all other vertices
     *
     * @param vertex the vertex to be selected
     */
    public void setSelectedVertex(VertexFD vertex) {
        this.deselectAll();
        this.addSelectedVertex(vertex);
        this.statusLabel.setText("Vertex " + vertex.id + " selected, drag-and-drop to draw an edge.");
    }

    /**
     * Set an edge to be selected and unselect all other edges
     *
     * @param edge the edge to be selected
     */
    public void setSelectedEdge(EdgeFD edge) {
        this.deselectAll();
        this.addSelectedEdge(edge);
        this.statusLabel.setText("Edge " + edge.id + " selected and highlighted.");
    }

    /**
     * Remove a vertex with id from the graph
     *
     * @param id the id of the vertex to be removed
     */
    public void removeVertex(int id) {
        Node toRemove = null;
        for (Node node : pane.getChildren())
            if (node.getId().equals("vertex-" + id)) {
                toRemove = node;
                break;
            }
        if (toRemove != null) {
            if (edgesConnectedToVertex.containsKey(vertices.get(id)))
                for (EdgeFD edge : edgesConnectedToVertex.get(vertices.get(id)))
                    removeEdge(edge.getId());
            pane.getChildren().remove(toRemove);
            try {
                backend.removeVertex(id);
            } catch (Exception ex) {
                this.criticalError("Backend error when removing Vertex " + id + ".", ex);
                return;
            }
            vertices.remove(id);
            this.statusLabel.setText("Vertex " + id + " removed.");
        }
    }

    /**
     * Remove an edge with id from the graph
     *
     * @param id the id of the edge to be removed
     */
    public void removeEdge(int id) {
        Node toRemove = null;
        for (Node node : pane.getChildren())
            if (node.getId().equals("edge-" + id)) {
                toRemove = node;
                break;
            }
        if (toRemove != null) {
            try {
                backend.removeEdge(id);
            } catch (Exception ex) {
                this.criticalError("Backend error when removing Edge " + id + ".", ex);
                return;
            }
            pane.getChildren().remove(toRemove);
            edges.remove(id);
            this.statusLabel.setText("Edge " + id + " removed.");
        }
    }

    /**
     * Deselect all selected vertices and edges
     */
    public void deselectAll() {
        // deselect all
        List<VertexFD> selectedVerticesCopy = new ArrayList<>(this.selectedVertices);
        for (VertexFD v : selectedVerticesCopy)
            v.deselect();
        List<EdgeFD> selectedEdgesCopy = new ArrayList<>(this.selectedEdges);
        for (EdgeFD e : selectedEdgesCopy)
            e.deselect();
    }

    /**
     * Calculator for the distance between two points
     */
    public double distance(double ax, double ay, double bx, double by) {
        return Math.sqrt((ax - bx) * (ax - bx) + (ay - by) * (ay - by));
    }

    /**
     * Return an edge from vertex a to vertex b
     *
     * @param a the first vertex
     * @param b the second vertex
     * @return the edge from a to b
     */
    public EdgeFD searchConnectedEdgeBetween(VertexFD a, VertexFD b) {
        if (!edgesConnectedToVertex.containsKey(a)) return null;
        for (EdgeFD e : edgesConnectedToVertex.get(a))
            if (e.getTo() == b.id) return e;
        return null;
    }

    /**
     * Return the vertex within range of x and y
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the vertex within range of x and y
     */
    private VertexFD findVertexAtPoint(double x, double y) {
        VertexFD vertex = null;
        for (VertexFD v : vertices.values()) {
            // find a vertex at the point
            if (!(Math.abs(v.pos.getX() - x) <= 15 && Math.abs(v.pos.getY() - y) <= 15))
                continue;
            if (v.equals(drawFromVertex))
                continue;
            if (this.searchConnectedEdgeBetween(drawFromVertex, v) != null)
                continue;
            vertex = v;
        }
        return vertex;
    }

    /**
     * Handle the mouse click event on the scroll pane
     *
     * @param e the mouse click event
     */
    private void handleScrollPaneMouseClicked(MouseEvent e) {
        double deltaX = scrollPane.getHvalue() * (pane.getWidth() - scrollPane.getWidth());
        double deltaY = scrollPane.getVvalue() * (pane.getHeight() - scrollPane.getHeight());
        double paneClickedX = e.getX() + deltaX;
        double paneClickedY = e.getY() + deltaY;
        if (e.getButton() == MouseButton.PRIMARY && !dragging) {
            VertexFD v = new VertexFD(vertexId, paneClickedX, paneClickedY, Integer.toString(vertexId), this);
            vertices.put(vertexId, v);
            pane.getChildren().add(v.group);
            try {
                this.backend.addVertex(v);
            } catch (Exception ex) {
                this.criticalError("Backend error when adding Vertex " + v.id + ".", ex);
                return;
            }
            this.statusLabel.setText("Vertex " + vertexId + " drawn.");
            vertexId++;
        } else deselectAll();
        if (e.getButton() == MouseButton.PRIMARY && dragging) {
            // edge drawn
            VertexFD drawToVertex = findVertexAtPoint(paneClickedX, paneClickedY);
            if (drawFromVertex != null && drawToVertex != null) {
                EdgeFD edge = new EdgeFD(edgeId, drawFromVertex, drawToVertex, this);
                edges.put(edgeId, edge);
                if (!edgesConnectedToVertex.containsKey(drawFromVertex))
                    edgesConnectedToVertex.put(drawFromVertex, new LinkedList<>());
                if (!edgesConnectedToVertex.containsKey(drawToVertex))
                    edgesConnectedToVertex.put(drawToVertex, new LinkedList<>());
                edgesConnectedToVertex.get(drawFromVertex).add(edge);
                edgesConnectedToVertex.get(drawToVertex).add(edge);
                pane.getChildren().add(edge.group);
                try {
                    this.backend.addEdge(edge);
                } catch (Exception ex) {
                    this.criticalError("Backend error when adding Edge " + edge.id + ".", ex);
                    return;
                }
                this.statusLabel.setText("Edge " + edgeId + " drawn from Vertex " + drawFromVertex.getId() + " to Vertex " + drawToVertex.getId() + ".");
                edgeId++;
            }
        }
    }

    /**
     * Handle the key pressed event on the scroll pane
     *
     * @param e the key pressed event
     */
    private void handleScrollPaneKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            deselectAll();
            this.statusLabel.setText("Deselected all vertices and edges.");
        }
        e.consume();
    }

    /**
     * Handle creation of a new graph
     */
    public void handleNew() {
        this.selectedVertices.clear();
        this.selectedEdges.clear();
        this.vertices.clear();
        this.edges.clear();
        this.edgesConnectedToVertex.clear();
        this.vertexId = 0;
        this.edgeId = 0;
        this.pane.getChildren().clear();
        this.drawFromVertex = null;
        this.statusLabel.setText("New map loaded.");
        this.currentGraphFile = null;
        this.stage.setTitle("BadgerMap - Untitled [New]");
        this.backend.clear();
    }

    /**
     * Handle opening a graph from a folder
     */
    public void handleOpen() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Open Graph");
        this.statusLabel.setText("Select a folder containing vertex and edge JSON files.");
        File file = directoryChooser.showDialog(this.pane.getScene().getWindow());
        handleOpen(file);
    }

    /**
     * Handle opening a graph from a folder with specified File path (for second step/testing)
     */
    public void handleOpen(File file) {
        if (file == null) {
            this.statusLabel.setText("No folder selected.");
            return;
        }
        try {
            this.backend.openFile(file);
            List<IVertex> verticesCopy = this.backend.getVertices();
            List<IEdge> edgesCopy = this.backend.getEdges();
            handleNew();
            for (IVertex v : verticesCopy) {
                vertexId = Math.max(vertexId, v.getId() + 1);
                VertexFD vertex = new VertexFD(v.getId(), v.getX(), v.getY(), v.getLabel(), this);
                vertices.put(v.getId(), vertex);
                pane.getChildren().add(vertex.group);
                this.backend.addVertex(v);
            }
            for (IEdge e : edgesCopy) {
                edgeId = Math.max(edgeId, e.getId() + 1);
                EdgeFD edge = new EdgeFD(e.getId(), vertices.get(e.getFrom()), vertices.get(e.getTo()), this);
                edges.put(e.getId(), edge);
                if (!edgesConnectedToVertex.containsKey(vertices.get(e.getFrom())))
                    edgesConnectedToVertex.put(vertices.get(e.getFrom()), new LinkedList<>());
                if (!edgesConnectedToVertex.containsKey(vertices.get(e.getTo())))
                    edgesConnectedToVertex.put(vertices.get(e.getTo()), new LinkedList<>());
                edgesConnectedToVertex.get(vertices.get(e.getFrom())).add(edge);
                edgesConnectedToVertex.get(vertices.get(e.getTo())).add(edge);
                pane.getChildren().add(edge.group);
                this.backend.addEdge(e);
            }
        } catch (Exception e) {
            this.criticalError("Failed to open the graph from folder.", e);
            return;
        }
        this.statusLabel.setText("Loaded a graph from folder successfully.");
        this.currentGraphFile = file;
        this.stage.setTitle("BadgerMap - " + getFileName());
    }

    /**
     * Handle saving a graph to a folder
     */
    public void handleSave() {
        if (!this.changed)
            return;
        File file = this.currentGraphFile;
        if (file == null) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Save Graph to");
            this.statusLabel.setText("Select a folder to save vertex and edge JSON files.");
            file = directoryChooser.showDialog(this.pane.getScene().getWindow());
            if (file == null) {
                this.statusLabel.setText("No folder selected.");
                return;
            }
        }
        try {
            this.backend.saveFile(file);
        } catch (Exception e) {
            this.statusLabel.setText("Failed to save the graph to folder.");
            return;
        }
        this.statusLabel.setText("Saved a graph to folder successfully.");
        this.currentGraphFile = file;
        this.stage.setTitle("BadgerMap - " + getFileName());
    }

    /**
     * Handle exiting the program
     */
    public void handleExit() {
        this.stage.close();
    }

    /**
     * Handle graph changed
     */
    private void handleChanged(Event e) {
        changed = true;
        this.stage.setTitle("BadgerMap - " + getFileName() + " [Unsaved]");
    }

    /**
     * Handle search edge menu item clicked
     */
    public void handleSearchEdge() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Search Edge");
        dialog.setHeaderText("Enter a edge weight to look up.");
        dialog.setContentText("Edge weight:");
        updateTextInputDialogButtons(dialog);
        this.statusLabel.setText("Search Edge dialog shown.");
        Optional<String> result = dialog.showAndWait();
        double weight = Double.NaN;
        if (result.isPresent()) {
            try {
                weight = Double.parseDouble(result.get());
            } catch (NumberFormatException e) {
                this.statusLabel.setText("Invalid edge weight.");
                return;
            }
        }
        if (!Double.isNaN(weight)) {
            this.deselectAll();
            List<Integer> search = backend.searchByEdge(weight);
            if (search.size() == 0)
                this.statusLabel.setText("No edge found with weight: " + weight + ".");
            else {
                // add search result to selected set
                for (Integer i : search)
                    this.addSelectedEdge(edges.get(i));
                this.statusLabel.setText("Found " + search.size() + " edge(s) with weight " + weight + ".");
            }
        } else {
            if (dialogCancelled)
                this.statusLabel.setText("Search cancelled.");
            else
                this.statusLabel.setText("Invalid edge weight.");
        }
    }

    /**
     * Handle search vertex menu item clicked
     */
    public void handleSearchVertex() {
        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Search Vertex");
        dialog.setHeaderText("Enter a vertex label to look up.");
        dialog.setContentText("Vertex label:");
        updateTextInputDialogButtons(dialog);
        this.statusLabel.setText("Search Vertex dialog shown.");
        Optional<String> result = dialog.showAndWait();
        String label = null;
        if (result.isPresent())
            label = result.get();
        if (label != null) {
            this.deselectAll();
            List<Integer> search = backend.searchByVertex(label);
            if (search.size() == 0)
                this.statusLabel.setText("No vertex found with label: " + label + ".");
            else {
                // add search result to selected set
                for (Integer i : search)
                    this.addSelectedVertex(vertices.get(i));
                this.statusLabel.setText("Found " + search.size() + " vertex(es) with label " + label + ".");
            }
        } else {
            if (dialogCancelled)
                this.statusLabel.setText("Search cancelled.");
            else
                this.statusLabel.setText("Invalid vertex label.");
        }
    }

    /**
     * Initialize the text input dialog buttons
     */
    private void updateTextInputDialogButtons(TextInputDialog dialog) {
        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Cancel");
        this.dialogCancelled = false;
        cancelButton.addEventFilter(ActionEvent.ACTION, e -> this.dialogCancelled = true);
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Search");
    }

    /**
     * Handle shortest path menu item clicked
     */
    public void handleShortestPath() {
        if (this.vertices.size() < 2) {
            this.statusLabel.setText("Need at least two vertices to find a shortest path.");
            return;
        }
        this.deselectAll();
        this.menuBar.setDisable(true);
        this.statusLabel.setText("(0/2 selected) Select two vertices to find the shortest path between them.");
        this.scrollPane.setOnMouseClicked(Event::consume);
        this.scrollPane.setOnKeyPressed(Event::consume);
        for (VertexFD v : vertices.values())
            v.group.setOnMouseClicked(v::handleGroupMouseClickedShortestPathSelection);
    }

    /**
     * After two vertices are selected, find the shortest path between them
     */
    public void processShortestPath() {
        int id1 = this.selectedVertices.get(0).getId();
        int id2 = this.selectedVertices.get(1).getId();
        this.deselectAll();
        try {
            double pathByDistance = backend.getShortestByDistance(id1, id2);
            List<Integer> pathByVertex = backend.getShortestByVertex(id1, id2);
            List<Integer> pathByEdge = backend.getShortestByEdge(id1, id2);
            pathByVertex.forEach(id -> this.addSelectedVertex(vertices.get(id)));
            pathByEdge.forEach(id -> this.addSelectedEdge(edges.get(id)));
            this.statusLabel.setText(String.format("Shortest path found with distance %.2f.", pathByDistance));
        } catch (NoSuchElementException e) {
            this.statusLabel.setText("No path found.");
        }

        this.menuBar.setDisable(false);
        this.scrollPane.setOnMouseClicked(this::handleScrollPaneMouseClicked);
        this.scrollPane.setOnKeyPressed(this::handleScrollPaneKeyPressed);
        for (VertexFD v : vertices.values())
            v.group.setOnMouseClicked(v::handleGroupMouseClicked);
    }

    /**
     * Dialog for critical error occurred, display error message and load a new graph
     */
    public void criticalError(String message, Exception e) {
        this.statusLabel.setText("Critical error occurred. A new graph will be loaded.");
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Critical Error");
        alert.setHeaderText("Critical Error");
        alert.setContentText(message + "\n" + e + ": " + e.getMessage());
        alert.showAndWait();
        handleNew();
    }

    /**
     * Getter of current file name
     *
     * @return current file name
     */
    private String getFileName() {
        if (currentGraphFile == null)
            return "Untitled";
        return currentGraphFile.getName();
    }
}
