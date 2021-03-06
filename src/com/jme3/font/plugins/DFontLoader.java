/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.font.plugins;

import com.jme3.asset.*;
import com.jme3.font.BitmapCharacter;
import com.jme3.font.BitmapCharacterSet;
import com.jme3.font.BitmapFont;
import com.jme3.font.DFont;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.Vector4f;
import com.jme3.texture.Texture;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DFontLoader implements AssetLoader {
    private static final Logger logger = Logger.getLogger(DFontLoader.class.getName());

    private DFont load(AssetManager assetManager, String folder, InputStream in, AssetKey k) throws IOException{
		MaterialDef spriteMat = 
                (MaterialDef) assetManager.loadAsset(new AssetKey("Common/MatDefs/Gui/DFont.j3md"));
		Vector4f outline = null;
		float dscale = 1/16f;
		float middle = 0.5f;
		float spread = 4f;
		Texture.MinFilter minFilter = Texture.MinFilter.BilinearNearestMipMap;
		Texture.MagFilter magFilter = Texture.MagFilter.Bilinear;
		if(k instanceof DFontKey) {
			DFontKey kk = (DFontKey)k;
			outline = kk.outline;
			dscale = kk.dscale;
			minFilter = kk.minFilter;
			magFilter = kk.magFilter;
			middle = kk.middle;
			spread = kk.spread;
		}
		
		BitmapCharacterSet charSet = new BitmapCharacterSet();
		Material[] matPages = null;
		DFont font = new DFont();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String regex = "[\\s=]+";      
        font.setCharSet(charSet);
        String line;
		
		int pUp=0, pRight=0, pDown=0, pLeft=0;
		
        while ((line = reader.readLine())!=null){                        
            String[] tokens = line.split(regex);
            if (tokens[0].equals("info")){
                // Get rendered size
				float inputSpread = Float.NaN;
                for (int i = 1; i < tokens.length; i++){
                    if (tokens[i].equals("size")){
                        charSet.setRenderedSize(Integer.parseInt(tokens[i + 1]));
                    }
					else if(tokens[i].equals("padding")) {
						String[] padding = tokens[i + 1].split(",");
						pUp = Integer.parseInt(padding[0]);
						pRight = Integer.parseInt(padding[1]);
						pDown = Integer.parseInt(padding[2]);
						pLeft = Integer.parseInt(padding[3]);
					}
					else if(tokens[i].equals("spread")) {
						inputSpread = Float.parseFloat(tokens[i + 1]);
					}
                }
				if(inputSpread == inputSpread) spread = inputSpread;
				else logger.log(Level.WARNING, "DFontLoader: \"spread\" not specified in {0}. Using {1} as default." , new Object[]{k.getName(), spread});
				
            }else if (tokens[0].equals("common")){
                // Fill out BitmapCharacterSet fields
                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("lineHeight")){
                        charSet.setLineHeight(Integer.parseInt(tokens[i + 1]) - pUp-pDown);
                    }else if (token.equals("base")){
                        charSet.setBase(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("scaleW")){
                        charSet.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("scaleH")){
                        charSet.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("pages")){
                        // number of texture pages
                        matPages = new Material[Integer.parseInt(tokens[i + 1])];
                        font.setPages(matPages);
                    }
                }
            }else if (tokens[0].equals("page")){
                int index = -1;
                Texture tex = null;

                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("id")){
                        index = Integer.parseInt(tokens[i + 1]);
                    }else if (token.equals("file")){
                        String file = tokens[i + 1];
                        if (file.startsWith("\"")){
                            file = file.substring(1, file.length()-1);
                        }
                        TextureKey key = new TextureKey(folder + file, true);
                       // key.setGenerateMips(false);
                        tex = assetManager.loadTexture(key);
                        tex.setMagFilter(magFilter);
                        tex.setMinFilter(minFilter);
                    }
                }
                // set page
                if (index >= 0 && tex != null){
                    Material mat = new Material(spriteMat);
                    mat.setTexture("ColorMap", tex);
                    mat.setBoolean("VertexColor", true);
					if(outline != null) mat.setVector4("TextOutline", outline);
					mat.setFloat("DScale", dscale);
					mat.setFloat("Middle", middle);
                    mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                    matPages[index] = mat;
                }
            }else if (tokens[0].equals("char")){
                // New BitmapCharacter
                BitmapCharacter ch = null;
                for (int i = 1; i < tokens.length; i++){
                    String token = tokens[i];
                    if (token.equals("id")){
                        int index = Integer.parseInt(tokens[i + 1]);
                        ch = new BitmapCharacter();
                        charSet.addCharacter(index, ch);
                    }else if (token.equals("x")){
                        ch.setX(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("y")){
                        ch.setY(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("width")){
                        ch.setWidth(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("height")){
                        ch.setHeight(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("xoffset")){
                        ch.setXOffset(Integer.parseInt(tokens[i + 1]) );
                    }else if (token.equals("yoffset")){
                        ch.setYOffset(Integer.parseInt(tokens[i + 1]));
                    }else if (token.equals("xadvance")){
                        ch.setXAdvance(Integer.parseInt(tokens[i + 1]) -pLeft-pRight);
                    } else if (token.equals("page")) {
                        ch.setPage(Integer.parseInt(tokens[i + 1]));
                    }
                }
            }else if (tokens[0].equals("kerning")){
                // Build kerning list
                int index = 0;
                int second = 0;
                int amount = 0;

                for (int i = 1; i < tokens.length; i++){
                    if (tokens[i].equals("first")){
                        index = Integer.parseInt(tokens[i + 1]);
                    }else if (tokens[i].equals("second")){
                        second = Integer.parseInt(tokens[i + 1]);
                    }else if (tokens[i].equals("amount")){
                        amount = Integer.parseInt(tokens[i + 1]);
                    }
                }

                BitmapCharacter ch = charSet.getCharacter(index);
                ch.addKerning(second, amount);
            }
        }
		font.setSpread(spread);
        return font;
    }
    
    public Object load(AssetInfo info) throws IOException {
        InputStream in = null;
        try {
            in = info.openStream();
            BitmapFont font = load(info.getManager(), info.getKey().getFolder(), in, info.getKey());
            return font;
        } finally {
            if (in != null){
                in.close();
            }
        }
    }
}

