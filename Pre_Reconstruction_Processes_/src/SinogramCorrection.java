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
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.Arrays;


public class SinogramCorrection implements PlugIn {
	String source_dirpath="";
	String destination_dirpath="";
	ImagePlus original_sinogram,median_filter_sinogram,substracted_sinogram,xyz;
	ImageCalculator ic;
	ImageProcessor original_sinogram_ip,substracted_sinogram_ip,resultant_image_ip;
	float[][] original_sinogram_pixel_arr;
	float[][] substracted_sinogram_pixel_arr;
	int stack_size,columns,rows;
	ImageStack imageStack;
	
	@Override
	public void run(String arg) {
		
		source_dirpath = IJ.getDirectory("Choose a Directory");
		IJ.run("Image Sequence...", "open=["+source_dirpath+"]  sort");
		original_sinogram = IJ.getImage();
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
		substracted_sinogram= ic.run("Subtract create 16-bit stack", original_sinogram, median_filter_sinogram);
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

