vec3 worldNormal;
#ifdef VERTEX_VERTEXNORMAL
    worldNormal = normalize(transpose(inverse(mat3(geometry.model))) * vertexNormal);
#endif
// @hook worldNormal
