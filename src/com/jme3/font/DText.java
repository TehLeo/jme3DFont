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
package com.jme3.font;

import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector4f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.texture.Texture;

/**
 *
 * @author Juraj Papp
 */
public class DText extends BitmapText {
	public float DScaleMult = 0.25f;
	public float Spread = 4f;
	public float middle = 0.5f;
	public Vector4f outlineW;
	protected Geometry[] textPages;
	protected Texture[] textures;
	protected Material[] mats;
	public DText(BitmapFont font) {
		super(font);
		init();
	}
	public DText(BitmapFont font, boolean rightToLeft) {
		super(font, rightToLeft);
		init();
	}
	public DText(BitmapFont font, boolean rightToLeft, boolean arrayBased) {
		super(font, rightToLeft, arrayBased);
		init();
	}
	protected void init() {
		 try {
            java.lang.reflect.Field f = BitmapText.class.getDeclaredField("textPages");
            f.setAccessible(true);
            textPages = (Geometry[])f.get(this);
			textures = new Texture[textPages.length];
			
			f = Class.forName("com.jme3.font.BitmapTextPage").getDeclaredField("texture");
            f.setAccessible(true);
			for(int i = 0; i < textures.length; i++)
				textures[i] = (Texture)f.get(textPages[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }
		BitmapFont font = getFont();
		mats = new Material[font.getPageSize()];
		for(int i = 0; i < mats.length; i++) {
			mats[i] = font.getPage(i).clone();
			textPages[i].setMaterial(mats[i]);
		}		
		setSize(getSize());
	}
	
	/**
	 * Color of outline and its thickness.
	 * @param rgbW color and thickness
	 */
	public void setOutline(Vector4f rgbW) {
		outlineW = rgbW;
		if(rgbW == null)
			for (int i = 0; i < textPages.length; i++) 
				mats[i].clearParam("TextOutline");
		else 
			for (int i = 0; i < textPages.length; i++) 
				mats[i].setVector4("TextOutline", rgbW);
	}
	
	/**
	 * @param middle [0-1] 0.5 default
	 */
	public void setMiddle(float middle) {
		this.middle = middle;
		for (int i = 0; i < textPages.length; i++) 
			mats[i].setFloat("Middle", middle);
	}
	
	@Override
	public void setSize(float size) {
		super.setSize(size);
		for (int i = 0; i < textPages.length; i++) 
			mats[i].setFloat("DScale", computeDScale());		
	}

	
	public float computeDScale() {
		return DScaleMult / (Spread*getScale());
	}
	public float getScale() {
        return getSize() / getFont().getCharSet().getRenderedSize();
    }

}
