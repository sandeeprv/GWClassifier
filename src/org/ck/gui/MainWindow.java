package org.ck.gui;

import java.util.ArrayList;

import org.ck.dt.DecisionTreeClassifier;
import org.ck.ga.Genome;
import org.ck.ga.OptimalScoreException;
import org.ck.gui.Constants.DatasetOptions;
import org.ck.sample.DataHolder;
import org.ck.sample.Sample;
import org.ck.sample.SampleCollection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

public class MainWindow implements Constants
{
	private Shell shell;
	private Display display;
	
	private int gridHorizontalSpacing = 10;
	private int gridVerticalSpacing = 4;
	private int gridMarginBottom = 5;
	private int gridMarginTop = 5;
	private int gridPadding = 2;
	
	private Slider fitnessSlider;
	private Label fitnessSliderLabel;
	private Button runButton;
	
	private Slider crossoverSlider;
	private Label crossoverSliderLabel;
	
	private Slider mutationSlider;
	private Label mutationSliderLabel;
	
	private Table trainingSamplesTable = null;
	private Button discretizeCheckBox;
	private Table testingSamplesTable = null;
		
	private StyledText accuracyTextArea;
	   
    private Table userSamplesTable = null;
    private Button classifyButton;
    private Label classifyResultLabel;
    
    private Tree graphicalDecisionTree = null;
    
	private OptimalScoreException currentException = null;
	/*
	 * A constructor that takes in a display parameter and initializes the shell and other components of the UI
	 */
	public MainWindow(Display display)
	{
		this.display = display;
		
		shell = new Shell(display);
		shell.setText("Decision Tree Classifier");
		
		centerShell();
		initUI();
		
		shell.setSize(1024, 900);
		shell.setLocation(480, 0);
		
		shell.open();
		while(!shell.isDisposed())
		{
			if(!display.readAndDispatch())
				display.sleep();
		}		
	}
	
	/*
	 * The Main method
	 */
	public static void main(String args[])
	{
		Display display = new Display();
		new WelcomeWindow(display);
		display.dispose();
	}
	
	/*
	 * Centers the shell on the screen.... Doesn't work
	 */
	private void centerShell() 
	{		
		Rectangle bds = shell.getDisplay().getBounds();
		
		Point p = shell.getSize();
		
		int nLeft = (bds.width - p.x) / 2;
		int nTop = (bds.height - p.y) / 2;
		
		shell.setBounds(nLeft, nTop, p.x, p.y);
	}

	
	/*
	 * Initializes the UI
	 */
	private void initUI()
	{	
		//Initialize Grid Layout parameters
		GridLayout gridLayout = new GridLayout(gridHorizontalSpacing, true);
		gridLayout.horizontalSpacing = gridHorizontalSpacing;
		gridLayout.verticalSpacing = gridVerticalSpacing;
		gridLayout.marginBottom = gridMarginBottom;
		gridLayout.marginTop = gridMarginTop;
		shell.setLayout(gridLayout);
		
		//Adding Widgets
		addLabel("THE ULTIMATE CLASSIFIER", gridHorizontalSpacing, SWT.CENTER);
		addDataSamplesComboBox();
		//addBreak(gridHorizontalSpacing / 2);
		
		addListBox();
		addBreak(gridHorizontalSpacing);
		
		fitnessSlider = addFitnessThresholdSlider();
		fitnessSlider.setVisible(false);
		
		crossoverSlider = addCrossoverRateSlider();
		crossoverSlider.setVisible(false);
		
		mutationSlider = addMutationRateSlider();
		mutationSlider.setVisible(false);
				
		addBreak(4);
		runButton = addRunButton();	
		runButton.setVisible(false);
		
		addBreak(gridHorizontalSpacing);
		addTrainingSamplesTable(false);
		addTestingSamplesTable(false);
		addDiscretizeCheckbox();		
		
		addResultDisplay();
		
		//addBreak(1);
		addEditableSamplesTable();
		addBreak(gridHorizontalSpacing / 2);
		classifyButton = addClassifyButton();		
		classifyButton.setVisible(false);	
		
		addBreak(gridHorizontalSpacing / 4);
		addGraphicalDecisionTree();
		graphicalDecisionTree.setVisible(false);
	}


	/*
	 * Adds a run button, to start Machine Learning 
	 */
	private Button addRunButton()
	{
		Button button =  new Button(shell, SWT.PUSH | SWT.CENTER);
		button.setText("Run the Engine");
		
		addToGrid(button, 2);		
		
		button.addSelectionListener(new SelectionAdapter() {
		  @Override
		  public void widgetSelected(SelectionEvent e) {
		    // Handle the selection event
			  try
			  {		
				  MainClass.sampleCaller2();
			  }
			  catch(OptimalScoreException exception)
			  {									  
				  //try{Thread.sleep(1000);} catch (InterruptedException e1){}
				  
				  currentException = exception;
				  
				  accuracyTextArea.setVisible(true);
				  
				  String result = "Training Set Accuracy = " + exception.getTrainingSetAccuracy() + "\n";
				  result += "Test Set Accuracy = " + exception.getTestSetAccuracy() + "\n";
				  result += "Selected Features = " + exception.getSelectedFeatures(); 
				  accuracyTextArea.setText(result);				  
				  
				  //Clear previous selection, if any
				  TableItem items[] = trainingSamplesTable.getItems();
				  for(int i=0; i < items.length; i++)
				  {
					  items[i].setBackground(display.getSystemColor(SWT.COLOR_WHITE));
					  items[i].setForeground(display.getSystemColor(SWT.COLOR_BLACK));
				  }
				  
				  ArrayList<Integer> trainingErrorIndices = exception.getTrainingErrorIndices();
				  for(int index : trainingErrorIndices)
				  {					  
					  trainingSamplesTable.getItem(index).setBackground(display.getSystemColor(SWT.COLOR_RED));
					  trainingSamplesTable.getItem(index).setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
				  }			
				  
				  ArrayList<Integer> testErrorIndices = exception.getTestErrorIndices();
				  for(int index : testErrorIndices)
				  {					  
					  testingSamplesTable.getItem(index).setBackground(display.getSystemColor(SWT.COLOR_RED));
					  testingSamplesTable.getItem(index).setForeground(display.getSystemColor(SWT.COLOR_YELLOW));
				  }			
				  
				  
				  
				  toggleIllegalWidgetsForStep2(true);				  
				  addGraphicalDecisionTree();
			  }
		  }
		});			
		
		return button;
	}

	/*
	 * This method can be used to insert 'num' blank labels to create spaces in the Grid Layout
	 */
	private void addBreak(int num)
	{
		final Label label = new Label(shell, SWT.LEFT);
	    label.setText("");
	
        GridData gridData = new GridData();
	    gridData.horizontalSpan = num;
	    gridData.horizontalAlignment = GridData.FILL;
	    label.setLayoutData(gridData);	
	}

	/*
	 * Adds a combo box to select the name of the data samples file
	 */
	private void addDataSamplesComboBox()
	{
		addLabel("Select Data Samples File: ", gridHorizontalSpacing / 2, SWT.RIGHT);

        final Combo combo = new Combo(shell, SWT.DROP_DOWN);
        for(int i = 0; i < DatasetOptions.values().length; i++)
        	combo.add("" + DatasetOptions.values()[i]);

        combo.select(0);
        combo.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	DataHolder.setDataset(DatasetOptions.valueOf(combo.getText()));
            	Genome.reInitializeStaticVariables();
            	addTrainingSamplesTable(discretizeCheckBox.getSelection());
            	addTestingSamplesTable(discretizeCheckBox.getSelection());
            	addEditableSamplesTable();
            	
            	accuracyTextArea.setText("");
            	toggleIllegalWidgetsForStep2(false);
            };
        });

        addToGrid(combo, gridHorizontalSpacing / 2 - gridPadding);
        addBreak(gridPadding);
	}

	/*
	 * Adds a slider to vary the fitness threshold for the fitness function of the genetic algorithm
	 * 	Returns the initialized slider
	 */
	private Slider addFitnessThresholdSlider()
	{        
		final double sliderRange = 1000;
		
		fitnessSliderLabel = addLabel("Fitness Threshold --->  ", gridHorizontalSpacing / 2, SWT.RIGHT);
		fitnessSliderLabel.setVisible(false);
		
		final Slider slider = new Slider(shell, SWT.HORIZONTAL);
		slider.setMaximum((int)sliderRange);
		slider.setSelection((int) (DataHolder.getFitnessScoreThreshold() * sliderRange));
		addToGrid(slider, gridHorizontalSpacing / 2 - gridPadding);
		addBreak(gridPadding);
		
		fitnessSliderLabel.setText(fitnessSliderLabel.getText() + slider.getSelection() / sliderRange);
		
		slider.addListener (SWT.Selection, new Listener () {
		    public void handleEvent (Event e) {		
		        double value = slider.getSelection() / sliderRange;
		        fitnessSliderLabel.setText("Fitness Threshold --->  " + value);
		        DataHolder.setFitnessScoreThreshold(value);
		        
		        accuracyTextArea.setText("");
		        toggleIllegalWidgetsForStep2(false);
		    }
		});
		
		slider.setVisible(false);
		
		return slider;
	}
	
	/*
	 * Adds a slider to vary the Crossover Rate for the natural selection process of the genetic algorithm
	 * 	Returns the initialized slider
	 */
	private Slider addCrossoverRateSlider()
	{        
		final double sliderRange = 10000;
		
		crossoverSliderLabel = addLabel("Crossover Rate --->  ", gridHorizontalSpacing / 2, SWT.RIGHT);
		crossoverSliderLabel.setVisible(false);
		
		final Slider slider = new Slider(shell, SWT.HORIZONTAL);
		slider.setMaximum((int)sliderRange);
		slider.setSelection((int) (DataHolder.getCrossoverProbabilityThreshold() * sliderRange));
		addToGrid(slider, gridHorizontalSpacing / 2 - gridPadding);
		addBreak(gridPadding);
		
		crossoverSliderLabel.setText(crossoverSliderLabel.getText() + slider.getSelection() / sliderRange);
		
		slider.addListener (SWT.Selection, new Listener () {
		    public void handleEvent (Event e) {		
		        double value = slider.getSelection() / sliderRange;
		        crossoverSliderLabel.setText("Crossove Rate --->  " + value);
		        DataHolder.setCrossoverProbabilityThreshold(value);
		        
		        accuracyTextArea.setText("");
		        toggleIllegalWidgetsForStep2(false);
		    }
		});
		
		slider.setVisible(false);
		
		return slider;
	}
	
	/*
	 * Adds a slider to vary the Mutation Rate for Genome Mutation of the genetic algorithm
	 * 	Returns the initialized slider
	 */
	private Slider addMutationRateSlider()
	{        
		final double sliderRange = 10000;
		
		mutationSliderLabel = addLabel("Mutation Rate --->  ", gridHorizontalSpacing / 2, SWT.RIGHT);
		mutationSliderLabel.setVisible(false);
		
		final Slider slider = new Slider(shell, SWT.HORIZONTAL);
		slider.setMaximum((int)sliderRange);
		slider.setSelection((int) (DataHolder.getMutationProbabilityThreshold() * sliderRange));
		addToGrid(slider, gridHorizontalSpacing / 2 - gridPadding);
		addBreak(gridPadding);
		
		mutationSliderLabel.setText(mutationSliderLabel.getText() + slider.getSelection() / sliderRange);
		
		slider.addListener (SWT.Selection, new Listener () {
		    public void handleEvent (Event e) {		
		        double value = slider.getSelection() / sliderRange;
		        mutationSliderLabel.setText("Mutation Rate --->  " + value);
		        DataHolder.setMutationProbabilityThreshold(value);
		        
		        accuracyTextArea.setText("");
		        toggleIllegalWidgetsForStep2(false);
		    }
		});
		
		slider.setVisible(false);
		
		return slider;
	}
	
	/*
	 * Adds a list box to select an appropriate algorithm for Machine Learning.
	 * 		The UI changes based on the algorithm selected
	 */
	private void addListBox()
	{       
        addLabel("Select Algorithm", gridHorizontalSpacing / 2, SWT.RIGHT);
        
        final List list = new List(shell, SWT.BORDER);        
        list.add("Decision Tree Classifier");
        //list.add("K-Means Clustering Classifier");
        //list.select(0);

        list.addListener(SWT.Selection, new Listener () {
            public void handleEvent (Event e) {
                String[] items = list.getSelection();
                
                if(items[0].equalsIgnoreCase("Decision Tree Classifier"))
                {
                	toggleIllegalWidgetsForStep1(true);
	            	
	            	accuracyTextArea.setText("");
	            	toggleIllegalWidgetsForStep2(false);
                }
                else
                {
                	toggleIllegalWidgetsForStep1(false);
	            	
	            	accuracyTextArea.setText("");
	            	toggleIllegalWidgetsForStep2(false);
                }
            }
        });

        addToGrid(list, gridHorizontalSpacing / 2 - gridPadding);
        addBreak(gridPadding);
	}
	
	private void addDiscretizeCheckbox()
	{
		 discretizeCheckBox = new Button(shell, SWT.CHECK);
	     discretizeCheckBox.setText("Show Discretized Values");
	     discretizeCheckBox.setSelection(false);
	     discretizeCheckBox.setVisible(false);
	     
	     addBreak(5);
	     addToGrid(discretizeCheckBox, 2);

	     discretizeCheckBox.addSelectionListener(new SelectionAdapter()
	     {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (discretizeCheckBox.getSelection()) {
                    addTrainingSamplesTable(true);
                    addTestingSamplesTable(true);
                } else {
                    addTrainingSamplesTable(false);
                    addTestingSamplesTable(false);
                }
            }
        });
	}
	
	/*
	 * Initializes the trainingSamplesTable and selects the appropriate SampleCollection, based on the value of
	 * 		isDiscretize
	 */
	private void addTrainingSamplesTable(boolean isDiscretize)
	{
		Table previousTable = trainingSamplesTable;					//Required for replacement in Grid Layout
		trainingSamplesTable = new Table (shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		
		SampleCollection samplesCollection = null;
		if(isDiscretize)
			samplesCollection = Genome.getSamples();
		else
			samplesCollection = new SampleCollection(DataHolder.getTrainingSamplesFileName(), DataHolder.getAttributesFileName());
		
		addTable(trainingSamplesTable, samplesCollection, previousTable);
	}
	
	/*
	 * Initializes the testingSamplesTable and selects the appropriate SampleCollection, based on the value of
	 * 		isDiscretize
	 */
	private void addTestingSamplesTable(boolean isDiscretize)
	{
		Table previousTable = testingSamplesTable;					//Required for replacement in Grid Layout
		testingSamplesTable = new Table (shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		
		SampleCollection samplesCollection = null;
		if(isDiscretize)
			samplesCollection = currentException.getCurrentDTClassifier().getTestingSamples();
		else
			samplesCollection = new SampleCollection(DataHolder.getTestingSamplesFileName(), DataHolder.getAttributesFileName());
		
		addTable(testingSamplesTable, samplesCollection, previousTable);
	}
	
	/*
	 * This method creates an Excel-type table to display all the samples of the selected SampleCollection 
	 * 		in a samplesTable, replacing any previousSamplesTable that was drawn previously, if any.
	 */
	private void addTable(Table samplesTable, SampleCollection samplesCollection, Table previousSamplesTable)
	{		
		samplesTable.setLinesVisible (true);
		samplesTable.setHeaderVisible (true);
		
		GridData data = new GridData(); //SWT.FILL, SWT.FILL, false, false);
		data.heightHint = 200;
		data.widthHint = 200;
		data.horizontalSpan = gridHorizontalSpacing / 2;
	    data.horizontalAlignment = GridData.FILL;
		samplesTable.setLayoutData(data);
		
		//This block is executed if a previous table has to be overwritten with a new table
		if(previousSamplesTable != null)
		{
			samplesTable.moveAbove(previousSamplesTable);
			previousSamplesTable.dispose();
			samplesTable.getParent().layout();
		}		
		
		ArrayList<String> featureList = samplesCollection.getfeatureList();
		for (String feature : featureList) 
		{
			TableColumn column = new TableColumn(samplesTable, SWT.NONE);
			column.setText(feature);
		}	
		
		//For the last column - classification
		TableColumn column = new TableColumn(samplesTable, SWT.NONE);
		column.setText("Class");
		
		ArrayList<Sample> samplesList = samplesCollection.getSampleAsArrayList();
		
		for(Sample sample : samplesList)
		{
			//sample.display();
			//System.out.println();
			TableItem item = new TableItem (samplesTable, SWT.NONE);
			
			int featureIndex = 0;
			for(String feature : featureList)
				item.setText(featureIndex++, "" + sample.getFeature(feature).getValue());
			item.setText(featureIndex, sample.getClassification());
		}
		
		for (int i=0; i <  featureList.size() + 1; i++) 
		{
			samplesTable.getColumn(i).pack();
		}
	}
	
	/*
	 * Creates a text area where the results of the classification process can be displayed
	 */
	private void addResultDisplay()
	{
		accuracyTextArea = new StyledText (shell, SWT.BORDER);
		accuracyTextArea.setVisible(false);
				
		GridData gridData = new GridData();
	    gridData.horizontalSpan = gridHorizontalSpacing / 2;
	    gridData.heightHint = 70;
	    gridData.horizontalAlignment = GridData.FILL;
	    (accuracyTextArea).setLayoutData(gridData);
	}
	
	/*
	 * This creates a table of 1 row, that accepts user-input for classification
	 */
	private void addEditableSamplesTable()
	{
		Table tempTable = userSamplesTable;					//Required for replacement in Grid Layout
		
		userSamplesTable = new Table (shell, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		userSamplesTable .setLinesVisible (true);
		userSamplesTable .setHeaderVisible (true);
		userSamplesTable.setVisible(false);
		
		GridData data = new GridData(); //SWT.FILL, SWT.FILL, false, false);
		data.heightHint = 60;
		data.widthHint = 100;
		data.horizontalSpan = gridHorizontalSpacing / 2;
	    data.horizontalAlignment = GridData.FILL;
	    userSamplesTable.setLayoutData(data);
		
		//This block is executed if a previous table has to be overwritten with a new table
		if(tempTable != null)
		{
			userSamplesTable.moveAbove(tempTable);
			tempTable.dispose();
			userSamplesTable.getParent().layout();
		}
		
		SampleCollection samplesCollection = new SampleCollection(DataHolder.getTrainingSamplesFileName(), DataHolder.getAttributesFileName());
		
		ArrayList<String> featureList = samplesCollection.getfeatureList();
		for (String feature : featureList) 
		{
			TableColumn column = new TableColumn(userSamplesTable, SWT.NONE);
			column.setText(feature);
		}	
		
		//Fill initial table with dummy values that can be modified
		TableItem item = new TableItem (userSamplesTable, SWT.NONE);		
		ArrayList<Sample> samplesList = samplesCollection.getSampleAsArrayList();					
		int featureIndex = 0;
		for(String feature : featureList)
			item.setText(featureIndex++, "" + samplesList.get(0).getFeature(feature).getValue());	
	
		
		for (int i=0; i <  featureList.size(); i++) 
		{
			userSamplesTable.getColumn(i).pack();
		}
		
		
		//Editor
		final TableEditor editor = new TableEditor (userSamplesTable);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;
		userSamplesTable.addListener (SWT.MouseDown, new Listener () {
			public void handleEvent (Event event) {
				Rectangle clientArea = userSamplesTable.getClientArea ();
				Point pt = new Point (event.x, event.y);
				int index = userSamplesTable.getTopIndex ();
				while (index < userSamplesTable.getItemCount ()) {
					boolean visible = false;
					final TableItem item = userSamplesTable.getItem (index);
					for (int i=0; i<userSamplesTable.getColumnCount (); i++) {
						Rectangle rect = item.getBounds (i);
						if (rect.contains (pt)) {
							final int column = i;
							final Text text = new Text (userSamplesTable, SWT.NONE);
							Listener textListener = new Listener () {
								public void handleEvent (final Event e) {
									switch (e.type) {
										case SWT.FocusOut:
											item.setText (column, text.getText ());
											text.dispose ();
											break;
										case SWT.Traverse:
											switch (e.detail) {
												case SWT.TRAVERSE_RETURN:
													item.setText (column, text.getText ());
													//FALL THROUGH
												case SWT.TRAVERSE_ESCAPE:
													text.dispose ();
													e.doit = false;
											}
											break;
									}
								}
							};
							text.addListener (SWT.FocusOut, textListener);
							text.addListener (SWT.Traverse, textListener);
							editor.setEditor (text, item, i);
							text.setText (item.getText (i));
							text.selectAll ();
							text.setFocus ();
							return;
						}
						if (!visible && rect.intersects (clientArea)) {
							visible = true;
						}
					}
					if (!visible) return;
					index++;
				}
			}
		});
	}
	
	/*
	 * Adds a "classify" button to classify the sample entered by the user in the Editable Table.
	 */
	private Button addClassifyButton()
	{
		Button button =  new Button(shell, SWT.PUSH);
		button.setText("Classify");
		button.setVisible(false);
		
		addToGrid(button, gridHorizontalSpacing / 3);	
		
		classifyResultLabel = addLabel("Result: ", gridHorizontalSpacing / 4, SWT.RIGHT);
		classifyResultLabel.setVisible(false);
		
		button.addSelectionListener(new SelectionAdapter() {
		  @Override
		  public void widgetSelected(SelectionEvent e) 
		  {
			  String line = "";
			  TableItem item = userSamplesTable.getItem(0);
			  for(int i = 0; i < userSamplesTable.getColumnCount(); i++)
				  line += item.getText(i) + ",";
			  line += "null";
			  System.out.println(line);
			  
			  Sample sample = new Sample(line, Genome.getSamples().getfeatureList());
			  //sample.display();
			  
			  SampleCollection trainingSamples = currentException.getCurrentDTClassifier().getTrainingSamples();
			  trainingSamples.discretizeSample(sample);
			  //sample.display();
			  
			  String classification = currentException.getCurrentDTClassifier().Classify(sample);
			  classifyResultLabel.setText("Result: " + classification);
		  }
		});			
		
		return button;
	}
	
	/*
	 * Draws the generated optimal decision tree in the GUI, replacing any instance of an older decision tree 
	 */
	private void addGraphicalDecisionTree()
	{
		Tree previousTree = graphicalDecisionTree;
		
		graphicalDecisionTree = new Tree(shell, SWT.BORDER);
		
		if(currentException != null)
			currentException.getCurrentDTClassifier().getGraphicalDecisionTree(graphicalDecisionTree);
		
		addToGrid(graphicalDecisionTree, gridHorizontalSpacing / 2);
		
		//This block is executed if a previous table has to be overwritten with a new table
		if(previousTree != null)
		{
			graphicalDecisionTree.moveAbove(previousTree);
			previousTree.dispose();
			graphicalDecisionTree.getParent().layout();
		}
	}
	
	/*
	 * This method creates a label widget with the lyrics as the text parameter, and size = horizontalSpacing
	 * 		and style as parameter.
	 * 		Returns the Initialized label
	 */
	private Label addLabel(String lyrics, int horizontalSpacing, int style)
	{	
		Label label = new Label(shell, style);  
        label.setText(lyrics);        
        addToGrid(label, horizontalSpacing);
        return label;
	}
	
	/*
	 * A Generic Method to attach a generic widget to the grid, initialized with horizontal span
	 */
	private <T> void addToGrid(T widget, int horizantalSpan)
	{
		GridData gridData = new GridData();
	    gridData.horizontalSpan = horizantalSpan;
	    gridData.horizontalAlignment = GridData.FILL;
	    ((Control) widget).setLayoutData(gridData);
	}
	
	/* 
	 * Shows/Hides all widgets that shouldn't be displayed in the GUI because algorithm hasn't been selected (Step 1)
	 */
	private void toggleIllegalWidgetsForStep1(boolean flag)
	{
		fitnessSliderLabel.setVisible(flag);
    	fitnessSlider.setVisible(flag);
    	crossoverSliderLabel.setVisible(flag);
    	crossoverSlider.setVisible(flag);
    	mutationSliderLabel.setVisible(flag);
    	mutationSlider.setVisible(flag);
    	runButton.setVisible(flag);
	}
	
	/*
	 * Shows/Hides all widgets that shouldn't be displayed before the Genetic Algorithm is run
	 */
	private void toggleIllegalWidgetsForStep2(boolean flag)
	{	
		userSamplesTable.setVisible(flag);
		classifyButton.setVisible(flag);
		classifyResultLabel.setVisible(flag);
		graphicalDecisionTree.setVisible(flag);
		discretizeCheckBox.setVisible(flag);
	}
}
