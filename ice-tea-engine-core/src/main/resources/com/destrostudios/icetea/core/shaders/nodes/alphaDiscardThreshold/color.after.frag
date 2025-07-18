#ifdef PARAMS_ALPHADISCARDTHRESHOLD
    if (outColor.a < params.alphaDiscardThreshold) {
        discard;
        return;
    }
#endif
