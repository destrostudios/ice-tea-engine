package com.destrostudios.icetea.core.asset.loader;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.animation.*;
import com.destrostudios.icetea.core.animation.sampled.*;
import com.destrostudios.icetea.core.asset.AssetKey;
import com.destrostudios.icetea.core.asset.AssetLoader;
import com.destrostudios.icetea.core.asset.AssetManager;
import com.destrostudios.icetea.core.asset.locator.FileAssetKey;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.model.*;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.LowEndianUtil;
import com.destrostudios.icetea.core.util.SpatialUtil;
import de.javagl.jgltf.model.*;
import de.javagl.jgltf.model.io.GltfModelReader;
import de.javagl.jgltf.model.v1.MaterialModelV1;
import de.javagl.jgltf.model.v2.MaterialModelV2;
import org.joml.*;

import java.io.*;
import java.util.*;

public class GltfLoader extends AssetLoader<Spatial, GltfLoaderSettings> {

    public GltfLoader() {
        nodesMap = new HashMap<>();
        skeletonMap = new HashMap<>();
        skeletonNodeMap = new HashMap<>();
        samplersDataMap = new HashMap<>();
        materialsMap = new HashMap<>();
        tmpMatrix4f = new float[16];
        buffers = new HashMap<>();
    }
    private GltfModel gltfModel;
    private String keyDirectory;
    private HashMap<NodeModel, Node> nodesMap;
    private HashMap<SkinModel, Skeleton> skeletonMap;
    private HashMap<NodeModel, SkeletonNode> skeletonNodeMap;
    private HashMap<AnimationModel.Sampler, AnimationSamplerData<?>> samplersDataMap;
    private HashMap<MaterialModel, Material> materialsMap;
    private HashMap<BufferModel, byte[]> buffers;
    private float[] tmpMatrix4f;

    @Override
    public void setContext(AssetManager assetManager, AssetKey assetKey, GltfLoaderSettings settings) {
        super.setContext(assetManager, assetKey, settings);
        // TODO: Share code between different loaders that need the directory for references
        int slashIndex = assetKey.getKey().lastIndexOf("/");
        if (slashIndex != -1) {
            keyDirectory = assetKey.getKey().substring(0, slashIndex + 1);
        } else {
            keyDirectory = "";
        }
    }

    @Override
    public Spatial load() throws IOException {
        GltfModelReader gltfModelReader = new GltfModelReader();
        if (assetKey instanceof FileAssetKey fileAssetKey) {
            gltfModel = gltfModelReader.read(fileAssetKey.getPath());
        } else {
            gltfModel = gltfModelReader.readWithoutReferences(assetKey.openInputStream());
        }
        return loadScenes();
    }

    private Spatial loadScenes() {
        Node rootNode = new Node();
        if (settings.isLoadAllNodesAsOneScene()) {
            for (NodeModel nodeModel : gltfModel.getNodeModels()) {
                Node nodeNode = loadNode(nodeModel);
                rootNode.add(nodeNode);
            }
        } else {
            for (SceneModel sceneModel : gltfModel.getSceneModels()) {
                Node sceneNode = loadScene(sceneModel);
                rootNode.add(sceneNode);
            }
        }
        Spatial spatial = (settings.isBakeGeometries() ? SpatialUtil.bakeGeometries(rootNode) : rootNode);
        Collection<Skeleton> skeletons = skeletonMap.values();
        if (skeletons.size() > 0) {
            spatial.addControl(new SkeletonsControl(skeletons));
        }
        ArrayList<CombinedAnimation> animations = loadAnimations(gltfModel.getAnimationModels());
        if (animations.size() > 0) {
            spatial.addControl(new AnimationControl(animations));
        }
        return spatial;
    }

    private Node loadScene(SceneModel sceneModel) {
        Node node = new Node();
        node.setName(sceneModel.getName());
        for (NodeModel nodeModel : sceneModel.getNodeModels()) {
            Node nodeNode = loadNode(nodeModel);
            node.add(nodeNode);
        }
        return node;
    }

    private Node loadNode(NodeModel nodeModel) {
        Node node = new Node();
        node.setName(nodeModel.getName());
        Skeleton skeleton = null;
        SkinModel skinModel = nodeModel.getSkinModel();
        if (skinModel != null) {
            skeleton = loadSkeleton(skinModel);
        }
        for (MeshModel meshModel : nodeModel.getMeshModels()) {
            Node meshNode = loadNode(meshModel, skeleton);
            node.add(meshNode);
        }
        for (NodeModel childNodeModel : nodeModel.getChildren()) {
            Node child = loadNode(childNodeModel);
            node.add(child);
        }
        node.setLocalTransform(loadTransform(nodeModel));
        nodesMap.put(nodeModel, node);
        return node;
    }

    private Node loadNode(MeshModel meshModel, Skeleton skeleton) {
        Node node = new Node();
        node.setName(meshModel.getName());
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

        geometry.setMesh(mesh);

        Material material = loadMaterial(meshPrimitiveModel.getMaterialModel());
        geometry.setMaterial(material);

        return geometry;
    }

    private Skeleton loadSkeleton(SkinModel skinModel) {
        return skeletonMap.computeIfAbsent(skinModel, sm -> {
            Matrix4f[] inverseBindMatrices = loadMatrices(sm.getInverseBindMatrices());
            List<NodeModel> jointNodeModels = sm.getJoints();
            Joint[] joints = new Joint[jointNodeModels.size()];
            ArrayList<SkeletonNode> skeletonNodes = new ArrayList<>();
            int jointIndex = 0;
            for (NodeModel nodeModel : jointNodeModels) {
                Joint joint = (Joint) getOrCreateSkeletonNode(nodeModel, jointNodeModels);
                joint.setInverseBindMatrix(inverseBindMatrices[jointIndex]);
                joints[jointIndex++] = joint;
                skeletonNodes.add(joint);
                addSkeletonNodeParents(nodeModel, jointNodeModels, skeletonNodes);
            }
            return new Skeleton(skeletonNodes.toArray(SkeletonNode[]::new), joints);
        });
    }

    private void addSkeletonNodeParents(NodeModel childNodeModel, List<NodeModel> jointNodeModels, ArrayList<SkeletonNode> skeletonNodes) {
        NodeModel parentNodeModel = childNodeModel.getParent();
        if (parentNodeModel != null) {
            SkeletonNode parentSkeletonNode = getOrCreateSkeletonNode(parentNodeModel, jointNodeModels);
            if (!(parentSkeletonNode instanceof Joint)) {
                skeletonNodes.add(parentSkeletonNode);
                addSkeletonNodeParents(parentNodeModel, jointNodeModels, skeletonNodes);
            }
        }
    }

    private SkeletonNode getOrCreateSkeletonNode(NodeModel nodeModel, List<NodeModel> jointNodeModels) {
        SkeletonNode skeletonNode = skeletonNodeMap.get(nodeModel);
        if (skeletonNode != null) {
            return skeletonNode;
        }
        Transform localResetTransform = loadTransform(nodeModel);
        if (jointNodeModels.contains(nodeModel)) {
            skeletonNode = new Joint(nodeModel.getChildren().size(), localResetTransform);
        } else {
            skeletonNode = new SkeletonNode(nodeModel.getChildren().size(), localResetTransform);
        }
        int childIndex = 0;
        for (NodeModel childNodeModel : nodeModel.getChildren()) {
            skeletonNode.setChild(childIndex, getOrCreateSkeletonNode(childNodeModel, jointNodeModels));
            childIndex++;
        }
        skeletonNodeMap.put(nodeModel, skeletonNode);
        return skeletonNode;
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

    private Transform loadTransform(NodeModel nodeModel) {
        Transform transform = new Transform();
        float[] matrix = nodeModel.getMatrix();
        if (matrix != null) {
            transform.set(loadMatrix(matrix));
        } else {
            float[] translation = nodeModel.getTranslation();
            if (translation != null) {
                transform.setTranslation(new Vector3f(translation[0], translation[1], translation[2]));
            }
            float[] scale = nodeModel.getScale();
            if (scale != null) {
                transform.setScale(new Vector3f(scale[0], scale[1], scale[2]));
            }
            float[] rotation = nodeModel.getRotation();
            if (rotation != null) {
                transform.setRotation(new Quaternionf(rotation[0], rotation[1], rotation[2], rotation[3]));
            }
        }
        return transform;
    }

    private ArrayList<CombinedAnimation> loadAnimations(List<AnimationModel> animationModels) {
        ArrayList<CombinedAnimation> animations = new ArrayList<>(animationModels.size());
        for (AnimationModel animationModel : animationModels) {
            animations.add(loadCombinedAnimation(animationModel));
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
        CombinedAnimation combinedAnimation = new CombinedAnimation(animations);
        combinedAnimation.setName(animationModel.getName());
        return combinedAnimation;
    }

    private SampledAnimation<?> loadAnimation(AnimationModel.Channel channel) {
        AnimationSamplerData<?> sampler = loadAnimationSamplerData(channel.getSampler());
        SkeletonNode skeletonNode = skeletonNodeMap.get(channel.getNodeModel());
        if (skeletonNode != null) {
            Joint joint = (Joint) skeletonNode;
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
            return new AnimationSamplerData<>(keyframeTimes, keyframeValues);
        });
    }

    private LinkedList<Object> readValues(AccessorModel accessorModel) {
        LinkedList<Object> values = new LinkedList<>();
        if (accessorModel == null) {
            return values;
        }
        BufferViewModel bufferViewModel = accessorModel.getBufferViewModel();
        byte[] buffer = buffers.get(bufferViewModel.getBufferModel());
        try {
            if (buffer == null) {
                try (InputStream inputStream = assetManager.loadInputStream(keyDirectory + bufferViewModel.getBufferModel().getUri())) {
                    buffer = inputStream.readAllBytes();
                }
                buffers.put(bufferViewModel.getBufferModel(), buffer);
            }
            ByteArrayInputStream bufferInputStream = new ByteArrayInputStream(buffer);
            bufferInputStream.skip(bufferViewModel.getByteOffset() + accessorModel.getByteOffset());
            int readBytes;
            for (int i = 0; i < accessorModel.getCount(); i++) {
                // TODO: Do all this in a generic way
                switch (accessorModel.getComponentType()) {
                    case GltfConstants.GL_UNSIGNED_BYTE:
                        switch (accessorModel.getElementType()) {
                            case VEC4: {
                                int x = bufferInputStream.read();
                                int y = bufferInputStream.read();
                                int z = bufferInputStream.read();
                                int w = bufferInputStream.read();
                                values.add(new int[]{ x, y, z, w });
                                readBytes = 4;
                                break;
                            }
                            default:
                                throw new UnsupportedOperationException("GLTF GL_UNSIGNED_BYTE element type " + accessorModel.getElementType());
                        }
                        break;
                    case GltfConstants.GL_UNSIGNED_SHORT:
                        switch (accessorModel.getElementType()) {
                            case SCALAR: {
                                int value = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                values.add(value);
                                readBytes = 2;
                                break;
                            }
                            case VEC2: {
                                int x = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                values.add(new int[]{ x, y });
                                readBytes = 4;
                                break;
                            }
                            case VEC3: {
                                int x = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int z = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                values.add(new int[]{ x, y, z });
                                readBytes = 6;
                                break;
                            }
                            case VEC4: {
                                int x = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int z = LowEndianUtil.readUnsignedShort(bufferInputStream);
                                int w = LowEndianUtil.readUnsignedShort(bufferInputStream);
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
                                int value = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                values.add(value);
                                readBytes = 2;
                                break;
                            }
                            case VEC2: {
                                int x = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                values.add(new int[]{ x, y });
                                readBytes = 4;
                                break;
                            }
                            case VEC3: {
                                int x = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int z = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                values.add(new int[]{ x, y, z });
                                readBytes = 6;
                                break;
                            }
                            case VEC4: {
                                int x = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int y = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int z = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                int w = LowEndianUtil.readUnsignedInt(bufferInputStream);
                                values.add(new int[]{ x, y, z, w });
                                readBytes = 8;
                                break;
                            }
                            default:
                                throw new UnsupportedOperationException("GLTF GL_UNSIGNED_INT element type " + accessorModel.getElementType());
                        }
                        break;
                    case GltfConstants.GL_FLOAT:
                        switch (accessorModel.getElementType()) {
                            case SCALAR: {
                                float value = LowEndianUtil.readFloat(bufferInputStream);
                                values.add(value);
                                readBytes = 4;
                                break;
                            }
                            case VEC2: {
                                float x = LowEndianUtil.readFloat(bufferInputStream);
                                float y = LowEndianUtil.readFloat(bufferInputStream);
                                values.add(new float[] { x, y });
                                readBytes = 8;
                                break;
                            }
                            case VEC3: {
                                float x = LowEndianUtil.readFloat(bufferInputStream);
                                float y = LowEndianUtil.readFloat(bufferInputStream);
                                float z = LowEndianUtil.readFloat(bufferInputStream);
                                values.add(new float[] { x, y, z });
                                readBytes = 12;
                                break;
                            }
                            case VEC4: {
                                float x = LowEndianUtil.readFloat(bufferInputStream);
                                float y = LowEndianUtil.readFloat(bufferInputStream);
                                float z = LowEndianUtil.readFloat(bufferInputStream);
                                float w = LowEndianUtil.readFloat(bufferInputStream);
                                values.add(new float[] { x, y, z, w });
                                readBytes = 16;
                                break;
                            }
                            case MAT4: {
                                for (int r = 0; r < tmpMatrix4f.length; r++) {
                                    tmpMatrix4f[r] = LowEndianUtil.readFloat(bufferInputStream);
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
                    bufferInputStream.skip(byteStride - readBytes);
                }
            }
            bufferInputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return values;
    }

    private Matrix4f loadMatrix(float[] matrix) {
        return new Matrix4f(
            matrix[0], matrix[1], matrix[2], matrix[3],
            matrix[4], matrix[5], matrix[6], matrix[7],
            matrix[8], matrix[9], matrix[10], matrix[11],
            matrix[12], matrix[13], matrix[14], matrix[15]
        );
    }

    private Material loadMaterial(MaterialModel materialModel) {
        return materialsMap.computeIfAbsent(materialModel, _ -> {
            Material material = new Material();
            material.setDefaultShaders();
            TextureModel baseColorTextureModel = null;
            float[] baseColorFactor = null;
            if (materialModel instanceof MaterialModelV2 materialModelV2) {
                baseColorTextureModel = materialModelV2.getBaseColorTexture();
                baseColorFactor = materialModelV2.getBaseColorFactor();
            } else if (materialModel instanceof MaterialModelV1 materialModelV1) {
                Object baseColorTextureValue = materialModelV1.getValues().get("baseColorTexture");
                if (baseColorTextureValue != null) {
                    int baseColorTextureIndex = (int) baseColorTextureValue;
                    baseColorTextureModel = gltfModel.getTextureModels().get(baseColorTextureIndex);
                }
                Object baseColorFactorValue = materialModelV1.getValues().get("baseColorFactor");
                if (baseColorFactorValue != null) {
                    baseColorFactor = (float[]) baseColorFactorValue;
                }
            }
            if (baseColorTextureModel != null) {
                String textureFilePath = keyDirectory + baseColorTextureModel.getImageModel().getUri();
                Texture texture = assetManager.loadTexture(textureFilePath);
                material.setTexture("diffuseMap", texture);
            }
            if (baseColorFactor != null) {
                material.getParameters().setVector4f("color", new Vector4f(baseColorFactor[0], baseColorFactor[1], baseColorFactor[2], baseColorFactor[3]));
            }
            return material;
        });
    }
}
