/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */

import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;

public class FlatFieldCorrection implements PlugIn {
	String source_dirpath="";
	String destination_dirpath="";
	String bck_filepath="";
	String ref_filepath="";
	ImagePlus source_imp,bck_imp,ref_imp,source_sub_bck_imp,ref_sub_bck_ipm;
	public void run(String arg) {
		source_dirpath = IJ.getDirectory("Choose Source Directory");
		IJ.run("Image Sequence...", "open=["+source_dirpath+"] sort");
		source_imp= IJ.getImage();
		
		
	
		bck_filepath=IJ.getFilePath("Choose a Background Image");
		//System.out.println(bck_filepath);
		IJ.open(bck_filepath);
		bck_imp=IJ.getImage();
		
		
		ref_filepath=IJ.getFilePath("Choose a Reference Image");
		IJ.open(ref_filepath);
		ref_imp=IJ.getImage();
		
		
		destination_dirpath = IJ.getDirectory("Choose Destination Directory");
		
		
		ImageCalculator ic = new ImageCalculator();
		source_sub_bck_imp = ic.run("Subtract create 32-bit stack", source_imp, bck_imp);
		//source_sub_bck_imp.show();
		
		ref_sub_bck_ipm = ic.run("Subtract create 32-bit stack", ref_imp, bck_imp);
		//ref_sub_bck_ipm.show();
		
		
		bck_imp.changes=false;
		bck_imp.close();
		ref_imp.changes=false;
		ref_imp.close();
		source_imp.changes=false;
		source_imp.close();
		
		//IJ.selectWindow(source_sub_bck_imp.getTitle());
		int a = source_sub_bck_imp.getNSlices();
		//TextWindow tw=new TextWindow("", 400, 500);
		for(int i=1; i<=a; i++) {
			
			source_sub_bck_imp.setSlice(i);
			ImagePlus divide_imp=ic.run("Divide create 32-bit", ref_sub_bck_ipm, source_sub_bck_imp);
			IJ.run(divide_imp, "Log", "");
			IJ.run(divide_imp, "16-bit", "");
			IJ.saveAs(divide_imp, "Tiff", destination_dirpath+"Image"+i);
			//tw.append(destination_dirpath+source_imp.getTitle()+i+" "+"done");
			IJ.log(destination_dirpath+source_imp.getTitle()+i+" "+"done");
			}
		
		
	}
}

