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
import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class Despeckle_ implements PlugIn, DialogListener {
	String noise_destination_dirpath;
	ImagePlus source_imp;
	
	public void run(String arg) {
		
		source_imp = IJ.getImage();
		
		
		// Create Gui and Start
		createGui();
		
		// invoke garbage collection
		System.gc();
					
		
	}

	private void createGui() {
		// TODO Auto-generated method stub
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes:Despeckle");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		
		CheckboxGroup noise = new CheckboxGroup();  
	    Checkbox checkBox1 = new Checkbox("Despeckle", noise, true);    
	    checkBox1.setPreferredSize(d_textField);
	    gd.add(checkBox1);
	    gd.addMessage("");
	    TextField textField1 = new TextField("Save Result..(optional)");
	  		textField1.setPreferredSize(d_textField);
	  		textField1.setEditable(false);
	  		gd.add(textField1);
	  		
	  		Button crop_save_button=new Button("Browse");
	  		gd.add(crop_save_button);
	  		crop_save_button.setPreferredSize(d_button);
	  		crop_save_button.addActionListener(new java.awt.event.ActionListener() {
	  	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	  	            	noise_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Result Image stack");
	  	            	textField1.setText(noise_destination_dirpath);
	  	            	
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
				
				int stack_size1 = source_imp.getImageStackSize();
				IJ.log("********Input Parameters********");
				IJ.log("Stack size: " +stack_size1);
				IJ.log("1.Despeckle is enabled");
				//execute
				despeckle();
				return true;
	    	}
			
			return true;

	}

	private void despeckle() {
		IJ.log("*****Processing:Despeckle*****");
		// TODO Auto-generated method stub
		IJ.run(source_imp, "Despeckle", "stack");
		if(noise_destination_dirpath!=null) {
		IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+noise_destination_dirpath+"]");
		IJ.log("Despeckle Result saved at "+noise_destination_dirpath);
		}
		IJ.log("*****Despeckle:Done*****");

	}
	
}

