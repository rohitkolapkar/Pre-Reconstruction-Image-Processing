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
import ij.gui.WaitForUserDialog;
import ij.plugin.PlugIn;


public class Crop_ implements PlugIn, DialogListener {
	
	String crop_destination_dirpath;
	ImagePlus source_imp,zproject_imp;
	
	public void run(String arg) {
		
		source_imp = IJ.getImage();
		
		
		// Create Gui and Start app
		createGui();
		
		// invoke garbage collection
		System.gc();
					
		
	}

	private void createGui() {
		// TODO Auto-generated method stub
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes:Crop");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		
		CheckboxGroup crop = new CheckboxGroup();  
	    Checkbox checkBox1 = new Checkbox("Crop", crop, true);    
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
	  	            	crop_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Cropped Image stack");
	  	            	textField1.setText(crop_destination_dirpath);
	  	            	
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
				IJ.log("1.Crop is enabled");
				//execute
				crop();
				return true;
	    	}
			
			return true;

	}

	private void crop() {
		IJ.log("*****Processing:Crop*****");
		// TODO Auto-generated method stub
		IJ.run(source_imp,"Z Project...", "projection=[Sum Slices]");
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
		if(crop_destination_dirpath!=null) {
		IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+crop_destination_dirpath+"]");
		IJ.log("Cropping Result saved at "+crop_destination_dirpath);

		}
		IJ.log("*****Crop:Done*****");

	}
	
}

