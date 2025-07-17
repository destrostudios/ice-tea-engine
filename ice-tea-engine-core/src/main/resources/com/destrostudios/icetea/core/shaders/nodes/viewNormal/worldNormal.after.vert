vec3 viewNormal = normalize(mat3(transpose(inverse(camera.view))) * worldNormal);
// @hook viewNormal
