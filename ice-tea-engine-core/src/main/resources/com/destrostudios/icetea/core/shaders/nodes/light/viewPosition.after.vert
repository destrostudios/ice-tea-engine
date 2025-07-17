#ifdef LIGHT
    LightInfo lightInfo = shaderLib_light_getLightInfo();
    outViewLightDirection = shaderLib_light_getViewLightDirection(lightInfo, camera.view, viewPosition);
#endif
