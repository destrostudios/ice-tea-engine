#ifdef CAMERA_CLIPPLANE
  if (camera.clipPlane.length() > 0) {
    gl_ClipDistance[0] = dot(worldPosition, camera.clipPlane);
  }
#endif
