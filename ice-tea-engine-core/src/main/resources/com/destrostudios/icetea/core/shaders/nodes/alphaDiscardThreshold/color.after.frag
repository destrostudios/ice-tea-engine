#ifdef PARAMS_ALPHADISCARDTHRESHHOLD
    if (outColor.a < params.alphaDiscardThreshold) {
        discard;
        return;
    }
}
#endif
