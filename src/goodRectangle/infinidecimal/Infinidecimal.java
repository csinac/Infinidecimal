package goodRectangle.infinidecimal;

import java.util.HashMap;
import processing.core.*;

/**
 * Infinidecimal.java - Infinidecimal Canvas library for Processing
 * Copyright (C) 2020 C. Sina Cetin
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * @example Demo_01_Introduction, Demo_02_Curves, Demo_03_Shapes, Demo_04_Image, Demo_05_Color, Demo_06_BufferedDrawing, Demo_07_Carving
 */

public class Infinidecimal {
	PApplet app;

	public final static String VERSION = "##library.prettyVersion##";

	private int w;
	private int h;
	private int len;
	private boolean centered;
	private boolean normalize;

	private float[] values;
	private float[] vals01;
	private boolean ready;

	private float min, max;

	private float weight;
	private float intensity;
	private boolean carve;

	private PGraphics buffer;
	private boolean bufferOpen;

	private float EPSILON = PApplet.EPSILON;

	private float[] colorMin = { 0, 0, 0 };
	private float[] colorMax = { 255, 255, 255 };
	private boolean isHSB;

	/**
	 * Creates an Infinidecimal instance
	 * 
	 * @param parent: the Processing app currently in use ("this" in most cases)
	 * @param w:      the desired width of the canvas
	 * @param h:      the desired height of the canvas
	 */
	public Infinidecimal(PApplet parent, int w, int h) {
		app = parent;
		this.w = w;
		this.h = h;
		this.len = w * h;

		values = new float[len];
		vals01 = new float[len];
		ready = false;
		min = max = 0;

		weight = 1;
		intensity = 1;
		bufferOpen = false;

		normalize = true;
		centered = false;
		carve = false;
		isHSB = false;

		buffer = app.createGraphics(w, h);
	}

	/**
	 * Sets color intensity. This parameter is analogous of stroke, except it isn't
	 * capped at 255.
	 * 
	 * @param val: new intensity value
	 */
	public void SetIntensity(float val) {
		ready = false;
		intensity = val;
	}

	/**
	 * Sets the stroke weight.
	 * 
	 * @param val: new stroke weight value
	 */
	public void SetWeight(float val) {
		weight = val;
	}

	/**
	 * Returns the width of the canvas
	 * 
	 * @return int
	 */
	public int Width() {
		return w;
	}

	/**
	 * Returns the height of the canvas
	 * 
	 * @return int
	 */
	public int Height() {
		return h;
	}

	/**
	 * Private Function
	 * Used for strokes that plots overlapping points that would
	 * otherwise cause inconsistent color values. The hashmap is a per-stroke-buffer
	 * that keeps a unique list of points to be drawn.
	 * 
	 * @param map: buffer of points to be drawn
	 */

	private void ApplyHashMap(HashMap<Integer, Float> map) {
		java.util.Set<Integer> keys = map.keySet();
		for (int k : keys) {
			values[k] += (carve ? -1: 1) * map.get(k) * intensity;
			if(values[k] < 0) values[k] = 0;
		}
	}

	/**
	 * Private Function
	 * Used for complex shapes that contains overlapping strokes,
	 * that would otherwise cause inconsistent color values. This function merges
	 * multiple hashmaps into one.
	 * 
	 * @param source: sub-buffer to be applied to the current buffer
	 * @param target: currently open buffer
	 */
	private void MergeMaps(HashMap<Integer, Float> source, HashMap<Integer, Float> target) {
		java.util.Set<Integer> keys = source.keySet();
		for (int k : keys) {
			float val = source.get(k);
			if (!target.containsKey(k) || target.get(k) < val)
				target.put(k, val);
		}
	}
	
	/**
	 * Clears the values array.
	 * Analogous of background(0);
	 */
	public void Clear() {
		for(int i = 0; i < len; i++) values[i] = 0;
		ready = false;
	}
	
	/**
	 * Clears a subsection of the canvas, anchored at (x, y) from
	 * it's top left corner and with the size (sixex, sizey);
	 * 
	 * @param x: left anchor of the area
	 * @param y: top anchor of the area
	 * @param sizex: width of the area
	 * @param sizey: height of the area
	 */
	public void Clear(int x, int y, int sizex, int sizey) {
		for(int i = 0; i < sizey; i++) {
			for(int j = 0; j < sizex; j++) {
				int px = x + j;
				int py = y + i;
				
				if(px >= 0 && px < w && py >= 0 && py < h) values[py * h + px] = 0;
			}
		}
		
		ready = false;
	}
	
	/**
	 * When the carve mode is on, the intensity values are subtracted, not added to
	 * the value array. This way, you can carve out with the shapes you draw.
	 * 
	 * Values are clipped at zero, which means a high enough intensity effectively
	 * punches out an entire hole on the canvas.
	 * 
	 * Hint: Use in combination with the "MaxValue()" method.
	 * 
	 * @param state: new state of the carve mode
	 */
	public void SetCarve(boolean state) {
		carve = state;
	}

	/**
	 * Plots an anti-aliased point on the value array, with the set intensity.
	 * 
	 * @param x: x position of the point
	 * @param y: y position of the point
	 */
	public void Dot(float x, float y) {
		Dot(x, y, intensity, null);
	}

	/**
	 * Plots an anti-aliased point on the value array, with the
	 * given intensity.
	 * 
	 * @param x:   x position of the point
	 * @param y:   y position of the point
	 * @param val: manually set intensity for the point
	 */
	public void Dot(float xf, float yf, float val) {
		Dot(xf, yf, val, null);
	}

	/**
	 * Private Function
	 * Adds an anti-aliased point to the buffer hashmap, with the
	 * given intensity.
	 * 
	 * @param x:   x position of the point
	 * @param y:   y position of the point
	 * @param val: manually set intensity for the point
	 * @param map: currently open buffer
	 */
	private void Dot(float xf, float yf, float val, HashMap<Integer, Float> map) {
		Dot(xf, yf, val, map, false);
	}

	/**
	 * Private Function
	 * Adds or updates an anti-aliased point to the buffer hashmap,
	 * with the given intensity.
	 * 
	 * @param x:        x position of the point
	 * @param y:        y position of the point
	 * @param val:      manually set intensity for the point
	 * @param map:      currently open buffer
	 * @param additive: new points are replaced if false, added to existing values
	 *                  if true
	 */
	private void Dot(float xf, float yf, float val, HashMap<Integer, Float> map, boolean additive) {
		ready = false;
		if (Float.isNaN(val))
			return;

		int x0, y0, x1, y1;

		x0 = PApplet.floor(xf);
		y0 = PApplet.floor(yf);

		x1 = x0 + 1;
		y1 = y0 + 1;

		float xr = xf - x0;
		float yr = yf - y0;

		int p00 = y0 * w + x0, p01 = y0 * w + x1, p11 = y1 * w + x1, p10 = y1 * w + x0;

		float v00 = (1 - xr) * (1 - yr) * val;
		float v01 =      xr  * (1 - yr) * val;
		float v11 =      xr  *      yr  * val;
		float v10 = (1 - xr) *      yr  * val;

		if (map == null) {
			if(carve) { v00 *= -1; v01 *= -1; v11 *= -1; v10 *= -1; }

			if (x0 >= 0 && x0 < w && y0 >= 0 && y0 < h) { values[p00] += v00; if(values[p00] < 0) values[p00] = 0; }
			if (x1 >= 0 && x1 < w && y0 >= 0 && y0 < h) { values[p01] += v01; if(values[p01] < 0) values[p01] = 0; }
			if (x1 >= 0 && x1 < w && y1 >= 0 && y1 < h) { values[p11] += v11; if(values[p11] < 0) values[p11] = 0; }
			if (x0 >= 0 && x0 < w && y1 >= 0 && y1 < h) { values[p10] += v10; if(values[p10] < 0) values[p10] = 0; }			
		} else if (additive) {
			float v00P = map.containsKey(p00) ? map.get(p00) : 0;
			float v01P = map.containsKey(p01) ? map.get(p01) : 0;
			float v11P = map.containsKey(p11) ? map.get(p11) : 0;
			float v10P = map.containsKey(p10) ? map.get(p10) : 0;

			if (x0 >= 0 && x0 < w && y0 >= 0 && y0 < h) map.put(p00, v00 + v00P);
			if (x1 >= 0 && x1 < w && y0 >= 0 && y0 < h) map.put(p01, v01 + v01P);
			if (x1 >= 0 && x1 < w && y1 >= 0 && y1 < h) map.put(p11, v11 + v11P);
			if (x0 >= 0 && x0 < w && y1 >= 0 && y1 < h) map.put(p10, v10 + v10P);
		} else {
			if (x0 >= 0 && x0 < w && y0 >= 0 && y0 < h && (!map.containsKey(p00) || (map.get(p00) < v00))) map.put(p00, v00);
			if (x1 >= 0 && x1 < w && y0 >= 0 && y0 < h && (!map.containsKey(p01) || (map.get(p01) < v01))) map.put(p01, v01);
			if (x1 >= 0 && x1 < w && y1 >= 0 && y1 < h && (!map.containsKey(p11) || (map.get(p11) < v11))) map.put(p11, v11);
			if (x0 >= 0 && x0 < w && y1 >= 0 && y1 < h && (!map.containsKey(p10) || (map.get(p10) < v10))) map.put(p10, v10);
		}
	}


	/**
	 * Private Function
	 * Maps the the existing value array to 0 - 1 range.
	 */
	private void MapToOne() {
		if (!ready) {
			if (normalize) {
				min = Float.MAX_VALUE;
				max = Float.MIN_VALUE;

				for (int i = 0; i < len; i++) {
					if (max < values[i])
						max = values[i];
					if (min > values[i])
						min = values[i];
				}

				for (int i = 0; i < len; i++) {
					vals01[i] = (values[i] - min) / (max - min);
				}
			} else {
				for (int i = 0; i < len; i++) {
					vals01[i] = values[i] > 255 ? 1 : values[i] / 255f;
				}
			}
		}
		ready = true;
	}

	/**
	 * Sets whether shapes and images are centered at their respective x and y
	 * positions or positioned by their top left corner. Shapes and images are
	 * centered if the value is true.
	 * 
	 * @param state: the new centered value.
	 */
	public void SetCentered(boolean state) {
		centered = state;
		ready = false;
	}

	/**
	 * Sets whether the value array is normalized by mapping the lowest value to
	 * zero and the highest value to 1, or just clamped at 0 and 255. Values are
	 * normalized if value is true.
	 * 
	 * @param state: the new normalize value.
	 */
	public void SetNormalize(boolean state) {
		normalize = state;
		ready = false;
	}

	/**
	 * Sets whether the colors are input in RGB values or HSB values. Color space is
	 * HSB if the value is true.
	 * 
	 * @param state: the new HSB flag
	 */
	public void SetHSB(boolean state) {
		if (isHSB == state)
			return;

		isHSB = state;

		if (isHSB) {
			colorMin = ToHSB(colorMin);
			colorMax = ToHSB(colorMax);
		}
		else {
			colorMin = ToRGB(colorMin);
			colorMax = ToRGB(colorMax);			
		}
	}

	/**
	 * Sets the color that corresponds to zero. Default value is black. In RGB, the
	 * range for each channel is 255. In HSB, the range for hue is 360 and the range
	 * for saturation and brightness is 100.
	 * 
	 * @param ch0: value for channel 0 (either red or hue)
	 * @param ch1: value for channel 1 (either green or saturation)
	 * @param ch2: value for channel 2 (either blue or brightness)
	 */
	public void SetStartColor(float ch0, float ch1, float ch2) {
		colorMin[0] = ch0;
		colorMin[1] = ch1;
		colorMin[2] = ch2;
	}

	/**
	 * Sets the color that corresponds to one. Default value is white. In RGB, the
	 * range for each channel is 255. In HSB, the range for hue is 360 and the range
	 * for saturation and brightness is 100.
	 * 
	 * @param ch0: value for channel 0 (either red or hue)
	 * @param ch1: value for channel 1 (either green or saturation)
	 * @param ch2: value for channel 2 (either blue or brightness)
	 */
	public void SetTargetColor(float ch0, float ch1, float ch2) {
		colorMax[0] = ch0;
		colorMax[1] = ch1;
		colorMax[2] = ch2;		
	}

	/**
	 * Converts RGB values in 0 - 255 range to 
	 * HSB color values in 0 - 359 (H) 0 - 99 (SB) range.
	 * 
	 * @param rgb: RGB values as float array
	 * @return HSB values as float array
	 */
	public float[] ToHSB(float[] rgb) {
		float[] hsb = new float[3];
		float minRGB = PApplet.min(rgb[0], rgb[1], rgb[2]);
		float maxRGB = PApplet.max(rgb[0], rgb[1], rgb[2]);
		float d = maxRGB - minRGB;
		hsb[0] = 0;
		hsb[2] = maxRGB;

		if (maxRGB != 0)
			hsb[1] = d / maxRGB;
		else
			hsb[1] = 0;

		if (hsb[1] != 0) {
			if (rgb[0] == maxRGB) {
				hsb[0] = (rgb[1] - rgb[2]) / d;
			} else {
				if (rgb[1] == maxRGB) {
					hsb[0] = 2 + (rgb[2] - rgb[0]) / d;
				} else {
					if (rgb[2] == maxRGB) {
						hsb[0] = 4 + (rgb[0] - rgb[1]) / d;
					}
				}
			}
		} else {
			hsb[0] = -1;
		}

		hsb[0] = hsb[0] * 60;
		if (hsb[0] < 0)
			hsb[0] = hsb[0] + 360;
		hsb[1] = hsb[1] * 100;
		hsb[2] = hsb[2] * 100 / 255;

		return hsb;
	}

	/**
	 * Converts HSB values in 0 - 359 (H) and 0 - 99 (SB) range to 
	 * RGB color values in 0 - 255 range.
	 * 
	 * @param hsb: HSB values as float array
	 * @return RGB values as float array
	 */
	public float[] ToRGB(float[] hsb) {
		float[] rgb = new float[3];
		float maxRGB, d, h, s, b;

		h = hsb[0] / 60;
		if (h > 5)
			h -= 6;
		s = hsb[1] * 255 / 100;
		b = hsb[2] * 255 / 100;
		maxRGB = b;

		if (s == 0) {
			rgb[0] = 0;
			rgb[1] = 0;
			rgb[2] = 0;
		} else {
			d = s * maxRGB / 255;
			if (h > 3) {
				rgb[2] = maxRGB;
				if (h > 4) {
					rgb[1] = maxRGB - d;
					rgb[0] = (h - 4) * d + rgb[1];
				} else {
					rgb[0] = maxRGB - d;
					rgb[1] = rgb[0] - (h - 4) * d;
				}
			} else {
				if (h > 1) {
					rgb[1] = maxRGB;

					if (h > 2) {
						rgb[0] = maxRGB - d;
						rgb[2] = (h - 2) * d + rgb[0];
					} else {
						rgb[2] = maxRGB - d;
						rgb[0] = rgb[2] - (h - 2) * d;
					}
				} else {
					if (h > -1) {
						rgb[0] = maxRGB;
						if (h > 0) {
							rgb[2] = maxRGB - d;
							rgb[1] = h * d + rgb[2];
						} else {
							rgb[1] = maxRGB - d;
							rgb[2] = rgb[1] - h * d;
						}
					}
				}
			}
		}

		return rgb;
	}

	/**
	 * Plots the output image based on the recorded values and returns it.
	 * 
	 * @return PImage
	 */
	public PImage Output() {
		if (bufferOpen)
			ApplyBuffer();

		MapToOne();

		if (!bufferOpen)
			buffer.beginDraw();

		if (isHSB)
			buffer.colorMode(PApplet.HSB, 360, 100, 100);
		else
			buffer.colorMode(PApplet.RGB, 255, 255, 255);

		buffer.loadPixels();
		for (int i = 0; i < values.length; i++) {
			buffer.pixels[i] = buffer.color(colorMin[0] * (1 - vals01[i]) + colorMax[0] * vals01[i],
											colorMin[1] * (1 - vals01[i]) + colorMax[1] * vals01[i],
											colorMin[2] * (1 - vals01[i]) + colorMax[2] * vals01[i]);
		}
		buffer.updatePixels();
		buffer.endDraw();

		bufferOpen = false;

		return buffer;
	}

	/**
	 * Returns the values currently recorded in the value array, in 0-1 range.
	 * 
	 * @return float[]
	 */
	public float[] GetValues() {
		if (bufferOpen)
			ApplyBuffer();

		MapToOne();

		return vals01;
	}

	/**
	 * Returns the values currently recorded in the value array.
	 * 
	 * @return float[]
	 */
	public float[] GetValuesRaw() {
		if (bufferOpen)
			ApplyBuffer();

		return values;
	}
	
	/**
	 * Returns the maximum value in the value array
	 * 
	 * @return float
	 */
	public float GetMaxValue() {
		float maxValue = 0;
		for(float v : values) if(maxValue < v) maxValue = v;
		return maxValue;
	}

	/** DRAWING BY VALUES **/

	/**
	 * Plots a line from (x1, y1) to (x2, y2)
	 * 
	 * @param x1: starting x position
	 * @param y1: starting y position
	 * @param x2: starting x position
	 * @param y2: starting y position
	 */
	public void Line(float x1, float y1, float x2, float y2) {
		Line(x1, y1, x2, y2, null);
	}

	/**
	 * Records the points of a line from (x1, y1) to (x2, y2) on the given hashmap
	 * 
	 * The line drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 * 
	 * @param x1:  starting x position
	 * @param y1:  starting y position
	 * @param x2:  starting x position
	 * @param y2:  starting y position
	 * @param map: hashmap to record the list of points
	 */
	private void Line(float x1, float y1, float x2, float y2, HashMap<Integer, Float> map) {
		ready = false;
		x1 = PApplet.round(x1);
		y1 = PApplet.round(y1);
		x2 = PApplet.round(x2);
		y2 = PApplet.round(y2);

		float dx = PApplet.abs(x2 - x1);
		float dy = PApplet.abs(y2 - y1);
		float mag = PApplet.sqrt(dx * dx + dy * dy);
		HashMap<Integer, Float> bufferMap = (map == null ? null : new HashMap<Integer, Float>());

		float sx = x1 < x2 ? 1 : -1;
		float sy = y1 < y2 ? 1 : -1;

		float e, e2 = mag;

		boolean cont = true;

		dx /= mag;
		dy /= mag;
		float th = weight - 1;

		float val;
		float s = map == null ? intensity : 1;

		if (dx < dy) {
			x2 = (th / 2) / dy;
			x1 -= x2 * sx;
			e = x2 * dy - th / 2;

			while (cont) {
				val = (1 - e) * s;
				x2 = x1;
				Dot(x2, y1, val, bufferMap, true);

				for (e2 = dy - e - th; e2 + dy < 1; e2 += dy) {
					x2 += sx;
					Dot(x2, y1, s, bufferMap, true);
				}

				val = (1 - e2) * s;
				Dot(x2 + sx, y1, val, bufferMap, true);

				e += dx;
				if (e > 1) {
					e -= dy;
					x1 += sx;
				}

				cont = PApplet.abs(y1 - y2) > EPSILON;
				y1 += sy;
			}

			if (map != null)
				MergeMaps(bufferMap, map);
			return;
		}

		y2 = (th / 2) / dx;
		y1 -= y2 * sy;
		e = y2 * dx - th / 2;

		while (cont) {
			y2 = y1;
			val = (1 - e) * s;
			Dot(x1, y2, val, bufferMap, true);

			for (e2 = dx - e - th; e2 + dx < 1; e2 += dx) {
				y2 += sy;
				Dot(x1, y2, s, bufferMap, true);
			}

			val = (1 - e2) * s;
			Dot(x1, y2 + sy, val, bufferMap, true);

			e += dy;
			if (e > 1) {
				e -= dx;
				y1 += sy;
			}

			cont = PApplet.abs(x1 - x2) > EPSILON;
			x1 += sx;
		}

		if (map != null)
			MergeMaps(bufferMap, map);
	}

	/* QuadBezier */
	/**
	 * Draws a Quadratic Bezier from (x0, y0) to (x2, y2), with the control point at
	 * (x1, y1)
	 * 
	 * @param x0: starting x position
	 * @param y0: starting y position
	 * @param x1: control point x
	 * @param y1: control point y
	 * @param x2: end x position
	 * @param y2: end y position
	 */
	public void QuadraticBezier(float x0, float y0, float x1, float y1, float x2, float y2) {
		QuadraticRationalBezier(x0, y0, x1, y1, x2, y2, 1);
	}

	/**
	 * Draws a Quadratic Rational Bezier from (x0, y0) to (x2, y2), with the control
	 * point at (x1, y1) and weight w for the control point
	 * 
	 * The quadratic rational bezier drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 *
	 * @param x0:      starting x position
	 * @param y0:      starting y position
	 * @param x1:      control point x
	 * @param y1:      control point y
	 * @param x2:      end x position
	 * @param y2:      end y position
	 * @param bweight: weight of the control point
	 */
	public void QuadraticRationalBezier(float x0, float y0, float x1, float y1, float x2, float y2, float bweight) {
		x0 = PApplet.round(x0);
		y0 = PApplet.round(y0);
		x1 = PApplet.round(x1);
		y1 = PApplet.round(y1);
		x2 = PApplet.round(x2);
		y2 = PApplet.round(y2);

		ready = false;
		HashMap<Integer, Float> map;
		map = new HashMap<Integer, Float>();

		float x = x0 - 2 * x1 + x2, y = y0 - 2 * y1 + y2;
		float xx = x0 - x1, yy = y0 - y1;
		float ww, t, q;

		if (bweight < 0.0f)
			return;

		if (xx * (x2 - x1) > 0) {
			if (yy * (y2 - y1) > 0) {
				if (PApplet.abs(xx * y) > PApplet.abs(yy * x)) {
					x0 = x2;
					x2 = xx + x1;
					y0 = y2;
					y2 = yy + y1;
				}
			}

			if (x0 == x2 || bweight == 1.0f) {
				t = (x0 - x1) / x;
			} else {
				q = PApplet.sqrt(4.0f * bweight * bweight * (x0 - x1) * (x2 - x1) + (x2 - x0) * (x2 - x0));
				if (x1 < x0)
					q = -q;

				t = (2.0f * bweight * (x0 - x1) - x0 + x2 + q) / (2.0f * (1.0f - bweight) * (x2 - x0));
			}

			q = 1.0f / (2.0f * t * (1.0f - t) * (bweight - 1.0f) + 1.0f);

			xx = (t * t * (x0 - 2.0f * bweight * x1 + x2) + 2.0f * t * (bweight * x1 - x0) + x0) * q;
			yy = (t * t * (y0 - 2.0f * bweight * y1 + y2) + 2.0f * t * (bweight * y1 - y0) + y0) * q;
			ww = t * (bweight - 1.0f) + 1.0f;
			ww *= ww * q;
			bweight = ((1.0f - t) * (bweight - 1.0f) + 1.0f) * PApplet.sqrt(q);
			x = PApplet.floor(xx + 0.5f);
			y = PApplet.floor(yy + 0.5f);
			yy = (xx - x0) * (y1 - y0) / (x1 - x0) + y0;

			QuadraticBezierSegment(x0, y0, x, PApplet.floor(yy + 0.5f), x, y, ww, map);

			yy = (xx - x2) * (y1 - y2) / (x1 - x2) + y2;
			y1 = PApplet.floor(yy + 0.5f);
			x0 = x1 = x;
			y0 = y;
		}

		if ((y0 - y1) * (y2 - y1) > 0) {
			if (y0 == y2 || bweight == 1.0f) {
				t = (y0 - y1) / (y0 - 2.0f * y1 + y2);
			} else {
				q = PApplet.sqrt(4.0f * bweight * bweight * (y0 - y1) * (y2 - y1) + (y2 - y0) * (y2 - y0));
				if (y1 < y0)
					q = -q;

				t = (2.0f * bweight * (y0 - y1) - y0 + y2 + q) / (2.0f * (1.0f - bweight) * (y2 - y0));
			}

			q = 1.0f / (2.0f * t * (1.0f - t) * (bweight - 1.0f) + 1.0f);
			xx = (t * t * (x0 - 2.0f * bweight * x1 + x2) + 2.0f * t * (bweight * x1 - x0) + x0) * q;
			yy = (t * t * (y0 - 2.0f * bweight * y1 + y2) + 2.0f * t * (bweight * y1 - y0) + y0) * q;
			ww = t * (bweight - 1.0f) + 1.0f;
			ww *= ww * q;

			bweight = ((1.0f - t) * (bweight - 1.0f) + 1.0f) * PApplet.sqrt(q);
			x = PApplet.floor(xx + 0.5f);
			y = PApplet.floor(yy + 0.5f);

			xx = (x1 - x0) * (yy - y0) / (y1 - y0) + x0;

			QuadraticBezierSegment(x0, y0, PApplet.floor(xx + 0.5f), y, x, y, ww, map);

			xx = (x1 - x2) * (yy - y2) / (y1 - y2) + x2;
			x1 = PApplet.floor(xx + 0.5f);
			x0 = x;
			y0 = y1 = y;
		}

		QuadraticBezierSegment(x0, y0, x1, y1, x2, y2, bweight * bweight, map);

		ApplyHashMap(map);
	}

	/**
	 * Private Function
	 * Records the points of a segment of a quadratic bezier, used by the
	 * QuadratcRationalBezier function
	 *
	 * The quadratic rational bezier drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 * 
	 * @param x0:      starting x position
	 * @param y0:      starting y position
	 * @param x1:      control point x
	 * @param y1:      control point y
	 * @param x2:      end x position
	 * @param y2:      end y position
	 * @param bweight: weight of the control point
	 * @param map:     hashmap to record the list of points
	 */
	private void QuadraticBezierSegment(float x0, float y0, float x1, float y1, float x2, float y2, float bweight, HashMap<Integer, Float> map) {
		ready = false;
		float th = weight;
		float sx = x2 - x1, sy = y2 - y1;
		float dx = x0 - x2, dy = y0 - y2;
		float xx = x0 - x1, yy = y0 - y1;

		float xy = xx * sy + yy * sx;
		float cur = xx * sy - yy * sx;
		float err, e2, ed;

		if (xx * sx > 0.0f || yy * sy > 0.0f)
			return;

		if (cur != 0.0f && bweight > 0.0f) {
			if (sx * sx + sy * sy > xx * xx + yy * yy) {
				x2 = x0;
				x0 -= dx;
				y2 = y0;
				y0 -= dy;
				cur = -cur;
			}

			xx = 2.0f * (4.0f * bweight * sx * xx + dx * dx);
			yy = 2.0f * (4.0f * bweight * sy * yy + dy * dy);

			sx = x0 < x2 ? 1 : -1;
			sy = y0 < y2 ? 1 : -1;

			xy = -2.0f * sx * sy * (2.0f * bweight * xy + dx * dy);

			if (cur * sx * sy < 0) {
				xx = -xx;
				yy = -yy;
				cur = -cur;
				xy = -xy;
			}

			dx = 4.0f * bweight * (x1 - x0) * sy * cur + xx / 2.0f;
			dy = 4.0f * bweight * (y0 - y1) * sx * cur + yy / 2.0f;

			if (bweight < 0.5f && (dx + xx <= 0 || dy + yy >= 0)) {
				cur = (bweight + 1.0f) / 2.0f;
				bweight = PApplet.sqrt(bweight);
				xy = 1.0f / (bweight + 1.0f);

				sx = PApplet.floor((x0 + 2.0f * bweight * x1 + x2) * xy / 2.0f + 0.5f);
				sy = PApplet.floor((y0 + 2.0f * bweight * y1 + y2) * xy / 2.0f + 0.5f);

				dx = PApplet.floor((bweight * x1 + x0) * xy + 0.5f);
				dy = PApplet.floor((y1 * bweight + y0) * xy + 0.5f);
				QuadraticBezierSegment(x0, y0, dx, dy, sx, sy, cur, map);

				dx = PApplet.floor((bweight * x1 + x2) * xy + 0.5f);
				dy = PApplet.floor((y1 * bweight + y2) * xy + 0.5f);
				QuadraticBezierSegment(sx, sy, dx, dy, x2, y2, cur, map);

				return;
			}

			fail: for (err = 0; dy + 2 * yy < 0 && dx + 2 * xx > 0;) {
				if (dx + dy + xy < 0) {
					do {
						ed = -dy - 2 * dy * dx * dx / (4.f * dy * dy + dx * dx);
						bweight = (th - 1) * ed;

						x1 = PApplet.floor((err - ed - bweight / 2) / dy);
						e2 = err - x1 * dy - bweight / 2;

						x1 = x0 - x1 * sx;
						Dot(x1, y0, 1 - e2 / ed, map);

						for (e2 = -bweight - dy - e2; e2 - dy < ed; e2 -= dy)
							Dot(x1 += sx, y0, 1, map);

						Dot(x1 + sx, y0, 1 - e2 / ed, map);
						if (PApplet.abs(y0 - y2) < EPSILON)
							return;

						err += dx;
						y0 += sy;
						dy += xy;
						dx += xx;

						if (2 * err + dy > 0) {
							err += dy;
							x0 += sx;
							dx += xy;
							dy += yy;
						}

						if (x0 != x2 && (dx + 2 * xx <= 0 || dy + 2 * yy >= 0)) {
							if (PApplet.abs(y2 - y0) > PApplet.abs(x2 - x0))
								break fail;
							else
								break;
						}
					} while (dx + dy + xy < 0);

					for (cur = err - dy - bweight / 2, y1 = y0; cur < ed; y1 += sy, cur += dx) {
						for (e2 = cur, x1 = x0; e2 - dy < ed; e2 -= dy)
							Dot(x1 -= sx, y1, 1, map);

						Dot(x1 - sx, y1, 1 - e2 / ed, map);
					}
				} else {
					do {
						ed = dx + 2 * dx * dy * dy / (4.f * dx * dx + dy * dy);
						bweight = (th - 1) * ed;

						y1 = PApplet.floor((err + ed + bweight / 2) / dx);
						e2 = y1 * dx - bweight / 2 - err;
						y1 = y0 - y1 * sy;

						Dot(x0, y1, 1 - e2 / ed, map);
						for (e2 = dx - e2 - bweight; e2 + dx < ed; e2 += dx)
							Dot(x0, y1 += sy, 1, map);

						Dot(x0, y1 + sy, 1 - e2 / ed, map);

						if (PApplet.abs(x0 - x2) < EPSILON)
							return;

						x0 += sx;
						dx += xy;
						err += dy;
						dy += yy;

						if (2 * err + dx < 0) {
							y0 += sy;
							dy += xy;
							err += dx;
							dx += xx;
						}

						if (y0 != y2 && (dx + 2 * xx <= 0 || dy + 2 * yy >= 0)) {
							if (PApplet.abs(y2 - y0) <= PApplet.abs(x2 - x0))
								break fail;
							else
								break;
						}
					} while (dx + dy + xy >= 0);

					for (cur = -err + dx - bweight / 2, x1 = x0; cur < ed; x1 += sx, cur -= dy) {
						for (e2 = cur, y1 = y0; e2 + dx < ed; e2 += dx)
							Dot(x1, y1 -= sy, 1, map);

						Dot(x1, y1 - sy, 1 - e2 / ed, map);
					}
				}
			}
		}
		Line(x0, y0, x2, y2, map);
	}

	/* Cubic Bezier */

	/**
	 * Draws a cubic bezier from (x0, x0) to (x3, y3) with control points (x1, y1)
	 * and (x2, y2)
	 * 
	 * The cubic bezier drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 *
	 * @param x0: starting point of the curve
	 * @param y0: starting point of the curve
	 * @param x1: control point 1
	 * @param y1: control point 1
	 * @param x2: control point 2
	 * @param y2: control point 2
	 * @param x3: end point of the curve
	 * @param y3: end point of the curve
	 */
	public void CubicBezier(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3) {
		x0 = PApplet.round(x0);
		y0 = PApplet.round(y0);
		x1 = PApplet.round(x1);
		y1 = PApplet.round(y1);
		x2 = PApplet.round(x2);
		y2 = PApplet.round(y2);
		x3 = PApplet.round(x3);
		y3 = PApplet.round(y3);
		
		ready = false;
		HashMap<Integer, Float> map = new HashMap<Integer, Float>();

		int n = 0;
		float i = 0;
		float xc = x0 + x1 - x2 - x3, xa = xc - 4 * (x1 - x2);
		float xb = x0 - x1 - x2 + x3, xd = xb + 4 * (x1 + x2);
		float yc = y0 + y1 - y2 - y3, ya = yc - 4 * (y1 - y2);
		float yb = y0 - y1 - y2 + y3, yd = yb + 4 * (y1 + y2);
		float fx0 = x0, fx1, fx2, fx3, fy0 = y0, fy1, fy2, fy3;
		float t1 = xb * xb - xa * xc, t2;
		float[] t = new float[7];

		if (xa == 0) {
			if (PApplet.abs(xc) < 2 * PApplet.abs(xb))
				t[n++] = xc / (2.0f * xb);
		} else if (t1 > 0.0f) {
			t2 = PApplet.sqrt(t1);
			t1 = (xb - t2) / xa;
			if (PApplet.abs(t1) < 1.0f)
				t[n++] = t1;

			t1 = (xb + t2) / xa;
			if (PApplet.abs(t1) < 1.0f)
				t[n++] = t1;
		}

		t1 = yb * yb - ya * yc;
		if (ya == 0) {
			if (PApplet.abs(yc) < 2 * PApplet.abs(yb))
				t[n++] = yc / (2.0f * yb);
		} else if (t1 > 0.0f) {
			t2 = PApplet.sqrt(t1);
			t1 = (yb - t2) / ya;
			if (PApplet.abs(t1) < 1.0f)
				t[n++] = t1;
			t1 = (yb + t2) / ya;
			if (PApplet.abs(t1) < 1.0f)
				t[n++] = t1;
		}
		t1 = 2 * (xa * yb - xb * ya);
		t2 = xa * yc - xc * ya;
		i = t2 * t2 - 2 * t1 * (xb * yc - xc * yb);

		if (i > 0) {
			i = PApplet.sqrt(i);
			t[n] = (t2 + i) / t1;
			if (PApplet.abs(t[n]) < 1.0f)
				n++;
			t[n] = (t2 - i) / t1;
			if (PApplet.abs(t[n]) < 1.0f)
				n++;
		}

		for (int j = 1; j < n; j++) {
			if ((t1 = t[j - 1]) > t[j]) {
				t[j - 1] = t[j];
				t[j] = t1;
				j = 0;
			}
		}

		t1 = -1.0f;
		t[n] = 1.0f;

		for (int j = 0; j <= n; j++) {
			t2 = t[j];
			fx1 = (t1 * (t1 * xb - 2 * xc) - t2 * (t1 * (t1 * xa - 2 * xb) + xc) + xd) / 8 - fx0;
			fy1 = (t1 * (t1 * yb - 2 * yc) - t2 * (t1 * (t1 * ya - 2 * yb) + yc) + yd) / 8 - fy0;
			fx2 = (t2 * (t2 * xb - 2 * xc) - t1 * (t2 * (t2 * xa - 2 * xb) + xc) + xd) / 8 - fx0;
			fy2 = (t2 * (t2 * yb - 2 * yc) - t1 * (t2 * (t2 * ya - 2 * yb) + yc) + yd) / 8 - fy0;
			fx0 -= fx3 = (t2 * (t2 * (3 * xb - t2 * xa) - 3 * xc) + xd) / 8;
			fy0 -= fy3 = (t2 * (t2 * (3 * yb - t2 * ya) - 3 * yc) + yd) / 8;
			x3 = PApplet.floor(fx3 + 0.5f);
			y3 = PApplet.floor(fy3 + 0.5f);
			if (fx0 != 0.0f) {
				fx1 *= fx0 = (x0 - x3) / fx0;
				fx2 *= fx0;
			}
			if (fy0 != 0.0f) {
				fy1 *= fy0 = (y0 - y3) / fy0;
				fy2 *= fy0;
			}

			if (PApplet.abs(x0 - x3) > EPSILON || PApplet.abs(y0 - y3) > EPSILON)
				CubicBezierSegment(x0, y0, x0 + fx1, y0 + fy1, x0 + fx2, y0 + fy2, x3, y3, map);

			x0 = x3;
			y0 = y3;
			fx0 = fx3;
			fy0 = fy3;
			t1 = t2;
		}

		ApplyHashMap(map);
	}

	/**
	 * Private Function
	 * Records the points of a cubic bezier from (x0, x0) to (x3,
	 * y3) with control points (x1, y1) and (x2, y2)
	 * 
	 * The cubic bezier segment drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param x3
	 * @param y3
	 * @param map
	 */
	private void CubicBezierSegment(float x0, float y0, float x1, float y1, float x2, float y2, float x3, float y3, HashMap<Integer, Float> map) {
		ready = false;
		float x = PApplet.floor((x0 + 3 * x1 + 3 * x2 + x3 + 4) / 8);
		float y = PApplet.floor((y0 + 3 * y1 + 3 * y2 + y3 + 4) / 8);

		QuadraticBezierSegment(x0, y0, PApplet.floor((x0 + 3 * x1 + 2) / 4), PApplet.floor((y0 + 3 * y1 + 2) / 4), x, y, 1, map);
		QuadraticBezierSegment(x, y, PApplet.floor((3 * x2 + x3 + 2) / 4), PApplet.floor((3 * y2 + y3 + 2) / 4), x3, y3, 1, map);
	}

	/* Shapes */
	/**
	 * Draws a rectangle at (x, y) with the size (sizex, sizey)
	 * 
	 * @param x:     x anchor point of the rectangle
	 * @param y:     y anchor point of the rectangle
	 * @param sizex: width of the rectangle
	 * @param sizey: height of the rectangle
	 */
	public void Rectangle(float x, float y, float sizex, float sizey) {
		ready = false;
		HashMap<Integer, Float> map = new HashMap<Integer, Float>();

		if (centered) {
			x = PApplet.round(x - sizex / 2);
			y = PApplet.round(y - sizey / 2);
		} else {
			x = PApplet.round(x);
			y = PApplet.round(y);
		}

		sizex = PApplet.round(sizex);
		sizey = PApplet.round(sizey);

		float o1 = weight / 2 - (weight + 1) % 2;
		float o2 = o1 - weight % 2;

		float x2 = x + sizex;
		float y2 = y + sizey;

		if (y > y2) {
			float t = y2;
			y2 = y;
			y = t;
		}
		if (x > x2) {
			float t = x2;
			x2 = x;
			x = t;
		}

		Line(x - o1, y, x2 + o2, y, map);
		Line(x - o1, y2, x2 + o2, y2, map);

		Line(x, y - o1, x, y2 + o2, map);
		Line(x2, y - o1, x2, y2 + o2, map);

		ApplyHashMap(map);
	}

	/**
	 * Draws an ellipse at (x, y) with the size (sizex, sizey)
	 * 
	 * The ellipse drawing algorithm is taken from:
	 * http://members.chello.at/~easyfilter/bresenham.html
	 *
	 * @param x:     x anchor point of the ellipse
	 * @param y:     y anchor point of the ellipse
	 * @param sizex: width of the ellipse
	 * @param sizey: height of the ellipse
	 */
	public void Ellipse(float x, float y, float sizex, float sizey) {
		ready = false;
		HashMap<Integer, Float> map = new HashMap<Integer, Float>();

		if (centered) {
			x = PApplet.round(x - sizex / 2);
			y = PApplet.round(y - sizey / 2);
		} else {
			x = PApplet.round(x);
			y = PApplet.round(y);
		}

		float x1 = PApplet.round(x + sizex);
		float y1 = PApplet.round(y + sizey);

		float th = weight;
		float a = PApplet.abs(x1 - x);
		float b = PApplet.abs(y1 - y);
		float b1 = PApplet.round(b) % 2;
		float a2 = a - 2 * th, b2 = b - 2 * th;
		float dx = 4 * (a - 1) * b * b, dy = 4 * (b1 - 1) * a * a;
		float i = a + b2, err = b1 * a * a, dx2, dy2, e2, ed;

		if ((th - 1) * (2 * b - th) > a * a)
			b2 = PApplet.sqrt(a * (b - a) * i * a2) / (a - th);
		if ((th - 1) * (2 * a - th) > b * b) {
			a2 = PApplet.sqrt(b * (a - b) * i * b2) / (b - th);
			th = (a - a2) / 2;
		}
		if (a == 0 || b == 0) {
			Line(x, y, x1, y1, map);
			return;
		}

		if (x > x1) {
			x = x1;
			x1 += a;
		}

		if (y > y1)
			y = y1;
		if (b2 <= 0)
			th = a;

		e2 = th - PApplet.floor(th);
		th = x + th - e2;

		dx2 = 4 * (a2 + 2 * e2 - 1) * b2 * b2;
		dy2 = 4 * (b1 - 1) * a2 * a2;
		e2 = dx2 * e2;

		y += PApplet.floor((b + 1) / 2);
		y1 = y - b1;
		a = 8 * a * a;
		b1 = 8 * b * b;
		a2 = 8 * a2 * a2;
		b2 = 8 * b2 * b2;

		do {
			for (;;) {
				if (err < 0 || x > x1) {
					i = x;
					break;
				}

				i = PApplet.min(dx, dy);
				ed = PApplet.max(dx, dy);

				if (y == y1 + 1 && 2 * err > dx && a > b1)
					ed = a / 4;
				else
					ed += 2 * ed * i * i / (4 * ed * ed + i * i + 1) + 1;

				i = 1 - err / ed;
				Dot(x, y, i, map);
				Dot(x, y1, i, map);
				Dot(x1, y, i, map);
				Dot(x1, y1, i, map);

				if (err + dy + a < dx) {
					i = x + 1;
					break;
				}
				x++;
				x1--;
				err -= dx;
				dx -= b1;
			}

			for (; i < th && 2 * i <= x + x1; i++) {
				Dot(i, y, 1, map);
				Dot(x + x1 - i, y, 1, map);
				Dot(i, y1, 1, map);
				Dot(x + x1 - i, y1, 1, map);
			}

			while (e2 > 0 && x + x1 >= 2 * th) {
				i = PApplet.min(dx2, dy2);
				ed = PApplet.max(dx2, dy2);

				if (y == y1 + 1 && 2 * e2 > dx2 && a2 > b2)
					ed = a2 / 4;
				else
					ed += 2 * ed * i * i / (4 * ed * ed + i * i);

				i = e2 / ed;
				Dot(th, y, i, map);
				Dot(x + x1 - th, y, i, map);
				Dot(th, y1, i, map);
				Dot(x + x1 - th, y1, i, map);

				if (e2 + dy2 + a2 < dx2)
					break;

				th++;
				e2 -= dx2;
				dx2 -= b2;
			}

			e2 += dy2 += a2;
			y++;
			y1--;
			err += dy += a;
		} while (x < x1);

		if (y - y1 <= b) {
			if (err > dy + a) {
				y--;
				y1++;
				err -= dy -= a;
			}

			for (; y - y1 <= b; err += dy += a) {
				i = 4 * err / b1;
				Dot(x, y, i, map);
				Dot(x1, y++, i, map);
				Dot(x, y1, i, map);
				Dot(x1, y1--, i, map);
			}
		}

		ApplyHashMap(map);
	}
	
	/**
	 * Draws a circle at (x, y) with the radius (radius)
	 * 
	 * @param x:      x anchor point of the circle
	 * @param y:      y anchor point of the circle
	 * @param radius: radius of the circle
	 */
	public void Circle(float x, float y, float radius) {
		Ellipse(x, y, radius * 2, radius * 2);
	}

	/* Image */

	/**
	 * Renders an image at (0,0) in its original size, mapped to the set intensity
	 * 
	 * @param img: source image to be drawn
	 */
	public void Image(PImage img) {
		Image(img, 0, 0, w, h, true);
	}

	/**
	 * Renders an image at (x,y) in its original size, mapped to the set intensity
	 * 
	 * @param img: source image to be drawn
	 * @param x:   x anchor
	 * @param y:   y anchor
	 */
	public void Image(PImage img, float x, float y) {
		ready = false;
		img.loadPixels();

		if (centered) {
			x -= img.width / 2;
			y -= img.height / 2;
		}

		for (int i = 0; i < img.height; i++) {
			for (int j = 0; j < img.width; j++) {
				int c = img.pixels[i * img.width + j];
				float val = intensity * ((c >> 16 & 0xFF) + (c >> 8 & 0xFF) + (c & 0xFF)) / 765;
				Dot(j + x, i + y, val);
			}
		}
	}

	/**
	 * Renders an image at (x,y) with the size (sizex, sizey), mapped to the set
	 * intensity
	 *
	 * @param img:   source image to be drawn
	 * @param x:     x anchor
	 * @param y:     y anchor
	 * @param sizex: width of the image
	 * @param sizey: height of the image
	 */
	public void Image(PImage img, float x, float y, float sizex, float sizey) {
		Image(img, x, y, sizex, sizey, false);
	}

	/**
	 * Renders an image at (x,y) with the size (sizex, sizey), mapped to the set
	 * intensity
	 *
	 * @param img:   source image to be drawn
	 * @param x:     x anchor
	 * @param y:     y anchor
	 * @param sizex: width of the image
	 * @param sizey: height of the image
	 * @param forceCorner: overrides centered.
	 */
	private void Image(PImage img, float x, float y, float sizex, float sizey, boolean forceCorner) {
		ready = false;
		img.loadPixels();

		sizex = PApplet.floor(sizex);
		sizey = PApplet.floor(sizey);

		if (centered && !forceCorner) {
			x -= sizex / 2;
			y -= sizey / 2;
		}

		for (int i = 0; i < sizey; i++) {
			for (int j = 0; j < sizex; j++) {
				float pxf = (img.width - 1) * j / (sizex - 1);
				float pyf = (img.height - 1) * i / (sizey - 1);

				int px = PApplet.floor(pxf), py = PApplet.floor(pyf);
				int pxn = px == img.width - 1 ? px : px + 1;
				int pyn = py == img.height - 1 ? py : py + 1;

				float rx = pxf - px, ry = pyf - py;

				int c00 = img.pixels[py * img.width + px];
				int c10 = img.pixels[py * img.width + pxn];
				int c01 = img.pixels[pyn * img.width + px];
				int c11 = img.pixels[pyn * img.width + pxn];

				float r00 = (1 - rx) * (1 - ry);
				float r01 = (1 - rx) * (ry);
				float r11 = (rx) * (ry);
				float r10 = (rx) * (1 - ry);

				float val = intensity * (r00 * ((c00 >> 16 & 0xFF) + (c00 >> 8 & 0xFF) + (c00 & 0xFF))
						+ r01 * ((c01 >> 16 & 0xFF) + (c01 >> 8 & 0xFF) + (c01 & 0xFF))
						+ r11 * ((c11 >> 16 & 0xFF) + (c11 >> 8 & 0xFF) + (c11 & 0xFF))
						+ r10 * ((c10 >> 16 & 0xFF) + (c10 >> 8 & 0xFF) + (c10 & 0xFF))) / 765;

				Dot(j + x, i + y, val);
			}
		}
	}
	

	/** BUFFERED PAINTING **/
	/**
	 * In case the given drawing functions are not enough, it is possible to use the
	 * buffer pgraphics object to draw with Processing's own methods and apply that
	 * to the values array.
	 * 
	 * This functions begins draw on the buffer canvas and returns it.
	 * 
	 * @return PGraphics objects to draw on
	 */
	public PGraphics GetBufferCanvas() {
		BeginBufferDraw();
		return buffer;
	}

	/**
	 * Begins drawing on the buffer context
	 */
	public void BeginBufferDraw() {
		if (bufferOpen)
			return;

		buffer.beginDraw();
		buffer.background(0);
		bufferOpen = true;
	}

	/**
	 * Ends drawing on the buffer context and applies it to the values array.
	 */
	public void ApplyBuffer() {
		if (!bufferOpen)
			return;
		ready = false;

		buffer.endDraw();
		bufferOpen = false;
		buffer.loadPixels();
		
		for (int i = 0; i < len; i++) {
			int c = buffer.pixels[i];
			float val = values[i] + (intensity * ((c >> 16 & 0xFF) + (c >> 8 & 0xFF) + (c & 0xFF)) / 765) * (carve ? -1 : 1);
			
			values[i] = val < 0 ? 0 : val;
		}
	}
}
