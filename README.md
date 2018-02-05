# jme3DFont
Distance field font for jme3
# JME3 Signed Distance Font

## Usage: 

```
assetManager.registerLoader(DFontLoader.class, "dfnt");
		
BitmapFont fnt = assetManager.loadFont("Interface/Fonts/FreeSans32.dfnt");

txt = new DText(fnt, false);
txt.setOutline(new Vector4f(0,0,0,0.25f)); //Sets an black rgb(0,0,0) outline, thickness 0.25 [0-0.5range]
//		txt.setOutline(null); //This would remove outline
txt.setSize(32); //Font size
txt.setMiddle(0.4f); //optionally vary thickness, default is 0.5
txt.setText(txtB);
txt.setLocalTranslation(0, txt.getHeight(), 0);
guiNode.attachChild(txt);

```

Alternatively, check:
[TestDFont.java](src/jme3test/gui/TestDFont.java)


## Creating new fonts:

You can check:

(https://github.com/libgdx/libgdx/wiki/Distance-field-fonts)

Where you can download:

(https://github.com/libgdx/libgdx/wiki/Hiero)
