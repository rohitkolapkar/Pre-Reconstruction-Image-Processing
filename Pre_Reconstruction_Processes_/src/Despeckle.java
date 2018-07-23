/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;

public class Despeckle implements PlugIn {
	String source_dirpath="";
	String destination_dirpath="";
	ImagePlus source_imp;
	public void run(String arg) {
		
		source_dirpath = IJ.getDirectory("Choose Source Directory");
		IJ.run("Image Sequence...", "open=["+source_dirpath+"] sort");
		source_imp= IJ.getImage();
		
		destination_dirpath = IJ.getDirectory("Choose Destination Directory");
		
		IJ.run(source_imp, "Despeckle", "stack");
		IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+destination_dirpath+"]");
		source_imp.changes=false;
		source_imp.close();
	}
	
}
