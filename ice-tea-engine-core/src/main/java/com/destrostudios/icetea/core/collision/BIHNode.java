package com.destrostudios.icetea.core.collision;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BIHNode extends BIHTreeItem {
    private int axis;
    private float leftPlane;
    private float rightPlane;
    private BIHTreeItem leftChild;
    private BIHTreeItem rightChild;
}
