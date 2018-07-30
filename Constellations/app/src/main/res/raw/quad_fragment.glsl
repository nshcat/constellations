#version 310 es

precision mediump float;

in vec2 tex_coords;

uniform sampler2D quad_tex;

layout (location = 0) out vec4 fragmentColor;

void main()
{
    vec2 coords = vec2(tex_coords.x, tex_coords.y);

    fragmentColor = texture(quad_tex, coords);
}
