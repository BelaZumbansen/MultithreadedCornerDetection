
import java.util.ArrayList;

public class AdjacentPixelSet {
	
	class AdjacentPixel {
		
		int x;
		int y;
		int comparisonFactor;
		
		AdjacentPixel (int x, int y) {
			this.x = x;
			this.y = y;
			this.comparisonFactor = 0;
		}
	}
	
	ArrayList<AdjacentPixel> pixelSet;
	int xCoord;
	int yCoord;
	
	public AdjacentPixelSet (int xCoord, int yCoord) {
		this.xCoord = xCoord;
		this.yCoord = yCoord;
		
		pixelSet = new ArrayList<>();
		
		getAdjacent();
	}
	
	private void getAdjacent() {
		
		AdjacentPixel p1  = new AdjacentPixel(xCoord,   yCoord+3);
		AdjacentPixel p2  = new AdjacentPixel(xCoord+1, yCoord+3);
		AdjacentPixel p3  = new AdjacentPixel(xCoord+2, yCoord+2);
		AdjacentPixel p4  = new AdjacentPixel(xCoord+3, yCoord+1);
		AdjacentPixel p5  = new AdjacentPixel(xCoord+3, yCoord);
		AdjacentPixel p6  = new AdjacentPixel(xCoord+3, yCoord-1);
		AdjacentPixel p7  = new AdjacentPixel(xCoord+2, yCoord-2);
		AdjacentPixel p8  = new AdjacentPixel(xCoord+1, yCoord-3);
		AdjacentPixel p9  = new AdjacentPixel(xCoord,   yCoord-3);
		AdjacentPixel p10 = new AdjacentPixel(xCoord-1, yCoord-3);
		AdjacentPixel p11 = new AdjacentPixel(xCoord-2, yCoord-2);
		AdjacentPixel p12 = new AdjacentPixel(xCoord-3, yCoord-1);
		AdjacentPixel p13 = new AdjacentPixel(xCoord-3, yCoord);
		AdjacentPixel p14 = new AdjacentPixel(xCoord-3, yCoord+1);
		AdjacentPixel p15 = new AdjacentPixel(xCoord-2, yCoord+2);
		AdjacentPixel p16 = new AdjacentPixel(xCoord-1, yCoord+3);
		
		pixelSet.add(p1);
		pixelSet.add(p2);
		pixelSet.add(p3);
		pixelSet.add(p4);
		pixelSet.add(p5);
		pixelSet.add(p6);
		pixelSet.add(p7);
		pixelSet.add(p8);
		pixelSet.add(p9);
		pixelSet.add(p10);
		pixelSet.add(p11);
		pixelSet.add(p12);
		pixelSet.add(p13);
		pixelSet.add(p14);
		pixelSet.add(p15);
		pixelSet.add(p16);
	}
	
	public AdjacentPixel getAdjacentPixel(int ndx) {
		return pixelSet.get(ndx);
	}
}
