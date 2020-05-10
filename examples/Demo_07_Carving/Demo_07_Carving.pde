/**
  * Infinidecimal Canvas Demo 07 - Carving
  * Copyright (C) 2020 C. Sina Cetin
  *
  * When the carve mode is on, new values are subtracted from the
  * array, not added.
  *
  * In this example we first create a network of bezier curves
  * inside a circle. (Fun fact: This is exactly the type of
  * visualization that led me to actually develop Infinidecimal)
  *
  * Then, we carve twice:
  * First, with the buffered canvas, and then with 5 thick lines.
  *
  * Notice how the circles still left some of the previous strokes
  * while the thick lines completely carved the canvas out. This 
  * is because the intensity for is set to the maximum value on the
  * value array, before carving out the lines.
  *
  * In addition to carving, you can also cut out parts of the canvas
  * by using the Clear() function.
  *
  * Clear() function takes four parameters; namely x and y for top-left
  * anchor and sizex & sizey for the size. If no parameters are given
  * it empties the entire value array, so it is analogous of background(0).
  *
  * Click, drag and release your mouse to carve out lines or to clear
  * subsections. Use arrow keys to set the intensity for carving.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idc;
int res = 720;
PImage output;

boolean clear = false;
float carveIntensity;
float maxValue;
PVector start, end;
int clickCount;

void setup() {
  start = new PVector();
  end = new PVector();
  
  surface.setResizable(true);
  surface.setSize(res, res);
  
  idc = new Infinidecimal(this, res, res);
  idc.SetCentered(true);
  
  for(int i = 0; i < 99999; i++) {
    float a = random(TWO_PI), b = random(TWO_PI);
    
    float r = res * random(0.45, 0.5);
    
    float x1 = cos(a) * r + res / 2, y1 = sin(a) * r + res / 2;
    float x2 = cos(b) * r + res / 2, y2 = sin(b) * r + res / 2;
    
    idc.QuadraticRationalBezier(x1, y1, res / 2, res / 2, x2, y2, 0.25);    
  }
  
  maxValue = idc.GetMaxValue();
  
  idc.SetIntensity(maxValue);
  idc.SetCarve(true);
  PGraphics buffer = idc.GetBufferCanvas();
  buffer.noStroke();
  buffer.fill(255);

  idc.SetIntensity(maxValue * 0.5);
  buffer.ellipse(0  , res / 2, res / 3, res / 3);
  buffer.ellipse(res, res / 2, res / 3, res / 3);
  buffer.ellipse(res / 2, 0  , res / 3, res / 3);
  buffer.ellipse(res / 2, res, res / 3, res / 3);
  idc.ApplyBuffer();
   
  idc.SetWeight(20);
  idc.SetIntensity(idc.GetMaxValue());
  for(int i = 0; i < 5; i++) {
    float x1 = random(0.25, 0.75) * res, y1 = random(0.25, 0.75) * res;
    float x2 = random(0.25, 0.75) * res, y2 = random(0.25, 0.75) * res;
    
    idc.Line(x1, y1, x2, y2);
  }
    
  carveIntensity = idc.GetMaxValue();
  maxValue = carveIntensity;
  output = idc.Output();
  stroke(255, 0, 0);
  noFill();
}

void draw() {
  image(output, 0, 0);
  
  text("Intensity: " + carveIntensity + " [↑↓] - current max value: " + maxValue, 20, 20); 
  text("Mode: " + (clear ? "Clear" : "Carve") + " [c]", 20, 40); 
  
  if(clickCount == 1) {
    if(clear) {
      strokeWeight(5);
      point(start.x, start.y);
      strokeWeight(1);
      rect(start.x, start.y, mouseX-start.x, mouseY-start.y);
    }
    else {
      strokeWeight(5);
      point(start.x, start.y);
      strokeWeight(1);
      line(start.x, start.y, mouseX, mouseY);
    }
  }
}

void mousePressed() {
  clickCount = 1;
  start.x = mouseX;
  start.y = mouseY;
}

void mouseReleased() {
  clickCount = 2;
  if(clear) {
    int x = floor(start.x);
    int y = floor(start.y);
    int w = floor(mouseX-start.x);
    int h = floor(mouseY-start.y);
    
    if(w < 0) { w *= -1; x -= w; }
    if(h < 0) { h *= -1; y -= h; }
    
    idc.Clear(x, y, w, h);
  }
  else {
    idc.Line(start.x, start.y, mouseX, mouseY);
  }
  maxValue = idc.GetMaxValue();
  output = idc.Output();
}

void keyPressed() {
  if(key == 'c') {
    clear = !clear;
  }
  else if(key == CODED) {
    if(keyCode == UP) {
      carveIntensity *= 1.25;
      idc.SetIntensity(carveIntensity);
    }
    else if(keyCode == DOWN) {
      carveIntensity /= 1.25;
      idc.SetIntensity(carveIntensity);
    }
  }
}
