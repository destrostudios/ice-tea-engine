package com.destrostudios.icetea.core.collision;

import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.util.MathUtil;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BIHTree {

    public BIHTree(Mesh mesh) {
        maxTrisPerNode = 21;
        maxDepth = 100;
        tmpTrianglePositions = new float[9];
        loadMesh(mesh);
    }
    private int maxTrisPerNode;
    private int maxDepth;
    private int[] trianglesIndices;
    private float[] trianglesPositions;
    private float[] tmpTrianglePositions;
    private BIHTreeItem root;

    private void loadMesh(Mesh mesh) {
        // Indices
        int indicesCount = mesh.getIndices().length;
        int trianglesCount = (indicesCount / 3);
        int positionsCount = (indicesCount * 3);
        trianglesIndices = new int[trianglesCount];
        for (int i = 0; i < trianglesCount; i++) {
            trianglesIndices[i] = i;
        }
        // Positions
        trianglesPositions = new float[positionsCount];
        int positionIndex = 0;
        for (int i = 0; i < indicesCount; i += 3) {
            int index = mesh.getIndices()[i];
            Vector3f position = mesh.getVertices()[index].getVector3f("modelSpaceVertexPosition");
            trianglesPositions[positionIndex++] = position.x();
            trianglesPositions[positionIndex++] = position.y();
            trianglesPositions[positionIndex++] = position.z();

            index = mesh.getIndices()[i + 1];
            position = mesh.getVertices()[index].getVector3f("modelSpaceVertexPosition");
            trianglesPositions[positionIndex++] = position.x();
            trianglesPositions[positionIndex++] = position.y();
            trianglesPositions[positionIndex++] = position.z();

            index = mesh.getIndices()[i + 2];
            position = mesh.getVertices()[index].getVector3f("modelSpaceVertexPosition");
            trianglesPositions[positionIndex++] = position.x();
            trianglesPositions[positionIndex++] = position.y();
            trianglesPositions[positionIndex++] = position.z();
        }

        BoundingBox sceneBoundingBox = createBox(0, trianglesCount - 1);
        root = createTreeItem(0, trianglesCount - 1, sceneBoundingBox, 0);
    }

    private BIHTreeItem createTreeItem(int leftTriangleIndex, int rightTriangleIndex, BoundingBox sceneGridBoundingBox, int depth) {
        if (((rightTriangleIndex - leftTriangleIndex) < maxTrisPerNode) || (depth > maxDepth)) {
            return new BIHLeaf(leftTriangleIndex, rightTriangleIndex);
        }

        int axis;
        if (sceneGridBoundingBox.getExtent().x() > sceneGridBoundingBox.getExtent().y()) {
            if (sceneGridBoundingBox.getExtent().x() > sceneGridBoundingBox.getExtent().z()) {
                axis = 0;
            } else {
                axis = 2;
            }
        } else {
            if (sceneGridBoundingBox.getExtent().y() > sceneGridBoundingBox.getExtent().z()) {
                axis = 1;
            } else {
                axis = 2;
            }
        }

        float split = sceneGridBoundingBox.getCenter().get(axis);
        int pivot = sortTriangles(leftTriangleIndex, rightTriangleIndex, split, axis);

        if (pivot < leftTriangleIndex) {
            BoundingBox rightSceneGridBoundingBox = new BoundingBox(sceneGridBoundingBox);
            rightSceneGridBoundingBox.setMin(axis, split);
            return createTreeItem(leftTriangleIndex, rightTriangleIndex, rightSceneGridBoundingBox, depth + 1);
        } else if (pivot > rightTriangleIndex) {
            BoundingBox leftSceneGridBoundingBox = new BoundingBox(sceneGridBoundingBox);
            leftSceneGridBoundingBox.setMax(axis, split);
            return createTreeItem(leftTriangleIndex, rightTriangleIndex, leftSceneGridBoundingBox, depth + 1);
        } else {
            // Left child
            BoundingBox leftBoundingBox = createBox(leftTriangleIndex, Math.max(leftTriangleIndex, pivot - 1));
            float leftPlane = leftBoundingBox.getMax().get(axis);
            BoundingBox leftSceneGridBoundingBox = new BoundingBox(sceneGridBoundingBox);
            leftSceneGridBoundingBox.setMax(axis, split);
            BIHTreeItem leftChild = createTreeItem(leftTriangleIndex, Math.max(leftTriangleIndex, pivot - 1), leftSceneGridBoundingBox, depth + 1);

            // Right child
            BoundingBox rightBoundingBox = createBox(pivot, rightTriangleIndex);
            float rightPlane = rightBoundingBox.getMin().get(axis);
            BoundingBox rightSceneGridBoundingBox = new BoundingBox(sceneGridBoundingBox);
            rightSceneGridBoundingBox.setMin(axis, split);
            BIHTreeItem rightChild = createTreeItem(pivot, rightTriangleIndex, rightSceneGridBoundingBox, depth + 1);

            return new BIHNode(axis, leftPlane, rightPlane, leftChild, rightChild);
        }
    }

    private BoundingBox createBox(int leftTriangleIndex, int rightTriangleIndex) {
        // TODO: Introduce TempVars
        Vector3f min = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        Vector3f max = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
        Vector3f vertex1 = new Vector3f();
        Vector3f vertex2 = new Vector3f();
        Vector3f vertex3 = new Vector3f();

        for (int i = leftTriangleIndex; i <= rightTriangleIndex; i++) {
            getTriangle(i, vertex1, vertex2, vertex3);
            MathUtil.updateMinMax(min, max, vertex1);
            MathUtil.updateMinMax(min, max, vertex2);
            MathUtil.updateMinMax(min, max, vertex3);
        }

        return new BoundingBox(min, max);
    }

    private int sortTriangles(int leftTriangleIndex, int redTriangleIndex, float split, int axis) {
        int pivot = leftTriangleIndex;
        int uncheckedMostLeft = redTriangleIndex;

        // TODO: Introduce TempVars
        Vector3f vertex1 = new Vector3f();
        Vector3f vertex2 = new Vector3f();
        Vector3f vertex3 = new Vector3f();
        Vector3f triangleCenter = new Vector3f();

        while (pivot <= uncheckedMostLeft) {
            getTriangle(pivot, vertex1, vertex2, vertex3);
            vertex1.add(vertex2, triangleCenter).add(vertex3).mul(1 / 3f);
            if (triangleCenter.get(axis) > split) {
                swapTriangles(pivot, uncheckedMostLeft);
                --uncheckedMostLeft;
            } else {
                ++pivot;
            }
        }

        // If all triangles are to the right side of the split, pivot will still be leftTriangleIndex here, so we have to subtract 1
        if (pivot == leftTriangleIndex) {
            pivot--;
        }

        return pivot;
    }

    private void getTriangle(int triangleIndex, Vector3f vertex1, Vector3f vertex2, Vector3f vertex3) {
        int vertexIndex = (triangleIndex * 9);
        vertex1.set(trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex++]);
        vertex2.set(trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex++]);
        vertex3.set(trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex++], trianglesPositions[vertexIndex]);
    }

    private void swapTriangles(int triangleIndex1, int triangleIndex2) {
        int vertexIndex1 = (triangleIndex1 * 9);
        int vertexIndex2 = (triangleIndex2 * 9);
        System.arraycopy(trianglesPositions, vertexIndex1, tmpTrianglePositions, 0, 9);
        System.arraycopy(trianglesPositions, vertexIndex2, trianglesPositions, vertexIndex1, 9);
        System.arraycopy(tmpTrianglePositions, 0, trianglesPositions, vertexIndex2, 9);

        int tmpIndex = trianglesIndices[triangleIndex1];
        trianglesIndices[triangleIndex1] = trianglesIndices[triangleIndex2];
        trianglesIndices[triangleIndex2] = tmpIndex;
    }

    public int collide(Ray worldRay, Matrix4f worldMatrix, BoundingBox worldBounds, List<CollisionResult> collisionResults) {
        CollisionResult_AABB_Ray worldBoundCollision = worldBounds.collide(worldRay);
        if (worldBoundCollision != null) {
            float tMin = worldBoundCollision.getTMin();
            float tMax = worldBoundCollision.getTMax();
            return collide(worldRay, worldMatrix, tMin, tMax, collisionResults);
        }
        return 0;
    }

    private int collide(Ray worldRay, Matrix4f worldMatrix, float sceneMin, float sceneMax, List<CollisionResult> collisionResults) {
        // TODO: Introduce TempVars
        Matrix4f inverseWorldMatrix = worldMatrix.invert(new Matrix4f());
        Vector3f modelRayOrigin = MathUtil.mul(worldRay.getOrigin(), inverseWorldMatrix, new Vector3f());
        Vector3f modelRayDirection = MathUtil.mul(worldRay.getDirection(), inverseWorldMatrix, new Vector3f());
        float[] modelRayOriginAxes = new float[] { modelRayOrigin.x(), modelRayOrigin.y(), modelRayOrigin.z() };
        float[] modelRayDirectionAxesInverse = new float[] { 1 / modelRayDirection.x(), 1 / modelRayDirection.y(), 1 / modelRayDirection.z()};

        // TODO: Introduce TempVars
        ArrayList<BIHStackData> stack = new ArrayList<>();
        Vector3f vertex1 = new Vector3f();
        Vector3f vertex2 = new Vector3f();
        Vector3f vertex3 = new Vector3f();

        int collisions = 0;

        stack.add(new BIHStackData(root, sceneMin, sceneMax));
        stackLoop:
        while (stack.size() > 0) {
            BIHStackData stackData = stack.remove(stack.size() - 1);

            float tMin = stackData.getTMin();
            float tMax = stackData.getTMax();
            if (tMax < tMin) {
                continue;
            }

            BIHTreeItem treeItem = stackData.getTreeItem();
            while (treeItem instanceof BIHNode) {
                BIHNode node = (BIHNode) treeItem;

                float rayOriginValue = modelRayOriginAxes[node.getAxis()];
                float rayDirectionValueInverse = modelRayDirectionAxesInverse[node.getAxis()];

                float tNearSplit = (node.getLeftPlane() - rayOriginValue) * rayDirectionValueInverse;
                float tFarSplit = (node.getRightPlane() - rayOriginValue) * rayDirectionValueInverse;
                BIHTreeItem nearChild = node.getLeftChild();
                BIHTreeItem farChild = node.getRightChild();

                if (rayDirectionValueInverse < 0) {
                    float tmpSplit = tNearSplit;
                    tNearSplit = tFarSplit;
                    tFarSplit = tmpSplit;

                    BIHTreeItem tmpNode = nearChild;
                    nearChild = farChild;
                    farChild = tmpNode;
                }

                if ((tMin > tNearSplit) && (tMax < tFarSplit)) {
                    continue stackLoop;
                }

                if (tMin > tNearSplit) {
                    tMin = Math.max(tMin, tFarSplit);
                    treeItem = farChild;
                } else if (tMax < tFarSplit) {
                    tMax = Math.min(tMax, tNearSplit);
                    treeItem = nearChild;
                } else {
                    stack.add(new BIHStackData(farChild, Math.max(tMin, tFarSplit), tMax));
                    tMax = Math.min(tMax, tNearSplit);
                    treeItem = nearChild;
                }
            }

            if (treeItem instanceof BIHLeaf) {
                BIHLeaf leaf = (BIHLeaf) treeItem;
                for (int i = leaf.getLeftTriangleIndex(); i <= leaf.getRightTriangleIndex(); i++) {
                    getTriangle(i, vertex1, vertex2, vertex3);
                    MathUtil.mul(vertex1, worldMatrix);
                    MathUtil.mul(vertex2, worldMatrix);
                    MathUtil.mul(vertex3, worldMatrix);
                    Float tWorld = worldRay.intersectsTriangle(vertex1, vertex2, vertex3);
                    if (tWorld != null) {
                        Vector3f position = worldRay.getDirection().mul(tWorld, new Vector3f()).add(worldRay.getOrigin());
                        Vector3f normal = MathUtil.getTriangleNormal(vertex1, vertex2, vertex3);
                        float worldDistance = worldRay.getOrigin().distance(position);
                        collisionResults.add(new CollisionResult(position, normal, worldDistance, trianglesIndices[i]));
                        collisions++;
                    }
                }
            }
        }

        return collisions;
    }
}
