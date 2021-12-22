package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.animation.*;
import com.destrostudios.icetea.core.animation.sampled.*;
import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.model.Joint;
import com.destrostudios.icetea.core.model.Skeleton;
import com.destrostudios.icetea.core.model.SkeletonGeometryControl;
import com.destrostudios.icetea.core.model.SkeletonNodeControl;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.util.LowEndianUtil;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import org.joml.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GltfLoader extends AssetLoader<Node, GltfLoaderSettings> {

    public GltfLoader() {
        nodesMap = new HashMap<>();
        jointsMap = new HashMap<>();
        samplersDataMap = new HashMap<>();
        materialsMap = new HashMap<>();
        tmpMatrix4f = new float[16];
    }
    private GltfModel gltfModel;
    private String keyDirectory;
    private HashMap<NodeModel, Node> nodesMap;
    private HashMap<NodeModel, Joint> jointsMap;
    private HashMap<AnimationModel.Sampler, AnimationSamplerData<?>> samplersDataMap;
    private HashMap<MaterialModel, Material> materialsMap;
    private float[] tmpMatrix4f;

    @Override
    public void setContext(AssetManager assetManager, String key, GltfLoaderSettings settings) {
        super.setContext(assetManager, key, settings);
        // TODO: Share code between different loaders that need the directory for references
        int slashIndex = key.lastIndexOf("/");
        if (slashIndex != -1) {
            keyDirectory = key.substring(0, slashIndex + 1);
        } else {
            keyDirectory = "";
        }
    }

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
        CombinedAnimation[] animations = loadAnimations(gltfModel.getAnimationModels());
        rootNode.addControl(new AnimationControl(animations));
        return rootNode;
    }

    private CombinedAnimation[] loadAnimations(List<AnimationModel> animationModels) {
        CombinedAnimation[] animations = new CombinedAnimation[animationModels.size()];
        int animationIndex = 0;
        for (AnimationModel animationModel : animationModels) {
            animations[animationIndex++] = loadCombinedAnimation(animationModel);
        }
        return animations;
    }

    private CombinedAnimation loadCombinedAnimation(AnimationModel animationModel) {
        List<AnimationModel.Channel> channels = animationModel.getChannels();
        Animation[] animations = new Animation[channels.size()];
        int animationIndex = 0;
        for (AnimationModel.Channel channel : channels) {
            animations[animationIndex++] = loadAnimation(channel);
        }
        return new CombinedAnimation(animations);
    }

    private SampledAnimation<?> loadAnimation(AnimationModel.Channel channel) {
        AnimationSamplerData<?> sampler = loadAnimationSamplerData(channel.getSampler());
        Joint joint = jointsMap.get(channel.getNodeModel());
        if (joint != null) {
            switch (channel.getPath()) {
                case "translation": return new JointTranslationAnimation((AnimationSamplerData<Vector3f>) sampler, joint);
                case "rotation": return new JointRotationAnimation((AnimationSamplerData<Quaternionf>) sampler, joint);
                case "scale": return new JointScaleAnimation((AnimationSamplerData<Vector3f>) sampler, joint);
            }
        } else {
            Node node = nodesMap.get(channel.getNodeModel());
            if (node != null) {
                switch (channel.getPath()) {
                    case "translation": return new SpatialTranslationAnimation((AnimationSamplerData<Vector3f>) sampler, node);
                    case "rotation": return new SpatialRotationAnimation((AnimationSamplerData<Quaternionf>) sampler, node);
                    case "scale": return new SpatialScaleAnimation((AnimationSamplerData<Vector3f>) sampler, node);
                }
            }
        }
        throw new UnsupportedOperationException("Animation channel path: " + channel.getPath());
    }

    private AnimationSamplerData<?> loadAnimationSamplerData(AnimationModel.Sampler sampler) {
        return samplersDataMap.computeIfAbsent(sampler, s -> {
            LinkedList<Object> input = readValues(sampler.getInput());
            LinkedList<Object> output = readValues(sampler.getOutput());
            int keyframeIndex = 0;
            float[] keyframeTimes = new float[input.size()];
            Object[] keyframeValues = new Object[output.size()];
            Iterator<Object> inputIterator = input.iterator();
            Iterator<Object> outputIterator = output.iterator();
            while (inputIterator.hasNext()) {
                keyframeTimes[keyframeIndex] = (float) inputIterator.next();
                float[] values = (float[]) outputIterator.next();
                if (values.length == 3) {
                    keyframeValues[keyframeIndex] = new Vector3f(values[0], values[1], values[2]);
                } else if (values.length == 4) {
                    keyframeValues[keyframeIndex] = new Quaternionf(values[0], values[1], values[2], values[3]);
                } else {
                    throw new UnsupportedOperationException("Animation sampler dimension: " + values.length);
                }
                keyframeIndex++;
            }
            return new AnimationSamplerData(keyframeTimes, keyframeValues);
        });
    }

    private Node loadNode(NodeModel nodeModel) {
        Node node = new Node();
        Skeleton skeleton = null;
        SkinModel skinModel = nodeModel.getSkinModel();
        if (skinModel != null) {
            skeleton = loadSkeleton(skinModel);
            node.addControl(new SkeletonNodeControl(skeleton));
        }
        for (MeshModel meshModel : nodeModel.getMeshModels()) {
            Node meshNode = loadNode(meshModel, skeleton);
            node.add(meshNode);
        }
        for (NodeModel childNodeModel : nodeModel.getChildren()) {
            Spatial child = loadNode(childNodeModel);
            node.add(child);
        }
        float[] matrix = nodeModel.getMatrix();
        if (matrix != null) {
            node.setLocalTransform(loadMatrix(matrix));
        }
        nodesMap.put(nodeModel, node);
        return node;
    }

    private Node loadNode(MeshModel meshModel, Skeleton skeleton) {
        Node node = new Node();
        for (MeshPrimitiveModel meshPrimitiveModel : meshModel.getMeshPrimitiveModels()) {
            Geometry geometry = loadGeometry(meshPrimitiveModel);
            if (skeleton != null) {
                geometry.addControl(new SkeletonGeometryControl(skeleton));
            }
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
        LinkedList<Object> jointsList = readValues(meshPrimitiveModel.getAttributes().get("JOINTS_0"));
        LinkedList<Object> weightsList = readValues(meshPrimitiveModel.getAttributes().get("WEIGHTS_0"));
        VertexData[] vertices = new VertexData[positionList.size()];
        Iterator<Object> positionIterator = positionList.iterator();
        Iterator<Object> texCoordIterator = texCoordList.iterator();
        Iterator<Object> normalIterator = normalList.iterator();
        Iterator<Object> jointsIterator = jointsList.iterator();
        Iterator<Object> weightsIterator = weightsList.iterator();
        for (i = 0; i < vertices.length; i++) {
            VertexData vertex = new VertexData();

            float[] positionArray = (float[]) positionIterator.next();
            Vector3f position = new Vector3f(positionArray[0], positionArray[1], positionArray[2]);
            vertex.setVector3f("vertexPosition", position);

            if (texCoordIterator.hasNext()) {
                float[] texCoordArray = (float[]) texCoordIterator.next();
                Vector2f texCoord = new Vector2f(texCoordArray[0], texCoordArray[1]);
                vertex.setVector2f("vertexTexCoord", texCoord);
            }

            if (normalIterator.hasNext()) {
                float[] normalArray = (float[]) normalIterator.next();
                Vector3f normal = new Vector3f(normalArray[0], normalArray[1], normalArray[2]);
                vertex.setVector3f("vertexNormal", normal);
            }

            if (jointsIterator.hasNext()) {
                int[] jointsArray = (int[]) jointsIterator.next();
                Vector4f jointsIndices = new Vector4f(jointsArray[0], jointsArray[1], jointsArray[2], jointsArray[3]);
                vertex.setVector4f("jointsIndices", jointsIndices);
            }

            if (weightsIterator.hasNext()) {
                float[] weightsArray = (float[]) weightsIterator.next();
                Vector4f jointsWeights = new Vector4f(weightsArray[0], weightsArray[1], weightsArray[2], weightsArray[3]);
                vertex.setVector4f("jointsWeights", jointsWeights);
            }

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

    private Skeleton loadSkeleton(SkinModel skinModel) {
        Matrix4f[] inverseBindMatrices = loadMatrices(skinModel.getInverseBindMatrices());
        List<NodeModel> jointNodeModels = skinModel.getJoints();
        Joint[] joints = new Joint[jointNodeModels.size()];
        int jointIndex = 0;
        for (NodeModel nodeModel : jointNodeModels) {
            Joint joint = getOrCreateJoint(nodeModel);
            joint.setInverseBindMatrix(inverseBindMatrices[jointIndex]);
            joints[jointIndex++] = joint;
        }
        return new Skeleton(joints);
    }

    private Matrix4f[] loadMatrices(AccessorModel accessorModel) {
        LinkedList<Object> matricesList = readValues(accessorModel);
        Matrix4f[] matrices = new Matrix4f[matricesList.size()];
        int matrixIndex = 0;
        for (Object matrixObject : matricesList) {
            matrices[matrixIndex++] = (Matrix4f) matrixObject;
        }
        return matrices;
    }

    private Joint getOrCreateJoint(NodeModel nodeModel) {
        Joint joint = jointsMap.get(nodeModel);
        if (joint == null) {
            Transform localResetTransform = new Transform();
            float[] localResetTransformMatrix = nodeModel.getMatrix();
            if (localResetTransformMatrix != null) {
                localResetTransform.set(loadMatrix(localResetTransformMatrix));
            }
            joint = new Joint(nodeModel.getChildren().size(), localResetTransform);
            int childIndex = 0;
            for (NodeModel childNodeModel : nodeModel.getChildren()) {
                joint.setChild(childIndex, getOrCreateJoint(childNodeModel));
                childIndex++;
            }
            jointsMap.put(nodeModel, joint);
        }
        return joint;
    }

    private Matrix4f loadMatrix(float[] matrix) {
        return new Matrix4f(
            matrix[0], matrix[1], matrix[2], matrix[3],
            matrix[4], matrix[5], matrix[6], matrix[7],
            matrix[8], matrix[9], matrix[10], matrix[11],
            matrix[12], matrix[13], matrix[14], matrix[15]
        );
    }

    private LinkedList<Object> readValues(AccessorModel accessorModel) {
        LinkedList<Object> values = new LinkedList<>();
        if (accessorModel == null) {
            return values;
        }
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        InputStream inputStream = assetManager.load(keyDirectory + bufferViewModel.getBufferModel().getUri());
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
                            case VEC4: {
                                int x = LowEndianUtil.readUnsignedShort(dataInputStream);
                                int y = LowEndianUtil.readUnsignedShort(dataInputStream);
                                int z = LowEndianUtil.readUnsignedShort(dataInputStream);
                                int w = LowEndianUtil.readUnsignedShort(dataInputStream);
                                values.add(new int[]{ x, y, z, w });
                                readBytes = 8;
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
                            case VEC4: {
                                float x = LowEndianUtil.readFloat(dataInputStream);
                                float y = LowEndianUtil.readFloat(dataInputStream);
                                float z = LowEndianUtil.readFloat(dataInputStream);
                                float w = LowEndianUtil.readFloat(dataInputStream);
                                values.add(new float[] { x, y, z, w });
                                readBytes = 16;
                                break;
                            }
                            case MAT4: {
                                for (int r = 0; r < tmpMatrix4f.length; r++) {
                                    tmpMatrix4f[r] = LowEndianUtil.readFloat(dataInputStream);
                                }
                                values.add(loadMatrix(tmpMatrix4f));
                                readBytes = 64;
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
        return materialsMap.computeIfAbsent(materialModel, mm -> {
            Material material = new Material();
            material.setVertexShader(new Shader("shaders/default.vert", new String[] { "light", "shadow" }));
            material.setFragmentShader(new Shader("shaders/default.frag", new String[] { "light", "shadow" }));
            Object baseColorTextureValue = materialModel.getValues().get("baseColorTexture");
            if (baseColorTextureValue != null) {
                int baseColorTextureIndex = (int) baseColorTextureValue;
                TextureModel baseColorTextureModel = gltfModel.getTextureModels().get(baseColorTextureIndex);
                String textureFilePath = keyDirectory + baseColorTextureModel.getImageModel().getUri();
                material.setTexture("diffuseMap", assetManager.loadTexture(textureFilePath));
            }
            return material;
        });
    }
}
