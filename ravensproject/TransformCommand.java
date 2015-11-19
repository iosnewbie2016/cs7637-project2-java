package ravensproject;

import java.awt.image.BufferedImage;

public abstract class TransformCommand implements Runnable {

  private BufferedImage image;
  private BufferedImage result;
  
  public TransformCommand(BufferedImage image) {
    this.image = image;
  }
  
  public BufferedImage getResult() {
    return result;
  }

  @Override
  public abstract void run();

}
