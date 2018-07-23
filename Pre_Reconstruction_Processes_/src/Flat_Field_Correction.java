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
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;


public class Flat_Field_Correction implements PlugIn, DialogListener {
	
	String flatfieldcorrection_destination_dirpath;
	ImagePlus source_imp;
	String bck_filepath;
	String ref_filepath;
	
	public void run(String arg) {
		
		source_imp = IJ.getImage();
		
		
		// Create Gui and Start
		createGui();
		
		// invoke garbage collection
		System.gc();
					
		
	}

	private void createGui() {
		// TODO Auto-generated method stub
		GenericDialog gd = new GenericDialog("Pre Reconstruction Processes:Flat Field Correction");
		Dimension d_textField=new Dimension();
		d_textField.height=25;
		d_textField.width=250;
		
		Dimension d_button=new Dimension();
		d_button.height=25;
		d_button.width=75;
		
		CheckboxGroup flatfieldcorrection = new CheckboxGroup();  
	    Checkbox checkBox1 = new Checkbox("Flat Field Correction", flatfieldcorrection, true);    
	    checkBox1.setPreferredSize(d_textField);
	    gd.add(checkBox1);
			gd.addMessage("");
			TextField textField4_0 = new TextField("Choose Bckground Img**");
			textField4_0.setPreferredSize(d_textField);
			textField4_0.setEditable(false);
			gd.add(textField4_0);
		
			Button flatfieldcorrection_loadbckimg_button=new Button("Browse*");
			flatfieldcorrection_loadbckimg_button.setPreferredSize(d_button);
			gd.add(flatfieldcorrection_loadbckimg_button);
			gd.addMessage("");
			flatfieldcorrection_loadbckimg_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		            	bck_filepath=IJ.getFilePath("Choose a Background Image");
		            	textField4_0.setText(bck_filepath);
		            }
		        });
			
			TextField textField4_1 = new TextField("Choose Reference Img**");
			textField4_1.setPreferredSize(d_textField);
			textField4_1.setEditable(false);
			gd.add(textField4_1);
		
			Button flatfieldcorrection_loadrefimg_button=new Button("Browse*");
			flatfieldcorrection_loadrefimg_button.setPreferredSize(d_button);
			gd.add(flatfieldcorrection_loadrefimg_button);
			gd.addMessage("");
			flatfieldcorrection_loadrefimg_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		            	ref_filepath=IJ.getFilePath("Choose a Reference Image");
		            	textField4_1.setText(ref_filepath);
		            }
		        });
			
			TextField textField4 = new TextField("Save Result(mandatory)");
			textField4.setPreferredSize(d_textField);
			textField4.setEditable(false);
			gd.add(textField4);
		
			Button flatfieldcorrection_save_button=new Button("Browse*");
			flatfieldcorrection_save_button.setPreferredSize(d_button);
			gd.add(flatfieldcorrection_save_button);
			
			flatfieldcorrection_save_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		        		flatfieldcorrection_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Flat Field Corrected Image Stack");
		        		textField4.setText(flatfieldcorrection_destination_dirpath);	
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
				
				if(bck_filepath==null||ref_filepath==null||flatfieldcorrection_destination_dirpath==null) {
	        			IJ.showMessage("Flatfield Field Correction Enabled:"+"\n"+"Provide 1.Background img path 2.Reference img path 3.Save result path"+"\n"+"and run the plugin again");
	        			return false;
	        		}
	        	
				
				int stack_size1 = source_imp.getImageStackSize();
				IJ.log("********Input Parameters********");
				IJ.log("Stack size: " +stack_size1);
				IJ.log("1.Flat Field Correction is enabled");
				//execute
				flatfieldcorrection();
				return true;
	    	}
			
			return true;

	}

	private void flatfieldcorrection() {
		IJ.log("*****Processing:Flat Field Correction*****");
		ImagePlus bck_imp,ref_imp,source_sub_bck_imp,ref_sub_bck_ipm;
		
		//flatfieldcorrection_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Flat Field Corrected Image Stack");

		//bck_filepath=IJ.getFilePath("Choose a Background Image");
		IJ.open(bck_filepath);
		bck_imp=IJ.getImage();
		
		
		//ref_filepath=IJ.getFilePath("Choose a Reference Image");
		IJ.open(ref_filepath);
		ref_imp=IJ.getImage();
		


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
			IJ.saveAs(divide_imp, "Tiff", flatfieldcorrection_destination_dirpath+"Image"+i);
			//tw.append(destination_dirpath+source_imp.getTitle()+i+" "+"done");
			//IJ.log(destination_dirpath+source_imp.getTitle()+i+" "+"done");
		}
		IJ.run("Image Sequence...", "open=["+flatfieldcorrection_destination_dirpath+"] sort");
		source_imp=IJ.getImage();
		
		IJ.log("Flat Field Correction Result Saved at "+flatfieldcorrection_destination_dirpath);
		IJ.log("*****Flat Field Correction:Done*****");
		

	}
	
}

