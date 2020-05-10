/**
  * Infinidecimal Canvas Demo 03 - Shapes
  * Copyright (C) 2020 C. Sina Cetin
  *
  * This sketch demonstrates various shapes that come with the
  * library.
  *
  * The ellipse drawing algorithm is based on bresenham's line drawing
  * algorithm and the implementation is directly applied from the
  * link below: 
  * http://members.chello.at/~easyfilter/bresenham.html
  * Here, you can find Alois Zingl's paper, c implementation and js demo.
  *
  * There are three (actually two) types of shapes available:
  * Rectangle, Ellipse and a Circle.
  *
  * Circle is a special case of Ellipse, where the height and width are equal.
  *
  * Shapes are either anchored at their top left corner, or at the center.
  * SetCentered(boolean) function toggles the anchoring mode.
  *
  * This sketch is divided into three panes for each shape type.
  * follow the on-screen instructions to drop points on each pane to create
  * new shapes to see how they behave.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idcRect, idcEllipse, idcCircle;
boolean centered = false;
boolean carve = false;

int res = 360;

PVector[] rectPoints = new PVector[2];
int rpc = 0; //rectangle point count

PVector[] ellipsePoints = new PVector[2];
int epc = 0; //ellipse point count

PVector[] circlePoints = new PVector[2];
int cpc = 0; //circle point count

void setup() {
  surface.setResizable(true);
  surface.setSize(res * 3, res);
  
  background(0);
  
  idcRect    = new Infinidecimal(this, res, res);
  idcEllipse = new Infinidecimal(this, res, res);
  idcCircle  = new Infinidecimal(this, res, res);
  
  idcRect.SetWeight(2);
  idcEllipse.SetWeight(2);
  idcCircle.SetWeight(2);
  
  noCursor();
}

/*
 * This entire draw function is for displaying how IDC works.
 * Actual drawing functionality is in mouseReleased() function.
 */
void draw() {
  image(    idcRect.Output(),     0, 0);
  image( idcEllipse.Output(),   res, 0);
  image(  idcCircle.Output(), 2*res, 0);
  
  noStroke();
  fill(255);
  text("rectangle\nplace two points by clicking in this pane.", 20, 20);
  text("ellipse\nplace two points by clicking in this pane.", 20+res, 20);
  text("circle\nplace two points by clicking in this pane.\nUnlike ellipse, circle's size parameter is radius.", 20+res*2, 20);
  text("center mode: " + centered + " [space]", 20, res-30);
  text("carve mode: " + carve + " [c]", 20, res-10);
  
  noFill();
  
  stroke(255);
  strokeWeight(2);
  line(res, 0, res, height);
  line(res*2, 0, res*2, height);
  
  if(mouseX < res) stroke(255, 0, 0);
  else if(mouseX < res * 2) stroke(0, 255, 0);
  else stroke(0, 0, 255);
  
  strokeWeight(5);
  point(mouseX, mouseY);
  
  if(rpc == 1) {
    strokeWeight(4);
    stroke(255, 0, 0);  
    point(rectPoints[0].x, rectPoints[0].y);
    
    if(mouseX < res) {
      strokeWeight(1);
      float x = rectPoints[0].x;
      float y = rectPoints[0].y;
      float w = mouseX - rectPoints[0].x;
      float h = mouseY - rectPoints[0].y;
      
      if(centered) {
        x -= w; y -= h;
        w *= 2; h *= 2;
      }

      rect(x, y, w, h);
    }
  }
  
  stroke(0, 255, 0);
  if(epc == 1) {
    strokeWeight(4);
    stroke(0, 255, 0);  
    point(ellipsePoints[0].x+res, ellipsePoints[0].y);
    
    if(mouseX < res * 2 && mouseX > res) {
      strokeWeight(1);
      float x = ellipsePoints[0].x+res;
      float y = ellipsePoints[0].y;
      float w = mouseX - ellipsePoints[0].x - res;
      float h = mouseY - ellipsePoints[0].y;

      if(centered) {
        w *= 2;
        h *= 2;
        ellipse(x, y, w, h);
      }
      else {
        x += w / 2;
        y += h / 2;
        ellipse(x, y, w, h);
      }
    }
  }

  if(cpc == 1) {
    strokeWeight(4);
    stroke(0, 0, 255);
    point(circlePoints[0].x + res * 2, circlePoints[0].y);
    
    if(mouseX > res * 2) {
      strokeWeight(1);
      float x = circlePoints[0].x+res * 2;
      float y = circlePoints[0].y;
      float w = mouseX - circlePoints[0].x - res * 2;
      float h = mouseY - circlePoints[0].y;
      
      float rad = sqrt(w*w + h*h);

      if(centered) {
        rad *= 2;
        ellipse(x, y, rad, rad);
      }
      else {
        int sw = w < 0 ? -1 : 1;
        int sh = h < 0 ? -1 : 1;
        
        w = h = max(abs(w), abs(h));
        w *= sw;
        h *= sh;
        
        x += w / 2;
        y += h / 2;
        ellipse(x, y, w, h);
      }
    }
  }
}

/*
 * The shape functions for cube, ellipse and circle takes 4, 4 and 3 parameters
 * respectively.
 * 
 * The mouseReleased function cycles through each mouse click. Every second click
 * creates a new shape, which gets recorded in the value array for the
 * corresponding idc object.
 *
 * Try pressing space to change the anchoring mode to see the difference.
 *
 * Notice how the image gets gradually darker as intersections increase. This is
 * because the coloring is in normalized mode; meaning that the current highest
 * value will be mapped to pure white. Because of this, each overlapping curve
 * increases the maximum value and parts outside intersections will appear dark.
 */
void mouseReleased() {
  if(mouseX < res) {
    if(rpc == 2) rpc = 0;
    
    rectPoints[rpc] = new PVector(mouseX, mouseY);
    rpc++;
    
    if(rpc == 2) {
      float w = rectPoints[1].x - rectPoints[0].x;
      float h = rectPoints[1].y - rectPoints[0].y;
      float x = rectPoints[0].x;
      float y = rectPoints[0].y;
      
      if(centered) {
        w *= 2;
        h *= 2;
        
        x -= w / 2;
        y -= h / 2;
      }
      
      idcRect.Rectangle(x, y, w, h);
    }    
  }
  else if(mouseX < res * 2) {
    if(epc == 2) epc = 0;
    
    ellipsePoints[epc] = new PVector(mouseX-res, mouseY);
    epc++;
    
    if(epc == 2) {
      float w = ellipsePoints[1].x - ellipsePoints[0].x;
      float h = ellipsePoints[1].y - ellipsePoints[0].y;
      float x = ellipsePoints[0].x;
      float y = ellipsePoints[0].y;
      
      if(centered) {
        w *= 2;
        h *= 2;
        
        x -= w / 2;
        y -= h / 2;
      }
      
      idcEllipse.Ellipse(x, y, w, h);
    }    
  }
  else {
    if(cpc == 2) cpc = 0;
    
    circlePoints[cpc] = new PVector(mouseX-res*2, mouseY);
    cpc++;
    
    if(cpc == 2) {
      float w = circlePoints[1].x - circlePoints[0].x;
      float h = circlePoints[1].y - circlePoints[0].y;
      float x = circlePoints[0].x;
      float y = circlePoints[0].y;
      
      if(centered) {
        float rad = sqrt(w*w + h*h);
        idcCircle.Circle(x, y, rad);
      }
      else {
        int sw = w < 0 ? -1 : 1;
        int sh = h < 0 ? -1 : 1;
        
        w = h = max(abs(w), abs(h));
        
        if(sw == -1) x -= w;
        if(sh == -1) y -= h;
        
        idcCircle.Circle(x, y, w / 2);
      }
    }    
  }
}

void keyPressed() {
  if(key == ' ') {
    centered = !centered;
    idcCircle.SetCentered(centered);
  }
  else if(key == 'c') {
    carve = !carve;
    idcRect.SetCarve(carve);
    idcEllipse.SetCarve(carve);
    idcCircle.SetCarve(carve);
  }
}
