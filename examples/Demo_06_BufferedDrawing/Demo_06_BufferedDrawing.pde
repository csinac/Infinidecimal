/**
  * Infinidecimal Canvas Demo 06 - Buffered Drawing
  * Copyright (C) 2020 C. Sina Cetin
  *
  * Although IDC has a few options to draw figures, you might want
  * to draw things by using Processing's own functions, such as
  * transformations, text or PShape.
  * 
  * To do this, you can use the buffer context embedded
  * in the IDC object.
  * 
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idc;
PImage output;
PShape bot;

void setup() {
  size(500, 500);
  
  bot = loadShape("bot1.svg");
    
  idc = new Infinidecimal(this, width, height);
  idc.SetIntensity(3);

  PGraphics idcBuffer = idc.GetBufferCanvas();
  idcBuffer.rectMode(CENTER);
  
  idcBuffer.pushMatrix();
    idcBuffer.translate(0, idcBuffer.height / 3);
    idcBuffer.rotate(QUARTER_PI);
    idcBuffer.fill(50);
    idcBuffer.stroke(255);
    idcBuffer.strokeWeight(2);
    idcBuffer.rect(0, 0, 200, 200);
  idcBuffer.popMatrix();
  
  idcBuffer.pushMatrix();
    idcBuffer.translate(idcBuffer.width, 2 * idcBuffer.height / 3);
    idcBuffer.rotate(QUARTER_PI);
    idcBuffer.fill(200);
    idcBuffer.stroke(100);
    idcBuffer.strokeWeight(20);
    idcBuffer.rect(0, 0, 200, 200);
  idcBuffer.popMatrix();
  
  idc.ApplyBuffer();
  
  /*
   * When you draw on the idcBuffer object, the buffer doesn't get
   * applied on the values array until either the ApplyBuffer or
   * Output functions are called.
   *
   * ApplyBuffer() function let's you manually apply the buffer to
   * the values array. This way, you can choose which objects
   * are rendered together, and which objects are applied onto
   * each other à la Infinidecimal.
   *
   * Above, two rectangles are drawn together. In the loop below,
   * each iteration begins a new buffer draw canvas, draws a single
   * text and applies it to the values list. This way, text behaves
   * like IDC's own intensity based drawing functions.
   */

  idc.SetIntensity(1);
  idcBuffer.textAlign(CENTER, CENTER);
  idcBuffer.noStroke();
  idcBuffer.fill(255);

  for(int i = 0; i < 10; i++) {
    idc.BeginBufferDraw();
    idcBuffer.background(0);
    idcBuffer.textSize(40 + random(-10, 10));
    idcBuffer.text("Infinidecimal\nCanvas", idc.Width() / 2 + random(-25, 25), idc.Height() / 2 + random(-25, 25));
    idc.ApplyBuffer();
  }

  idc.SetIntensity(7);
  idc.BeginBufferDraw();
  idcBuffer.textSize(40);
  idcBuffer.text("Infinidecimal\nCanvas", idc.Width() / 2, idc.Height() / 2);
  idc.ApplyBuffer();

  /*
   * One thing to note here is that intensity and stroke / fill work together
   * here, like in the case of drawing images. Every context is in 0 - 255
   * range and they get applied based on the current intensity value.
   *
   * Notice how the text in the loop and the final text all have stroke color
   * value 1, but the last text is applied with the intensity value of 2,
   * while the text in the loop is applied with the intensity value 0.5.
   */
   
   idc.BeginBufferDraw();
   idcBuffer.shape(bot, idc.Width() - 150, 2 * idc.Height() / 3, 100, 100);
   idcBuffer.shape(bot, 20, 80, 100, 100);
      
   output = idc.Output();
}

void draw() {
  image(output, 0, 0);
}
