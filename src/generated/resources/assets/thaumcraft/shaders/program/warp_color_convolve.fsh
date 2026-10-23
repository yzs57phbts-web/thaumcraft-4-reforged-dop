#version 150
uniform sampler2D DiffuseSampler;
in vec2 texCoord;
uniform vec3 ColorScale;
uniform float Saturation;
out vec4 fragColor;
const vec3 Gray=vec3(0.3,0.59,0.11);
void main(){ vec4 c=texture(DiffuseSampler,texCoord); vec3 s=c.rgb*ColorScale; float l=dot(s,Gray); fragColor=vec4((s-l)*Saturation+l,c.a); }
