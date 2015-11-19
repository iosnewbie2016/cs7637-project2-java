package ravensproject;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FractalsCommand {

  private BufferedImage source;
  private BufferedImage destination;
  private int abstractionLevel;

  public FractalsCommand(BufferedImage source, BufferedImage destination, int abstractionLevel) {
    this.source = source;
    this.destination = destination;
    this.abstractionLevel = abstractionLevel;
  }

  public Set<FractalRepresentation> execute() {
    Set<FractalRepresentation> fractals = new HashSet<>();
    
    // Partition images into a grid of image fragments
    BufferedImage[][] sourceFragments = partitionImage(source, abstractionLevel);
    BufferedImage[][] destinationFragments = partitionImage(destination, abstractionLevel);
    Map<Pair<Integer, Integer>, int[][]> sourceFragmentPixelMatrices = new HashMap<>();
    Map<Pair<Integer, Integer>, int[][]> destinationFragmentPixelMatrices = new HashMap<>();
    // Transformation name as key and transformed image as value
    Map<String, int[][]> transformations = new LinkedHashMap<>();
    // Transformation name as key and correspondence score as value
    Map<String, Double> transformCorrespondences = new LinkedHashMap<>();
    // Fragment location as key and transformation as value
    Map<Pair<Integer, Integer>, String> fragmentTransformations = new LinkedHashMap<>();
    // Fragment location as key and correspondence score as value
    Map<Pair<Integer, Integer>, Double> fragmentCorrespondences = new LinkedHashMap<>();
    
    // Create pixel matrix for image fragments
    for (int i = 0; i < destinationFragments.length; i++) {
      for (int j = 0; j < destinationFragments.length; j++) {
        sourceFragmentPixelMatrices.put(new Pair<>(i, j) ,getGrayscalePixelMatrix(sourceFragments[i][j]));
        destinationFragmentPixelMatrices.put(new Pair<>(i, j) ,getGrayscalePixelMatrix(destinationFragments[i][j]));
      }
    }

    // Iterate through destination image fragments
    for (int i = 0; i < destinationFragments.length; i++) {
      for (int j = 0; j < destinationFragments.length; j++) {
        // Iterate through source image fragments
        for (int x = 0; x < sourceFragments.length; x++) {
          for (int y = 0; y < sourceFragments.length; y++) {
            // Get each affine transformation for the source image fragment
            // Identity
            transformations.put("identity", sourceFragmentPixelMatrices.get(new Pair<>(x, y)));
            // Horizontal flip
            transformations.put("hFlip", getGrayscalePixelMatrix(horizontalFlip(sourceFragments[x][y])));
            // Vertical flip
            transformations.put("vFlip", getGrayscalePixelMatrix(verticalFlip(sourceFragments[x][y])));
            // Rotate 90 degrees
            transformations.put("r90", getGrayscalePixelMatrix(rotateImage(sourceFragments[x][y], Math.PI / 2)));
            // Rotate 180 degrees
            transformations.put("r180", getGrayscalePixelMatrix(rotateImage(sourceFragments[x][y], Math.PI)));
            // Rotate 270 degrees
            transformations.put("r270", getGrayscalePixelMatrix(rotateImage(sourceFragments[x][y], 3 * Math.PI / 2)));
            // Reflect ynx
            transformations.put("reflYNX", getGrayscalePixelMatrix(rotate90Flip(sourceFragments[x][y])));
            // Reflect yx
            transformations.put("reflYX", getGrayscalePixelMatrix(rotate270Flip(sourceFragments[x][y])));

            // Get the correspondence value for each transformation
            for (Map.Entry<String, int[][]> transformation : transformations.entrySet()) {
              transformCorrespondences.put(
                  transformation.getKey(),
                  getCorrespondence(
                      transformation.getValue(), 
                      destinationFragmentPixelMatrices.get(new Pair<>(i, j)), 
                      new Pair<>(x, y), 
                      new Pair<>(i, j)
                  )
              );
            }

            // Find the transformation with the lowest correspondence value
            Map.Entry<String, Double> minimumTransform = null;
            for (Map.Entry<String, Double> entry : transformCorrespondences.entrySet()) {
              if (minimumTransform == null || minimumTransform.getValue() > entry.getValue()) {
                minimumTransform = entry;
              }
            }

            // Store the fragment with its transformation and correspondence value
            fragmentTransformations.put(new Pair<>(x, y), minimumTransform.getKey());
            fragmentCorrespondences.put(new Pair<>(x, y), minimumTransform.getValue());
          }
        }

        // Get the fragment with the lowest correspondence value
        Map.Entry<Pair<Integer, Integer>, Double> correspondingFragment = null;
        for (Map.Entry<Pair<Integer, Integer>, Double> entry : fragmentCorrespondences.entrySet()) {
          if (correspondingFragment == null || correspondingFragment.getValue() > entry.getValue()) {
            correspondingFragment = entry;
          }
        }

        // Create the Fractal Representation from the fractal codes
        FractalRepresentation fractal = new FractalRepresentation(
            correspondingFragment.getKey(),
            new Pair<>(i, j),
            fragmentTransformations.get(correspondingFragment.getKey()),
            destinationFragments[i][j].getHeight(),
            getColorContraction(
                sourceFragmentPixelMatrices.get(new Pair<>(
                    correspondingFragment.getKey().getElement0(), 
                    correspondingFragment.getKey().getElement1()
                )),
                destinationFragmentPixelMatrices.get(new Pair<>(i, j))
            )
        );

        // Store fractal representation
        fractals.add(fractal);
      }
    }
    
    return fractals;
  }

  /**
   * This method splits an image into a matrix of image fragments. Credit goes to
   * Kalani Ruwanpathrana for the original code:
   *
   * http://kalanir.blogspot.com/2010/02/how-to-split-image-into-chunks-java.html
   *
   * @param image
   * @param size
   * @return
   */
  public BufferedImage[][] partitionImage(BufferedImage image, int size) {
    int rows = size;
    int columns = size;

    // Determine the fragment width and height
    int fragmentWidth = image.getWidth() / columns;
    int fragmentHeight = image.getHeight() / rows;

    BufferedImage[][] images = new BufferedImage[rows][columns];
    for (int x = 0; x < rows; x++) {
      for (int y = 0; y < columns; y++) {
        // Initialize the image fragments in the array
        images[x][y] = new BufferedImage(fragmentWidth, fragmentHeight, image.getType());

        // Draw the image fragment
        Graphics2D graphic = images[x][y].createGraphics();
        graphic.drawImage(image, 0, 0, fragmentWidth, fragmentHeight, fragmentWidth * y, fragmentHeight * x,
            fragmentWidth * y + fragmentWidth, fragmentHeight * x + fragmentHeight, null
        );
        graphic.dispose();
      }
    }

    return images;
  }
  
  /**
   * Returns a matrix of pixel values, where 255 equals white and 0 equals black.
   *
   * Original code by blackSmith:
   * http://stackoverflow.com/questions/17278829/grayscale-bitmap-into-2d-array
   */
  public int[][] getGrayscalePixelMatrix(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    int[][] matrix = new int[width][height];

    Raster raster = image.getData();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        matrix[i][j] = raster.getSample(i, j, 0);
      }
    }

    return matrix;
  }
  
  /**
   * This method returns the color contraction for two images.
   *
   * @param a
   * @param b
   * @return
   */
  public double getColorContraction(int[][] pixelMatrixA, int[][] pixelMatrixB) {
    return 0.75 * (getColorMean(pixelMatrixB) - getColorMean(pixelMatrixA));
  }

  /**
   * This method returns the mean color of an image. Credit goes to Dan O for
   * the original code:
   *
   * http://stackoverflow.com/questions/12408431/how-can-i-get-the-average-colour-of-an-image
   *
   * @param image
   * @return
   */
  public int getColorMean(int[][] pixelMatrix) {
    long totalRed = 0;
    long totalGreen = 0;
    long totalBlue = 0;
    int pixels = pixelMatrix.length * pixelMatrix.length;

    for (int x = 0; x < pixelMatrix.length; x++) {
      for (int y = 0; y < pixelMatrix.length; y++) {
        // Get rgb values from pixel
        int pixel = pixelMatrix[x][y];
        int red = (pixel >> 16) & 0xFF;
        int green = (pixel >> 8) & 0xFF;
        int blue = pixel & 0xFF;

        totalRed += red;
        totalGreen += green;
        totalBlue += blue;
      }
    }

    // Scale the colors from 0-255 to 0.0-1.0
    float red = (totalRed / pixels) / 255f;
    float green = (totalGreen / pixels) / 255f;
    float blue = (totalBlue / pixels) / 255f;

    return new Color(red, green, blue).getRGB();
  }

  /**
   * This method returns the correspondence between two images.
   *
   * @param a
   * @param b
   * @param aFragmentOrigin
   * @param bFragmentOrigin
   * @return
   */
  public double getCorrespondence(
      int[][] pixelMatrixA, 
      int[][] pixelMatrixB,                    
      Pair<Integer, Integer> aFragmentOrigin, 
      Pair<Integer, Integer> bFragmentOrigin) {
    return 1 * getPhotometricCorrespondence(pixelMatrixA, pixelMatrixB) + 0.1 * getDistance(aFragmentOrigin, bFragmentOrigin);
  }

  /**
   * This method returns the photometric correspondence between two images.
   *
   * @param a
   * @param b
   * @return
   */
  public double getPhotometricCorrespondence(int[][] pixelMatrixA, int[][] pixelMatrixB) {
    double c = 0;

    for (int x = 0; x < pixelMatrixA.length; x++) {
      for (int y = 0; y < pixelMatrixA.length; y++) {
          // TODO remove after testing
//        c += Math.pow(getPhotometric(pixelMatrixB[x][y]) - getPhotometric(pixelMatrixA[x][y]), 2);
          c += Math.pow(pixelMatrixB[x][y] - pixelMatrixA[x][y], 2);
      }
    }

    return c;
  }

  /**
   * This method gets the photometric value for a pixel in the RGB color space.
   *
   * @param pixel
   * @return
   */
  public double getPhotometric(int pixel) {
    int red = (pixel >> 16) & 0xFF;
    int green = (pixel >> 8) & 0xFF;
    int blue = pixel & 0xFF;

    return 0.3 * red + 0.59 * green + 0.11 * blue;
  }

  /**
   * This method implements the distance formula for two ordered pairs.
   *
   * @param aFragmentOrigin
   * @param bFragmentOrigin
   * @return
   */
  public double getDistance(Pair<Integer, Integer> aFragmentOrigin, Pair<Integer, Integer> bFragmentOrigin) {
    return Math.sqrt(
        Math.pow(bFragmentOrigin.getElement0() - aFragmentOrigin.getElement0(), 2)
            + Math.pow(bFragmentOrigin.getElement1() - aFragmentOrigin.getElement1(), 2)
    );
  }

  /**
   * This method flips an image horizontally. Credit goes to Byron Kiourtzoglou
   * for the original code:
   *
   * http://examples.javacodegeeks.com/desktop-java/awt/image/flipping-a-buffered-image/
   *
   * @param image
   * @return
   */
  public BufferedImage horizontalFlip(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-image.getWidth(null), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    op.filter(image, result);

    return result;
  }

  /**
   * This method flips an image vertically. Credit goes to Byron Kiourtzoglou
   * for the original code:
   *
   * http://examples.javacodegeeks.com/desktop-java/awt/image/flipping-a-buffered-image/
   *
   * @param image
   * @return
   */
  public BufferedImage verticalFlip(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());

    AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
    tx.translate(0, -image.getHeight(null));
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    op.filter(image, result);

    return result;
  }

  /**
   * This method rotates an image about its center. Theta is a radian value:
   *
   * 90 degrees = pi / 2
   * 180 degrees = pi
   * 270 degrees = 3 * pi / 2
   *
   * @return The result of the transformation
   * @image The image to rotate
   * @theta A radian value for the rotation.
   */
  public BufferedImage rotateImage(BufferedImage image, double theta) {
    BufferedImage result = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());

        /* Transformations are stacked then applied so the last transformation is the first to happen */
    AffineTransform tx = new AffineTransform();

    tx.translate(image.getHeight() / 2, image.getWidth() / 2);
    tx.rotate(theta);
    tx.translate(-image.getWidth() / 2, -image.getHeight() / 2);

    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
    op.filter(image, result);

    return result;
  }

  /**
   * This method reflects an image over the y-axis and then rotates the
   * image 270 degrees.
   *
   * @param image
   * @return
   */
  public BufferedImage rotate90Flip(BufferedImage image) {
    BufferedImage result = null;

    // Rotate 90 degrees
    result = rotateImage(image, Math.PI / 2);
    // Reflect image over x axis
    result = verticalFlip(result);

    return result;
  }

  /**
   * This method reflects an image over the x-axis and then rotates the
   * image 270 degrees.
   *
   * @param image
   * @return
   */
  public BufferedImage rotate270Flip(BufferedImage image) {
    BufferedImage result = null;

    // Rotate 270 degrees
    result = rotateImage(image, 3 * Math.PI / 2);
    // Reflect image over x axis
    result = verticalFlip(result);

    return result;
  }
  
}
