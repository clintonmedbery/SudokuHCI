package com.example.sudokuhci;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.vaadin.data.Property;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptAll;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.ui.DragAndDropWrapper.WrapperTransferable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.DragAndDropWrapper;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.FinishedListener;
import com.vaadin.ui.Upload.Receiver;

public class UploadReceiver implements Receiver, FinishedListener {

	private static final long serialVersionUID = 1L;
	private FileOutputStream fos = null;
	private FileInputStream fis = null;
	private InputStreamReader isr = null;

	private GridLayout grid;
	private Board board;
	
	public UploadReceiver( GridLayout grid, Board board )
	{
		this.grid = grid;
		this.board = board;
	}

	// The receiveUpload() method is called when the user clicks the submit
	// button.
	@Override
	public OutputStream receiveUpload(String filename, String mimeType) {
		
		System.out.println("Recieving upload from:" + filename + "\n");
		try {
			fos = new FileOutputStream("sudoinput.txt");
		} 
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				fos.write(b);
			}

		};
	}

	@Override
	public void uploadFinished(FinishedEvent event) {


		try {
			fis = new FileInputStream("sudoinput.txt");
			isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			String sCurrentLine;

			int row = 0; int col = 0;
			while ((sCurrentLine = br.readLine()) != null) {

				String[] field = sCurrentLine.split(" ");

				col = 0;
				for (String s : field ) 
				{
					// set to ReadOnly to false to allow for update
					board.setReadOnly( col, row, false );
					
					board.setValue(col, row, s, s.equals("0") ? false : true);
					
					col++;
				}
				row++;
			}			
			
			// update the tiles for display
			for( col = 0; col < 9; col++ ){
				for( row = 0; row < 9; row++ ){
					//((Label)grid.getComponent(col, row)).setPropertyDataSource(board.getCellElement(col, row));
					grid.removeComponent(col, row);
					
					final Label label = new Label();
					
					final int ourCol = col;
					final int ourRow = row;
					
					label.setPropertyDataSource(board.getCellElement(col, row));
					label.addValueChangeListener(new CEValueChangeListener());

					label.setWidth(null);
					label.setImmediate(true);
					
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
					    }
					
					});
				
					grid.addComponent( layoutWrapper, col, row );
					grid.setComponentAlignment(layoutWrapper, Alignment.MIDDLE_CENTER);
				}	
			}		
			System.out.println( board );

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO: handle exception
		}

	}
}
