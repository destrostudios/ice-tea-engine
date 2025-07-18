package com.destrostudios.icetea.core.util;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SpatialUtil {

    // Careful - This is not covering controls
    public static Spatial flattenNodes(Spatial spatial) {
        if (spatial instanceof Node node) {
            updateTransformRecursive(node);
            ArrayList<Geometry> geometries = new ArrayList<>();
            node.forEachGeometry(geometry -> {
                applyInheritedProperties(geometry);
                geometries.add(geometry);
            });
            if (geometries.size() != 1) {
                Node rootNode = new Node();
                rootNode.addAll(geometries);
                return rootNode;
            }
            return geometries.getFirst();
        }
        return spatial;
    }

    // Careful - This is not covering controls
    public static Spatial batchGeometries(Spatial spatial) {
        if (spatial instanceof Node node) {
            updateTransformRecursive(node);
            HashMap<Material, ArrayList<Geometry>> geometriesByMaterial = new HashMap<>();
            node.forEachGeometry(geometry -> {
                applyInheritedProperties(geometry);
                geometriesByMaterial.computeIfAbsent(geometry.getMaterial(), _ -> new ArrayList<>()).add(geometry);
            });
            if (geometriesByMaterial.size() > 1) {
                Node rootNode = new Node();
                geometriesByMaterial.forEach((material, geometries) -> {
                    Geometry batchedGeometry = batchGeometries(geometries, material);
                    rootNode.add(batchedGeometry);
                });
                return rootNode;
            } else if (geometriesByMaterial.size() > 0) {
                Map.Entry<Material, ArrayList<Geometry>> onlyEntry = geometriesByMaterial.entrySet().iterator().next();
                return batchGeometries(onlyEntry.getValue(), onlyEntry.getKey());
            }
        }
        return spatial;
    }

    private static Geometry batchGeometries(ArrayList<Geometry> geometries, Material material) {
        ArrayList<VertexData> vertices = new ArrayList<>();
        ArrayList<Integer> indices = new ArrayList<>();
        int indexOffset = 0;
        for (Geometry geometry : geometries) {
            Mesh mesh = geometry.getMesh();
            for (VertexData vertex : mesh.getVertices()) {
                VertexData batchedVertex = vertex.clone(CloneContext.reuseAll());
                Vector3f vertexPosition = batchedVertex.getVector3f("vertexPosition");
                if (vertexPosition != null) {
                    // Transform from model space to world space
                    MathUtil.mulPosition(vertexPosition, geometry.getWorldTransform().getMatrix());
                }
                vertices.add(batchedVertex);
            }
            for (int index : mesh.getIndices()) {
                indices.add(indexOffset + index);
            }
            indexOffset += mesh.getVertices().length;
        }
        Mesh mesh = new Mesh();
        mesh.setVertices(vertices.toArray(VertexData[]::new));
        mesh.setIndices(ListUtil.toArray(indices));
        mesh.updateBounds();

        Geometry geometry = new Geometry();
        geometry.setMesh(mesh);
        geometry.setMaterial(material);
        return geometry;
    }

    public static void updateTransformRecursive(Spatial spatial) {
        spatial.updateTransform();
        if (spatial instanceof Node node) {
            for (Spatial child : node.getChildren()) {
                updateTransformRecursive(child);
            }
        }
    }

    private static void applyInheritedProperties(Spatial spatial) {
        // Requires world transform to be up-to-date
        spatial.setLocalTransform(spatial.getWorldTransform());
        spatial.setShadowMode(spatial.getEffectiveShadowMode());
    }
}
