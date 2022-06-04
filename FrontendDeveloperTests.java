import edu.wisc.cs.cs400.JavaFXTester;
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the frontend of the BadgerMap application
 *
 * @author Ruixuan Tu
 */
public class FrontendDeveloperTests extends JavaFXTester {
    /**
     * Constructor
     */
    public FrontendDeveloperTests() {
        super(App.class);
    }

    /**
     * Helper method to find a child of a node with a specific type
     *
     * @param children the children of the node
     * @param type     the type of the child
     * @return the child of the node with the specified type
     */
    public Node findChildWithType(List<Node> children, Class<? extends Node> type) {
        for (Node child : children)
            if (child.getClass().equals(type))
                return child;
        return null;
    }

    /**
     * Delete a file recursively
     *
     * @param target the file to be deleted
     */
    private void deleteFile(File target) {
        if (target == null) return;
        if (!target.exists()) return;
        if (target.isDirectory())
            for (File f : Objects.requireNonNull(target.listFiles()))
                deleteFile(f);
        target.delete();
    }

    /**
     * Set up an empty folder at TestGraph/
     *
     * @return the File instance of the folder
     */
    private File setupTestGraph() throws IOException {
        File testDir = new File("TestGraph");
        deleteFile(testDir);
        if (!testDir.mkdirs())
            throw new RuntimeException("Failed to create directory: " + testDir.getAbsolutePath());
        return testDir;
    }

    /**
     * Test the exit menu item of the program
     */
    @Test
    @Order(1)
    public void testExitMenuItem() {
        assertEquals(1, this.listWindows().size()); // check if there is a window
        MenuBar menuBar = lookup("#menuBar").query();
        MenuItem exitMenuItem = menuBar.getMenus().get(0).getItems().get(3);
        interact(exitMenuItem::fire); // click the exit menu item
        assertEquals(0, this.listWindows().size()); // check if the window is closed
    }

    /**
     * Test to draw a vertex on a new map by click on the map
     */
    @Test
    @Order(2)
    public void testDrawVertex() {
        ScrollPane scrollPane = lookup("#scrollPane").query();
        Pane pane = lookup("#pane").query();
        assertEquals(0, pane.getChildren().size());
        clickOn(scrollPane);
        assertEquals(1, pane.getChildren().size()); // check if there is a vertex
        Group vertex = (Group) pane.getChildren().get(0);
        assertEquals("vertex-0", vertex.getId());
        assertEquals("0",
                ((Label) Objects.requireNonNull(findChildWithType(vertex.getChildren(), Label.class))).getText());
        assertEquals(2, vertex.getChildren().size()); // check if there are two children (label and circle)
    }

    /**
     * Test to change a vertex's label by right-click on the vertex and type a new label
     */
    @Test
    @Order(3)
    public void testChangeVertexLabel() {
        ScrollPane scrollPane = lookup("#scrollPane").query();
        Pane pane = lookup("#pane").query();
        clickOn(scrollPane);
        Group vertex = (Group) findChildWithType(pane.getChildren(), Group.class);
        rightClickOn(vertex); // request context menu
        Bounds screenBounds = scrollPane.localToScreen(scrollPane.getBoundsInLocal());
        clickOn(screenBounds.getCenterX() + 10, screenBounds.getCenterY() + 10); // click "Rename" menu item
        assertEquals(2, vertex.getChildren().size()); // check if Label is removed and TextField is added
        TextField textField = (TextField) findChildWithType(vertex.getChildren(), TextField.class);
        clickOn(textField); // start typing
        String expectedChangedLabel = "test";
        if (Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_CAPS_LOCK))
            expectedChangedLabel = "TEST";
        type(KeyCode.BACK_SPACE); // erase "0"
        type(KeyCode.T); // type "TEST" and press enter
        type(KeyCode.E);
        type(KeyCode.S);
        type(KeyCode.T);
        type(KeyCode.ENTER); // end typing
        assertEquals(expectedChangedLabel,
                ((Label) Objects.requireNonNull(findChildWithType(vertex.getChildren(), Label.class))).getText());
        assertEquals(2, vertex.getChildren().size()); // check if TextField is removed and Label is added
    }

    /**
     * Test to delete a vertex by right-click on the vertex and click "Delete" menu item
     */
    @Test
    @Order(4)
    public void testRemoveVertex() {
        ScrollPane scrollPane = lookup("#scrollPane").query();
        Pane pane = lookup("#pane").query();
        clickOn(scrollPane);
        Group vertex = (Group) findChildWithType(pane.getChildren(), Group.class);
        rightClickOn(vertex); // request context menu
        Bounds screenBounds = scrollPane.localToScreen(scrollPane.getBoundsInLocal());
        clickOn(screenBounds.getCenterX() + 10, screenBounds.getCenterY() + 50); // click "Delete" menu item
        assertEquals(0, pane.getChildren().size()); // check if vertex is removed
    }

    /**
     * Test to draw an edge by click and drag-and-drop on two vertices
     */
    @Test
    @Order(5)
    public void testDrawEdge() {
        ScrollPane scrollPane = lookup("#scrollPane").query();
        Pane pane = lookup("#pane").query();
        assertEquals(0, pane.getChildren().size());
        Bounds screenBounds = scrollPane.localToScreen(scrollPane.getBoundsInLocal());
        clickOn(screenBounds.getMinX() + 100, screenBounds.getMinY() + 100);
        clickOn(screenBounds.getMinX() + 200, screenBounds.getMinY() + 200);
        assertEquals(2, pane.getChildren().size()); // check if there are two vertices
        Group vertex0 = (Group) pane.getChildren().get(0);
        Group vertex1 = (Group) pane.getChildren().get(1);
        assertEquals("vertex-0", vertex0.getId());
        assertEquals("vertex-1", vertex1.getId());
        clickOn(vertex0); // one direction
        drag(vertex0);
        dropTo(vertex1);
        assertEquals(3, pane.getChildren().size()); // check if there are two vertices and an edge
        Group edge0 = (Group) pane.getChildren().get(2);
        assertEquals("edge-0", edge0.getId());
        assertEquals(String.format("%.2f", Math.sqrt(2 * 100 * 100)),
                ((Label) Objects.requireNonNull(findChildWithType(edge0.getChildren(), Label.class))).getText());
        clickOn(vertex1); // another direction
        drag(vertex1);
        dropTo(vertex0);
        assertEquals(4, pane.getChildren().size()); // check if there are two vertices and two edges
        Group edge1 = (Group) pane.getChildren().get(3);
        assertEquals("edge-1", edge1.getId());
        assertEquals(String.format("%.2f", Math.sqrt(2 * 100 * 100)),
                ((Label) Objects.requireNonNull(findChildWithType(edge1.getChildren(), Label.class))).getText());
        assertEquals(((Label) Objects.requireNonNull(findChildWithType(edge1.getChildren(), Label.class))).getText(),
                ((Label) Objects.requireNonNull(findChildWithType(edge0.getChildren(), Label.class))).getText());
    }

    // below are additional unit tests for integration

    /**
     * Test to normally save a graph to an empty folder (DataLoader involved)
     */
    @Test
    @Order(6)
    public void testNormalSave() throws IOException {
        assertEquals("BadgerMap - Untitled [New]", App.controller.stage.getTitle());
        // draw a vertex
        ScrollPane scrollPane = lookup("#scrollPane").query();
        clickOn(scrollPane);
        assertEquals("BadgerMap - Untitled [Unsaved]", App.controller.stage.getTitle());
        // save the graph
        File testDir = setupTestGraph();
        interact(() -> {
            App.controller.currentGraphFile = testDir;
            App.controller.handleSave();
        });
        // check if the file is saved correctly
        File verticesFile = new File(testDir, "vertices.json");
        File edgesFile = new File(testDir, "edges.json");
        assertTrue(verticesFile.exists() && verticesFile.isFile());
        assertTrue(edgesFile.exists() && edgesFile.isFile());
        assertEquals(List.of("[]"), Files.readAllLines(edgesFile.toPath()));
        List<String> verticesStrArr = Files.readAllLines(verticesFile.toPath());
        verticesStrArr.replaceAll(s -> s.replaceAll(" +", ""));
        verticesStrArr.replaceAll(s -> s.replaceAll("\\d+.\\d+", "")); // ignore specific positions for different platforms
        assertEquals(List.of("[", "{", "\"id\":0,", "\"label\":\"0\",", "\"x\":,", "\"y\":", "}", "]"), verticesStrArr);
        assertEquals("BadgerMap - TestGraph", App.controller.stage.getTitle());
        assertEquals("Saved a graph to folder successfully.", App.controller.statusLabel.getText());
    }

    /**
     * Test to save a graph to a non-existing folder (DataLoader involved)
     */
    @Test
    @Order(7)
    public void testUnexpectedSave() throws IOException {
        // draw a vertex
        ScrollPane scrollPane = lookup("#scrollPane").query();
        clickOn(scrollPane);
        // delete the folder
        File testDir = setupTestGraph();
        testDir.delete();
        // save the graph
        interact(() -> {
            App.controller.currentGraphFile = testDir;
            App.controller.handleSave();
        });
        assertEquals("BadgerMap - Untitled [Unsaved]", App.controller.stage.getTitle());
        assertEquals("Failed to save the graph to folder.", App.controller.statusLabel.getText());
    }
}
