#ifdef RENDERJOB_SHADOWMAP
    gl_Position = shadowInfo.viewProjectionMatrices[constants.cascadeIndex] * worldPosition;
#endif
