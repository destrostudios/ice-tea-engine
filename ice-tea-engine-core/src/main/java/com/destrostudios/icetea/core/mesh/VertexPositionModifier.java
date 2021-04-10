package com.destrostudios.icetea.core.mesh;

import com.destrostudios.icetea.core.data.VertexData;
import org.joml.Vector3f;

public interface VertexPositionModifier {

    void modifyVertexPosition(VertexData vertex, Vector3f vertexPosition);
}
