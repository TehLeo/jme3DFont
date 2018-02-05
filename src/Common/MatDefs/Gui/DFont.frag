#import "Common/ShaderLib/GLSLCompat.glsllib"

#if defined(HAS_GLOWMAP) || defined(HAS_COLORMAP) || (defined(HAS_LIGHTMAP) && !defined(SEPARATE_TEXCOORD))
    #define NEED_TEXCOORD1
#endif

#if defined(DISCARD_ALPHA)
    uniform float m_AlphaDiscardThreshold;
#endif

#if defined(TEXT_OUTLINE)
	uniform vec4 m_TextOutline;
#endif

uniform float m_Middle;
uniform float m_DScale;


uniform vec4 m_Color;
uniform sampler2D m_ColorMap;
//uniform sampler2D m_LightMap;

varying vec2 texCoord1;
//varying vec2 texCoord2;

varying vec4 vertColor;

void main(){
    vec4 color = vec4(1.0);

    //#ifdef HAS_COLORMAP
    color *= texture2D(m_ColorMap, texCoord1); 

	//const float SCALE = 1.0/16.0;
	
	float SCALE = m_DScale;
	//float SCALE = 0.25/(4.0*0.5); 
	
	#ifdef TEXT_OUTLINE
		//Outline
		color.rgb = mix(m_TextOutline.rgb, color.rgb, smoothstep(m_Middle - SCALE, m_Middle + SCALE, color.a));
		color.a = smoothstep(m_TextOutline.a - SCALE, m_TextOutline.a + SCALE, color.a);
	#else
		//Plain Text
		color.a = smoothstep(m_Middle - SCALE, m_Middle + SCALE, color.a);
    #endif

    #ifdef HAS_VERTEXCOLOR
        color *= vertColor;
    #endif

    #ifdef HAS_COLOR
        color *= m_Color;
    #endif

   /* #ifdef HAS_LIGHTMAP
        #ifdef SEPARATE_TEXCOORD
            color.rgb *= texture2D(m_LightMap, texCoord2).rgb;
        #else
            color.rgb *= texture2D(m_LightMap, texCoord1).rgb;
        #endif
    #endif

    #if defined(DISCARD_ALPHA)
        if(color.a < m_AlphaDiscardThreshold){
           discard;
        }
    #endif*/
	//color.rgb = vec3(1.0);
	

    gl_FragColor = color;
}