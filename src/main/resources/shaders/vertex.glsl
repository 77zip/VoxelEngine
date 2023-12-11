#version 330 core

layout(location = 0) in vec3 point;    // Vertex attribute
layout(location = 1) in vec2 vertexUV;          // UV attribute
layout(location = 2) in float lightLevel;  // Brightness attribute

out float brightness;
uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;
out vec2 UV;

void main() {
  gl_Position = (projection * view * model) * vec4(point, 1.0);
  UV = vertexUV;
  brightness = lightLevel;
}