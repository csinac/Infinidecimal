# Infinidecimal Canvas (2020)

Infinidecimal Canvas is a [Processing](https://processing.org) Library numeric canvas that stores the image as a number array to create visuals with arbitrary ranges.

Infinidecimal maintains an array of numbers to represent the canvas. It uses "intensity" to represent color. This way, the color resolution and range is as high as the resolution and range of a floating-point number.

Infinidecimal is tested in Processing v3.5.4 on macOS Mojave and v3.5.3 Windows 10 Pro

###### The following images are from the example sketches that come with the library.

When working with 8bits, there are certain limitations (well, 8 bits, to be precise) to the opacity and color resolution. This is especially limiting when you want to create visuals by drawing transparent object on top of each other. Infinidecimal Canvas is a solution for this problem.

The image below shows 99,999 lines drawn on each other. First one is drawn by processing's line function, at 1 / 255 opacity. The second image is the output of Infinidecimal. The third pane shows some of the values from Infinidecimal's intensity array.
![why though](https://rect.dev/media/infinidecimal/01_why.jpg)

###### Infinidecimal Canvas offers 4 curve types
![curves](https://rect.dev/media/infinidecimal/02_curves.jpg)

###### 3 shapes
![shapes](https://rect.dev/media/infinidecimal/03_shapes.jpg)

###### Image plotting
![image](https://rect.dev/media/infinidecimal/04_image.jpg)

###### Color mapping in RGB and HSB modes
![color](https://rect.dev/media/infinidecimal/05_color.jpg)

###### A proxy canvas to use Processing's own drawing functions
![buffered](https://rect.dev/media/infinidecimal/06_buffered.jpg)

###### And carving mode for negative intensities
![carve](https://rect.dev/media/infinidecimal/07_carve.jpg)

## Acknowledgement
The anti-aliased, thick line drawing algorithms (namely for line, quadratic bezier, quadratic rational bezier, cubic bezier and ellipse) are all taken directly from Alois Zingl's bresenham implementation, available [here](http://members.chello.at/easyfilter/bresenham.html).

## How to install GR Infinidecimal Canvas

### Install with the Contribution Manager

Add contributed Libraries by selecting the menu item _Sketch_ → _Import Library..._ → _Add Library..._ This will open the Contribution Manager, where you can browse for GR Infinidecimal Canvas, or any other Library you want to install.

Not all available Libraries have been converted to show up in this menu. If a Library isn't there, it will need to be installed manually by following the instructions below.

### Manual Install

Contributed Libraries may be downloaded separately and manually placed within the `libraries` folder of your Processing sketchbook. To find (and change) the Processing sketchbook location on your computer, open the Preferences window from the Processing application (PDE) and look for the "Sketchbook location" item at the top.

By default the following locations are used for your sketchbook folder: 
  * For Mac users, the sketchbook folder is located inside `~/Documents/Processing` 
  * For Windows users, the sketchbook folder is located inside `My Documents/Processing`

Download GR Infinidecimal Canvas from http://rect.dev/infinidecimal

Unzip and copy the contributed Library's folder into the `libraries` folder in the Processing sketchbook. You will need to create this `libraries` folder if it does not exist.

The folder structure for Library GR Infinidecimal Canvas should be as follows:

```
Processing
  libraries
    GR Infinidecimal Canvas
      examples
      library
        GR Infinidecimal Canvas.jar
      reference
      src
```
             
Some folders like `examples` or `src` might be missing. After Library GR Infinidecimal Canvas has been successfully installed, restart the Processing application.

### Troubleshooting

If you're having trouble, have a look at the [Processing Wiki](https://github.com/processing/processing/wiki/How-to-Install-a-Contributed-Library) for more information, or contact the author [C. Sina Cetin](http://rect.dev).