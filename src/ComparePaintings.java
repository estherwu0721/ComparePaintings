import java.awt.image.BufferedImage;

/**
 * @author (Boyang Wei/Shuyang Wu)
 *
 */
public class ComparePaintings {
	private ColorHash htOne;	 // ColorHash table
	private FeatureVector fvOne; // Feature vector
	int bbp;					 // BitsPerPixel
	int noc; 					 // Number of collisions
	public ComparePaintings(){}; // constructor.
	
	// Load the image, construct the hash table, count the colors.
	ColorHash countColors(String filename, int bitsPerPixel) {
		bbp = bitsPerPixel;
		htOne = new ColorHash(3,bitsPerPixel,"Quadratic Probing",0.5);
		ImageLoader image = new ImageLoader(filename);
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				ColorKey key = image.getColorKey(i, j, bitsPerPixel);
				htOne.increment(key);
			}
		fvOne = new FeatureVector(bitsPerPixel);
		fvOne.getTheCounts(htOne);
		}

		return htOne;
	}
	
	// Load the image, construct the hash table, count the colors.
	// Also take in a String of resolutionMethod to do either linear probing or quatratic probing.
	ColorHash countColors(String filename, int bitsPerPixel, String resolutionMethod) {
		bbp = bitsPerPixel;
		htOne = new ColorHash(3,bitsPerPixel,resolutionMethod,0.5);
		ImageLoader image = new ImageLoader(filename);
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				ColorKey key = image.getColorKey(i, j, bitsPerPixel);
				htOne.increment(key);
			}
		}
		fvOne = new FeatureVector(bitsPerPixel);
		fvOne.getTheCounts(htOne);
		return htOne;
	}
	
	// This method is used to count number of collisions when putting each key of the loaded image into the colorHash table.
	int countCollision(String filename, int bitsPerPixel, String resolutionMethod) {
		int countCollision = 0;
		htOne = new ColorHash(3,bitsPerPixel,resolutionMethod,0.5);
		ImageLoader image = new ImageLoader(filename);
		int w = image.getWidth();
		int h = image.getHeight();
		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				ColorKey key = image.getColorKey(i, j, bitsPerPixel);
				htOne.increment(key);
				countCollision = countCollision + htOne.increment(key).nCollisions;
			}
		}
		noc = countCollision;
		return noc; 
	}

	// This method is used to comprare the similarity of two paintings given each painting in its colorHash table form.
	double compare(ColorHash painting1, ColorHash painting2) {
		FeatureVector paintingOneVetor = new FeatureVector(bbp);
		FeatureVector paintingTwoVetor = new FeatureVector(bbp);
		paintingOneVetor.getTheCounts(painting1);
		paintingTwoVetor.getTheCounts(painting2);
		return paintingOneVetor.cosineSimilarity(paintingTwoVetor);
	}

	 //	A basic test for the compare method: S(x,x)
	void basicTest(String filename) {
		ColorHash myTest = countColors(filename,bbp);
		FeatureVector myTestV = new FeatureVector(bbp);
		myTestV.getTheCounts(myTest);
		System.out.println(myTestV.cosineSimilarity(myTestV));
	}

	// Using the three given painting images and a variety of bits-per-pixel values, compute and print out a table of collision counts in the following format:
	void CollisionTests() {
		System.out.format("%-15s%15s%15s%15s%15s%15s%15s%n", "Bits Per Pixel", "C(Mona,linear)", "C(Mona,quadratic)",  "C(Starry,linear)", "C(Starry,quadratic)", "C(Christina,linear)", "C(Christina,quadratic)");
		for (int i = 24; i > 0; i-=3) {
			System.out.print(i + "         ");
			int[] list = new int[6]; 
			list[0] = countCollision("MonaLisa.jpg",i,"Linear Probing");
			list[1] = countCollision("MonaLisa.jpg",i,"Quadratic Probing");
			list[2] = countCollision("StarryNight.jpg",i,"Linear Probing");
			list[3] = countCollision("StarryNight.jpg",i,"Quadratic Probing");
			list[4] = countCollision("ChristinasWorld.jpg",i,"Linear Probing");
			list[5] = countCollision("ChristinasWorld.jpg",i,"Quadratic Probing");
			System.out.format ("%15s%15s%15s%15s%15s%20s", list[0], list[1], list[2],list[3], list[4], list[5]);
			System.out.println();
		}		
	}
		
	// This method checks that if the images can be loaded
	void imageLoadingTest() {
		ImageLoader mona = new ImageLoader("MonaLisa.jpg");
		ImageLoader starry = new ImageLoader("StarryNight.jpg");
		ImageLoader christina = new ImageLoader("ChristinasWorld.jpg");
		System.out.println("It looks like we have successfully loaded all three test images.");
	}
	
	// Test and print the table of all three images' similarity comparations
	void fullSimilarityTests() {
		System.out.format("%-15s%15s%15s%15s%n", "Bits Per Pixel", "S(Mona,Starry)", "S(Mona,Christina)", "S(Starry,Christina)");
		for (int i = 24; i > 0; i-=3) {
			System.out.print(i + "         ");	
			double[] list = new double[3]; 
			FeatureVector one = new FeatureVector(i);
			FeatureVector two = new FeatureVector(i);
			FeatureVector three = new FeatureVector(i);
			one.getTheCounts(countColors("MonaLisa.jpg",i));
			two.getTheCounts(countColors("StarryNight.jpg",i));
			three.getTheCounts(countColors("ChristinasWorld.jpg",i));
			list[0] = one.cosineSimilarity(two);
			list[1] = one.cosineSimilarity(three);
			list[2] = two.cosineSimilarity(three);
			System.out.format ("%-15s%15s%15s%n", list[0], list[1], list[2]);
			System.out.println();
		}
	}
	
	// Proform the test of S(x,x), the fullSimilarityTests and the collision test.
	public static void main(String[] args) {
		ComparePaintings cp = new ComparePaintings();
		cp.basicTest("MonaLisa.jpg");
		cp.fullSimilarityTests();
		cp.CollisionTests();
	}

}
