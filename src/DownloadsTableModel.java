import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

class DownloadsTableModel extends AbstractTableModel implements Observer
{
  private static final String[] columnNames = {"URL", "Size ( KB )","Progress", "Status"};
  
  private static final Class[] columnClasses = {String.class,String.class, JProgressBar.class, String.class};

  private ArrayList downloadList = new ArrayList();

  // Add a new download to the table.
  public void addDownload(Download download)
  {
	download.addObserver(this);
    downloadList.add(download);
    fireTableRowsInserted(getRowCount(), getRowCount());
  }

  // Get a download for the specified row.
  public Download getDownload(int row)
  {
    return (Download) downloadList.get(row);
  }

  // Remove a download from the list.
  public void clearDownload(int row) 
  	{
		downloadList.remove(row);
		fireTableRowsDeleted(row, row);
	}

    public int getColumnCount()
	{
		return columnNames.length;
	}

	public String getColumnName(int col) 
	{
		return columnNames[col];
	}

    public Class getColumnClass(int col)
	{
		return columnClasses[col];
	}
  
	public int getRowCount() 
	{
		return downloadList.size();
	}

  // Get value for a specific row and column combination.
  public Object getValueAt(int row, int col)
  {
    Download download = (Download) downloadList.get(row);
    switch (col) 
	{
      case 0: 
			return download.getUrl();
      case 1: 
		int size = download.getSize();
        return (size == -1) ? "" : Integer.toString(size);
      case 2:
		return new Float(download.getProgress());
      case 3: 
		return Download.STATUSES[download.getStatus()];
    }
    return "";
  }

  /* when a Download notifies its observers of any changes */
  public void update(Observable o, Object arg) 
  {
	  //1
    int index = downloadList.indexOf(o);

    fireTableRowsUpdated(index, index);
  }
}