#ifdef RENDERJOB_SHADOWMAP
    layout (push_constant) uniform pushConstants {
        int cascadeIndex;
    } constants;
#endif
