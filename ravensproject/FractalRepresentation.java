package ravensproject;

public class FractalRepresentation {

	Pair<Double, Double> sourceFragmentOrigin;
	Pair<Double, Double> destinationFragmentOrigin;
	// The affine transformation used to rotate, flip, or reflect the pixels
	String orthonormalTransformation;
	// The width and height of the region in pixels
	Integer regionSize;
	// The photometric or colorshifting operation needed to match the source
	// and destination blocks
	Double colorimetricContraction;

	public Pair<Double, Double> getSourceFragmentOrigin() {
		return sourceFragmentOrigin;
	}

	public void setSourceFragmentOrigin(Pair<Double, Double> sourceFragmentOrigin) {
		this.sourceFragmentOrigin = sourceFragmentOrigin;
	}

	public Pair<Double, Double> getDestinationFragmentOrigin() {
		return destinationFragmentOrigin;
	}

	public void setDestinationFragmentOrigin(Pair<Double, Double> destinationFragmentOrigin) {
		this.destinationFragmentOrigin = destinationFragmentOrigin;
	}

	public String getOrthonormalTransformation() {
		return orthonormalTransformation;
	}

	public void setOrthonormalTransformation(String orthonormalTransformation) {
		this.orthonormalTransformation = orthonormalTransformation;
	}

	public Integer getRegionSize() {
		return regionSize;
	}

	public void setRegionSize(Integer regionSize) {
		this.regionSize = regionSize;
	}

	public Double getColorimetricContraction() {
		return colorimetricContraction;
	}

	public void setColorimetricContraction(Double colorimetricContraction) {
		this.colorimetricContraction = colorimetricContraction;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = prime * result + ((colorimetricContraction == null) ? 0 : colorimetricContraction.hashCode());
		result = prime * result + ((destinationFragmentOrigin == null) ? 0 : destinationFragmentOrigin.hashCode());
		result = prime * result + ((orthonormalTransformation == null) ? 0 : orthonormalTransformation.hashCode());
		result = prime * result + ((regionSize == null) ? 0 : regionSize.hashCode());
		result = prime * result + ((sourceFragmentOrigin == null) ? 0 : sourceFragmentOrigin.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		
		if (obj == null)
			return false;
		
		if (!(obj instanceof FractalRepresentation))
			return false;
		
		FractalRepresentation other = (FractalRepresentation) obj;
		
		if (colorimetricContraction == null) {
			if (other.colorimetricContraction != null)
				return false;
		} else if (!colorimetricContraction
				.equals(other.colorimetricContraction))
			return false;
		
		if (destinationFragmentOrigin == null) {
			if (other.destinationFragmentOrigin != null)
				return false;
		} else if (!destinationFragmentOrigin
				.equals(other.destinationFragmentOrigin))
			return false;
		
		if (orthonormalTransformation == null) {
			if (other.orthonormalTransformation != null)
				return false;
		} else if (!orthonormalTransformation
				.equals(other.orthonormalTransformation))
			return false;
		
		if (regionSize == null) {
			if (other.regionSize != null)
				return false;
		} else if (!regionSize.equals(other.regionSize))
			return false;
		
		if (sourceFragmentOrigin == null) {
			if (other.sourceFragmentOrigin != null)
				return false;
		} else if (!sourceFragmentOrigin.equals(other.sourceFragmentOrigin))
			return false;
		
		return true;
	}
}
