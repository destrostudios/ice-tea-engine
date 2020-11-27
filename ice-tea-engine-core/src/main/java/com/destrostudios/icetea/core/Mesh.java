package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.io.InputStream;
import java.nio.LongBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;
import static org.lwjgl.vulkan.VK10.vkFreeMemory;

public class Mesh {

    private Application application;
    @Getter
    @Setter
    protected Vertex[] vertices;
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

    public void loadModel(String filePath) {
        InputStream inputStream = getSystemClassLoader().getResourceAsStream(filePath);
        Model model = ObjLoader.loadModel(inputStream);

        int vertexCount = model.getPositions().size();
        vertices = new Vertex[vertexCount];
        Vector3fc color = new Vector3f(1, 1, 1);
        for (int i = 0; i < vertexCount; i++) {
            Vertex vertex = new Vertex();
            vertex.setPosition(model.getPositions().get(i));
            vertex.setColor(color);
            vertex.setTexCoords(model.getTexCoords().get(i));
            vertex.setNormal((model.getNormals() != null) ? model.getNormals().get(i) : new Vector3f());
            vertices[i] = vertex;
        }

        indices = new int[model.getIndices().size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = model.getIndices().get(i);
        }
    }

    public void generateNormals() {
        HashMap<Vertex, List<Vector3f>> triangleNormals = new HashMap<>();
        for (int i = 0; i < indices.length; i += 3) {
            Vertex v1 = vertices[indices[i]];
            Vertex v2 = vertices[indices[i + 1]];
            Vertex v3 = vertices[indices[i + 2]];

            Vector3f edge1 = v2.getPosition().sub(v1.getPosition(), new Vector3f());
            Vector3f edge2 = v3.getPosition().sub(v1.getPosition(), new Vector3f());
            Vector3f triangleNormal = edge1.cross(edge2, new Vector3f()).normalize();

            triangleNormals.computeIfAbsent(v1, v -> new LinkedList<>()).add(triangleNormal);
            triangleNormals.computeIfAbsent(v2, v -> new LinkedList<>()).add(triangleNormal);
            triangleNormals.computeIfAbsent(v3, v -> new LinkedList<>()).add(triangleNormal);
        }
        for (Map.Entry<Vertex, List<Vector3f>> entry : triangleNormals.entrySet()) {
            Vector3f normal = new Vector3f();
            for (Vector3f triangleNormal : entry.getValue()) {
                normal.add(triangleNormal);
            }
            normal.normalize();
            entry.getKey().setNormal(normal);
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
            long bufferSize = VertexDescriptions.SIZEOF * vertices.length;

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
            BufferUtil.memcpy(data.getByteBuffer(0, (int) bufferSize), vertices);
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
