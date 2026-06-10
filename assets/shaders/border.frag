#ifdef GL_ES
precision mediump float;
#endif

uniform sampler2D u_texture;

uniform vec2 u_pixelSize;
uniform vec4 u_borderColor;

varying vec4 v_color;
varying vec2 v_texCoords;

void main() {
    vec4 center = texture2D(u_texture, v_texCoords);

    if (center.a > 0.0) {
        gl_FragColor = center * v_color;
        return;
    }

    float alpha = 0.0;

    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2( u_pixelSize.x, 0.0)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2(-u_pixelSize.x, 0.0)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2(0.0,  u_pixelSize.y)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2(0.0, -u_pixelSize.y)).a);

    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2( u_pixelSize.x,  u_pixelSize.y)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2(-u_pixelSize.x,  u_pixelSize.y)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2( u_pixelSize.x, -u_pixelSize.y)).a);
    alpha = max(alpha, texture2D(u_texture, v_texCoords + vec2(-u_pixelSize.x, -u_pixelSize.y)).a);

    gl_FragColor = vec4(u_borderColor.rgb, alpha > 0 ? u_borderColor.a : 0.0);
}
