package com.destrostudios.icetea.core;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;

import java.io.File;
import java.nio.IntBuffer;

import static org.lwjgl.assimp.Assimp.aiGetErrorString;
import static org.lwjgl.assimp.Assimp.aiImportFile;

public class ModelLoader {

    public static Model loadModel(File file, int flags) {
        try (AIScene scene = aiImportFile(file.getAbsolutePath(), flags)) {
            if ((scene == null) || (scene.mRootNode() == null)) {
                throw new RuntimeException("Could not load model: " + aiGetErrorString());
            }
            Model model = new Model();
            processNode(scene.mRootNode(), scene, model);
            return model;
        }
    }

    private static void processNode(AINode node, AIScene scene, Model model) {
        if (node.mMeshes() != null) {
            processNodeMeshes(scene, node, model);
        }
        if (node.mChildren() != null) {
            PointerBuffer children = node.mChildren();
            for (int i = 0; i < node.mNumChildren(); i++) {
                processNode(AINode.create(children.get(i)), scene, model);
            }
        }
    }

    private static void processNodeMeshes(AIScene scene, AINode node, Model model) {
        PointerBuffer pMeshes = scene.mMeshes();
        IntBuffer meshIndices = node.mMeshes();
        for (int i = 0; i < meshIndices.capacity(); i++) {
            AIMesh mesh = AIMesh.create(pMeshes.get(meshIndices.get(i)));
            processMesh(mesh, model);
        }
    }

    private static void processMesh(AIMesh mesh, Model model) {
        // Positions
        AIVector3D.Buffer vertices = mesh.mVertices();
        for (int i = 0; i < vertices.capacity(); i++) {
            AIVector3D position = vertices.get(i);
            model.getPositions().add(new Vector3f(position.x(), position.y(), position.z()));
        }
        // TexCoords
        AIVector3D.Buffer aiTexCoords = mesh.mTextureCoords(0);
        for (int i = 0; i < aiTexCoords.capacity(); i++) {
            AIVector3D coords = aiTexCoords.get(i);
            model.getTexCoords().add(new Vector2f(coords.x(), coords.y()));
        }
        // Indices
        AIFace.Buffer aiFaces = mesh.mFaces();
        for (int i = 0; i < mesh.mNumFaces(); i++) {
            AIFace face = aiFaces.get(i);
            IntBuffer pIndices = face.mIndices();
            for (int j = 0; j < face.mNumIndices(); j++) {
                model.getIndices().add(pIndices.get(j));
            }
        }
        // Normals
        AIVector3D.Buffer normals = mesh.mNormals();
        if (normals != null) {
            for (int i = 0; i < normals.capacity(); i++) {
                AIVector3D normal = normals.get(i);
                model.getNormals().add(new Vector3f(normal.x(), normal.y(), normal.z()));
            }
        } else {
            model.setNormals(null);
        }
    }
}
