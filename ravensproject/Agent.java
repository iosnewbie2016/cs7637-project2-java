package ravensproject;

// Uncomment these lines to access image processing.

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * <p/>
 * You may also create and submit new files in addition to modifying this file.
 * <p/>
 * Make sure your file retains methods with the signatures: public Agent()
 * public char Solve(RavensProblem problem)
 * <p/>
 * These methods will be necessary for the project's main method to run.
 */
public class Agent {
  /**
   * The default constructor for your Agent. Make sure to execute any processing
   * necessary before your Agent starts solving problems here.
   * <p/>
   * Do not add any variables to this signature; they will not be used by
   * main().
   */
  public Agent() {

  }

  /**
   * The primary method for solving incoming Raven's Progressive Matrices. For
   * each problem, your Agent's Solve() method will be called. At the conclusion
   * of Solve(), your Agent should return a String representing its answer to
   * the question: "1", "2", "3", "4", "5", or "6". These Strings are also the
   * Names of the individual RavensFigures, obtained through
   * RavensFigure.getName().
   * <p/>
   * In addition to returning your answer at the end of the method, your Agent
   * may also call problem.checkAnswer(String givenAnswer). The parameter passed
   * to checkAnswer should be your Agent's current guess for the problem;
   * checkAnswer will return the correct answer to the problem. This allows your
   * Agent to check its answer. Note, however, that after your agent has called
   * checkAnswer, it will *not* be able to change its answer. checkAnswer is
   * used to allow your Agent to learn from its incorrect answers; however, your
   * Agent cannot change the answer to a question it has already answered.
   * <p/>
   * If your Agent calls checkAnswer during execution of Solve, the answer it
   * returns will be ignored; otherwise, the answer returned at the end of Solve
   * will be taken as your Agent's answer to this problem.
   *
   * @param problem
   *          the RavensProblem your agent should solve
   * @return your Agent's answer to this problem
   */
  public int Solve(RavensProblem problem) {
    return initialization(problem);
  }

  public int initialization(RavensProblem problem) {
    Map<String, BufferedImage> images = new HashMap<>();
    Map<String, BufferedImage> answerImages = new HashMap<>();
    Map<Integer, List<String>> completedAnalogies = new HashMap<>();
    Map<Integer, List<String>> partialAnalogies = new HashMap<>();
    List<List<Integer>> completedParallelAnalogies = new ArrayList<List<Integer>>();

    // Open all images for problem
    for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
      if (!Character.isDigit(figure.getKey().charAt(0)))
        images.put(figure.getKey(), convertToGrayscale(openImage(problem.getFigures().get(figure.getKey()).getVisual())));
    }

    // Open all answer images for problem
    for (Map.Entry<String, RavensFigure> figure : problem.getFigures().entrySet()) {
      if (Character.isDigit(figure.getKey().charAt(0)))
        answerImages.put(figure.getKey(), convertToGrayscale(openImage(problem.getFigures().get(figure.getKey()).getVisual())));
    }

    // Define completed analogies
    completedAnalogies.put(1, Arrays.asList("A", "B", "C"));
    completedAnalogies.put(2, Arrays.asList("D", "E", "F"));
    completedAnalogies.put(3, Arrays.asList("A", "D", "G"));
    completedAnalogies.put(4, Arrays.asList("B", "E", "H"));

    // Define parallel partial analogies
    partialAnalogies.put(1, Arrays.asList("G", "H", "?"));
    partialAnalogies.put(2, Arrays.asList("G", "H", "?"));
    partialAnalogies.put(3, Arrays.asList("C", "F", "?"));
    partialAnalogies.put(4, Arrays.asList("C", "F", "?"));

    // Define completed parallel analogies
    completedParallelAnalogies.add(Arrays.asList(1, 2));
    completedParallelAnalogies.add(Arrays.asList(3, 4));

    // Get final transformation
    Map.Entry<Integer, Pair<String, Double>> finalTransform = transformationInduction(completedAnalogies, images);

    // Get answer
    return answerSelection(completedAnalogies, partialAnalogies, finalTransform, images, answerImages);
  }

  public Map.Entry<Integer, Pair<String, Double>> transformationInduction(
      Map<Integer, List<String>> completedAnalogies,
      Map<String, BufferedImage> images) {
    Map<Integer, Pair<String, Double>> analogyTransforms = new HashMap<>();

    for (Map.Entry<Integer, List<String>> entry : completedAnalogies.entrySet()) {
      BufferedImage image1 = images.get(entry.getValue().get(0));
      BufferedImage image2 = images.get(entry.getValue().get(1));
      BufferedImage image3 = images.get(entry.getValue().get(2));
      int[][] pixelMatrix1 = getGrayscalePixelMatrix(image1);
      int[][] pixelMatrix2 = getGrayscalePixelMatrix(image2);
      int[][] pixelMatrix3 = getGrayscalePixelMatrix(image3);
      Map<String, Double> transformations = new HashMap<>();

      /* Binary transformations */
      // Union
      transformations.put("union", calculateTverskyRatio2(pixelMatrix3, unionTransform(pixelMatrix1, pixelMatrix2)));
      // Intersection
      transformations.put("intersect", calculateTverskyRatio2(pixelMatrix3, intersectTransform(pixelMatrix1, pixelMatrix2)));
      // Subtraction
      transformations.put("subtract", calculateTverskyRatio2(pixelMatrix3, subtractTransform(pixelMatrix1, pixelMatrix2)));
      // Back-subtraction
      transformations.put("backSubtract", calculateTverskyRatio2(pixelMatrix3, backSubtractTransform(pixelMatrix1, pixelMatrix2)));
      // Exclusive-or
      transformations.put("exclusiveOr", calculateTverskyRatio2(pixelMatrix3, exclusiveOrTransform(pixelMatrix1, pixelMatrix2)));

      // Select the best fit transform by highest similarity
      Map.Entry<String, Double> bestTransform = null;
      for (Map.Entry<String, Double> transform : transformations.entrySet()) {
        if (bestTransform == null || bestTransform.getValue() < transform.getValue()) {
          bestTransform = transform;
        }
      }

      // Assign transform to analogy
      analogyTransforms.put(entry.getKey(), new Pair<String, Double>(bestTransform.getKey(), bestTransform.getValue()));
    }

    // Select the best fit transform across all analogies
    Map.Entry<Integer, Pair<String, Double>> finalTransform = null;
    for (Map.Entry<Integer, Pair<String, Double>> transform : analogyTransforms.entrySet()) {
      if (finalTransform == null || finalTransform.getValue().getElement1() < transform.getValue().getElement1()) {
        finalTransform = transform;
      }
    }

    return finalTransform;
  }

  public int answerSelection(
      Map<Integer, List<String>> completedAnalogies, 
      Map<Integer, List<String>> partialAnalogies,
      Map.Entry<Integer, Pair<String, Double>> finalTransform, 
      Map<String, BufferedImage> images, 
      Map<String, BufferedImage> answerImages) {
    String transform = finalTransform.getValue().getElement0();
    List<String> analogies = partialAnalogies.get(finalTransform.getKey());
    BufferedImage image1 = images.get(analogies.get(0));
    BufferedImage image2 = images.get(analogies.get(0));
    int[][] pixelMatrix1 = getGrayscalePixelMatrix(image1);
    int[][] pixelMatrix2 = getGrayscalePixelMatrix(image2);
    int[][] candidateMatrix = new int[pixelMatrix1.length][pixelMatrix1.length];
    Map<String, Double> answers = new HashMap<>();

    // TODO remove after testing
    // printMatrix(pixelMatrix1);
    // System.out.println("");
    // printMatrix(pixelMatrix2);

    if (transform.equals("union"))
      candidateMatrix = unionTransform(pixelMatrix1, pixelMatrix2);
    else if (transform.equals("intersect"))
      candidateMatrix = intersectTransform(pixelMatrix1, pixelMatrix2);
    else if (transform.equals("subtract"))
      candidateMatrix = subtractTransform(pixelMatrix1, pixelMatrix2);
    else if (transform.equals("backSubtract"))
      candidateMatrix = backSubtractTransform(pixelMatrix1, pixelMatrix2);
    else if (transform.equals("exclusiveOr"))
      candidateMatrix = exclusiveOrTransform(pixelMatrix1, pixelMatrix2);

    for (Map.Entry<String, BufferedImage> entry : answerImages.entrySet()) {
      int[][] pixelMatrix3 = getGrayscalePixelMatrix(entry.getValue());

      answers.put(entry.getKey(), calculateTverskyRatio2(candidateMatrix, pixelMatrix3));
    }

    // Select the answer with the highest similarity with candidate answer
    Map.Entry<String, Double> answer = null;
    for (Map.Entry<String, Double> entry : answers.entrySet()) {
      if (answer == null || answer.getValue() < entry.getValue()) {
        answer = entry;
      }
    }

    return Integer.valueOf(answer.getKey());
  }

  /**
   * This method flips an image horizontally. Credit goes to Byron Kiourtzoglou
   * for the original code:
   * <p/>
   * http://examples.javacodegeeks.com/desktop-java/awt/image/flipping-a-
   * buffered-image/
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
   * <p/>
   * http://examples.javacodegeeks.com/desktop-java/awt/image/flipping-a-
   * buffered-image/
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
   * <p/>
   * 90 degrees = pi / 2 180 degrees = pi 270 degrees = 3 * pi / 2
   *
   * @return The result of the transformation
   * @image The image to rotate
   * @theta A radian value for the rotation.
   */
  public BufferedImage rotateImage(BufferedImage image, double theta) {
    BufferedImage result = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());

    /*
     * Transformations are stacked then applied so the last transformation is
     * the first to happen
     */
    AffineTransform tx = new AffineTransform();

    tx.translate(image.getHeight() / 2, image.getWidth() / 2);
    tx.rotate(theta);
    tx.translate(-image.getWidth() / 2, -image.getHeight() / 2);

    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
    op.filter(image, result);

    return result;
  }

  /**
   * This method rotates an image 90 degrees then reflects the image over the x
   * axis.
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
   * This method rotates an image 270 degrees then reflects the image over the x
   * axis.
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

  public int[][] unionTransform(int[][] matrix1, int[][] matrix2) {
    int[][] resultPixelArray = new int[matrix1.length][matrix1.length];

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        resultPixelArray[i][j] = Math.max(matrix1[i][j], matrix2[i][j]);
      }
    }

    return resultPixelArray;
  }

  public int[][] intersectTransform(int[][] matrix1, int[][] matrix2) {
    int[][] resultPixelArray = new int[matrix1.length][matrix1.length];

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        resultPixelArray[i][j] = Math.min(matrix1[i][j], matrix2[i][j]);
      }
    }

    return resultPixelArray;
  }

  public int[][] subtractTransform(int[][] matrix1, int[][] matrix2) {
    int[][] resultPixelArray = new int[matrix1.length][matrix1.length];

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        resultPixelArray[i][j] = matrix1[i][j] - matrix2[i][j];
      }
    }

    return resultPixelArray;
  }

  public int[][] backSubtractTransform(int[][] matrix1, int[][] matrix2) {
    int[][] resultPixelArray = new int[matrix1.length][matrix1.length];

    for (int i = 0; i < matrix2.length; i++) {
      for (int j = 0; j < matrix2[i].length; j++) {
        resultPixelArray[i][j] = matrix2[i][j] - matrix1[i][j];
      }
    }

    return resultPixelArray;
  }

  public int[][] exclusiveOrTransform(int[][] matrix1, int[][] matrix2) {
    int[][] resultPixelArray = new int[matrix1.length][matrix1.length];

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        resultPixelArray[i][j] = Math.max(matrix1[i][j], matrix2[i][j]) - Math.min(matrix1[i][j], matrix2[i][j]);
      }
    }

    return resultPixelArray;
  }

  public double calculateTverskyRatio(int[][] matrix1, int[][] matrix2) {
    double alpha = 2.0;
    double beta = 1.0;
    double intersection = intersectMatrices(matrix1, matrix2);
    double difference = differenceMatrices(matrix1, matrix2);
    double difference2 = differenceMatrices(matrix2, matrix1);

    return intersection / (intersection + alpha * difference + beta * difference2);
  }

  public double calculateTverskyRatio2(int[][] matrix1, int[][] matrix2) {
    return (double) intersectMatrices(matrix1, matrix2) / unionMatrices(matrix1, matrix2);
  }

  public int unionMatrices(int[][] matrix1, int[][] matrix2) {
    int count = 0;

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        if (matrix1[i][j] != 0 || matrix2[i][j] != 0)
          count++;
      }
    }

    return count;
  }

  public int intersectMatrices(int[][] matrix1, int[][] matrix2) {
    int count = 0;

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        if (matrix1[i][j] != 0 && matrix1[i][j] == matrix2[i][j])
          count++;
      }
    }

    return count;
  }

  public int differenceMatrices(int[][] matrix1, int[][] matrix2) {
    int count = 0;

    for (int i = 0; i < matrix1.length; i++) {
      for (int j = 0; j < matrix1[i].length; j++) {
        if (matrix1[i][j] != 0 && matrix1[i][j] != matrix2[i][j])
          count++;
      }
    }

    return count;
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
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

  /**
   * Converts an RGB color image to a grayscale image.
   *
   * Original code by vulkanino:
   * http://stackoverflow.com/questions/9131678/convert
   * -a-rgb-image-to-grayscale-image-reducing-the-memory-in-java
   */
  public BufferedImage convertToGrayscale(BufferedImage image) {
    BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
    Graphics graphics = result.getGraphics();

    graphics.drawImage(image, 0, 0, null);
    graphics.dispose();

    return result;
  }

  /**
   * TODO could be int[][]?
   *
   * Original code by blackSmith:
   * http://stackoverflow.com/questions/17278829/grayscale -bitmap-into-2d-array
   */
  public int[][] getGrayscalePixelMatrix(BufferedImage image) {
    int width = image.getWidth();
    int height = image.getHeight();
    int[][] matrix = new int[width][height];

    Raster raster = image.getData();
    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        // Original code
        // matrix[i][j] = raster.getSample(i, j, 0);
        // TODO testing
        int value = raster.getSample(i, j, 0);
        if (value == 255)
          matrix[i][j] = 0;
        else
          matrix[i][j] = 1;
      }
    }

    return matrix;
  }

  // TODO remove after testing
  // Displays a 2d array in the console, one line per row.
  static void printMatrix(double[][] grid) {
    for (int r = 0; r < grid.length; r++) {
      for (int c = 0; c < grid[r].length; c++)
        System.out.print(grid[r][c] + " ");
      System.out.println();
    }
  }
}
