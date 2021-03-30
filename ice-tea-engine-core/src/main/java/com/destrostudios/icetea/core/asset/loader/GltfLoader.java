package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.util.LowEndianUtil;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import static java.lang.ClassLoader.getSystemClassLoader;

public class GltfLoader extends AssetLoader<Node, GltfLoaderSettings> {

    private GltfModel gltfModel;

    @Override
    public Node load(InputStream inputStream) throws IOException {
        gltfModel = new GltfModelReader().readWithoutReferences(inputStream);
        return loadScenes();
    }

    private Node loadScenes() {
        Node rootNode = new Node();
        for (SceneModel sceneModel : gltfModel.getSceneModels()) {
            Node sceneNode = new Node();
            for (NodeModel nodeModel : sceneModel.getNodeModels()) {
                Node node = loadNode(nodeModel);
                sceneNode.add(node);
            }
            rootNode.add(sceneNode);
        }
        return rootNode;
    }

    private Node loadNode(NodeModel nodeModel) {
        Node node = new Node();
        for (MeshModel meshModel : nodeModel.getMeshModels()) {
            Node meshNode = loadNode(meshModel);
            node.add(meshNode);
        }
        for (NodeModel childNodeModel : nodeModel.getChildren()) {
            Spatial child = loadNode(childNodeModel);
            node.add(child);
        }
        float[] matrix = nodeModel.getMatrix();
        if (matrix != null) {
            node.setLocalTransform(new Matrix4f(
                matrix[0], matrix[1], matrix[2], matrix[3],
                matrix[4], matrix[5], matrix[6], matrix[7],
                matrix[8], matrix[9], matrix[10], matrix[11],
                matrix[12], matrix[13], matrix[14], matrix[15]
            ));
        }
        return node;
    }

    private Node loadNode(MeshModel meshModel) {
        Node node = new Node();
        for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
            Geometry geometry = loadGeometry(meshPrimitiveModel);
            node.add(geometry);
        }
        return node;
    }

    private Geometry loadGeometry(MeshPrimitiveModel meshPrimitiveModel) {
        Geometry geometry = new Geometry();
        Mesh mesh = new Mesh();

        LinkedList<Object> indicesList = readValues(meshPrimitiveModel.getIndices());
        int[] indices = new int[indicesList.size()];
        int i = 0;
        for (Object index : indicesList) {
            indices[i] = (int) index;
            i++;
        }
        mesh.setIndices(indices);

        LinkedList<Object> positionList = readValues(meshPrimitiveModel.getAttributes().get("POSITION"));
        LinkedList<Object> texCoordList = readValues(meshPrimitiveModel.getAttributes().get("TEXCOORD_0"));
        LinkedList<Object> normalList = readValues(meshPrimitiveModel.getAttributes().get("NORMAL"));
        VertexData[] vertices = new VertexData[positionList.size()];
        Iterator<Object> positionIterator = positionList.iterator();
        Iterator<Object> texCoordIterator = texCoordList.iterator();
        Iterator<Object> normalIterator = normalList.iterator();
        for (i = 0; i < vertices.length; i++) {
            float[] positionArray = (float[]) positionIterator.next();
            float[] texCoordArray = (float[]) texCoordIterator.next();
            float[] normalArray = (float[]) normalIterator.next();

            Vector3f position = new Vector3f(positionArray[0], positionArray[1], positionArray[2]);
            Vector2f texCoord = new Vector2f(texCoordArray[0], texCoordArray[1]);
            Vector3f normal = new Vector3f(normalArray[0], normalArray[1], normalArray[2]);

            VertexData vertex = new VertexData();
            vertex.setVector3f("modelSpaceVertexPosition", position);
            vertex.setVector3f("vertexColor", new Vector3f(1, 1, 1));
            vertex.setVector2f("vertexTexCoord", texCoord);
            vertex.setVector3f("vertexNormal", normal);
            vertices[i] = vertex;
        }
        mesh.setVertices(vertices);
        mesh.updateBounds();

        if (settings.isGenerateNormals()) {
            mesh.generateNormals();
        }

        geometry.setMesh(mesh);

        Material material = loadMaterial(meshPrimitiveModel.getMaterialModel());
        geometry.setMaterial(material);

        return geometry;
    }

    private LinkedList<Object> readValues(AccessorModel accessorModel) {
        LinkedList<Object> values = new LinkedList<>();
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        InputStream inputStream = getSystemClassLoader().getResourceAsStream("models/" + bufferViewModel.getBufferModel().getUri());
        try {
            DataInputStream dataInputStream = new DataInputStream(inputStream);
            dataInputStream.skip(bufferViewModel.getByteOffset() + accessorModel.getByteOffset());
            int readBytes;
            for (int i = 0; i < accessorModel.getCount(); i++) {
                switch (accessorModel.getComponentType()) {
                    case GltfConstants.GL_UNSIGNED_SHORT:
                        switch (accessorModel.getElementType()) {
                            case SCALAR: {
                                int value = LowEndianUtil.readUnsignedShort(dataInputStream);
                                values.add(value);
                                readBytes = 2;
                                break;
                            }
                            default:
                                throw new UnsupportedOperationException("GLTF GL_UNSIGNED_SHORT element type " + accessorModel.getElementType());
                        }
                        break;
                    case GltfConstants.GL_UNSIGNED_INT:
                        switch (accessorModel.getElementType()) {
                            case SCALAR: {
                                int value = LowEndianUtil.readUnsignedInt(dataInputStream);
                                values.add(value);
                                readBytes = 2;
                                break;
                            }
                            default:
                                throw new UnsupportedOperationException("GLTF GL_UNSIGNED_INT element type " + accessorModel.getElementType());
                        }
                        break;
                    case GltfConstants.GL_FLOAT:
                        switch (accessorModel.getElementType()) {
                            case SCALAR: {
                                float value = LowEndianUtil.readFloat(dataInputStream);
                                values.add(value);
                                readBytes = 4;
                                break;
                            }
                            case VEC2: {
                                float x = LowEndianUtil.readFloat(dataInputStream);
                                float y = LowEndianUtil.readFloat(dataInputStream);
                                values.add(new float[] { x, y });
                                readBytes = 8;
                                break;
                            }
                            case VEC3: {
                                float x = LowEndianUtil.readFloat(dataInputStream);
                                float y = LowEndianUtil.readFloat(dataInputStream);
                                float z = LowEndianUtil.readFloat(dataInputStream);
                                values.add(new float[] { x, y, z });
                                readBytes = 12;
                                break;
                            }
                            default:
                                throw new UnsupportedOperationException("GLTF GL_FLOAT element type " + accessorModel.getElementType());
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("GLTF component type " + accessorModel.getComponentType());
                }
                Integer byteStride = bufferViewModel.getByteStride();
                if (byteStride != null) {
                    dataInputStream.skipBytes(byteStride - readBytes);
                }
            }
            dataInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return values;
    }

    private Material loadMaterial(MaterialModel materialModel) {
        Material material = new Material();
        material.setVertexShader(new Shader("shaders/my_shader.vert", new String[] { "light", "shadow" }));
        material.setFragmentShader(new Shader("shaders/my_shader.frag", new String[] { "light", "shadow" }));
        int baseColorTextureIndex = (int) materialModel.getValues().get("baseColorTexture");
        TextureModel baseColorTextureModel = gltfModel.getTextureModels().get(baseColorTextureIndex);
        String textureFilePath = "models/" + baseColorTextureModel.getImageModel().getUri();
        material.setTexture("diffuseMap", assetManager.loadTexture(textureFilePath));
        return material;
    }
}
