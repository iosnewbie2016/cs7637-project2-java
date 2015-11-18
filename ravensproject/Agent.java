package ravensproject;

// Uncomment these lines to access image processing.

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.imageio.ImageIO;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * <p/>
 * You may also create and submit new files in addition to modifying this file.
 * <p/>
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * <p/>
 * These methods will be necessary for the project's main method to run.
 */
public class Agent {
  /**
   * The default constructor for your Agent. Make sure to execute any
   * processing necessary before your Agent starts solving problems here.
   * <p/>
   * Do not add any variables to this signature; they will not be used by
   * main().
   */
  public Agent() {

  }

  /**
   * The primary method for solving incoming Raven's Progressive Matrices.
   * For each problem, your Agent's Solve() method will be called. At the
   * conclusion of Solve(), your Agent should return a String representing its
   * answer to the question: "1", "2", "3", "4", "5", or "6". These Strings
   * are also the Names of the individual RavensFigures, obtained through
   * RavensFigure.getName().
   * <p/>
   * In addition to returning your answer at the end of the method, your Agent
   * may also call problem.checkAnswer(String givenAnswer). The parameter
   * passed to checkAnswer should be your Agent's current guess for the
   * problem; checkAnswer will return the correct answer to the problem. This
   * allows your Agent to check its answer. Note, however, that after your
   * agent has called checkAnswer, it will *not* be able to change its answer.
   * checkAnswer is used to allow your Agent to learn from its incorrect
   * answers; however, your Agent cannot change the answer to a question it
   * has already answered.
   * <p/>
   * If your Agent calls checkAnswer during execution of Solve, the answer it
   * returns will be ignored; otherwise, the answer returned at the end of
   * Solve will be taken as your Agent's answer to this problem.
   *
   * @param problem the RavensProblem your agent should solve
   * @return your Agent's answer to this problem
   */
  public int[] abstractionLevels = {2};

  public int Solve(RavensProblem problem) {
    if (problem.getProblemType().equals("3x3")) {
      return executeFractalAlgorithmThreeByThree(problem);
    }
    else {
      return executeFractalAlgorithmTwoByTwo(problem);
    }
  }

  /**
   * Performs the fractal processing algorithm for a 2x2 RPM problem.
   *
   * @param problem The Raven's Problem to solve.
   * @return An integer number representing the agent's answer to the RPM problem.
   */
  public int executeFractalAlgorithmTwoByTwo(RavensProblem problem) {
    Map<String, BufferedImage> images = new HashMap<>();
    int abstraction = 0;
    int answer = -1;

    // Open all images for problem
    for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
      images.put(figure.getKey(), openImage(problem.getFigures().get(figure.getKey()).getVisual()));
    }

    while (answer == -1 && abstraction < abstractionLevels.length) {
      // Figure number as key and similarity score as value
      Map<Integer, Double> similarities = new HashMap<>();
      // Relationship sets
      Set<FractalRepresentation> horizontalRelations = new HashSet<>();
      Set<FractalRepresentation> verticalRelations = new HashSet<>();

      // Get horizontal relationships
      horizontalRelations.addAll(
          getMutualFractals(
              images.get("A"),
              images.get("B"),
              abstractionLevels[abstraction]
          )
      );
      // Get vertical relationships
      verticalRelations.addAll(
          getMutualFractals(
              images.get("A"),
              images.get("C"),
              abstractionLevels[abstraction]
          )
      );

      for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
        if (Character.isDigit(figure.getKey().charAt(0))) {
          // Answer relationship sets
          Set<FractalRepresentation> answerHorizontal = new HashSet<>();
          Set<FractalRepresentation> answerVertical = new HashSet<>();
          // Similarity vector
          double[] vector = new double[2];

          // Get horizontal relationships
          answerHorizontal.addAll(
              getMutualFractals(
                  images.get("C"),
                  images.get(figure.getKey()),
                  abstractionLevels[abstraction]
              )
          );
          // Get vertical relationships
          answerVertical.addAll(
              getMutualFractals(
                  images.get("B"),
                  images.get(figure.getKey()),
                  abstractionLevels[abstraction]
              )
          );

          vector[0] = calculateSimilarity(horizontalRelations, answerHorizontal);
          vector[1] = calculateSimilarity(verticalRelations, answerVertical);

          similarities.put(Integer.valueOf(figure.getKey()), calculateEuclideanDistance(vector));
        }
      }

      // Find the answer with the highest similarity value
      Map.Entry<Integer, Double> bestAnswer = null;
      for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
        if (bestAnswer == null || bestAnswer.getValue() < entry.getValue()) {
          bestAnswer = entry;
        }
      }

      // Return most confident answer
      answer = bestAnswer.getKey();

      // Normalize data for a range of 0.0-1.0
      for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
        entry.setValue(entry.getValue() / bestAnswer.getValue());
      }

      // Calculate mean
      double mean = mean(similarities.values());
      // Calculate standard deviation
      double standardDeviation = standardDeviation(similarities.values(), mean);
      // Calculate standard error
      double standardError = standardError(similarities.size(), standardDeviation);
      // Calculate getDeviations
      Map<Integer, Double> deviations = getDeviations(similarities, mean, standardError);

      // Get most confident answer
      double threshold = getConfidenceThreshold();
      List<Integer> answers = new ArrayList<>();
      for (Map.Entry<Integer, Double> deviation : deviations.entrySet()) {
        if (deviation.getValue() > threshold)
          answers.add(deviation.getKey());
      }

//      if (answers.size() == 1)
//        answer = answers.get(0);

      // Move to next level of abstraction
      abstraction++;
    }

    // Print out answer to problem
    System.out.println(problem.getName());
    System.out.println(answer);

    return answer;
  }

  /**
   * Performs the fractal processing algorithm for a 3x3 RPM problem.
   *
   * @param problem The Raven's Problem to solve.
   * @return An integer number representing the agent's answer to the RPM problem.
   */
  public int executeFractalAlgorithmThreeByThree(RavensProblem problem) {
    Map<String, BufferedImage> images = new HashMap<>();
    int abstraction = 0;
    int answer = -1;

    // Open all images for problem
    for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
      images.put(figure.getKey(), openImage(problem.getFigures().get(figure.getKey()).getVisual()));
    }

    while (answer == -1 && abstraction < abstractionLevels.length) {
      // Figure number as key and similarity score as value
      Map<Integer, Double> similarities = new HashMap<>();
      // Relationship sets
      Set<FractalRepresentation> horizontalRelations1 = new HashSet<>();
      Set<FractalRepresentation> horizontalRelations2 = new HashSet<>();
      Set<FractalRepresentation> verticalRelations1 = new HashSet<>();
      Set<FractalRepresentation> verticalRelations2 = new HashSet<>();

      // Get horizontal relationships
      horizontalRelations1.addAll(
          getMutualFractalsThreeByThree(
              images.get("A"),
              images.get("B"),
              images.get("C"),
              abstractionLevels[abstraction]
          )
      );
      horizontalRelations2.addAll(
          getMutualFractalsThreeByThree(
              images.get("D"),
              images.get("E"),
              images.get("F"),
              abstractionLevels[abstraction]
          )
      );
      // Get vertical relationships
      verticalRelations1.addAll(
          getMutualFractalsThreeByThree(
              images.get("A"),
              images.get("D"),
              images.get("G"),
              abstractionLevels[abstraction]
          )
      );
      verticalRelations2.addAll(
          getMutualFractalsThreeByThree(
              images.get("B"),
              images.get("E"),
              images.get("H"),
              abstractionLevels[abstraction]
          )
      );

      for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
        if (Character.isDigit(figure.getKey().charAt(0))) {
          // Answer relationship sets
          Set<FractalRepresentation> answerHorizontal = new HashSet<>();
          Set<FractalRepresentation> answerVertical = new HashSet<>();
          // Similarity vector
          double[] vector = new double[4];

          // Get horizontal relationships
          answerHorizontal.addAll(
              getMutualFractalsThreeByThree(
                  images.get("G"),
                  images.get("H"),
                  images.get(figure.getKey()),
                  abstractionLevels[abstraction]
              )
          );
          // Get vertical relationships
          answerVertical.addAll(
              getMutualFractalsThreeByThree(
                  images.get("C"),
                  images.get("F"),
                  images.get(figure.getKey()),
                  abstractionLevels[abstraction]
              )
          );

          vector[0] = calculateSimilarity(horizontalRelations1, answerHorizontal);
          vector[1] = calculateSimilarity(horizontalRelations2, answerHorizontal);
          vector[2] = calculateSimilarity(verticalRelations1, answerVertical);
          vector[3] = calculateSimilarity(verticalRelations2, answerVertical);

          similarities.put(Integer.valueOf(figure.getKey()), calculateEuclideanDistance(vector));
        }
      }

      // Find the answer with the highest similarity value
      Map.Entry<Integer, Double> bestAnswer = null;
      for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
        if (bestAnswer == null || bestAnswer.getValue() < entry.getValue()) {
          bestAnswer = entry;
        }
      }

      // Return most confident answer
      answer = bestAnswer.getKey();

      // Normalize data for a range of 0.0-1.0
      for (Map.Entry<Integer, Double> entry : similarities.entrySet()) {
        entry.setValue(entry.getValue() / bestAnswer.getValue());
      }

      // Calculate mean
      double mean = mean(similarities.values());
      // Calculate standard deviation
      double standardDeviation = standardDeviation(similarities.values(), mean);
      // Calculate standard error
      double standardError = standardError(similarities.size(), standardDeviation);
      // Calculate getDeviations
      Map<Integer, Double> deviations = getDeviations(similarities, mean, standardError);

      // Get most confident answer
      double threshold = getConfidenceThreshold();
      List<Integer> answers = new ArrayList<>();
      for (Map.Entry<Integer, Double> deviation : deviations.entrySet()) {
        if (deviation.getValue() > threshold)
          answers.add(deviation.getKey());
      }

      // TODO need to increase efficiency of program before implementing multiple abstraction levels
//      if (answers.size() == 1)
//        answer = answers.get(0);

      // Move to next level of abstraction
      abstraction++;
    }

    // Print out answer to problem
    System.out.println(problem.getName());
    System.out.println(answer);

    return answer;
  }

  /**
   * Returns a set of fractal representations for two images.
   *
   * @param image1
   * @param image2
   * @param abstractionLevel The size of the grid to partition the image with.
   * @return A set of FractalRepresentation objects.
   */
  public Set<FractalRepresentation> getMutualFractals(BufferedImage image1, BufferedImage image2, int abstractionLevel) {
    Set<FractalRepresentation> fractals = new HashSet<>();

    fractals.addAll(getFractals(image1, image2, abstractionLevel));
    fractals.addAll(getFractals(image2, image1, abstractionLevel));

    return fractals;
  }

  /**
   * Returns a set of fractal representations for three images.
   *
   * @param image1
   * @param image2
   * @param image3
   * @param abstractionLevel The size of the grid to partition the image with.
   * @return A set of FractalRepresentation objects.
   */
  public Set<FractalRepresentation> getMutualFractalsThreeByThree(BufferedImage image1, BufferedImage image2, BufferedImage image3,
      int abstractionLevel) {
    Set<FractalRepresentation> fractals = new HashSet<>();

    fractals.addAll(getMutualFractals(image1, image2, abstractionLevel));
    fractals.addAll(getMutualFractals(image2, image3, abstractionLevel));
    fractals.addAll(getMutualFractals(image1, image3, abstractionLevel));

    return fractals;
  }

  /**
   * Returns a set of fractal codes for two images.
   *
   * @param source
   * @param destination
   * @param abstractionLevel The size of the grid to partition the image with.
   * @return A set of FractalRepresentation objects.
   */
  public Set<FractalRepresentation> getFractals(BufferedImage source, BufferedImage destination, int abstractionLevel) {
    Set<FractalRepresentation> fractals = new HashSet<>();

    // Partition images into a grid of image fragments
    BufferedImage[][] sourceFragments = partitionImage(source, abstractionLevel);
    BufferedImage[][] destinationFragments = partitionImage(destination, abstractionLevel);
    // Transformation name as key and transformed image as value
    Map<String, BufferedImage> transformations = new LinkedHashMap<>();
    // Transformation name as key and correspondence score as value
    Map<String, Double> transformCorrespondences = new LinkedHashMap<>();
    // Fragment location as key and transformation as value
    Map<Pair<Integer, Integer>, String> fragmentTransformations = new LinkedHashMap<>();
    // Fragment location as key and correspondence score as value
    Map<Pair<Integer, Integer>, Double> fragmentCorrespondences = new LinkedHashMap<>();

    // Iterate through destination image fragments
    for (int i = 0; i < destinationFragments.length; i++) {
      for (int j = 0; j < destinationFragments.length; j++) {
        // Iterate through source image fragments
        for (int x = 0; x < sourceFragments.length; x++) {
          for (int y = 0; y < sourceFragments.length; y++) {
            // Get each affine transformation for the source image fragment
            // Identity
            transformations.put("identity", sourceFragments[x][y]);
            // Horizontal flip
            transformations.put("hFlip", horizontalFlip(sourceFragments[x][y]));
            // Vertical flip
            transformations.put("vFlip", verticalFlip(sourceFragments[x][y]));
            // Rotate 90 degrees
            transformations.put("r90", rotateImage(sourceFragments[x][y], Math.PI / 2));
            // Rotate 180 degrees
            transformations.put("r180", rotateImage(sourceFragments[x][y], Math.PI));
            // Rotate 270 degrees
            transformations.put("r270", rotateImage(sourceFragments[x][y], 3 * Math.PI / 2));
            // Reflect ynx
            transformations.put("reflYNX", reflectYNX(sourceFragments[x][y]));
            // Reflect yx
            transformations.put("reflYX", reflectYX(sourceFragments[x][y]));

            // Get the correspondence value for each transformation
            for (Map.Entry<String, BufferedImage> transformation : transformations.entrySet()) {
              transformCorrespondences.put(
                  transformation.getKey(),
                  getCorrespondence(transformation.getValue(), destinationFragments[i][j], new Pair<>(x, y), new Pair<>(i, j))
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
                sourceFragments[correspondingFragment.getKey().getElement0()][correspondingFragment.getKey().getElement1()],
                destinationFragments[i][j]
            )
        );

        // Store fractal representation
        fractals.add(fractal);
      }
    }

    return fractals;
  }

  /**
   * Returns a set of fractal features for a fractal representation.
   *
   * @param fractal
   * @return A set of String objects representing the fractal features for a representation.
   */
  public Set<String> getFractalFeatures(FractalRepresentation fractal) {
    Set<String> features = new HashSet<>();

    features.add("S" + fractal.getSourceFragmentOrigin().toString());
    features.add("D" + fractal.getDestinationFragmentOrigin().toString());
    features.add("T" + fractal.getOrthonormalTransformation());
    features.add("R" + fractal.getRegionSize());
    features.add("C" + fractal.getColorimetricContraction());

    return features;
  }

  /**
   * This method calculates the Tversky ratio for two sets of fractal representations.
   *
   * @param fractalSet1
   * @param fractalSet2
   * @return The Tversky ratio for two sets of fractal representations.
   */
  public double calculateSimilarity(Set<FractalRepresentation> fractalSet1, Set<FractalRepresentation> fractalSet2) {
    Set<String> fractalSet1Features = new HashSet<>();
    Set<String> fractalSet2Features = new HashSet<>();
    Set<String> set1 = new HashSet<>();
    Set<String> set2 = new HashSet<>();
    Set<String> set3 = new HashSet<>();
    double alpha = 2.0;
    double beta = 1.0;

    // Generate set of features for fractals in first set
    for (FractalRepresentation fractal : fractalSet1) {
      fractalSet1Features.addAll(getFractalFeatures(fractal));
    }

    // Generate set of features for fractals in second set
    for (FractalRepresentation fractal : fractalSet2) {
      fractalSet2Features.addAll(getFractalFeatures(fractal));
    }

    set1.addAll(fractalSet1Features);
    set1.retainAll(fractalSet2Features);

    set2.addAll(fractalSet1Features);
    set2.removeAll(fractalSet2Features);

    set3.addAll(fractalSet2Features);
    set3.removeAll(fractalSet1Features);

    return set1.size() / (set1.size() + alpha * set2.size() + beta * set3.size());
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
   * This method returns the color contraction for two images.
   *
   * @param a
   * @param b
   * @return
   */
  public double getColorContraction(BufferedImage a, BufferedImage b) {
    return 0.75 * (getColorMean(b) - getColorMean(a));
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
  public int getColorMean(BufferedImage image) {
    long totalRed = 0;
    long totalGreen = 0;
    long totalBlue = 0;
    int [][] pixelMatrix = getPixelMatrix(image);
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
  public double getCorrespondence(BufferedImage a, BufferedImage b,
                                  Pair<Integer, Integer> aFragmentOrigin, Pair<Integer, Integer> bFragmentOrigin) {
    return 1 * getPhotometricCorrespondence(a, b) + 0.1 * getDistance(aFragmentOrigin, bFragmentOrigin);
  }

  /**
   * This method returns the photometric correspondence between two images.
   *
   * @param a
   * @param b
   * @return
   */
  public double getPhotometricCorrespondence(BufferedImage a, BufferedImage b) {
    double c = 0;

    int[][] pixelMatrixA = getPixelMatrix(a);
    int[][] pixelMatrixB = getPixelMatrix(b);

    for (int x = 0; x < b.getWidth(); x++) {
      for (int y = 0; y < b.getHeight(); y++) {
        c += Math.pow(getPhotometric(pixelMatrixB[x][y]) - getPhotometric(pixelMatrixA[x][y]), 2);
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
   * This method rotates an image 90 degrees then reflects the image over the
   * x axis.
   *
   * @param image
   * @return
   */
  public BufferedImage reflectYNX(BufferedImage image) {
    BufferedImage result = null;

	// Rotate 90 degrees
    result = rotateImage(result, Math.PI / 2);
    // Reflect image over x axis
    result = verticalFlip(image);

    return result;
  }

  /**
   * This method rotates an image 270 degrees then reflects the image over the
   * x axis.
   *
   * @param image
   * @return
   */
  public BufferedImage reflectYX(BufferedImage image) {
    BufferedImage result = null;

	// Rotate 270 degrees
    result = rotateImage(result, 3 * Math.PI / 2);
    // Reflect image over x axis
    result = verticalFlip(image);

    return result;
  }

  /**
   * This method returns a 2D array composed of the RGB values for each pixel
   * in an image. Credit goes to Motasim for the original code:
   *
   * http://stackoverflow.com/questions/6524196/java-get-pixel-array-from-image
   *
   * @param image
   * @return
   */
  private static int[][] getPixelMatrix(BufferedImage image) {
    final byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
    final int width = image.getWidth();
    final int height = image.getHeight();
    final boolean hasAlphaChannel = image.getAlphaRaster() != null;

    int[][] result = new int[height][width];
    if (hasAlphaChannel) {
      final int pixelLength = 4;
      for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
        int argb = 0;
        argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
        argb += ((int) pixels[pixel + 1] & 0xff); // blue
        argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
        argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
        result[row][col] = argb;
        col++;
        if (col == width) {
          col = 0;
          row++;
        }
      }
    }
    else {
      final int pixelLength = 3;
      for (int pixel = 0, row = 0, col = 0; pixel < pixels.length; pixel += pixelLength) {
        int argb = 0;
        argb += -16777216; // 255 alpha
        argb += ((int) pixels[pixel] & 0xff); // blue
        argb += (((int) pixels[pixel + 1] & 0xff) << 8); // green
        argb += (((int) pixels[pixel + 2] & 0xff) << 16); // red
        result[row][col] = argb;
        col++;
        if (col == width) {
          col = 0;
          row++;
        }
      }
    }

    return result;
  }

  /**
   * This method opens an image from a file.
   *
   * @param path
   * @return
   */
  public BufferedImage openImage(String path) {
    // Open image
    try {
      return ImageIO.read(new File(path));
    }
    catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  /**
   * This method implements the Euclidean distance formula.
   *
   * @param vector
   * @return
   */
  public double calculateEuclideanDistance(double[] vector) {
    double sum = 0;

    for (int i = 0; i < vector.length; i++) {
      sum += Math.pow(vector[i], 2);
    }

    return Math.sqrt(sum);
  }

  /**
   * This method calculates the mean value for a vector of values.
   *
   * @param vector
   * @return
   */
  public double mean(Collection<Double> vector) {
    double sum = 0;

    for (Double value : vector)
      sum += value;

    return sum / vector.size();
  }

  /**
   * This method calculates the standard deviation for a vector of values.
   *
   * @param vector
   * @return
   */
  public double standardDeviation(Collection<Double> vector, double mean) {
    double sum = 0;

    for (Double value : vector)
      sum += Math.pow(value - mean, 2);

    return Math.sqrt(Math.pow(vector.size(), -1) * sum);
  }

  /**
   * This method calculates the standard error value for a vector of values.
   *
   * @param size
   * @return
   */
  public double standardError(double size, double standardDeviation) {
    return standardDeviation / Math.sqrt(size);
  }

  /**
   * This method returns a set of deviations for a vector of values.
   *
   * @param map
   * @param mean
   * @param standardError
   * @return
   */
  public Map<Integer, Double> getDeviations(Map<Integer, Double> map, double mean, double standardError) {
    Map<Integer, Double> deviations = new HashMap<>();

    for (Map.Entry<Integer, Double> entry : map.entrySet()) {
      deviations.put(entry.getKey(), (entry.getValue() - mean) / standardError);
    }

    return deviations;
  }

  /**
   * This method implements an approximation of the Gaussian error function.
   *
   * @param x
   * @return
   */
  public double erf(double x) {
    double sign = (x < 0) ? -1 : 1;
    double a = getA();

    double part1 = (Math.pow(-x, 2)) * ((4 / Math.PI + a * Math.pow(x, 2)) / (1 + a * Math.pow(x, 2)));

    return Math.sqrt(1 - Math.exp(part1));
  }

  /**
   * This method implements an approximation of the Gaussian error function as an inverse.
   *
   * @param x
   * @return
   */
  public double inverseErf(double x) {
    double sign = (x < 0) ? -1 : 1;
    double a = getA();

    double part3 = 2 / (Math.PI * a) + Math.log(1 - Math.pow(x, 2)) / 2;
    double part1 = Math.pow(part3, 2);
    double part2 = Math.log(1 - Math.pow(x, 2)) / a;

    return Math.sqrt(Math.sqrt(part1 - part2) - part3);
  }

  /**
   * This method returns a confidence threshold value.
   *
   * @return
   */
  public double getConfidenceThreshold() {
    double confidence = 0.95;

    return Math.sqrt(2) * inverseErf(confidence);
  }

  /**
   * This method returns the a value used in the error functions.
   *
   * @return
   */
  public double getA() {
    return (8 * (Math.PI - 3)) / (3 * Math.PI * (4 - Math.PI));
  }
}
