import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataLoaderPlaceholder implements IDataLoader {
    Gson gson;

    public DataLoaderPlaceholder() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }


    @Override
    public List<IVertex> loadVertices(String path) throws IOException {
        File file = new File(path + File.separator + "vertices.json");
        String json = Files.readString(file.toPath());
        VertexGeneral[] verticesArray = gson.fromJson(json, VertexGeneral[].class);
        return new ArrayList<>(Arrays.asList(verticesArray));
    }

    @Override
    public List<IEdge> loadEdges(String path) throws IOException {
        File file = new File(path + File.separator + "edges.json");
        String json = Files.readString(file.toPath());
        EdgeGeneral[] edgesArray = gson.fromJson(json, EdgeGeneral[].class);
        return new ArrayList<>(Arrays.asList(edgesArray));
    }

    @Override
    public void writeVertices(List<IVertex> vertices, String path) throws IOException {
        List<VertexGeneral> toSave = new ArrayList<>();
        for (IVertex v : vertices)
            toSave.add(new VertexGeneral(v.getId(), v.getLabel(), v.getX(), v.getY()));
        String json = gson.toJson(toSave);
        File file = new File(path + File.separator + "vertices.json");
        if (!file.exists())
            if (!file.createNewFile())
                throw new IOException("Could not create file");
        Files.writeString(file.toPath(), json);
    }

    @Override
    public void writeEdges(List<IEdge> edges, String path) throws IOException {
        List<EdgeGeneral> toSave = new ArrayList<>();
        for (IEdge e : edges)
            toSave.add(new EdgeGeneral(e.getId(), e.getWeight(), e.getFrom(), e.getTo()));
        String json = gson.toJson(toSave);
        File file = new File(path + File.separator + "edges.json");
        if (!file.exists())
            if (!file.createNewFile())
                throw new IOException("Could not create file");
        Files.writeString(file.toPath(), json);
    }
}
