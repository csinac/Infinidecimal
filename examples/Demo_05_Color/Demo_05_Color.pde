/**
  * Infinidecimal Canvas Demo 05 - Color
  * Copyright (C) 2020 C. Sina Cetin
  *
  * Even though IDC is monochromatic in nature, the image
  * can be tinted.
  *
  * There two color modes; RGB and HSB.
  *
  * In each mode, you can set the start color, i.e. the color
  * that corresponds to zero, and the target color, i.e. the
  * color that corresponds to 1.0.
  *
  * In RGB, the image appears duotone, whereas in HSB the
  * hue transition yields cascading colors.
  *
  * The four panes on the left show random start and target
  * colors in RGB mode. Press space bar to randomize again.
  * 
  * The big, fifth pane on the right shows an animated, image
  * in HSB color.
  *
  * Let's make Mr. Warhol wish he had Processing and Infinidecimal
  * Canvas in 1962 with this demo.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal[] idc;
PImage[] output;

PImage mandrill;

int tileSize = 200;

void setup() {
  surface.setResizable(true);
  surface.setSize(tileSize * 4, tileSize * 2);
  background(0);
  
  idc = new Infinidecimal[5];
  output = new PImage[5];
  
  mandrill = loadImage("mandrill.png");
  
  //four rgb panels
  for(int i = 0; i < 4; i++) {
    idc[i] = new Infinidecimal(this, tileSize, tileSize);
    
    idc[i].SetStartColor(random(255), random(255), random(255));
    idc[i].SetTargetColor(random(255), random(255), random(255));
  }
  
  //hsb
  idc[4] = new Infinidecimal(this, tileSize*2, tileSize*2);
  idc[4].SetHSB(true); //color mode is RGB by default
    
  for(int i = 0; i < idc.length; i++) {
    //Let's sprinkle some random noise
    idc[i].SetIntensity(0.1);
    for(int j = 0; j < 99999; j++) {
      idc[i].Dot(random(idc[i].Width()), random(idc[i].Height()));
    }
    
    idc[i].SetIntensity(0.3);
    idc[i].SetWeight(5);
    //And some random shapes, why not
    for(int j = 0; j < 5; j++) {
      float x = idc[i].Width() * random(0.05, 0.95);
      float y = idc[i].Height() * random(0.2, 0.8);
      float w = idc[i].Width() * random(0.03, 0.10);
      float h = idc[i].Height() * random(0.03, 0.10);
      
      float r = random(1);
      
      if(r < 0.33) idc[i].Rectangle(x, y, w, h);
      else if(r < 0.66) idc[i].Ellipse(x, y, w, h);
      else idc[i].Circle(x, y, sqrt(w*w + h*h) / 2);
    }
  }
  
  
  //Image function with just one PImage parameter stretches
  //the image over the entire canvas.
  for(int i = 0; i < idc.length; i++) {
    idc[i].SetIntensity(1);
    idc[i].Image(mandrill);
    output[i] = idc[i].Output();
  }
}

void draw() {
  for(int i = 0; i < 4; i++)
    image(output[i], tileSize * (i % 2), tileSize * (i / 2), tileSize, tileSize);
  
  idc[4].SetStartColor((cos(frameCount / 200f) + 1) * 180, 100, 10);
  idc[4].SetTargetColor((sin(frameCount / 100f) + 1) * 180, 100, 100);
  output[4] = idc[4].Output();
  image(output[4], tileSize * 2, 0, tileSize * 2, tileSize * 2);
}

void keyPressed() {
  if(key == ' ') {
    for(int i = 0; i < 4; i++) {
      idc[i].SetStartColor(random(255), random(255), random(255));
      idc[i].SetTargetColor(random(255), random(255), random(255));
      output[i] = idc[i].Output();
    }
  }
}
