#version 300 es

precision highp float;

in vec4 tex;
out vec4 fragmentColor;

uniform struct {
	sampler2D colorTexture;
	vec4 tint;
} material;


void main(void) {
  fragmentColor = texture(material.colorTexture, tex.xy) * material.tint;
}
