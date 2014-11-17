package com.example.sudokuhci;


import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.Transferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.event.dd.DropTarget;
import com.vaadin.event.dd.TargetDetails;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.Not;
import com.vaadin.event.dd.acceptcriteria.SourceIsTarget;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.DragAndDropWrapper.DragStartMode;
import com.vaadin.ui.DragAndDropWrapper.WrapperTargetDetails;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;

/**
 * The Application's "main" class
 */
@Push
@SuppressWarnings("serial")
@Theme("sudokuhcitheme")
public class Sudoku extends UI implements Broadcaster.BroadcastListener{
	
	private Panel panel;
	private GridLayout grid;
	private Board board;
	
	private GridLayout inputGrid;
	private Panel inputPanel;
	
	private final VerticalLayout vLayout = new VerticalLayout();
	private final HorizontalLayout hLayout = new HorizontalLayout();
	
	private final VerticalLayout chatLayout = new VerticalLayout();
	
	private final HorizontalLayout h2Layout = new HorizontalLayout();
	
	private UploadReceiver uploadReceiver;
	private Upload upload;
	private Button solveButton = new Button("Solve");
	
	//WEBSOCKET and Chat Panel setup
	final ChatText chatBox = new ChatText();
	String name = "";

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = Sudoku.class)
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {


		// Find the application directory
		String basepath = VaadinService.getCurrent()
		                  .getBaseDirectory().getAbsolutePath();
		
		System.out.println( basepath );


		// build a panel
		// https://vaadin.com/api/7.3.2/com/vaadin/ui/Panel.html
		
		// place a 9 x 9 grid control into the panel
		// https://vaadin.com/api/7.3.2/com/vaadin/ui/GridLayout.html
		
		// add a label to each element of the grid control
		// https://vaadin.com/api/com/vaadin/ui/Label.html
		// https://vaadin.com/api/7.3.2/com/vaadin/data/Property.Viewer.html
		// https://vaadin.com/api/com/vaadin/data/Property.html
		// https://vaadin.com/book/-/page/datamodel.html
		
		// each component should now be addressable by x,y

		//Setting up the grid for the Game Board
		grid = new GridLayout( 9, 9 );
		grid.setMargin(false);
		grid.setSpacing(false);
		grid.setWidth("100%");
		grid.setHeight("100%");
		//grid.addLayoutClickListener(new GridClickListener());
		
		//Getting our input grid
		inputGrid = new GridLayout(9,1);
		inputGrid.setWidth("100%");
		inputGrid.setHeight("100%");
		
		//Setting up our UI panel
		panel = new Panel();
		panel.setContent(grid);
		panel.setWidth("315px");
		panel.setHeight("315px");
		panel.setStyleName("panel");
		
		//Setting up our input panel
		inputPanel = new Panel();
		inputPanel.setContent(inputGrid);
		inputPanel.setWidth("315px");
		inputPanel.setHeight("100%");
		inputPanel.setStyleName("panel");
		inputGrid.setMargin(false);
		inputGrid.setSpacing(true);
		
		h2Layout.setSpacing(true);
		
		board = new Board();
		
		// Putting in our Drag and Drop listeners and numbers into game board
		for( int col = 0; col < 9; col++ )
			for( int row = 0; row < 9; row++ )
			{
				final Label label = new Label();
				
				final int ourCol = col;
				final int ourRow = row;
				
				label.setPropertyDataSource(board.getCellElement(col, row));
				label.addValueChangeListener(new CEValueChangeListener());

				label.setWidth(null);
				label.setImmediate(true);
				
				if(board.getCellElement(col, row).isReadOnly()){
					label.setStyleName("readonly");
				} else {
					label.setStyleName("notreadonly");
				}
				
				DragAndDropWrapper layoutWrapper = new DragAndDropWrapper(label);
				
				
				
				layoutWrapper.setDropHandler(new DropHandler() {
					public AcceptCriterion getAcceptCriterion() {
				       return AcceptAll.get();
					}
					public void drop(DragAndDropEvent event) {
						
						WrapperTransferable dragInput = (WrapperTransferable) event.getTransferable();
						String draggedItem = dragInput.getDraggedComponent().getCaption();
						
						//WrapperTargetDetails details = (WrapperTargetDetails) event.getTargetDetails();
						
						
						System.out.print("Data is: "  + draggedItem + "\n");
						board.setValue(ourCol, ourRow, draggedItem, draggedItem.equals("0") ? false : true);
						label.setPropertyDataSource(board.getCellElement(ourCol, ourRow));
						
						System.out.println("Drag and Drop Update");
						update();
				        

						
				    }
				
				});
			
				
				
				
				grid.addComponent( layoutWrapper, col, row );
				grid.setComponentAlignment(layoutWrapper, Alignment.MIDDLE_CENTER);
				
			}
		
		//Making out 1-9 grid for input
		for(int i = 1; i <= 9; i++){
			
			Label inputLabel = new Label();
			inputLabel.setValue(Integer.toString(i));
			inputLabel.setCaption(Integer.toString(i));
			System.out.print("Putting in: "  + inputLabel.getCaption() + "\n");
			
			
			inputLabel.setWidth(null);
			inputLabel.setImmediate(true);
			
			DragAndDropWrapper labelWrap = new DragAndDropWrapper(inputLabel);
			labelWrap.setDragStartMode(DragStartMode.COMPONENT);
			
			inputGrid.addComponent(labelWrap);
			inputGrid.setComponentAlignment(labelWrap, Alignment.MIDDLE_CENTER);
			
		}
		//CHAT SETUP
		//Making variables to get the user's name
		final Button nameButton = new Button("Enter");
		final TextField nameInput = new TextField();
				
				//Getting the chat input
		final TextField chatInput = new TextField();
		chatInput.setWidth("500px");
		
		final Button sendButton = new Button("Send");
		final Label enterName = new Label("Enter your name:");
		this.setStyleName("body");
		
	
		
		nameButton.addClickListener(new Button.ClickListener() {
			@Override
		    public void buttonClick(ClickEvent event) {
		        name = nameInput.getValue();
		        chatLayout.addComponent(chatBox);
		        
				vLayout.addComponent( hLayout );
				hLayout.addComponent(upload);
				hLayout.addComponent(solveButton);
				
				solveButton.setStyleName("button");
				
				
				h2Layout.addComponent(panel);
				h2Layout.addComponent(chatLayout);
				
				
				vLayout.addComponent(h2Layout);
				vLayout.addComponent(inputPanel);
				inputPanel.setStyleName("chatbox");
				
				hLayout.setMargin(true);
				hLayout.setSpacing(true);
				hLayout.setComponentAlignment(solveButton, Alignment.BOTTOM_RIGHT);
				
				
				
				vLayout.setMargin(true);
				vLayout.setSpacing(true);
				
				
				chatLayout.addComponent(chatInput);
				chatInput.setStyleName("chatbox");
				chatLayout.addComponent(sendButton);
				
				
				vLayout.removeComponent(enterName);
				vLayout.removeComponent(nameInput);
				vLayout.removeComponent(nameButton);
				
		    }
		});
		
		sendButton.addClickListener(new Button.ClickListener() {
			@Override
		    public void buttonClick(ClickEvent event) {
		        // Broadcast the message
				
		        Broadcaster.broadcast(name + ":  " + chatInput.getValue());
		        
		        chatInput.setValue("");
		    }
		});
		
		//Putting the UI into Layouts
		uploadReceiver = new UploadReceiver(grid, board);
		upload = new Upload(" ", uploadReceiver);
		upload.setButtonCaption("Load Soduko File");
		upload.setStyleName("button");
		upload.setImmediate(true);
		
		//Chat items
		vLayout.addComponent(enterName);
		vLayout.addComponent(nameInput);
		vLayout.addComponent(nameButton);
		
		//Style settings
		
		
		setContent(vLayout);

		// upload.setReceiver(uploadReceiver);
		upload.addFinishedListener(uploadReceiver);
		
		/*
		 * Click on the Solve Button
		 */
		solveButton.addClickListener(new Button.ClickListener() {
			@Override
		    public void buttonClick(ClickEvent event) {
		        // Broadcast the message
				SolveChecker checker = new SolveChecker(grid, board);
				if(!checker.solveSudoko()){
					//Replace this with a UI broadcast
					Notification.show("Incorrect!");
					System.out.println("INCORRECT");
				} else {
					Notification.show("Correct!");
					System.out.println("CORRECT");

				}
		        
		    }
		});
		
		Broadcaster.register(this);
	}
	
	public void update(){
        Broadcaster.broadcastBoard(board);

	}
	
	@Override
    public void detach() {
        Broadcaster.unregister(this);
        super.detach();
    }
	
	 public void receiveBroadcast(final String message) {
	        // Must lock the session to execute logic safely
	        access(new Runnable() {
	            @Override
	            public void run() {
	                // Show it somehow
	               chatBox.addChat(message);
	            }
	        });
	    }
	 
	 public void checkForUpdate(final Board checkBoard) {
	        // Must lock the session to execute logic safely
	        access(new Runnable() {
	            @Override
	            public void run() {
	                // Show it somehow
	              
	               
	               for( int col = 0; col < 9; col++ ){
	    				for( int row = 0; row < 9; row++ ){
	    					String value = Integer.toString(checkBoard.getIntegerValue(row, col));
	    					
	    					if(value != "0"){
	    						Broadcaster.broadcastBoard(board);
	    					}
	    					
	    					
	    				}	
	    			}
	            }
	        });
	    }
	 
	 public void updateBoard(final Board newBoard) {
	        // Must lock the session to execute logic safely
	        access(new Runnable() {
	            @Override
	            public void run() {
	                // Show it somehow
	            	System.out.println("UPDATING BOARD");
	            	for( int col = 0; col < 9; col++ ){
	    				for( int row = 0; row < 9; row++ ){
	    					String newValue = Integer.toString(newBoard.getIntegerValue(row, col));
	    					
	    					board.setReadOnly(col, row, false);
	    					
	    					board.setValue(row, col, newValue, newValue.equals("0") ? false : true);
	    				}	
	    			}
	            	
	            	
	            	
	            	for(int col = 0; col < 9; col++ ){
	    				for(int row = 0; row < 9; row++ ){
	    					
	    					grid.removeComponent(col, row);
	    					
	    					final Label label = new Label();
	    					
	    					final int ourCol = col;
	    					final int ourRow = row;
	    					
	    					label.setPropertyDataSource(board.getCellElement(col, row));
	    					label.addValueChangeListener(new CEValueChangeListener());

	    					label.setWidth(null);
	    					label.setImmediate(true);
	    					
	    					if(board.getCellElement(col, row).isReadOnly()){
	    						label.setStyleName("readonly");
	    					} else {
	    						label.setStyleName("notreadonly");
	    					}
	    					
	    					DragAndDropWrapper layoutWrapper = new DragAndDropWrapper(label);
	    					
	    					layoutWrapper.setDropHandler(new DropHandler() {
	    						public AcceptCriterion getAcceptCriterion() {
	    					       return AcceptAll.get();
	    						}
	    						public void drop(DragAndDropEvent event) {
	    							
	    							WrapperTransferable dragInput = (WrapperTransferable) event.getTransferable();
	    							String draggedItem = dragInput.getDraggedComponent().getCaption();
	    							
	    							//WrapperTargetDetails details = (WrapperTargetDetails) event.getTargetDetails();
	    							
	    							
	    							System.out.print("Data is: "  + draggedItem + "\n");
	    							board.setValue(ourCol, ourRow, draggedItem, draggedItem.equals("0") ? false : true);
	    							label.setPropertyDataSource(board.getCellElement(ourCol, ourRow));
	    							update();
	    					    }
	    					
	    					});
	    				
	    					grid.addComponent( layoutWrapper, col, row );
	    					grid.setComponentAlignment(layoutWrapper, Alignment.MIDDLE_CENTER);
	    				}	
	    			}
	            }
	        });
	    }
}