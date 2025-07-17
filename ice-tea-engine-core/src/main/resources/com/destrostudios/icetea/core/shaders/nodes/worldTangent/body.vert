vec3 worldTangent;
float tangentHandedness;
#ifdef VERTEX_VERTEXTANGENT
    worldTangent = outWorldTangent = normalize(transpose(inverse(mat3(geometry.model))) * vertexTangent.xyz);
    tangentHandedness = vertexTangent.w;
#endif
// @hook worldTangent
