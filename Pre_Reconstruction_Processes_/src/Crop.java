/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;


public class Crop implements PlugIn {
	String source_dirpath="";
	String destination_dirpath="";
	ImagePlus source_imp,zproject_imp;
	
	public void run(String arg) {
		source_dirpath = IJ.getDirectory("Choose a Directory");
		IJ.run("Image Sequence...", "open=["+source_dirpath+"] sort");
		source_imp = IJ.getImage();
		
		destination_dirpath = IJ.getDirectory("Choose a Directory");
		IJ.run("Z Project...", "projection=[Sum Slices]");
		zproject_imp = IJ.getImage();
		
		
		
		while(zproject_imp.getRoi()==null) {
			new WaitForUserDialog("Please Select Rectangular Area to crop then click OK").show();
				if(zproject_imp.isVisible()==false||source_imp.isVisible()==false) {
					IJ.run("Quit");
				}
				
		}
		
		IJ.selectWindow(source_imp.getTitle());
		IJ.run("Restore Selection", "");
		
		zproject_imp.changes=false;
		zproject_imp.close();
		
		IJ.run(source_imp, "Crop", "");
		IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+destination_dirpath+"]");
		//IJ.run ("Select None");
		
		source_imp.changes=false;
		source_imp.close();
			
		
	}
}
