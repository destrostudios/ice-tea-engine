#ifdef VERTEX_VERTEXNORMAL
  #ifdef LIGHT_DIRECTION
    outLightVertexInfo = shaderLib_light_getVertexInfo_DirectionalLight(camera.view, geometry.model, worldPosition, vertexNormal, light.direction);
  #elif LIGHT_TRANSLATION
    outLightVertexInfo = shaderLib_light_getVertexInfo_SpotLight(camera.view, geometry.model, worldPosition, vertexNormal, light.translation);
  #endif
#endif
