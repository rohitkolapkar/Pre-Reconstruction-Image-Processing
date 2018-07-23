/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;


public class NormalisationBeamCorrection implements PlugIn{
	String dir="";
	String dir1="";
	ImagePlus imp,imp1;
	int a;
	double u,v,w,x;
	public void run(String arg) {
		
		dir = IJ.getDirectory("Choose a Directory");
		dir1 = IJ.getDirectory("Choose a Directory");
		IJ.run("Image Sequence...", "open=["+dir+"] sort");
		imp = IJ.getImage();
		IJ.wait(1000);
		IJ.run("Z Project...", "projection=[Sum Slices]");
		imp1 = IJ.getImage();
		
		
		
		while(imp1.getRoi()==null) {
			new WaitForUserDialog("Please select area then click OK").show();
				if(imp1.isVisible()==false||imp.isVisible()==false) {
					IJ.run("Quit");
				}
				
		}
		
		IJ.selectWindow(imp.getTitle());
		IJ.run("Restore Selection", "");
		imp1.changes=false;
		imp1.close();
		a=imp.getNSlices();
		imp.setSlice(a/2);
		IJ.run(imp,"Measure","");
		for(int i=1; i<=a; i++){
			imp.setSlice(i);
			IJ.run(imp,"Measure","");
		}
		IJ.run(imp,"Select All","");
		for(int i=1; i<=a; i++) {
			imp.setSlice(i);
			u=ResultsTable.getResultsTable().getValue("Mean", 0);
			v =ResultsTable.getResultsTable().getValue("Mean",i);
			w=u/v;
			x=v/u;
			
			
			if(w<1){
				//System.out.println(w);
				//System.out.println("Multiply");
				//System.out.println(w*x);
				IJ.run(imp,"Multiply...", "value="+x);
			}
			if(w>1){
				//System.out.println(w);
				//System.out.println("Divide");
				//System.out.println(w*x);
				IJ.run(imp,"Divide...", "value="+x);
			}

			}
			IJ.resetMinAndMax();
			IJ.run(imp, "Image Sequence... ", "format=TIFF name=Image save=["+dir1+"]");
			imp.changes=false;
		    imp.close();
			
		}

	}

