package ravensproject;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

public class MutualFractalsCommand implements Runnable {

  private BufferedImage source;
  private BufferedImage destination;
  private int abstractionLevel;
  private Set<FractalRepresentation> fractals;
  
  public MutualFractalsCommand(
      BufferedImage source, 
      BufferedImage destination, 
      int abstractionLevel) {
    this.source = source;
    this.destination = destination;
    this.abstractionLevel = abstractionLevel;
    this.fractals = new HashSet<>();
  }
  
  public Set<FractalRepresentation> getFractals() {
    return fractals;
  }

  @Override
  public void run() {
    fractals.addAll(new FractalsCommand(source, destination, abstractionLevel).execute());
    fractals.addAll(new FractalsCommand(destination, source, abstractionLevel).execute());
  }

}
