package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.buffer.StagedResizableMemoryBuffer;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.collision.*;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.data.values.DataValue;
import com.destrostudios.icetea.core.object.MultiConsumableNativeObject;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

public class Mesh extends MultiConsumableNativeObject<Geometry> implements ContextCloneable {

    public Mesh() {
        bounds = new BoundingBox();
    }

    public Mesh(Mesh mesh, CloneContext context) {
        topology = mesh.topology;
        vertices = new VertexData[mesh.vertices.length];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = mesh.vertices[i].clone(context);
        }
        indices = new int[mesh.indices.length];
        System.arraycopy(mesh.indices, 0, indices, 0, indices.length);
        bounds = mesh.bounds.clone(context);
        // TODO: CollisionTree cloning, for now it would be recalculated
    }
    @Getter
    protected int topology = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
    @Getter
    @Setter
    protected VertexData[] vertices;
    @Getter
    @Setter
    protected int[] indices;
    @Getter
    private StagedResizableMemoryBuffer vertexBuffer;
    @Getter
    private StagedResizableMemoryBuffer indexBuffer;
    private boolean buffersOutdated;
    @Getter
    private boolean wereBuffersOutdated;
    @Getter
    private BoundingBox bounds;
    private BIHTree collisionTree;

    public void updateBounds() {
        // TODO: Introduce TempVars
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        for (VertexData vertex : vertices) {
            MathUtil.updateMinMax(min, max, vertex.getVector3f("vertexPosition"));
        }
        bounds.setMinMax(min, max);
    }

    public void generateNormals() {
        HashMap<VertexData, List<Vector3f>> triangleNormals = new HashMap<>();
        for (int i = 0; i < indices.length; i += 3) {
            VertexData v1 = vertices[indices[i]];
            VertexData v2 = vertices[indices[i + 1]];
            VertexData v3 = vertices[indices[i + 2]];

            // TODO: Introduce TempVars
            Vector3f edge1 = v2.getVector3f("vertexPosition").sub(v1.getVector3f("vertexPosition"), new Vector3f());
            Vector3f edge2 = v3.getVector3f("vertexPosition").sub(v1.getVector3f("vertexPosition"), new Vector3f());
            Vector3f triangleNormal = edge1.cross(edge2, new Vector3f()).normalize();

            triangleNormals.computeIfAbsent(v1, v -> new LinkedList<>()).add(triangleNormal);
            triangleNormals.computeIfAbsent(v2, v -> new LinkedList<>()).add(triangleNormal);
            triangleNormals.computeIfAbsent(v3, v -> new LinkedList<>()).add(triangleNormal);
        }
        for (Map.Entry<VertexData, List<Vector3f>> entry : triangleNormals.entrySet()) {
            Vector3f normal = new Vector3f();
            for (Vector3f triangleNormal : entry.getValue()) {
                normal.add(triangleNormal);
            }
            normal.normalize();
            entry.getKey().setVector3f("vertexNormal", normal);
        }
    }

    @Override
    protected void initNative() {
        super.initNative();
        setBuffersOutdated();
    }

    protected void setBuffersOutdated() {
        buffersOutdated = true;
    }

    @Override
    protected void updateNative() {
        super.updateNative();
        if (buffersOutdated) {
            if (vertexBuffer == null) {
                vertexBuffer = new StagedResizableMemoryBuffer(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
            }
            if ((indices != null) && (indexBuffer == null)) {
                indexBuffer = new StagedResizableMemoryBuffer(VK_BUFFER_USAGE_INDEX_BUFFER_BIT);
            }
        }
        updateVertexBufferNative();
        updateIndexBufferNative();
        wereBuffersOutdated = buffersOutdated;
        buffersOutdated = false;
    }

    private void updateVertexBufferNative() {
        if (vertexBuffer != null) {
            vertexBuffer.updateNative(application);
            if (buffersOutdated) {
                long bufferSize = 0;
                for (VertexData vertex : vertices) {
                    bufferSize += vertex.getSize();
                }
                vertexBuffer.write(bufferSize, byteBuffer -> {
                    int index = 0;
                    for (VertexData vertex : vertices) {
                        for (DataValue<?> dataValue : vertex.getFields().values()) {
                            vertex.write(byteBuffer, index, dataValue);
                            index += vertex.getSize(dataValue);
                        }
                    }
                });
            }
        }
    }

    private void updateIndexBufferNative() {
        if (indexBuffer != null) {
            indexBuffer.updateNative(application);
            if (buffersOutdated) {
                long bufferSize = ((long) Integer.BYTES) * indices.length;
                indexBuffer.write(bufferSize, byteBuffer -> {
                    BufferUtil.memcpy(byteBuffer, indices);
                });
            }
        }
    }

    public int collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults) {
        // TODO: Reset collision tree when mesh changed - For now, we just assume the mesh never changes after creating a tree
        if (collisionTree == null) {
            loadCollisionTree();
        }
        if (collisionTree != null) {
            return collisionTree.collide(ray, worldMatrix, worldBoundsTMin, worldBoundsTMax, collisionResults);
        }
        return 0;
    }

    public void loadCollisionTree() {
        // Only triangle collisions are supported currently
        if (topology == VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST) {
            collisionTree = new BIHTree(this);
        } else {
            collisionTree = null;
        }
    }

    public int collideDynamic(Ray ray, List<VertexPositionModifier> vertexPositionModifiers, Matrix4f worldMatrix, ArrayList<CollisionResult> collisionResults) {
        // Only triangle collisions are supported currently
        if (topology == VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST) {
            int collisions = 0;
            // TODO: Introduce TempVars
            Vector3f vertexPosition1 = new Vector3f();
            Vector3f vertexPosition2 = new Vector3f();
            Vector3f vertexPosition3 = new Vector3f();
            for (int i = 0; i < indices.length; i += 3) {
                VertexData vertex1 = vertices[indices[i]];
                VertexData vertex2 = vertices[indices[i + 1]];
                VertexData vertex3 = vertices[indices[i + 2]];
                vertexPosition1.set(vertex1.getVector3f("vertexPosition"));
                vertexPosition2.set(vertex2.getVector3f("vertexPosition"));
                vertexPosition3.set(vertex3.getVector3f("vertexPosition"));
                // TODO: These can be cached during the calculation to make it faster, but one has to care about the memory and gc - Maybe something with TempVars
                for (VertexPositionModifier vertexPositionModifier : vertexPositionModifiers) {
                    vertexPositionModifier.modifyVertexPosition(vertex1, vertexPosition1);
                    vertexPositionModifier.modifyVertexPosition(vertex2, vertexPosition2);
                    vertexPositionModifier.modifyVertexPosition(vertex3, vertexPosition3);
                }
                MathUtil.mulPosition(vertexPosition1, worldMatrix);
                MathUtil.mulPosition(vertexPosition2, worldMatrix);
                MathUtil.mulPosition(vertexPosition3, worldMatrix);
                CollisionResult collisionResult = ray.collideWithTriangle(vertexPosition1, vertexPosition2, vertexPosition3);
                if (collisionResult != null) {
                    collisionResult.setTriangleIndex(i);
                    collisionResults.add(collisionResult);
                    collisions++;
                }
            }
            return collisions;
        }
        return 0;
    }

    @Override
    protected void cleanupNativeInternal() {
        if (vertexBuffer != null) {
            vertexBuffer.cleanupNative();
            vertexBuffer = null;
        }
        if (indexBuffer != null) {
            indexBuffer.cleanupNative();
            indexBuffer = null;
        }
        super.cleanupNativeInternal();
    }

    @Override
    public Mesh clone(CloneContext context) {
        return new Mesh(this, context);
    }
}
