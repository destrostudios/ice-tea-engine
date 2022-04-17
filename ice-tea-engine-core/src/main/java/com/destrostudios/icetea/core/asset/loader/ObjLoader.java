package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.mesh.Mesh;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.function.Supplier;

public class ObjLoader extends AssetLoader<Mesh, Void> {

    private ArrayList<Vector3f> positionsList;
    private ArrayList<Vector2f> texCoordsList;
    private ArrayList<Integer> indicesList;
    private ArrayList<Vector3f> normalsList;

    @Override
    public Mesh load(Supplier<InputStream> inputStreamSupplier) throws IOException {
        Obj obj;
        try (InputStream inputStream = inputStreamSupplier.get()) {
            obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));
        }
        loadData(obj);
        return createMesh();
    }

    private void loadData(Obj obj) {
        IntBuffer indicesBuffer = ObjData.getFaceVertexIndices(obj);
        FloatBuffer verticesBuffer = ObjData.getVertices(obj);
        FloatBuffer normalsBuffer = ObjData.getNormals(obj);
        FloatBuffer texCoordsBuffer = ObjData.getTexCoords(obj, 2);

        positionsList = new ArrayList<>(verticesBuffer.capacity());
        texCoordsList = new ArrayList<>(texCoordsBuffer.capacity());
        indicesList = new ArrayList<>(indicesBuffer.capacity());
        normalsList = new ArrayList<>(normalsBuffer.capacity());

        for (int i = 0; i < indicesBuffer.capacity(); i++) {
            int index = indicesBuffer.get();
            indicesList.add(index);
        }
        for (int i = 0; i < verticesBuffer.capacity(); i += 3) {
            float x = verticesBuffer.get();
            float y = verticesBuffer.get();
            float z = verticesBuffer.get();
            positionsList.add(new Vector3f(x, y, z));
        }
        if (normalsBuffer.capacity() > 0) {
            for (int i = 0; i < normalsBuffer.capacity(); i += 3) {
                float x = normalsBuffer.get();
                float y = normalsBuffer.get();
                float z = normalsBuffer.get();
                normalsList.add(new Vector3f(x, y, z));
            }
        } else {
            normalsList = null;
        }
        if (texCoordsBuffer.capacity() > 0) {
            for (int i = 0; i < texCoordsBuffer.capacity(); i += 2) {
                float x = texCoordsBuffer.get();
                float y = (1 - texCoordsBuffer.get()); // Flip
                texCoordsList.add(new Vector2f(x, y));
            }
        } else {
            texCoordsList = null;
        }
    }

    private Mesh createMesh() {
        int vertexCount = positionsList.size();
        VertexData[] vertices = new VertexData[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            VertexData vertex = new VertexData();
            vertex.setVector3f("vertexPosition", positionsList.get(i));
            if (texCoordsList != null) {
                vertex.setVector2f("vertexTexCoord", texCoordsList.get(i));
            }
            if (normalsList != null) {
                vertex.setVector3f("vertexNormal", normalsList.get(i));
            }
            vertices[i] = vertex;
        }

        int[] indices = new int[indicesList.size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = indicesList.get(i);
        }

        Mesh mesh = new Mesh();
        mesh.setVertices(vertices);
        mesh.setIndices(indices);
        mesh.updateBounds();
        return mesh;
    }
}
