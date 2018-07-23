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
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;


public class Normalisation_Beam_Correction implements PlugIn, DialogListener {
	
	String beamcorrection_destination_dirpath;
	ImagePlus source_imp,zproject_imp;
	
	public void run(String arg) {
		
		source_imp = IJ.getImage();
		
		
		// Create Gui and Start
		createGui();
		
		// invoke garbage collection
		System.gc();
					
		
	}

	private void createGui() {
		// TODO Auto-generated method stub
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes:NormalisationBeamCorrection");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		
		CheckboxGroup beamcorrection = new CheckboxGroup();  
	    Checkbox checkBox1 = new Checkbox("Normalisation Beam Correction", beamcorrection, true);    
	    checkBox1.setPreferredSize(d_textField);
	    gd.add(checkBox1);
	    gd.addMessage("");
	    TextField textField1 = new TextField("Save Result..(optional)");
	  		textField1.setPreferredSize(d_textField);
	  		textField1.setEditable(false);
	  		gd.add(textField1);
	  		
	  		Button beamcorrection_save_button=new Button("Browse");
			beamcorrection_save_button.setPreferredSize(d_button);
			gd.add(beamcorrection_save_button);
			gd.addMessage("");
			beamcorrection_save_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
						beamcorrection_destination_dirpath=IJ.getDirectory("Choose a Directory to Save Beam Corrected Image Stack");
						textField1.setText(beamcorrection_destination_dirpath);
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
				IJ.log("1.Normalisation Beam Correction is enabled");
				//execute
				beamcorrection();
				return true;
	    	}
			
			return true;

	}

	private void beamcorrection() {
		IJ.log("*****Processing:Beam Correction*****");
		int a;
		double u,v,w,x;
		
		IJ.log("Calculating Z Projections: Sum Slices");
		IJ.run(source_imp,"Z Project...", "projection=[Sum Slices]");
		zproject_imp = IJ.getImage();

		while(zproject_imp.getRoi()==null) {
			new WaitForUserDialog("For Normalisation(Beam Correction):Select Area then click OK").show();
				if(zproject_imp.isVisible()==false||source_imp.isVisible()==false) {
					IJ.run("Quit");
				}
		}
		
		IJ.selectWindow(source_imp.getTitle());
		IJ.run("Restore Selection", "");
		
		zproject_imp.changes=false;
		zproject_imp.close();
		
		a=source_imp.getNSlices();
		source_imp.setSlice(a/2);
		IJ.run(source_imp,"Measure","");
		for(int i=1; i<=a; i++){
			source_imp.setSlice(i);
			IJ.run(source_imp,"Measure","");
		}
		IJ.run(source_imp,"Select All","");
		for(int i=1; i<=a; i++) {
			source_imp.setSlice(i);
			u=ResultsTable.getResultsTable().getValue("Mean", 0);
			v =ResultsTable.getResultsTable().getValue("Mean",i);
			w=u/v;
			x=v/u;
			
			
			if(w<1){
				//System.out.println(w);
				//System.out.println("Multiply");
				//System.out.println(w*x);
				IJ.run(source_imp,"Multiply...", "value="+x);
			}
			if(w>1){
				//System.out.println(w);
				//System.out.println("Divide");
				//System.out.println(w*x);
				IJ.run(source_imp,"Divide...", "value="+x);
			}

			}
			IJ.resetMinAndMax();
			
			if(beamcorrection_destination_dirpath!=null) {
				//beamcorrection_destination_dirpath=IJ.getDirectory("Choose a Directory to Save Beam Corrected Image Stack");
				IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+beamcorrection_destination_dirpath+"]");
				IJ.log("Beam Correction Result Saved at "+beamcorrection_destination_dirpath);
			}
			IJ.log("*****Beam Correction:Done*****");

	}
	
}

