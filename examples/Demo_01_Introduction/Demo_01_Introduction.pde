/**
  * Infinidecimal Canvas Demo 01 - Introduction
  * Copyright (C) 2020 C. Sina Cetin
  *
  * This Processing sketch demonstrates why I needed this library in the
  * first place. I occasionally use Processing for data visualization,
  * and more often than not, I want to blend more objects than 8bit
  * color resolution can handle.
  *
  * Keeping the opacity at a high value means you hit full opacity
  * after a few layers. Keeping the opacity too low, however suffers
  * from opacity artifacts where you lose color resolution and occasionally
  * get stuck at mid-range values.
  * 
  * Infinidecimal Canvas is a solution to this problem. Instead of drawing
  * directly on the canvas, Infinidecimal Canvas (will be referred to as
  * IDC from this point forward, and in the following demos) "draws" on a
  * number array, which is converted to an actual image only when Output()
  * function is invoked.
  * 
  * "Intensity" replaces color, and strokes behave like screen blending.
  * i.e. colors accumulate. This means, this library is suitable to create
  * images by drawing many objects over and over. It is NOT suitable for
  * realtime rendering or drawing colored, vector shapes that occludes
  * each other.
  * 
  * This introductory demo simply draws 99,999 random lines on a canvas
  * twice. Once with 1/255 opacity, with Processing's own line function.
  * Then IDC's own line function, with the intensity value of one.
  * 
  * They are rendered on the left and in the middle, respectively.
  * The third panel shows a subset of raw intensity values from the
  * IDC object.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idc;
PGraphics pg;

int c = 99999;
PVector[] p1 = new PVector[c]; //list of starting points for the random lines
PVector[] p2 = new PVector[c]; //list of end points for the random lines

int res = 400;

float[] rawValues;

void setup() {
  background(255);
  surface.setResizable(true);
  surface.setSize(res * 3, res);

  for (int i = 0; i < c; i++) { //create random positions
    p1[i] = new PVector(random(.05, .95) * res, random(.05, .95) * res);
    p2[i] = new PVector(random(.05, .95) * res, random(.05, .95) * res);
  }

  pg = createGraphics(res, res);
  pg.beginDraw();
  pg.stroke(255, 1); //white stroke with 1/255 opacity
  pg.strokeWeight(2);
  pg.background(0);
  
  for (int i = 0; i < c; i++) 
    pg.line(p1[i].x, p1[i].y, p2[i].x, p2[i].y);
  
  pg.endDraw();
  
  idc = new Infinidecimal(this, res, res);
  idc.SetIntensity(1);  //this is arbitrary. play around. set this to 1000
                        //or 0.001 and the result will be the same, since
                        //the output image is normalized.
  idc.SetWeight(1);

  for (int i = 0; i < c; i++)
    idc.Line(p1[i].x, p1[i].y, p2[i].x, p2[i].y);

  rawValues = idc.GetValuesRaw(); //array of raw values, analogous of pixels array.
  
  fill(0);
  noStroke();
  textAlign(CENTER, CENTER);  
  textSize(8);
}

void draw() {
  background(255);
  image(pg, 0, 0);
  image(idc.Output(), res, 0);

  for(int i = 0; i < res; i+=10) {  
    for(int j = 0; j < res; j+=20) {  
      text(floor(rawValues[i * res + j]), res * 2 + j + 10, i + 5);
    }
  }
}
