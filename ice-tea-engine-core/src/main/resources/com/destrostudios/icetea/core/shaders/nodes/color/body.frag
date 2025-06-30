#ifdef DIFFUSEMAP
    outColor = texture(diffuseMap, inVertexTexCoord);
#else
    outColor = vec4(1);
#endif

#ifdef PARAMS_COLOR
    outColor *= params.color;
#endif

// @hook color
