package com.destrostudios.icetea.core.model;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.material.descriptor.AdditionalMaterialDescriptorProvider;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorWithLayout;
import com.destrostudios.icetea.core.material.descriptor.SkeletonDescriptor;
import com.destrostudios.icetea.core.material.descriptor.SkeletonDescriptorLayout;
import com.destrostudios.icetea.core.mesh.VertexPositionModifier;
import com.destrostudios.icetea.core.scene.Control;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.LinkedList;
import java.util.List;

public class SkeletonGeometryControl extends Control implements AdditionalMaterialDescriptorProvider, VertexPositionModifier {

    public SkeletonGeometryControl(SkeletonGeometryControl skeletonGeometryControl, CloneContext context) {
        this(context.cloneByReference(skeletonGeometryControl.skeleton));
    }

    public SkeletonGeometryControl(Skeleton skeleton) {
        this.skeleton = skeleton;
        additionalMaterialDescriptors = new LinkedList<>();
        additionalMaterialDescriptors.add(new MaterialDescriptorWithLayout(new SkeletonDescriptorLayout(), new SkeletonDescriptor("skeleton", skeleton)));
    }
    @Getter
    private Skeleton skeleton;
    private LinkedList<MaterialDescriptorWithLayout> additionalMaterialDescriptors;

    @Override
    public List<MaterialDescriptorWithLayout> getAdditionalMaterialDescriptors(Geometry geometry) {
        return additionalMaterialDescriptors;
    }

    @Override
    public void modifyVertexPosition(VertexData vertex, Vector3f vertexPosition) {
        Matrix4f[] jointMatrices = skeleton.getJointMatrices();
        Vector4f jointsWeights = vertex.getVector4f("jointsWeights");
        Vector4f jointsIndices = vertex.getVector4f("jointsIndices");
        // TODO: Introduce TempVars
        Matrix4f skinMatrixJoint1 = MathUtil.mul(jointMatrices[(int) jointsIndices.x()], jointsWeights.x(), new Matrix4f());
        Matrix4f skinMatrixJoint2 = MathUtil.mul(jointMatrices[(int) jointsIndices.y()], jointsWeights.y(), new Matrix4f());
        Matrix4f skinMatrixJoint3 = MathUtil.mul(jointMatrices[(int) jointsIndices.z()], jointsWeights.z(), new Matrix4f());
        Matrix4f skinMatrixJoint4 = MathUtil.mul(jointMatrices[(int) jointsIndices.w()], jointsWeights.w(), new Matrix4f());
        Matrix4f skinMatrix = skinMatrixJoint1.add(skinMatrixJoint2, new Matrix4f()).add(skinMatrixJoint3).add(skinMatrixJoint4);
        MathUtil.mulPosition(vertexPosition, skinMatrix);
    }

    @Override
    public Control clone(CloneContext context) {
        return new SkeletonGeometryControl(this, context);
    }
}
