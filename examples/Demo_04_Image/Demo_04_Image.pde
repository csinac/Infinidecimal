/**
  * Infinidecimal Canvas Demo 04 - Image
  * Copyright (C) 2020 C. Sina Cetin
  *
  * In addition to shapes and curves, IDC can also plot
  * images on the value array.
  *
  * Just like the other shapes, images are also "tinted"
  * with the set intensity. Meaning, the pixels' brightness
  * is mapped from 0 - 255 to 0 - intensity.
  *
  * In this example, the sketch starts off with 5 super-imposed
  * mandrill images. Each image has a different intensity
  * value and therefore each of them appear at different
  * brightnesses.
  *
  * Add more mandrills and play around with different intensity
  * values to see how it changes the resulting image.
  *
  * Press space bar to switch between anchor modes and
  * use the arrow keys to increase or decrease the intensity.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idc;

PImage mandrill;
PImage output;

boolean centered = false;
int intensity = 10;

void setup() {
  size(512, 512);
  
  mandrill = loadImage("mandrill.png");
  
  idc = new Infinidecimal(this, 512, 512);
  output = idc.Output();
  
  idc.SetIntensity(1);
  idc.Image(mandrill,   0,   0, 256, 256);
  idc.SetIntensity(2);
  idc.Image(mandrill, 256,   0, 256, 256);
  idc.SetIntensity(3);
  idc.Image(mandrill, 256, 256, 256, 256);
  idc.SetIntensity(5);
  idc.Image(mandrill,   0, 256, 256, 256);
  
  idc.SetIntensity(2);
  idc.Image(mandrill, 0, 0);
  
  output = idc.Output();
  
  noCursor();
}

void draw() {
  image(output, 0, 0);
  
  fill(255);
  text("Current intensity: " + (intensity / 10f) + " [↑↓]", 20, 20);
  text("Centered: " + centered + " [space]", 20, 40);
  
  stroke(255, 0, 0);
  noFill();
  strokeWeight(1);
  rect(mouseX - (centered ? 64 : 0), mouseY - (centered ? 64 : 0), 128, 128);
  strokeWeight(5);
  point(mouseX, mouseY);
}

void mousePressed() {
  idc.Image(mandrill, mouseX, mouseY, 128, 128);
  output = idc.Output();
}

void keyPressed() {
  if(key == ' ') {
    centered = !centered;
    idc.SetCentered(centered);
  }
  else if(key == CODED) {
    if(keyCode == UP) {
      if(intensity < 10) intensity ++;
      else intensity += 10;
    }
    else if(keyCode == DOWN) {
      if(intensity <= 10) intensity -= 1;
      else intensity -= 10;
      
      if(intensity < 1) intensity = 1;
    }
  }
  
  idc.SetIntensity(intensity / 10f);
}
