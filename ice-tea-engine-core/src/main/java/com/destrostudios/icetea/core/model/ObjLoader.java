package com.destrostudios.icetea.core.model;

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

public class ObjLoader {

    public static ObjModel loadModel(InputStream inputStream) {
        try {
            Obj obj = ObjUtils.convertToRenderable(ObjReader.read(inputStream));

            IntBuffer indices = ObjData.getFaceVertexIndices(obj);
            FloatBuffer vertices = ObjData.getVertices(obj);
            FloatBuffer normals = ObjData.getNormals(obj);
            FloatBuffer texCoords = ObjData.getTexCoords(obj, 2);

            ObjModel objModel = new ObjModel();
            for (int i = 0; i < indices.capacity(); i++) {
                int index = indices.get();
                objModel.getIndices().add(index);
            }
            for (int i = 0; i < vertices.capacity(); i += 3) {
                float x = vertices.get();
                float y = vertices.get();
                float z = vertices.get();
                objModel.getPositions().add(new Vector3f(x, y, z));
            }
            if (normals.capacity() > 0) {
                for (int i = 0; i < normals.capacity(); i += 3) {
                    float x = normals.get();
                    float y = normals.get();
                    float z = normals.get();
                    objModel.getNormals().add(new Vector3f(x, y, z));
                }
            } else {
                objModel.setNormals(null);
            }
            if (texCoords.capacity() > 0) {
                for (int i = 0; i < texCoords.capacity(); i += 2) {
                    float x = texCoords.get();
                    float y = (1 - texCoords.get()); // Flip
                    objModel.getTexCoords().add(new Vector2f(x, y));
                }
            } else {
                objModel.setTexCoords(null);
            }
            return objModel;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
