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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.WaitForUserDialog;
import ij.measure.ResultsTable;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Pre_Reconstruction_Processes_ implements PlugIn, DialogListener{
	
	// Variables
	private int crop_type=2,noise_type=3,beamcorrection_type=2,flatfieldcorrection_type=2,sinogram_type=2,sinogram_correction_type=2; 
	//Boolean crop_save_change,noise_save_change,beamcorrection_save_change,sinogram_save_change;
	long start_time;
	short[][][] F;
	short[][][] Fnew;
	String crop_destination_dirpath,noise_destination_dirpath,beamcorrection_destination_dirpath,flatfieldcorrection_destination_dirpath,sinogram_destination_dirpath,sinogram_correction_destination_dirpath;
	String bck_filepath;
	String ref_filepath;
	static ImagePlus source_imp,sino_source_imp,zproject_imp,original_sinogram;
			
	public void run(String arg) {
		
		// TODO Auto-generated method stub
		/*source_dirpath = IJ.getDirectory("Choose a Directory");
		IJ.run("Image Sequence...", "open=["+source_dirpath+"] sort");*/
		source_imp = IJ.getImage();
		
		
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
		
		
		//crop
		Checkbox crop_checkbox = new Checkbox("Crop");
		crop_checkbox.setPreferredSize(d_textField);
		crop_checkbox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) {	
            	if(crop_checkbox.getState()==true) {
            		crop_type=1;	
            	}
            	if(crop_checkbox.getState()==false) {
            		crop_type=2;
            	}
               
            }  
         });
		gd.add(crop_checkbox);
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
		

		
		
		//Despeckle or Remove Outlier
		
		Checkbox noise_checkbox = new Checkbox("Noise");
		CheckboxGroup noise = new CheckboxGroup();  
	    Checkbox checkBox3 = new Checkbox("Despeckle", noise, true);    
	    Checkbox checkBox4 = new Checkbox("Remove Outliers", noise, false);
		noise_checkbox.setPreferredSize(d_textField);
		checkBox3.setPreferredSize(d_textField);
		checkBox4.setPreferredSize(d_textField);
		noise_checkbox.addItemListener(new ItemListener() {  
            public void itemStateChanged(ItemEvent e) { 
            	if(noise_checkbox.getState()==true) {
            		//checkBox3.setState(true);
	            	noise_type=1;
            	}
            	if(noise_checkbox.getState()==false) {
            		checkBox3.setState(false);
            		checkBox4.setState(false);
	            	noise_type=3;
            	}
               
            	
            }  
         });
		
		 gd.add(noise_checkbox);
		 gd.addMessage("");

	     gd.add(checkBox3);
	     gd.addMessage("");
		 gd.add(checkBox4);
		 gd.addMessage("");
	   
	    checkBox3.addItemListener(new ItemListener() {
	            public void itemStateChanged(ItemEvent e) {   
	            	if(noise_checkbox.getState()==true) {
	            		 if(noise.getSelectedCheckbox()==checkBox3) {
		            		 noise_type=1;
		     	        	
		     	        }
	            	}
	            	
	            }
	         });
	    checkBox4.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {   
            	if(noise_checkbox.getState()==true) {
            		 if(noise.getSelectedCheckbox()==checkBox4) {
                		 noise_type=2;
         	        }
            	}
            	
            }
         });
	    TextField textField2 = new TextField("Save Result..(optional)");
		textField2.setPreferredSize(d_textField);
		textField2.setEditable(false);
		gd.add(textField2);
		
		Button noise_save_button=new Button("Browse");
		noise_save_button.setPreferredSize(d_button);
		gd.add(noise_save_button);
		
		noise_save_button.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent evt) {
	    			noise_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Result Image Stack");
	    			textField2.setText(noise_destination_dirpath);
	            }
	        });
	
		gd.addMessage("");
		gd.addMessage("");
		
		 
		 //beam correction
		 Checkbox beam_correction_checkbox = new Checkbox("Normalisation(Beam Correction)");
		 beam_correction_checkbox.setPreferredSize(d_textField);
		 beam_correction_checkbox.addItemListener(new ItemListener() {  
	            public void itemStateChanged(ItemEvent e) { 
	            	if(beam_correction_checkbox.getState()==true) {
	            		beamcorrection_type=1;
	            	}
	            	if(beam_correction_checkbox.getState()==false) {
	            		beamcorrection_type=2;
	            	}
	            }  
	         });
			
			gd.add(beam_correction_checkbox);
			 gd.addMessage("");
			TextField textField3 = new TextField("Save Result..(optional)");
			textField3.setPreferredSize(d_textField);
			textField3.setEditable(false);
			gd.add(textField3);
			
			Button beamcorrection_save_button=new Button("Browse");
			beamcorrection_save_button.setPreferredSize(d_button);
			gd.add(beamcorrection_save_button);
			gd.addMessage("");
			beamcorrection_save_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
						beamcorrection_destination_dirpath=IJ.getDirectory("Choose a Directory to Save Beam Corrected Image Stack");
						textField3.setText(beamcorrection_destination_dirpath);
		            }
		        });
			 gd.addMessage("");
			 gd.addMessage("");
			 
			 //flat field correction
			 
			 Checkbox flat_field_correction_checkbox = new Checkbox("Flat Field Correction");
			 flat_field_correction_checkbox.setPreferredSize(d_textField);
			 flat_field_correction_checkbox.addItemListener(new ItemListener() {  
		            public void itemStateChanged(ItemEvent e) { 
		            	if(flat_field_correction_checkbox.getState()==true) {
		            		flatfieldcorrection_type=1;
		            	}
		            	if(flat_field_correction_checkbox.getState()==false) {
		            		flatfieldcorrection_type=2;
		            	}
		            }  
		         });
				gd.add(flat_field_correction_checkbox);
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
				
				//sinogram creation
				
				Checkbox sinogram_checkbox = new Checkbox("Create Sinogram");
				sinogram_checkbox.setPreferredSize(d_textField);
				sinogram_checkbox.addItemListener(new ItemListener() {  
			            public void itemStateChanged(ItemEvent e) {
			            	if(sinogram_checkbox.getState()==true) {
			            		sinogram_type=1;
			            	}
			            	if(sinogram_checkbox.getState()==false) {
			            		sinogram_type=2;
			            	}
			            	
			            }  
			         });
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
				
				
				//sinogram_correction
				Checkbox sinogram_correction_checkbox = new Checkbox("Sinogram Correction");
				sinogram_correction_checkbox.setPreferredSize(d_textField);
				sinogram_correction_checkbox.addItemListener(new ItemListener() {  
		            public void itemStateChanged(ItemEvent e) {
		            	if(sinogram_correction_checkbox.getState()==true) {
		            		sinogram_correction_type=1;
		            	}
		            	if(sinogram_correction_checkbox.getState()==false) {
		            		sinogram_correction_type=2;
		            	}
		            	
		            }  
		         });
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
				
				
				//checkall //uncheckall
				CheckboxGroup toggle = new CheckboxGroup();
				Checkbox check_all = new Checkbox("Select All",toggle,false);
				check_all.setPreferredSize(d_textField);
				check_all.addItemListener(new ItemListener() {  
		            public void itemStateChanged(ItemEvent e) { 
		            	    
		            	
		            	crop_checkbox.setState(true);
                    	crop_type=1;
                    	noise_checkbox.setState(true);
                    	checkBox3.setState(true);
                    	noise_type=1;
                    	beam_correction_checkbox.setState(true);
                    	beamcorrection_type=1;
			            flat_field_correction_checkbox.setState(true);
			            flatfieldcorrection_type=1;
			            sinogram_checkbox.setState(true);
			            sinogram_type=1;
			            sinogram_correction_checkbox.setState(true);
			            sinogram_correction_type=1;
			            	
		            	
		            	
		            }  
		         });
				gd.add(check_all);
				gd.addMessage("");
				
				Checkbox uncheck_all = new Checkbox("Select None",toggle,false);
				uncheck_all.setPreferredSize(d_textField);
				uncheck_all.addItemListener(new ItemListener() {  
		            public void itemStateChanged(ItemEvent e) { 
		            	
		            		crop_checkbox.setState(false);
		            		crop_type=2;
			            	noise_checkbox.setState(false);
			            	checkBox3.setState(false);
			            	
			            	noise_type=3;
			            	beam_correction_checkbox.setState(false);
			            	beamcorrection_type=2;
			            	flat_field_correction_checkbox.setState(false);
			            	flatfieldcorrection_type=2;
			            	sinogram_checkbox.setState(false);
			            	sinogram_type=2;
			            	sinogram_correction_checkbox.setState(false);
			            	sinogram_correction_type=2;
			            	
		            	
		            }  
		         });
				gd.add(uncheck_all);
				
				
				/*Button check_all_button=new Button("Select All");
				check_all_button.setPreferredSize(d_button);
				gd.add(check_all_button);
				check_all_button.addActionListener(new java.awt.event.ActionListener() {
		            public void actionPerformed(java.awt.event.ActionEvent evt) {
		            if(check_all_button.getLabel()=="Select All") {
		            	crop_checkbox.setState(true);
			            noise_checkbox.setState(true);
			            beam_correction_checkbox.setState(true);
			            flat_field_correction_checkbox.setState(true);
			            sinogram_checkbox.setState(true);
			            sinogram_correction_checkbox.setState(true);
			            check_all_button.setLabel("Select None");
			   
		            }
		            if(check_all_button.getLabel()=="Select None") {
		            	crop_checkbox.setState(false);
			            noise_checkbox.setState(false);
			            beam_correction_checkbox.setState(false);
			            flat_field_correction_checkbox.setState(false);
			            sinogram_checkbox.setState(false);
			            sinogram_correction_checkbox.setState(false);
			            check_all_button.setLabel("Select All");
		            }
		            
		            }
		        });*/
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
			
			if(flatfieldcorrection_type==1) {
        		if(bck_filepath==null||ref_filepath==null||flatfieldcorrection_destination_dirpath==null) {
        			IJ.showMessage("Flatfield Field Correction Enabled:"+"\n"+"Provide 1.Background img path 2.Reference img path 3.Save result path"+"\n"+"and run the plugin again");
        			return false;
        		}
        	}
			
			int stack_size1 = source_imp.getImageStackSize();
			IJ.log("********Input Parameters********");
			IJ.log("Stack size: " +stack_size1);
			
			// crop
			if (crop_type==1) {
				IJ.log("1.Crop is enabled");
				 // 1 is used for enable 
			}
			else {
				IJ.log("1.Crop is disabled");
				 // 2 is for disable
			}
					
			//Noise
			if(noise_type==1) {
				IJ.log("2.Despeckle is enabled");
			 // 1 is used for despeckle 
			}
			else if (noise_type==2){
				IJ.log("2.Remove Outliers is enabled");
				 // 2 is for remove outliers
			}
			else {
				IJ.log("2.Despeckle/Remove Outliers is disabled");
				 // 3 is for disable
			}
			
			
			// Beam Correction
			if (beamcorrection_type==1) {
				IJ.log("3.Beam Correction is enabled");
				// 1 is used for enable 
			}
			else {
				IJ.log("3.Beam Correction is disabled");
				 // 2 is for disable
			}

			
			// Flat Field Correction
			if (flatfieldcorrection_type==1) {
				IJ.log("4. Flat Field Correction is enabled");
				 // 1 is used for enable
			}
			else {
				IJ.log("4. Flat Field Correction is disabled");
				 // 2 is for disable
			}
			
			// sinogram creation
			if (sinogram_type==1){
				IJ.log("5. Sinogram creation is enabled");
				 
			}
			else {
				IJ.log("5. Sinogram creation is disabled");
				sinogram_type=2; 
			}
			
			// sinogram correction
			if (sinogram_correction_type==1){
				IJ.log("6. Sinogram correction is enabled");
				
			}
			else {
				IJ.log("6. Sinogram correction is disabled");
				 
			}
			
			//execute
			execute(crop_type,noise_type,beamcorrection_type,flatfieldcorrection_type,sinogram_type,sinogram_correction_type);
			return true;
    	}
		
		return true;
		
	}
	
	//execute functionalities according to different selections
	
	void execute(int crop_types,int noise_types,int beamcorrection_types,int flatfieldcorrection_types,int sinogram_types,int sinogram_correction_types) {
		
		if(crop_types==1) {
			crop();
		}
		if(noise_types==1) {
			despeckle();
		}
		if(noise_types==2) {
			removeOutliers();
		}
		if(beamcorrection_types==1) {
			beamcorrection();
		}
		if(flatfieldcorrection_types==1) {
			flatfieldcorrection();
			
		}
		if(sinogram_types==1) {
			create_3Darray();
		}
		if(sinogram_correction_types==1) {
			sinogramcorrection();
		}
		
		IJ.log("Done!");
	}

      //crop function
	void crop() {
		
		IJ.log("*****Processing:Crop*****");
		IJ.run("Z Project...", "projection=[Sum Slices]");
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
		if (crop_destination_dirpath!=null) {
			//crop_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Cropped Image stack ");
			IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+crop_destination_dirpath+"]");
			IJ.log("Cropping Result saved at "+crop_destination_dirpath);
			}
		IJ.log("*****Crop:Done*****");
		
	}
	
	//despeckle function
	void despeckle() {
		IJ.log("*****Processing:Despeckle*****");
		IJ.run(source_imp, "Despeckle", "stack");
		if(noise_destination_dirpath!=null) {	
			//noise_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Despeckle Result");
			IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+noise_destination_dirpath+"]");
			IJ.log("Despekle Result Saved at "+noise_destination_dirpath);
		}
		IJ.log("*****Despeckle:Done*****");
	}
	
	//removeOutliers Function
	void removeOutliers() {
		
		IJ.log("*****Processing:Remove Outliers*****");
		IJ.run(source_imp, "Remove Outliers...", "radius=4 threshold=20 which=Bright stack");
		IJ.wait(1000);
		if(noise_destination_dirpath!=null) {
			//noise_destination_dirpath = IJ.getDirectory("Choose a Directory to Save Remove Outliers Result");
			IJ.run(source_imp, "Image Sequence... ", "format=TIFF name=Image save=["+noise_destination_dirpath+"]");
			IJ.log("Remove Outlier Result Saved at "+noise_destination_dirpath);
		}
		IJ.log("*****Remove Outliers:Done*****");
	}
	
	//beamCorrection Function
	void beamcorrection() {
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
	
	//Flat Field Correction function
	void flatfieldcorrection(){
		
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
		
	// Create 3D array
	void create_3Darray(){
		
			/*if(flatfieldcorrection_destination_dirpath!=null) {
				
			IJ.run("Image Sequence...", "open=["+flatfieldcorrection_destination_dirpath+"] sort");
			sino_source_imp = IJ.getImage();
			source_imp=sino_source_imp;
			}*/
		
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
				
				IJ.log("********Processing:Sinogram Creation**************");
				long t1 = System.currentTimeMillis(); // start timer
			
			// 1. Loop through all slices (set 3D array)
				IJ.log("1. Creating 3D array");
				for (int ss=1;ss<=stack_size;ss++){
					//Set ith image from stack
					source_imp.setSlice(ss);
				
					// Create processor
					ip_temp = source_imp.getProcessor();
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

	void sinogramcorrection() {
		
		IJ.log("*****Processing:Sinogram Correction*****");
		
		if(sinogram_type==2) {
			original_sinogram=source_imp;
		}
		
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