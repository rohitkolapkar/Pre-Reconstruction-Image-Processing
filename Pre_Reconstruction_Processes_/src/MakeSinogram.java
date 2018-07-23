/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
import java.awt.AWTEvent;
import java.util.ArrayList;
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class MakeSinogram implements PlugInFilter, DialogListener{
	
	// Variables
	private int type=1, rot_type=1;
	private double lat_tilt_angle=(double) 0.0;
	long start_time;
	short[][][] F;
	short[][][] Fnew;
	String source_dirpath="";
	String destination_dirpath="";
	static ImagePlus source_imp;
	
	// Setup of PlugInFilter Interface
	@Override
	public int setup(String arg, ImagePlus source_imp) {
		
		MakeSinogram.source_imp = source_imp;
		return DOES_ALL;
		
	}
	
	// Run
	@Override
	public void run(ImageProcessor ip) {
		  // Create Gui and Start
				createGui();
				
				// invoke garbage collection
				F=null; Fnew=null;
				System.gc();
		
	}

	
	
	void createGui() {
		
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes");
	
		// sinogram
		String[] items0 = {"Enabled"};
		gd.addRadioButtonGroup("Create Sinogram from Intensity Stack", items0, 1, 1, "Enabled");

		// Lateral tilt
		String[] items1 = {"Disable", "Enable"};
		gd.addRadioButtonGroup("Correct for lateral tilt: ", items1, 2, 1, "Disable");	
		//gd.addMessage("Positive rotations - clockwise");
		gd.addNumericField("Lateral tilt correction (degrees): ", lat_tilt_angle, 1);
		
		//add dialog listener 
		gd.addDialogListener(this);
		
		// Display Gui
		gd.setResizable(true);
		gd.showDialog();
		

   }
	
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {

		  // Handle changes in Numerical Fields
    lat_tilt_angle = (double) gd.getNextNumber();
    
    // Handle Changes in Radio Buttons - filter and reconstruction type
    
    String type_change = gd.getNextRadioButton();
    String rot_change = gd.getNextRadioButton();
    
    // Intensity axis
    if (type_change=="1 Image Row = 1 Horizontal 2D Slice Intensity"){
    }
    
    // Rotation angle
    if (rot_change=="Disable"){
    	rot_type=1;
    }
    if (rot_change=="Enable"){
    	rot_type=2;
    }
    
    
		// Exit if cancel is pressed
		if (gd.wasCanceled()) return false;
    
		// Run if ok is pressed
		if (gd.wasOKed()){
    	// convert to 8 bit
    	//ImageConverter ic = new ImageConverter(imp);
        //ic.convertToGray8();
    	// Create Sinogram/s
    	
    	create_3Darray(type, rot_type, lat_tilt_angle);
    	return true;
    	}
		return true;
	}

      
		
	// Create 3D array
	void create_3Darray(int type_s, int rot_type, double lat_tilt_angle){
			// set Parameters
			ImageProcessor ip = null; ImageProcessor ip_temp = null;
			
			// Get stack size
			ImageStack current_stack = source_imp.getImageStack();

			int stack_size = current_stack.getSize();
			int w = current_stack.getWidth();
			int h = current_stack.getHeight();
	    	
			// F will hold stack as 3D array
			F = new short [h][w][stack_size];
			
			// Parameters
				IJ.log("********Input Parameters********");
				IJ.log("Stack size: " +stack_size);

				// Lateral tilt correction
				if (rot_type==2){
					IJ.log("Lateral tilt correction enabled");
					IJ.log("Rotate by: " +lat_tilt_angle +"\u00b0");
				} else {
					IJ.log("Lateral tilt correction disabled");
				}
				
				
				
				
			// If lateral tilt correction - create stack to hold rotated intensity images
			ImageStack rotated_stack = null;
			if (rot_type==2 ){
				rotated_stack = new ImageStack(w,h);
			}
				
			IJ.log("********Processing**************");
			long t1 = System.currentTimeMillis(); // start timer
			
			// 1. Loop through all slices (set 3D array)
			IJ.log("1. Creating 3D array");
			for (int ss=1;ss<=stack_size;ss++){
				//Set ith image from stack
				source_imp.setSlice(ss);
				
				// Create processor
				ip_temp = source_imp.getProcessor();
				
				// A. lateral tilt and static offset Correction
				if (rot_type==2){
					ip = ip_temp.createProcessor(ip_temp.getWidth(),ip_temp.getHeight());
					ip.setPixels(ip_temp.getPixelsCopy());// hold in new processor
					ip.setBackgroundValue(0);
					ip.setInterpolationMethod(1); // Bilinear interpolation = 1
					
					ip.rotate(lat_tilt_angle); // rotate
					rotated_stack.addSlice("Corrected Image Stack", ip); // hold corrected slices to display
				} else {
					// Create processor 
					ip = ip_temp.createProcessor(ip_temp.getWidth(),ip_temp.getHeight());
					ip.setPixels(ip_temp.getPixelsCopy()); // hold in new processor
					
				}

				for (int i=0; i<h; i++){
					for (int j=0; j<w; j++){
						double p = ip.getPixel(j,i);
						F[i][j][ss-1]=(short) p;
						//ip_f.putPixel(j,i,(int)F[i][j][ss-1]);
					}
				}			
			}
			
			// continue with sinogram creation 
			
			sinogram_create(F, stack_size, w, h, ip, t1);
			
			F = null; current_stack = null; ip=null; ip_temp=null;
		}
		
		
	// Create Sinograms
	void sinogram_create(short [][][]F, int stack_size, int w, int h, ImageProcessor ip, long t1){
			Fnew = new short [stack_size][w][h];
			ImageProcessor ip_f = null; ImageProcessor ip_get_parms = null; ImageStack final_stack = null;
			ImageProcessor ip_img = null; ImageProcessor ip_img2 = null;
			
			// 2. Permute 3D array
			IJ.log("2. Permuting 3D array");
			for (int i=0; i<h; i++){
				for (int j=0; j<w; j++){
					for (int ss=1;ss<=stack_size;ss++){
						Fnew[ss-1][j][i] = F[i][j][ss-1];
					}
				}	
			}
			        
	        // Create List to hold slices/images
	     	List<ImagePlus> result_array = new ArrayList<ImagePlus>();
	     		
			// 3. Set up slices/images, create stack, and display
	     	IJ.log("3. Creating slices (images)");
			for (int ss=1;ss<=h;ss++){
				ip_f = ip.createProcessor(w,stack_size);
				for (int i=0; i<stack_size; i++){
					for (int j=0; j<w; j++){
						ip_f.putPixel(j,i,Fnew[i][j][ss-1]);
					}
				}
				ImagePlus slice = new ImagePlus("Reconstruction", ip_f);
				result_array.add(slice);
			}        
			
			// Set width and height of stack = to 1st slice in list
			IJ.log("4. Creating stack");
	        int width, height;
	        ip_get_parms = result_array.get(0).getProcessor();
	        height=ip_get_parms.getHeight();
	    	width=ip_get_parms.getWidth();
	    	
	    	long ts1 = System.currentTimeMillis() ;
				// Create Image Stack
		        final_stack = new ImageStack(height,width);
		        
				// Add all images (ImagePlus) from result_array to stack      
		        for (ImagePlus img : result_array){
		          	ip_img = img.getProcessor();   // rotate: changes axis, flip changes rotation direction (comment out as required)
		        	ip_img2 = ip_img.rotateRight();
		        	ip_img2.flipHorizontal();
		        	
		        	// Correct for static offset - this can be done on sinogram or intensity image
		        	//if (offset_type==2){ 
		        	//	ip_img2.translate(0, s_offset);
		        	//}
		        	
		        	final_stack.addSlice("Image Stack", ip_img2);
		        }
	        long ts2 = System.currentTimeMillis();
	        		
	        IJ.log("5. Sinogram ready");
	        long t2 = System.currentTimeMillis();
	        
	        
	        // Display final stack
	        new ImagePlus("Sinogram Stack", final_stack).show();
	        IJ.log("");
	        IJ.log("********Finished****************");
	        IJ.log("Total processing time: " +(t2-t1) +" ms");
	        IJ.log("Of which: " +(ts2-ts1) +" ms ("+(100*(ts2-ts1)/(t2-t1)) +"%) to create the stack");
	        IJ.log("");
	        Fnew = null; ip_f=null;	ip_get_parms=null; 	final_stack=null; ip_img = null; ip_img2 = null; F = null; ip = null;
		}

		
		

	// Just to test in Eclipse - call ImageJ
	public static void main (String[] args) {
		new ij.ImageJ();
		new MakeSinogram().run(null);  
	}
			
	
}

	






