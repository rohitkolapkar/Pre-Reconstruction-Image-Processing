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
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Dimension;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;


public class Sinogram_Correction implements PlugIn,DialogListener {
	
	String sinogram_correction_destination_dirpath;
	ImagePlus original_sinogram;
	
	@Override
	public void run(String arg) {
		
		original_sinogram = IJ.getImage();
		
		// Create Gui and Start
		createGui();
		
		// invoke garbage collection
		System.gc();
		
				
	}
	private void createGui() {
		// TODO Auto-generated method stub
		//sinogram_correction
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes: Sinogram Correction");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		
		CheckboxGroup sinogramcorrection = new CheckboxGroup();
		Checkbox sinogram_correction_checkbox=new Checkbox("Sinogram Correction",sinogramcorrection,true);
		sinogram_correction_checkbox.setPreferredSize(d_textField);
		gd.add(sinogram_correction_checkbox);
		gd.addMessage("");
		
		TextField textField6 = new TextField("Save Result..(optional)");
		textField6.setPreferredSize(d_textField);
		textField6.setEditable(false);
		gd.add(textField6);
		
		Button sinogram_correction_save_button=new Button("Browse");
		sinogram_correction_save_button.setPreferredSize(d_button);
		gd.add(sinogram_correction_save_button);
	
		sinogram_correction_save_button.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	            	sinogram_correction_destination_dirpath=IJ.getDirectory("Choose a Directory to Save corrected sinogram Stack");
		        	textField6.setText(sinogram_correction_destination_dirpath);
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
		// TODO Auto-generated method stub
		 // Exit if cancel is pressed
		if (gd.wasCanceled()) return false;
    
		// Run if ok is pressed
		if (gd.wasOKed()){
			
			int stack_size1 = original_sinogram.getImageStackSize();
			IJ.log("********Input Parameters********");
			IJ.log("Stack size: " +stack_size1);
			IJ.log("1.Sinogram Correction is enabled");
			//execute
			sinogram();
			return true;
    	}
		
		return true;
	}
	private void sinogram() {
		// TODO Auto-generated method stub
		IJ.log("*****Processing:Sinogram Correction*****");
		ImagePlus median_filter_sinogram,substracted_sinogram,xyz;
		ImageCalculator ic;
		ImageProcessor original_sinogram_ip,substracted_sinogram_ip,resultant_image_ip;
		float[][] original_sinogram_pixel_arr;
		float[][] substracted_sinogram_pixel_arr;
		int stack_size,columns,rows;
		ImageStack imageStack;
		
		
		
		stack_size=original_sinogram.getStackSize();
		columns = original_sinogram.getWidth();
		rows = original_sinogram.getHeight();
		imageStack=new ImageStack(columns,rows);
		float u[]=new float[rows];
	    float medianArray[]=new float[columns];
	    float[][] corrected_pixel_arr=new float[columns][rows];
	    float[][] corrected_pixel_arr1=new float[columns][rows];
	    
	   
		
		median_filter_sinogram=original_sinogram.duplicate();
		IJ.run(median_filter_sinogram, "Median...", "radius=2 stack");
		
		ic = new ImageCalculator();
		//16 bit or 32 bit .. have to decide
		substracted_sinogram= ic.run("Subtract create 32-bit stack", original_sinogram, median_filter_sinogram);
		substracted_sinogram.show();
		resultant_image_ip=new FloatProcessor(columns,rows);
	    
	     
		
		for(int r=1;r<=stack_size;r++) {
			
			original_sinogram.setSlice(r);
			substracted_sinogram.setSlice(r);
			original_sinogram_ip=original_sinogram.getProcessor();
			original_sinogram_pixel_arr=original_sinogram_ip.getFloatArray();
			substracted_sinogram_ip=substracted_sinogram.getProcessor();
			substracted_sinogram_pixel_arr=substracted_sinogram_ip.getFloatArray();
	 
	        
	        for(int j=0;j<columns;j++) {
	        	
	        	for(int i=0;i<rows;i++) {
	               	u[i]=substracted_sinogram_pixel_arr[j][i];
	               }
	        	int n=u.length;
	       		float a=(float) median(u,n);
	       		medianArray[j]=a;
	       		
	       		for(int i=0;i<rows;i++) {
	       			
	       			corrected_pixel_arr[j][i]=(original_sinogram_pixel_arr[j][i]-medianArray[j]);
	       		}
	        }
	        
	        int n=medianArray.length;
	       
	        float meanVal=(float) mean(medianArray,n);
	     
	        
	        for(int j=0;j<columns;j++) {
	        	for(int i=0;i<rows;i++) {
	        		corrected_pixel_arr1[j][i]=(corrected_pixel_arr[j][i]-meanVal);
	        	}
	        }
	        
	        resultant_image_ip.setFloatArray(corrected_pixel_arr1);
	        imageStack.addSlice(resultant_image_ip);
	        /*System.out.print("\n");
			
			for( int x=0;x<728;x++) {
				
				System.out.print(medianArray[x]+",");
			}*/
			
		}
		
		
		  xyz =new ImagePlus("Corrected Sinogram",imageStack);
	      xyz.show();
	      if(sinogram_correction_destination_dirpath!=null) {
	        	//sinogram_destination_dirpath=IJ.getDirectory("Choose a Directory to Save sinogram Stack");
	        	IJ.run("Image Sequence... ", "format=TIFF name=Image save=["+sinogram_correction_destination_dirpath+"]");
	        	IJ.log("Corrected Sinograms Saved at "+sinogram_correction_destination_dirpath);
	        }
	      
	      IJ.log("*****Sinogram Correction:Done*****");
	      original_sinogram_pixel_arr=null;
	      substracted_sinogram_pixel_arr=null;
	      u=null;
	      medianArray=null;
	      corrected_pixel_arr=null;
	      corrected_pixel_arr1=null;
	
	}
	public  double mean(float a[], int n)
    {
        int sum = 0;
        for (int i = 0; i < n; i++) 
            sum += a[i];
     
        return (double)sum / (double)n;
    }
	
	public double median(float a[], int n)
	    {
	        // First we sort the array
	        Arrays.sort(a);
	 
	        // check for even case
	        if (n % 2 != 0)
	        return (double)a[n / 2];
	     
	        return (double)(a[(n - 1) / 2] + a[n / 2]) / 2.0;
	    }
	
	
}


