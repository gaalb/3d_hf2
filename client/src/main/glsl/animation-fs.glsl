#version 300 es
precision highp float;

in vec4 tex;
out vec4 fragmentColor;

uniform struct {
  sampler2D colorTexture;
  vec4  tint;
  float frame;
  float cols;
  float rows;
} material;

void main() {
  float col = mod(material.frame, material.cols);
  float row = floor(material.frame / material.cols);
  vec2 cellSize = vec2(1.0 / material.cols, 1.0 / material.rows);
  vec2 uv = tex.xy * cellSize + vec2(col, row) * cellSize;
  fragmentColor = texture(material.colorTexture, uv) * material.tint;
}
