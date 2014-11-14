package com.example.sudokuhci;


import com.vaadin.data.Property;

public class Board 
{
	public final static int T_COL = 9;
	public final static int T_ROW = 9;

	private CellElement cell[][] = new CellElement[T_COL][T_ROW];
	
	public Board()
	{
		for( int col=0; col < T_ROW; col++ )
			for( int row = 0; row < T_COL; row++ )
				cell[col][row] = new CellElement( new Integer(0), false );
	}
	
	public Property getCellElement(int x, int y)
	{
		return cell[x][y];
	}
	
	public int getIntValue( int x, int y )
	{
		return cell[x][y].getIntValue();
	}
	
	public Integer getIntegerValue( int x, int y )
	{
		return cell[x][y].getIntegerValue();
	}
	
	public boolean isModifiable( int x, int y )
	{
		return cell[x][y].isReadOnly();
	}
	
	
	public void setValue( int x, int y, Object value, boolean readOnly )
	{
		if( !cell[x][y].isReadOnly() )
		{
			cell[x][y].setValue(value);
			cell[x][y].setReadOnly(readOnly);
		}
	}
	
	public boolean isSelected( int x, int y )
	{
		return cell[x][y].isSelected();
	}
	
	public void setSelected( int x, int y, boolean selected )
	{
		cell[x][y].setSelected( selected );
	}
	
	public String toString() 
	{
		StringBuffer strbuf = new StringBuffer();
		
		for( int row=0; row<T_ROW; row++ )
		{
			for( int col=0; col<T_COL; col++ )
			{
				Integer value = this.getIntegerValue(col, row);
				strbuf.append((value.equals("0"))? "- " : value + " " );
			}
			strbuf.append("\n");
		}
		
		return strbuf.toString();
	}

	public void setReadOnly(int x, int y, boolean readOnly) {
		// TODO Auto-generated method stub
		cell[x][y].setReadOnly(readOnly);
	}

	
}