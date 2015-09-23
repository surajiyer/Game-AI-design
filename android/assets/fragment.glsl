#ifdef GL_ES 
precision mediump float;
#endif

uniform sampler2D u_texture;

varying vec3 v_position;
varying vec2 v_texCoords;

void main() {
    //gl_FragColor = vec4(v_position.yyy, 1.0);
	gl_FragColor = texture2D(u_texture, v_texCoords);
}