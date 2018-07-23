/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
/**
 * 
 */

/**
 * @author rohitkolapkar
 *
 */
import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Dimension;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;


public class Make_Sinogram implements PlugIn, DialogListener{

	long start_time;
	short[][][] F;
	short[][][] Fnew;
	String sinogram_destination_dirpath;
	static ImagePlus original_sinogram;
	@Override
	public void run(String arg) {
		// TODO Auto-generated method stub
        original_sinogram = IJ.getImage();
		
		
		// Create Gui and Start
		createGui();
		
		// invoke garbage collection
		F=null; Fnew=null;
		System.gc();
		
	}
	
	void createGui() {
		
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		CheckboxGroup sinogram=new CheckboxGroup();
		Checkbox sinogram_checkbox = new Checkbox("Create Sinogram",sinogram,true);
		sinogram_checkbox.setPreferredSize(d_textField);
		gd.add(sinogram_checkbox);	
		gd.addMessage("");
		
		TextField textField5 = new TextField("Save Result..(optional)");
		textField5.setPreferredSize(d_textField);
		textField5.setEditable(false);
		gd.add(textField5);
		
		Button sinogram_save_button=new Button("Browse");
		sinogram_save_button.setPreferredSize(d_button);
		gd.add(sinogram_save_button);
		
		sinogram_save_button.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
		        	sinogram_destination_dirpath=IJ.getDirectory("Choose a Directory to Save sinogram Stack");
		        	textField5.setText(sinogram_destination_dirpath);
	            }
	        });
		gd.addMessage("");
		gd.addMessage("");
		//add dialog listener 
		gd.addDialogListener(this);
		
		// Display Gui
		gd.setResizable(true);
		gd.showDialog();
		
   }
	
	
	@Override
	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {

		// Exit if cancel is pressed
		if (gd.wasCanceled()) return false;
    
		// Run if ok is pressed
		if (gd.wasOKed()){
    	// convert to 8 bit
    	//ImageConverter ic = new ImageConverter(imp);
        //ic.convertToGray8();
    	// Create Sinogram/s
    	
    	create_3Darray();
    	return true;
    	}
		return true;
	}

      
		
	// Create 3D array
	void create_3Darray(){

		/*if(flatfieldcorrection_destination_dirpath!=null) {
			
		IJ.run("Image Sequence...", "open=["+flatfieldcorrection_destination_dirpath+"] sort");
		sino_source_imp = IJ.getImage();
		original_sinogram=sino_source_imp;
		}*/
	
		// set Parameters
		ImageProcessor ip = null; ImageProcessor ip_temp = null;
		
		// Get stack size
		ImageStack current_stack = original_sinogram.getImageStack();

		int stack_size = current_stack.getSize();
		int w = current_stack.getWidth();
		int h = current_stack.getHeight();
    	
		// F will hold stack as 3D array
		F = new short [h][w][stack_size];
		
		// Parameters
			
			IJ.log("********Processing:Sinogram Creation**************");
			long t1 = System.currentTimeMillis(); // start timer
		
		// 1. Loop through all slices (set 3D array)
			IJ.log("1. Creating 3D array");
			for (int ss=1;ss<=stack_size;ss++){
				//Set ith image from stack
				original_sinogram.setSlice(ss);
			
				// Create processor
				ip_temp = original_sinogram.getProcessor();
				// Create processor 
				ip = ip_temp.createProcessor(ip_temp.getWidth(),ip_temp.getHeight());
				ip.setPixels(ip_temp.getPixelsCopy()); // hold in new processor
				
			
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
        
        original_sinogram=new ImagePlus("Sinogram Stack", final_stack);
        IJ.run(original_sinogram, "Rotate 90 Degrees Right", "");
        IJ.run(original_sinogram, "Flip Horizontally", "stack");
        original_sinogram.show();
        original_sinogram = IJ.getImage();
        //saving sinogram
        if(sinogram_destination_dirpath!=null) {
        	//sinogram_destination_dirpath=IJ.getDirectory("Choose a Directory to Save sinogram Stack");
        	IJ.run("Image Sequence... ", "format=TIFF name=Image save=["+sinogram_destination_dirpath+"]");
    		IJ.log("Sinograms Saved at "+sinogram_destination_dirpath);
        }
        IJ.log("");
        IJ.log("********Sinogram Correction:Done****************");
        IJ.log("Total processing time: " +(t2-t1) +" ms");
        IJ.log("Of which: " +(ts2-ts1) +" ms ("+(100*(ts2-ts1)/(t2-t1)) +"%) to create the stack");
        IJ.log("");
        Fnew = null; ip_f=null;	ip_get_parms=null; 	final_stack=null; ip_img = null; ip_img2 = null; F = null; ip = null;

	}

		
		

	// Just to test in Eclipse - call ImageJ
	public static void main (String[] args) {
		new ij.ImageJ();
		new Make_Sinogram().run(null);  
	}



			
	
}

	







