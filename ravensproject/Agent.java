package ravensproject;

// Uncomment these lines to access image processing.
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.imageio.ImageIO;

/**
 * Your Agent for solving Raven's Progressive Matrices. You MUST modify this
 * file.
 * 
 * You may also create and submit new files in addition to modifying this file.
 * 
 * Make sure your file retains methods with the signatures:
 * public Agent()
 * public char Solve(RavensProblem problem)
 * 
 * These methods will be necessary for the project's main method to run.
 * 
 */
public class Agent {
    /**
     * The default constructor for your Agent. Make sure to execute any
     * processing necessary before your Agent starts solving problems here.
     * 
     * Do not add any variables to this signature; they will not be used by
     * main().
     * 
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
     * 
     * In addition to returning your answer at the end of the method, your Agent
     * may also call problem.checkAnswer(String givenAnswer). The parameter
     * passed to checkAnswer should be your Agent's current guess for the
     * problem; checkAnswer will return the correct answer to the problem. This
     * allows your Agent to check its answer. Note, however, that after your
     * agent has called checkAnswer, it will *not* be able to change its answer.
     * checkAnswer is used to allow your Agent to learn from its incorrect
     * answers; however, your Agent cannot change the answer to a question it
     * has already answered.
     * 
     * If your Agent calls checkAnswer during execution of Solve, the answer it
     * returns will be ignored; otherwise, the answer returned at the end of
     * Solve will be taken as your Agent's answer to this problem.
     * 
     * @param problem the RavensProblem your agent should solve
     * @return your Agent's answer to this problem
     */
    public int Solve(RavensProblem problem) {
    	if (problem.getName().equals("Basic Problem B-03")) {
        	BufferedImage a = null;
        	BufferedImage b = null;
        	
        	// Open image
    		try {
    			a = ImageIO.read(new File(problem.getFigures().get("A").getVisual()));
    			b = ImageIO.read(new File(problem.getFigures().get("B").getVisual()));
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
        	
    		BufferedImage newImage = rotateImage(a, Math.PI / 2);
    		
    		// Save image
    	    try {
				ImageIO.write(newImage, "png", new File("newImage.png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
		
//    	Set<FractalRepresentation> fractals = getFractals(a, b);
//    	
//    	int count = 1;
//    	for (FractalRepresentation fractal : fractals) {
//    		System.out.println("/**** Fractal " + count + " ****/");
//    		System.out.println(fractal.getSourceFragmentOrigin());
//    		System.out.println(fractal.getDestinationFragmentOrigin());
//    		System.out.println(fractal.getOrthonormalTransformation());
//    		System.out.println(fractal.getRegionSize());
//    		System.out.println(fractal.getColorimetricOperation());
//    		System.out.println(fractal.getColorimetricContraction());
//    	}
    	
        return -1;
    }
    
    public Set<FractalRepresentation> getFractals(BufferedImage source, BufferedImage destination) {
    	Set<FractalRepresentation> fractals = new HashSet<>();
    	
    	// Partition images into a grid of image fragments
    	BufferedImage [] sourceFragments = partitionImage(source);
    	BufferedImage [] destinationFragments = partitionImage(source);
    	
    	for (int i = 0; i < destinationFragments.length; i++) {
        	// Search source for equivalent sub-image to destination sub-image such that an affine transformation
        	// of source sub-image will result in destination sub-image
        	// TODO
        	
        	// Let the transformation be the representation of the chosen transformation associated with
        	// destination sub-image
        	
        	// Create the Fractal Representation from the fractal codes
        	FractalRepresentation fractal = new FractalRepresentation();
        	
        	// Store fractal representation
        	fractals.add(fractal);	
    	}
    	
    	return fractals;
    }
    
	public BufferedImage[] partitionImage(BufferedImage image) {
		int rows = 4;
		int columns = 4;
		int fragments = rows * columns;

		// Determine the fragment width and height
		int fragmentWidth = image.getWidth() / columns;
		int fragmentHeight = image.getHeight() / rows;

		int count = 0;
		BufferedImage images[] = new BufferedImage[fragments];
		for (int x = 0; x < rows; x++) {
			for (int y = 0; y < columns; y++) {
				// Initialize the image fragments in the array
				images[count] = new BufferedImage(fragmentWidth, fragmentHeight, image.getType());
				
				// Draw the image fragment  
                Graphics2D graphic = images[count++].createGraphics();  
                graphic.drawImage(image, 0, 0, fragmentWidth, fragmentHeight, fragmentWidth * y, fragmentHeight * x, 
                		fragmentWidth * y + fragmentWidth, fragmentHeight * x + fragmentHeight, null
                );  
                graphic.dispose();  
			}
		}

		return images;
	}
	
	public double getColorContraction(BufferedImage a, BufferedImage b) {
		return 0.75 * (getColorMean(b) - getColorMean(a));
	}
	
	public int getColorMean(BufferedImage image) {
		long sumr = 0, sumg = 0, sumb = 0;
		int size = image.getWidth() * image.getHeight();
		
		for (int x = 0; x < image.getWidth(); x++) {
			for (int y = 0; y < image.getHeight(); y++) {
				Color pixel = new Color(image.getRGB(x, y));
				sumr += pixel.getRed();
				sumg += pixel.getGreen();
				sumb += pixel.getBlue();
			}
		}
		
		return new Color(sumr / size, sumg / size, sumb / size).getRGB();
	}
	
	public double getCorrespondence(BufferedImage a, BufferedImage b) {
		double correspondence = Double.POSITIVE_INFINITY;
		
		
		
		return correspondence;
	}
	
	public double getPhotometricCorrespondence(BufferedImage a, BufferedImage b) {
		double c = 0;
		
		int [][] pixelMatrixA = getPixelMatrix(a);
		int [][] pixelMatrixB = getPixelMatrix(b);
		
		for (int x = 0; x < b.getWidth(); x++) {
			for (int y = 0; y < b.getHeight(); y++) {
				c = Math.pow(getPhotometric(pixelMatrixB[x][y]) - getPhotometric(pixelMatrixA[x][y]), 2);
			}
		}
		
		return c;
	}
	
	public double getPhotometric(int pixel) {
		int red = (pixel >> 16) & 0xFF;
		int green = (pixel >> 8) & 0xFF;
		int blue = pixel & 0xFF;
		
		return 0.3 * red + 0.59 * green + 0.11 * blue;
	}
	
	/*
	 * The 8 affine transformations are:
	 * identity
	 * horizontal flip
	 * vertical flip
	 * rotate 90 degrees
	 * rotate 180 degrees
	 * rotate 270 degrees
	 */
	
	public BufferedImage horizontalFlip(BufferedImage image) {
		BufferedImage result = null;
		
		AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
		tx.translate(-image.getWidth(null), 0);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		
		return result;
	}
	
	public BufferedImage verticalFlip(BufferedImage image) {
		BufferedImage result = null;
		
		AffineTransform tx = AffineTransform.getScaleInstance(1, -1);
		tx.translate(0, -image.getHeight(null));
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
		image = op.filter(image, null);
		
		return result;
	}
	
	/**
	  * This method rotates an image about its center. Theta is a radian value:
	  * 
	  * 90 degrees = pi / 2
	  * 180 degrees = pi
	  * 270 degrees = 3 * pi / 2
	  * 
	  * @image the image to rotate
	  * @theta a radian value for the rotation.
	  * @return the result of the transformation
	  */
	public BufferedImage rotateImage(BufferedImage image, double theta) {
		BufferedImage result = new BufferedImage(image.getHeight(), image.getWidth(), image.getType());
		
		/* Transformations are stacked then applied so the last transformation is the first to happen */
		AffineTransform tx = new AffineTransform();

		tx.translate(image.getHeight() / 2, image.getWidth() / 2);
		tx.rotate(theta);
		tx.translate(-image.getWidth() / 2,-image.getHeight() / 2);

		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		op.filter(image, result);
		
		return result;
	}
	
	public BufferedImage reflectXNY(BufferedImage image) {
		BufferedImage result = null;
		
		// Reflect image over y axis
		
		// Rotate 270 degrees
		
		return result;
	}
	
	public BufferedImage reflectXY(BufferedImage image) {
		BufferedImage result = null;
		
		// Reflect image over x axis
		
		// Rotate 270 degrees
		
		return result;
	}
	
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
		} else {
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
}
