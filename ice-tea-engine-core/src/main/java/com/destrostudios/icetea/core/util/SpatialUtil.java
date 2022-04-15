package com.destrostudios.icetea.core.util;

import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;

import java.util.HashMap;
import java.util.LinkedList;

public class SpatialUtil {

    // Careful - This is not covering controls
    public static Spatial bakeGeometries(Spatial spatial) {
        updateWorldTransformRecursive(spatial);
        if (spatial instanceof Node) {
            HashMap<String, LinkedList<Geometry>> groupedGeometries = new HashMap<>();
            Node node = (Node) spatial;
            node.forEachGeometry(geometry -> {
                geometry.setLocalTransform(geometry.getWorldTransform());
                String key = geometry.getShadowMode().name();
                LinkedList<Geometry> geometries = groupedGeometries.computeIfAbsent(key, k -> new LinkedList<>());
                geometries.add(geometry);
            });
            LinkedList<Spatial> groupSpatials = new LinkedList<>();
            for (LinkedList<Geometry> geometries : groupedGeometries.values()) {
                if (geometries.size() > 1) {
                    Node groupNode = new Node();
                    for (Geometry geometry : geometries) {
                        groupNode.add(geometry);
                    }
                    groupSpatials.add(groupNode);
                } else {
                    groupSpatials.add(geometries.getFirst());
                }
            }
            if (groupSpatials.size() != 1) {
                Node rootNode = new Node();
                for (Spatial groupSpatial : groupSpatials) {
                    rootNode.add(groupSpatial);
                }
                return rootNode;
            } else {
                return groupSpatials.getFirst();
            }
        } else {
            spatial.removeFromParent();
            return spatial;
        }
    }

    public static void updateWorldTransformRecursive(Spatial spatial) {
        spatial.updateWorldTransform();
        if (spatial instanceof Node) {
            Node node = (Node) spatial;
            for (Spatial child : node.getChildren()) {
                updateWorldTransformRecursive(child);
            }
        }
    }
}
