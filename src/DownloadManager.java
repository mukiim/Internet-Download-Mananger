import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;


public class DownloadManager extends JFrame implements Observer,ActionListener
{
	JTextField addTextField;
	DownloadsTableModel tableModel;
	JTable table;
	JButton pauseButton, resumeButton,addButton,cancelButton, clearButton;
	JLabel urllab;
	Download selectedDownload;
	private JFileChooser fileChooser= new JFileChooser();
	
	boolean clearing;

	DownloadManager()
	{
    super("Download Manager");
    setBounds(300,100,1050, 480);
	addWindowListener(new WindowAdapter() {public void windowClosing(WindowEvent e) {System.exit(0);}});
	setVisible(true);
	
    JPanel addPanel = new JPanel();
    addTextField = new JTextField(80);
    addPanel.add(addTextField);
    
	addButton = new JButton("Add To Download");
    addButton.addActionListener(this);
    addPanel.add(addButton);

	
    // Set up Downloads table.
    tableModel = new DownloadsTableModel();
    table = new JTable(tableModel);
    table.getSelectionModel().addListSelectionListener(new ListSelectionListener()
	{
    public void valueChanged(ListSelectionEvent e) {tableSelectionChanged();}
	});
  
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // Set up ProgressBar as renderer for progress column.
    ProgressRenderer renderer = new ProgressRenderer(0, 100);
    renderer.setStringPainted(true); 
    table.setDefaultRenderer(JProgressBar.class, renderer);

    // Set table's row height large enough to fit JProgressBar.
    table.setRowHeight((int) renderer.getPreferredSize().getHeight());

    JPanel downloadsPanel = new JPanel();
    downloadsPanel.setBorder(
    BorderFactory.createTitledBorder("Downloads"));
    downloadsPanel.setLayout(new BorderLayout());
    downloadsPanel.add(new JScrollPane(table),
    BorderLayout.CENTER);

    JPanel buttonsPanel = new JPanel();
    pauseButton = new JButton("Pause");
    pauseButton.addActionListener(this);
	
    pauseButton.setEnabled(false);
    buttonsPanel.add(pauseButton);
    resumeButton = new JButton("Resume");
    resumeButton.addActionListener(this);
     

    resumeButton.setEnabled(false);
    buttonsPanel.add(resumeButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    cancelButton.setEnabled(false);
    buttonsPanel.add(cancelButton);
    clearButton = new JButton("Clear");
    clearButton.addActionListener(this); 

    clearButton.setEnabled(false);
    buttonsPanel.add(clearButton);

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(addPanel, BorderLayout.NORTH);
    getContentPane().add(downloadsPanel, BorderLayout.CENTER);
    getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
  }
  public void actionPerformed(ActionEvent ae) 
  {
			if(ae.getSource()==addButton) {
//				JFileChooser f = new JFileChooser();
//				f.showSaveDialog((Component) ae.getSource());
				
				int returnValue = fileChooser.showSaveDialog((Component) ae.getSource());
				String path = fileChooser.getSelectedFile().getAbsolutePath();
				if(returnValue == JFileChooser.APPROVE_OPTION) {
//					System.out.println(path);
					actionAdd(path);
					}
			}
				
			if(ae.getSource()==pauseButton)
				actionPause();
				
			if(ae.getSource()==cancelButton)
				actionCancel();
				
			if(ae.getSource()==resumeButton)
				actionResume();		
				
			if(ae.getSource()==clearButton)	
				actionClear();    
   }

  // for add a new download.
  void actionAdd(String path) 
  {
		URL verifiedUrl = verifyUrl(addTextField.getText());
		if (verifiedUrl != null) 
		{
			tableModel.addDownload(new Download(verifiedUrl, path));
			addTextField.setText(""); // reset add text field
		}
		else
		{
			JOptionPane.showMessageDialog(this,"Invalid URL. Please Enter Valid URL", "Error",JOptionPane.ERROR_MESSAGE);
		}
  }

  // for verify download URL.
  private URL verifyUrl(String url) 
  {
//      if (!url.toLowerCase().startsWith("http://"))
//      return null;

	  URL verifiedUrl = null;
	  try 
	  {
		verifiedUrl = new URL(url);
      }
	  catch (Exception e) 
	  {
		return null;
	  }
	  
//  	  if (verifiedUrl.getFile().length() < 2)
//		return null;
  	  
//  	  System.out.println(verifiedUrl);
    return verifiedUrl;
  }

  // Called when table row selection changes.
	public void tableSelectionChanged() 
	{
		if (selectedDownload != null)
		selectedDownload.deleteObserver(DownloadManager.this);

		if (!clearing) 
		{
			selectedDownload = tableModel.getDownload(table.getSelectedRow());
			selectedDownload.addObserver(DownloadManager.this);
			updateButtons();
		}
    }

  // Pause the selected download.
	private void actionPause() 
	{
		selectedDownload.pause();
		updateButtons();
	}

  // Resume the selected download.
	private void actionResume() 
	{
		selectedDownload.resume();
		updateButtons();
    }

  // Cancel the selected download.
  private void actionCancel() 
	{	
		selectedDownload.cancel();
		updateButtons();
	}

  // Clear the selected download.
  private void actionClear() 
  {
		clearing = true;
		tableModel.clearDownload(table.getSelectedRow());
		clearing = false;
		selectedDownload = null;
		updateButtons();
  }

  private void updateButtons()
  {
    if (selectedDownload != null)
    {
      int status = selectedDownload.getStatus();
      switch (status)
      {
        case Download.DOWNLOADING:
          pauseButton.setEnabled(true);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.PAUSED:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(true);
          clearButton.setEnabled(false);
          break;
        case Download.ERROR:
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(true);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
          break;
        default: 
          pauseButton.setEnabled(false);
          resumeButton.setEnabled(false);
          cancelButton.setEnabled(false);
          clearButton.setEnabled(true);
      }
    } 
	else 
	{
		  pauseButton.setEnabled(false);
	  	  resumeButton.setEnabled(false);
		  cancelButton.setEnabled(false);
	 	  clearButton.setEnabled(false);
    }
  }

  /* called when a Download notifies its observers of any changes. */
  public void update(Observable o, Object arg) 
  {
		if (selectedDownload != null && selectedDownload.equals(o))
		updateButtons();
  }
 
  public static void main(String[] args) {
    DownloadManager manager = new DownloadManager();
    manager.show();
  }
 
}