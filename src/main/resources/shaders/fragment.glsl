#version 330 core

out vec4 outputColor;
in vec2 UV;
uniform sampler2D tex;
in float brightness;
float fogSize = 0.005;
float fogDist = 0.998;

void main() {
  vec4 fragColor = texture2D( tex, UV );
  //light calculation
  vec4 result = mix(vec4(0,0,0,1), fragColor, clamp((brightness/16),0,0.8)+0.1);
  //face brightness
  gl_FragColor = result;
}