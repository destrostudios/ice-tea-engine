const int LIGHT_TYPE_POINT = 0;
const int LIGHT_TYPE_DIRECTIONAL = 1;

struct LightInfo {
    int type;
    vec3 lightColor;
    vec3 phongAmbientColor;
    vec3 phongSpecularColor;
    vec3 position;
    vec3 direction;
};

LightInfo shaderLib_light_getLightInfo() {
    int type;
    vec3 lightColor;
    vec3 phongAmbientColor;
    vec3 phongSpecularColor;
    vec3 position;
    vec3 direction;
    #ifdef LIGHT
        lightColor = light.lightColor;
        phongAmbientColor = light.phongAmbientColor;
        phongSpecularColor = light.phongSpecularColor;
        #ifdef LIGHT_TRANSLATION
            type = LIGHT_TYPE_POINT;
            position = light.position;
        #elif LIGHT_DIRECTION
            type = LIGHT_TYPE_DIRECTIONAL;
            direction = light.direction;
        #endif
    #endif
    return LightInfo(type, lightColor, phongAmbientColor, phongSpecularColor, position, direction);
}
