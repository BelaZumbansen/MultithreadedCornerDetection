import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.awt.Color;
import java.awt.Graphics2D;

import javax.imageio.*;

public class CornerDetector {
	
	/**
	 * Thread which retrieves eligible pixels from a queue and checks whether the pixel 
	 * represents a corner until the pixel queue is empty.
	 */
	private static class MultithreadCornerCheck extends Thread {
		
		/* Thread identifier */
		private int threadNumber;
		
		public MultithreadCornerCheck(int threadNumber) {
			this.threadNumber = threadNumber;
		}
		
		@Override
		public void run() {
			
			// Retrieve size of all but the final partition
			int partitionSize = height / threads;
			
			// Calculate the first row for this partition
			int starting_y = threadNumber*partitionSize;
			
			AdjacentPixelSet corner;
			
			// If we are in the first partition ensure we are not checking the first 3 rows to avoid edges
			if (threadNumber == 0) {
				starting_y = 3;
			}
			
			// If we are not in the last partition
			if (threadNumber != (threads - 1)) {
				
				// Iterate through all pixels in the partition
				for (int i = 3; i < width - 3; i++) {
					for (int j = 0; j < partitionSize; j++) {
						
						// Check whether the pixel is a corner
						corner = isCorner(i, starting_y + j);
						
						// If the pixel is a corner add it to the corner queue so a circle can be drawn later in the program
						if (corner != null) {
							addCorner(corner);
						}
					}
				}
			} else {
				
				// Iterate through all pixels in the partition
				for (int i = 3; i < width - 3; i++) {
					for (int j = starting_y; j < height - 3; j++) {
						
						// Check whether the corner is a pixel
						corner = isCorner(i, j);
						
						// If the pixel is a corner add it to the corner queue so a circle can be drawn later in the program
						if (corner != null) {
							addCorner(corner);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Thread which retrieves pixels deemed to be pixels from a queue and draws 
	 * a circle around them.
	 *
	 */
	private static class MultithreadCircleDrawer extends Thread {
		
		/* Thread identifier */
		private int threadNumber;
		
		/* Image on which to draw circles */
		private BufferedImage subImg;
		
		public MultithreadCircleDrawer(int threadNumber, BufferedImage subImg) {
			this.threadNumber = threadNumber;
			this.subImg 	  = subImg;
		}
		
		@Override
		public void run() {
			
			// Retrieve the set of pixels to be coloured in this partition
			HashSet<Pixel> myPixelLs = toColour.get(threadNumber);
			
			// Create the red color
			int r = 255;
			int g = 0;
			int b = 0;
			Color color = new Color(r, g, b);
			
			// Create an iterator for the pixel set
			Iterator<Pixel> iterator = myPixelLs.iterator();
			
			// Iterate through the pixels in the sub image and colour each one red
			while (iterator.hasNext()) {
				Pixel pixelInSubImg = partitionPixel(iterator.next());
			    subImg.setRGB(pixelInSubImg.x, pixelInSubImg.y, color.getRGB()); 
			}
		}
	}
	
    // input image
    public static BufferedImage img;

    // parameters and their default values
    public static String imagename = "test1.png" ; // name of the input image
    public static int threads = 1; // number of threads to use
    public static double threshold = 0.1; // threshold to detect luminosity change
    public static int n = 8; // number of same luminosities in a sequence required.
    
    public static int width;
    public static int height;
    
    /** Data Sets and Lists Required for Synchronization */
    public static ArrayList<HashSet<Pixel>> toColour;
    public static ArrayList<Semaphore> 	 	semaphoreLs;
    public static ArrayList<BufferedImage>  subImages;
    
    /** Thread lists for synchronization */
    public static ArrayList<MultithreadCornerCheck>  cornerThreads = new ArrayList<>();
    public static ArrayList<MultithreadCircleDrawer> circleThreads = new ArrayList<>();
    
    // helper; returns luminosity of the RGB pixel value 
    public static double luminosity(int p) {
        // just extract the RGB values (masking off the alpha value) and call the other helper
        return luminosity(((p>>16)&0xff),((p>>8)&0xff),((p)&0xff));
    }
    // helper; returns luminosity given the 3 separated RGB pixel values
    // this follows a common luminosity calculation
    public static double luminosity(int r,int g,int b) {
        return 0.21*linearize(((double)r)/255.0)+0.72*linearize(((double)g)/255.0)+0.07*linearize(((double)b)/255.0);
    }
    // linearizing an R, G, or B value normalized to 0.0-1.0
    static double linearize(double d) {
        return (d<=0.04045) ? d/12.92 : Math.pow((d+0.055)/1.055,2.4);
    }

    // print out command-line parameter help and exit
    public static void help(String s) {
        System.out.println("Could not parse argument \""+s+"\".  Please use only the following arguments:");
        System.out.println(" -i imagename (string; current=\""+imagename+"\")");
        System.out.println(" -d luminosity threshold (floating point value 0.0-1.0; current=\""+threshold+"\")");
        System.out.println(" -n different luminosities threshold (integer value 0-16; current=\""+n+"\")");
        System.out.println(" -t threads (integer value >=1; current=\""+threads+"\")");
        System.exit(1);
    }
    
    /**
     * Add a corner to the queue of pixels around which the program will later draw a circle
     * 
     * @param corner the pixel and the associated pixels which to color red to draw a circle
     */
    private static void addCorner(AdjacentPixelSet corner) {
    	
    	for (int i = 0; i < 16; i++) {
    		addPixelToRelevantQueue(new Pixel(corner.getAdjacentPixel(i).x, corner.getAdjacentPixel(i).y));
    	}
    }
    
    // process command-line options
    public static void opts(String[] args) {
        int i = 0;

        try {
            for (;i<args.length;i++) {

                if (i==args.length-1)
                    help(args[i]);

                if (args[i].equals("-i")) {
                    imagename = args[i+1];
                } else if (args[i].equals("-d")) {
                    threshold = Double.parseDouble(args[i+1]);
                } else if (args[i].equals("-n")) {
                    n = Integer.parseInt(args[i+1]);
                } else if (args[i].equals("-t")) {
                    threads = Integer.parseInt(args[i+1]);
                } else {
                    help(args[i]);
                }
                // an extra increment since our options consist of 2 pieces
                i++;
            }
        } catch (Exception e) {
            System.err.println(e);
            help(args[i]);
        }
    }

    // main.  we allow an IOException in case the image loading/storing fails.
    public static void main(String[] args) throws IOException {
        // process options
        opts(args);

        // read in the image
        img = ImageIO.read(new File(imagename));
        int width  = img.getWidth();
        int height = img.getHeight();
        
        CornerDetector.height = height;
        CornerDetector.width  = width;
        
        // Initialize the array of sub images
        subImages = new ArrayList<>();
        
        // Initialize the list of pixel sets
        toColour = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
        	toColour.add(new HashSet<>());
        }
        
        // Initialize a list of semaphores, one for each partition
        semaphoreLs = new ArrayList<>();
        for (int i = 0; i < threads; i++) {
        	semaphoreLs.add(new Semaphore(1));
        }
        
        // Calculate the size of a partition
        int partitionSize = height / threads;
        
        // Create the sub images required, one for each partition
        for (int i = 0; i < threads; i++) {
        	
        	if (i == (threads - 1)) {
        		subImages.add(img.getSubimage(0, i*partitionSize, width, (height - (partitionSize*i))));
        	} else {
        		subImages.add(img.getSubimage(0, i*partitionSize, width, partitionSize));
        	}
        } 
        
        // Initialize the correct number of threads with the required task
        for (int i = 0; i < threads; i++) {
        	
        	MultithreadCornerCheck cornerCheck = new MultithreadCornerCheck(i);
        	cornerThreads.add(cornerCheck);
        	cornerCheck.start();
        }
        
        // Log the start of checking for corners
        long cornerCheckStart = System.currentTimeMillis();
        
        // Wait on all threads to finish executing before moving on to the next stage
        for (int i = 0; i < cornerThreads.size(); i++) {
        	
        	try {
				cornerThreads.get(i).join();
			} catch (InterruptedException e) {}
        }
        
        // Log the end of checking for corners
        long cornerCheckEnd = System.currentTimeMillis();
        
        // Initialize the correct number of threads with the required task
        for (int i = 0; i < threads; i++) {
        	
        	MultithreadCircleDrawer circleDrawer = new MultithreadCircleDrawer(i, subImages.get(i));
        	circleThreads.add(circleDrawer);
        	circleDrawer.start();
        }
        
        // Log the beginning of circle drawing
        long circleDrawStart = System.currentTimeMillis();
        
        // Wait on all threads to finish executing before moving on to the next stage
        for (int i = 0; i < circleThreads.size(); i++) {
        	
        	try {
				circleThreads.get(i).join();
			} catch (InterruptedException e) {}
        }
        
        // Log the ending of circle drawing
        long circleDrawEnd = System.currentTimeMillis();
        
        // Initialize the final output image
        BufferedImage combinedImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        // Create graphics for the output
        Graphics2D g = combinedImg.createGraphics();
        
        // Stack the partitions on top of each other in the combined image
        for (int i = 0; i < threads; i++) {
        	g.drawImage(subImages.get(i), 0, (partitionSize*i), null);
        }
        
        // Finalize graphics
        g.dispose();
        
        System.out.println("Searching for corners with " + threads + " threads took: " + (cornerCheckEnd - cornerCheckStart) + "milliseconds");
        System.out.println("Drawing circles around all corners with " + threads + " threads took: " + (circleDrawEnd - circleDrawStart) + "milliseconds");
        
        // Write out the image
        File outputfile = new File("outputimage.png");
        ImageIO.write(combinedImg, "png", outputfile);
    }
    
    /**
     * Check whether a certain pixel is considered a corner by the FAST algorithm
     * 
     * @param x the x coordinate of the pixel to verify
     * @param y the y coordinate of the pixel to verify
     * @return the pixel with its associated adjacent pixels or 
     * {@code null} if the pixel is not a corner
     */
	public static AdjacentPixelSet isCorner(int x, int y) {
		
		// Initialize necessary local variables for computation
		int 	sequence = 0;
		int 	ndx 	 = 0;
		boolean cont 	 = true;
		int 	curComp  = 0;
		double  curLumin;
		
		// Calculate the luminosity of the pixel being considered
		double pixelLumin = luminosity(img.getRGB(x, y));
		
		// Construct the set of "adjacent" pixels as considered by the FAST algorithm
		AdjacentPixelSet adjacentSet = new AdjacentPixelSet(x,y);
		
		while (cont) {
			
			// Retrieve the luminosity of the current adjacent pixel
			if (ndx < 16) {
				curLumin = luminosity(img.getRGB(adjacentSet.getAdjacentPixel(ndx).x, adjacentSet.getAdjacentPixel(ndx).y));
			} else {
				curLumin = luminosity(img.getRGB(adjacentSet.getAdjacentPixel(ndx-16).x, adjacentSet.getAdjacentPixel(ndx-16).y));
			}
			
			// If the luminosity difference is above the threshold and the adjacent luminosity is higher
			if ((curLumin - pixelLumin) >= threshold) {
				
				// If the last luminosity was also higher by the threshold value, increment the sequence counter
				if (curComp == 1) {
					sequence++;
				} 
				
				// If this is the beginning of a new sequence, reset the counter
				else {
					sequence = 1;
					curComp  = 1;
					
					// If we are resetting the counter and have checked all indices once then
					// we have proven that there is no sufficient sequence, hence the pixel
					// is not a corner.
					if (ndx >= 16) {
						cont = false;
					}
				}
			} 
			// If the luminosity difference is above the threshold and the adjacent luminosity is lower
			else if ((pixelLumin - curLumin) >= threshold) {
				
				// If the last luminosity was also lower by the threshold value, increment the sequence counter
				if (curComp == -1) {
					sequence++;
				} 
				
				// If this is the beginning of a new sequence, reset the counter
				else {
					sequence = 1;
					curComp = -1;
					
					// If we are resetting the counter and have checked all indices once then
					// we have proven that there is no sufficient sequence, hence the pixel
					// is not a corner.
					if (ndx >= 16) {
						cont = false;
					}
				}
			} 
			// If the luminosity difference is negligible
			else {
				
				// Reset the counter sequence
				curComp  = 0;
				sequence = 0;
				
				// If we have reached the end of a sequence and have checked all indices then
				// we have proven that there is no sufficient sequence, hence the pixel is not a corner.
				if (ndx >= 16) {
					cont = false;
				}
			}
			
			// If the required sequence has been met, return the set of adjacent vertices
			if (sequence >= n) {
				return adjacentSet;
			}
			
			// Increment the index
			ndx++;
		}
		
		// No sequence was found, the pixel is not a corner, hence return null
		return null;
	}
	
	/**
	 * Take a pixel from the image and place it in the queue to be coloured for the partition in which
	 * it is. The thread assigned to this queue will later colour the pixel red.
	 * 
	 * @param pixel the pixel to be queued
	 */
	private static void addPixelToRelevantQueue(Pixel pixel) {
		
		// Determine in which partition the pixel is
		int partition = pixelBelongsTo(pixel);
		
		// Make sure no other thread is adding a pixel to this queue at the same time
		// NOTE: this will only lock out other threads in rare case where an adjacent pixel is outside of the partition
		// of the center pixel. This can only happen along the edge of the partitions
		try {
			// Aquire the semaphore for the queue
			semaphoreLs.get(partition - 1).acquire();
			toColour.get(partition - 1).add(pixel);
		} catch (InterruptedException e) {} 
		finally {
			// Release the semaphore for the queue
			semaphoreLs.get(partition - 1).release(); 
		}
	}
	
	/**
	 * Determine which partition the pixel belongs to
	 * 
	 * @param pixel the pixel for which to determine the partition
	 * @return the partition index
	 */
	private static int pixelBelongsTo(Pixel pixel) {
		
		// Calculate the size of the partition
		int partitionSize = height / threads;
		
		// Partition is based on number of partitions and y coordinate of the pixel
		for (int i = 1; i <= threads; i++) {
			
			if (i == threads) {
				return threads;
			}
			
			if (pixel.y < (partitionSize*i)) {
				return i;
			}
		}
		
		return 0;
	}
	
	/**
	 * Determine the location of a Pixel from the regular image in the sub image
	 * 
	 * @param pixel the pixel to be located in the sub image
	 * @return the Pixel location in the sub image
	 */
	private static Pixel partitionPixel(Pixel pixel) {
		
		int partitionSize = height / threads;
		int partition = pixelBelongsTo(pixel);
		
		// Calculate the new row
		int new_y = pixel.y - (partition - 1)*partitionSize;
		return new Pixel(pixel.x, new_y);
	}
	
}
