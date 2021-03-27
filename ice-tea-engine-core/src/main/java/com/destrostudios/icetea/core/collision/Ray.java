package com.destrostudios.icetea.core.collision;

import com.destrostudios.icetea.core.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Vector3f;

@AllArgsConstructor
@Getter
public class Ray {

    private Vector3f origin;
    private Vector3f direction;

    public Float intersectsTriangle(Vector3f v1, Vector3f v2, Vector3f v3) {
        float edge1X = (v2.x() - v1.x());
        float edge1Y = (v2.y() - v1.y());
        float edge1Z = (v2.z() - v1.z());

        float edge2X = (v3.x() - v1.x());
        float edge2Y = (v3.y() - v1.y());
        float edge2Z = (v3.z() - v1.z());

        float normX = ((edge1Y * edge2Z) - (edge1Z * edge2Y));
        float normY = ((edge1Z * edge2X) - (edge1X * edge2Z));
        float normZ = ((edge1X * edge2Y) - (edge1Y * edge2X));

        float dirDotNorm = ((direction.x() * normX) + (direction.y() * normY) + (direction.z() * normZ));

        float diffX = (origin.x() - v1.x());
        float diffY = (origin.y() - v1.y());
        float diffZ = (origin.z() - v1.z());

        float sign;
        if (dirDotNorm > MathUtil.EPSILON_FLOAT) {
            sign = 1;
        } else if (dirDotNorm < (-1 * MathUtil.EPSILON_FLOAT)) {
            sign = -1;
            dirDotNorm = -dirDotNorm;
        } else {
            // Ray and triangle are parallel
            return null;
        }

        float diffEdge2X = ((diffY * edge2Z) - (diffZ * edge2Y));
        float diffEdge2Y = ((diffZ * edge2X) - (diffX * edge2Z));
        float diffEdge2Z = ((diffX * edge2Y) - (diffY * edge2X));

        float dirDotDiffxEdge2 = (sign * ((direction.x() * diffEdge2X) + (direction.y() * diffEdge2Y) + (direction.z() * diffEdge2Z)));
        if (dirDotDiffxEdge2 >= 0) {

            diffEdge2X = ((edge1Y * diffZ) - (edge1Z * diffY));
            diffEdge2Y = ((edge1Z * diffX) - (edge1X * diffZ));
            diffEdge2Z = ((edge1X * diffY) - (edge1Y * diffX));

            float dirDotEdge1xDiff = (sign * ((direction.x() * diffEdge2X) + (direction.y() * diffEdge2Y) + (direction.z() * diffEdge2Z)));
            if (dirDotEdge1xDiff >= 0) {
                if ((dirDotDiffxEdge2 + dirDotEdge1xDiff) <= dirDotNorm) {
                    float diffDotNorm = ((-1 * sign) * ((diffX * normX) + (diffY * normY) + (diffZ * normZ)));
                    if (diffDotNorm >= 0) {
                        // Ray intersects triangle
                        return (diffDotNorm / dirDotNorm);
                    }
                }
            }
        }

        return null;
    }
}
