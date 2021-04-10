package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.collision.*;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.data.values.UniformValue;
import com.destrostudios.icetea.core.util.BufferUtil;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;

public class Mesh {

    public Mesh() {
        bounds = new BoundingBox();
    }
    private Application application;
    @Getter
    protected int topology = VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
    @Getter
    @Setter
    protected VertexData[] vertices;
    @Getter
    @Setter
    protected int[] indices;
    @Getter
    private Long vertexBuffer;
    @Getter
    private Long vertexBufferMemory;
    @Getter
    private Long indexBuffer;
    @Getter
    private Long indexBufferMemory;
    private int usingGeometriesCount;
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

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
        recreateVertexBuffer();
        recreateIndexBuffer();
    }

    private void recreateVertexBuffer() {
        cleanupVertexBuffer();

        try (MemoryStack stack = stackPush()) {
            long bufferSize = 0;
            for (VertexData vertex : vertices) {
                bufferSize += vertex.getSize();
            }

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            );

            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(application.getLogicalDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
            ByteBuffer byteBuffer = data.getByteBuffer(0, (int) bufferSize);
            int index = 0;
            for (VertexData vertex : vertices) {
                for (UniformValue<?> uniformValue : vertex.getFields().values()) {
                    uniformValue.write(byteBuffer, index);
                    index += uniformValue.getSize();
                }
            }
            vkUnmapMemory(application.getLogicalDevice(), stagingBufferMemory);

            application.getBufferManager().createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                pBuffer,
                pBufferMemory
            );

            vertexBuffer = pBuffer.get(0);
            vertexBufferMemory = pBufferMemory.get(0);

            application.getBufferManager().copyBuffer(stagingBuffer, vertexBuffer, bufferSize);
            vkDestroyBuffer(application.getLogicalDevice(), stagingBuffer, null);
            vkFreeMemory(application.getLogicalDevice(), stagingBufferMemory, null);
        }
    }

    private void cleanupVertexBuffer() {
        if (vertexBuffer != null) {
            vkDestroyBuffer(application.getLogicalDevice(), vertexBuffer, null);
            vertexBuffer = null;
        }
        if (vertexBufferMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), vertexBufferMemory, null);
            vertexBufferMemory = null;
        }
    }

    private void recreateIndexBuffer() {
        cleanupIndexBuffer();

        if (indices != null) {
            try (MemoryStack stack = stackPush()) {
                long bufferSize = Integer.BYTES * indices.length;
                LongBuffer pBuffer = stack.mallocLong(1);
                LongBuffer pBufferMemory = stack.mallocLong(1);
                application.getBufferManager().createBuffer(
                        bufferSize,
                        VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                        pBuffer,
                        pBufferMemory
                );

                long stagingBuffer = pBuffer.get(0);
                long stagingBufferMemory = pBufferMemory.get(0);

                PointerBuffer data = stack.mallocPointer(1);

                vkMapMemory(application.getLogicalDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
                BufferUtil.memcpy(data.getByteBuffer(0, (int) bufferSize), indices);
                vkUnmapMemory(application.getLogicalDevice(), stagingBufferMemory);

                application.getBufferManager().createBuffer(
                        bufferSize,
                        VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                        VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                        pBuffer,
                        pBufferMemory
                );

                indexBuffer = pBuffer.get(0);
                indexBufferMemory = pBufferMemory.get(0);

                application.getBufferManager().copyBuffer(stagingBuffer, indexBuffer, bufferSize);
                vkDestroyBuffer(application.getLogicalDevice(), stagingBuffer, null);
                vkFreeMemory(application.getLogicalDevice(), stagingBufferMemory, null);
            }
        }
    }

    private void cleanupIndexBuffer() {
        if (indexBuffer != null) {
            vkDestroyBuffer(application.getLogicalDevice(), indexBuffer, null);
            indexBuffer = null;
        }
        if (indexBufferMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), indexBufferMemory, null);
            indexBufferMemory = null;
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

    public void increaseUsingGeometriesCount() {
        usingGeometriesCount++;
    }

    public void decreaseUsingGeometriesCount() {
        usingGeometriesCount--;
    }

    public boolean isUnused() {
        return (usingGeometriesCount <= 0);
    }

    public void cleanup() {
        cleanupVertexBuffer();
        cleanupIndexBuffer();
    }
}
