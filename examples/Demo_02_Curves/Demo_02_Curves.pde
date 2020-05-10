/**
  * Infinidecimal Canvas Demo 02 - Curves
  * Copyright (C) 2020 C. Sina Cetin
  *
  * This sketch demonstrates various curve types that come with the
  * library.
  *
  * The line drawing algorithms are based on bresenham's line drawing
  * algorithm and the implementations are directly applied from the
  * link below: 
  * http://members.chello.at/~easyfilter/bresenham.html
  * Here, you can find Alois Zingl's paper, c implementation and js demo.
  *
  * There are four (actually three) types of curves available:
  * Cubic Bezier, Quadratic Bezier, Quadratic Rational Bezier and Line.
  *
  * Quadratic Bezier is a special case of Quadratic Rational Bezier, where
  * the weight of the control point is 1.
  *
  * This sketch is divided into four panes for each curve type.
  * follow the on-screen instructions to drop points on each pane to create
  * new curves to see how they behave.
  */

import goodRectangle.infinidecimal.*;

Infinidecimal idcLine, idcQBez, idcQRBez, idcCubic;
float qrbWeight = 1;

int res = 360;

PVector[] linePoints = new PVector[2];
int lpc = 0; //line point count

PVector[] qBezierPoints = new PVector[3];
int qbc = 0; //quadratic bezier point count

PVector[] qRBezierPoints = new PVector[3];
int qrbc = 0; //quadratic rational bezier point count

PVector[] cBezierPoints = new PVector[4];
int cbc = 0; //cubic bezier point count

void setup() {
  surface.setResizable(true);
  surface.setSize(res * 2, res * 2);
  
  background(0);
  
  idcLine  = new Infinidecimal(this, res, res);
  idcQBez  = new Infinidecimal(this, res, res);
  idcQRBez = new Infinidecimal(this, res, res);
  idcCubic = new Infinidecimal(this, res, res);
  
  idcLine.SetWeight(2);
  idcQBez.SetWeight(2);
  idcQRBez.SetWeight(2);
  idcCubic.SetWeight(2);
  
  noCursor();
}

/*
 * This entire draw function is for displaying how IDC works.
 * Actual drawing functionality is in mouseReleased() function.
 */
void draw() {
  image( idcLine.Output(),   0,   0);
  image( idcQBez.Output(), res,   0);
  image(idcQRBez.Output(), res, res);
  image(idcCubic.Output(),   0, res);
  
  noStroke();
  fill(255);
  text("line\nplace two points by clicking in this pane.", 20, 20);
  text("quadratic bezier\nplace three points by clicking in this pane.", 20+res, 20);
  text("quadratic rational bezier\nplace three points by clicking in this pane.\npress 1 to 9 to set weight. current weight: " + qrbWeight, 20+res, 20+res);
  text("quadratic cubic bezier\nplace four points by clicking in this pane.", 20, 20+res);
  
  stroke(255);
  strokeWeight(2);
  line(0, res, width, res);
  line(res, 0, res, height);
  
  if(mouseX < res && mouseY < res) stroke(255, 0, 0);
  else if(mouseX > res && mouseY < res) stroke(0, 255, 0);
  else if(mouseX > res && mouseY > res) stroke(0, 0, 255);
  else stroke(255, 220, 0);

  strokeWeight(5);
  point(mouseX, mouseY);
  
  strokeWeight(4);
  stroke(255, 0, 0);
  for(int i = 0; i < lpc; i++) {
    point(linePoints[i].x, linePoints[i].y);
  }
  stroke(0, 255, 0);
  for(int i = 0; i < qbc; i++) {
    strokeWeight(4);
    point(qBezierPoints[i].x+res, qBezierPoints[i].y);

    strokeWeight(1);
    if(i != qbc-1) line(qBezierPoints[i].x+res, qBezierPoints[i].y, qBezierPoints[i+1].x+res, qBezierPoints[i+1].y);
  }

  stroke(0, 0, 255);
  for(int i = 0; i < qrbc; i++) {
    strokeWeight(4);
    point(qRBezierPoints[i].x+res, qRBezierPoints[i].y+res);

    strokeWeight(1);
    if(i != qrbc-1) line(qRBezierPoints[i].x+res, qRBezierPoints[i].y+res, qRBezierPoints[i+1].x+res, qRBezierPoints[i+1].y+res);
  }

  stroke(255, 220, 0);
  for(int i = 0; i < cbc; i++) {
    strokeWeight(4);
    point(cBezierPoints[i].x, cBezierPoints[i].y+res);
    
    strokeWeight(1);
    if(i != cbc-1) line(cBezierPoints[i].x, cBezierPoints[i].y+res, cBezierPoints[i+1].x, cBezierPoints[i+1].y+res);
  }
}

/*
 * The curve functions for line, quadratic bezier, quadratic bezier and cubic bezier
 * takes 2, 3, 3 and 4 positions respectively. Quadratic rational bezier also takes
 * an extra weight parameter.
 *
 * The mouseReleased function cycles through each mouse click. When there are enough
 * positions for each curve type, a new curve will be created and get recorded
 * in the value array for the corresponding idc object.
 *
 * Notice how the image gets gradually darker as intersections increase. This is
 * because the coloring is in normalized mode; meaning that the current highest
 * value will be mapped to pure white. Because of this, each overlapping curve
 * increases the maximum value and parts outside intersections will appear dark.
 */
void mouseReleased() {
  if(mouseX < res && mouseY < res) {
    if(lpc == 2) lpc = 0;
    
    linePoints[lpc] = new PVector(mouseX, mouseY);
    lpc++;
    
    if(lpc == 2) {
      idcLine.Line(linePoints[0].x, linePoints[0].y, linePoints[1].x, linePoints[1].y);
    }    
  }
  else if(mouseX > res && mouseY < res) {
    if(qbc == 3) qbc = 0;
    
    qBezierPoints[qbc] = new PVector(mouseX-res, mouseY);
    qbc++;
    
    if(qbc == 3) {
      idcQBez.QuadraticBezier(qBezierPoints[0].x, qBezierPoints[0].y,
                              qBezierPoints[1].x, qBezierPoints[1].y,
                              qBezierPoints[2].x, qBezierPoints[2].y);
    }    
  }
  else if(mouseX > res && mouseY > res) {
    if(qrbc == 3) qrbc = 0;
    
    qRBezierPoints[qrbc] = new PVector(mouseX-res, mouseY-res);
    qrbc++;
    
    if(qrbc == 3) {
      idcQRBez.QuadraticRationalBezier(qRBezierPoints[0].x, qRBezierPoints[0].y,
                                       qRBezierPoints[1].x, qRBezierPoints[1].y,
                                       qRBezierPoints[2].x, qRBezierPoints[2].y,
                                       qrbWeight);
    }    
  }
  else {
    if(cbc == 4) cbc = 0;
    
    cBezierPoints[cbc] = new PVector(mouseX, mouseY-res);
    cbc++;
    
    if(cbc == 4) {
      idcCubic.CubicBezier(cBezierPoints[0].x, cBezierPoints[0].y,
                           cBezierPoints[1].x, cBezierPoints[1].y,
                           cBezierPoints[2].x, cBezierPoints[2].y,
                           cBezierPoints[3].x, cBezierPoints[3].y);
    }    
  }
}

void keyPressed() {
  if(key >= '1' && key <= '9') {
    qrbWeight = key - '0';
  }
}
