#ifdef SKELETON_JOINTMATRICES
    mat4 skinMatrix = (jointsWeights.x * skeleton.jointMatrices[int(jointsIndices.x)])
                    + (jointsWeights.y * skeleton.jointMatrices[int(jointsIndices.y)])
                    + (jointsWeights.z * skeleton.jointMatrices[int(jointsIndices.z)])
                    + (jointsWeights.w * skeleton.jointMatrices[int(jointsIndices.w)]);
    modelPosition = skinMatrix * modelPosition;
#endif
