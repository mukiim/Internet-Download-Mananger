import java.awt.Component;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

class Download extends Observable implements Runnable 
{
		static final int MAX_BUFFER_SIZE = 1024;
		static final String STATUSES[] = {"Downloading","Paused", "Completed", "Cancelled", "Error"};

		public static final int DOWNLOADING = 0;
		public static final int PAUSED = 1;
		public static final int COMPLETE = 2;
		public static final int CANCELLED = 3;
		public static final int ERROR = 4;
		
		JFileChooser fileChooser;
		
		private String path;
		private URL url; // download URL
		int size; // size of download in kb
		int downloaded; // number of bytes downloaded
		int status; // current status of download
		

	public Download(URL url, String path)
	{
		this.path =path;
		this.url = url;
		size = -1;
		downloaded = 0;
		status = DOWNLOADING;
		download();
	}

	//download's URL.
	public String getUrl() 
	{
		return url.toString();
	}

	//download's size in kb
	public int getSize() 
	{
		return size/1024;
	}

  //download's progress.
	public float getProgress()
	{
		return ((float) downloaded / size) * 100;
	}

  //download's status.
	public int getStatus() 
	{
		return status;
	}

  // Pause download.
	public void pause()
	{
		status = PAUSED;
		stateChanged();
	}

  // Resume download.
	public void resume()
	{
		status = DOWNLOADING;
		stateChanged();
		download();
	}

  // Cancel download.
	public void cancel() 
	{
		status = CANCELLED;
		stateChanged();
	}

  // if download having an error.
	private void error() 
	{
		status = ERROR;
		stateChanged();
	}

  // Start or resume downloading.
	private void download() 
	{
		Thread thread = new Thread(this);
		thread.start();
	}

  // Get file name portion of URL.
	private String getFileName(URL url, String path) 
	{
		
				String fileName = url.getFile();
				String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
				String fullName = path+fileExtension;
				System.out.println(fullName);
				return fullName;
			
	}

  // Download file.
	public void run() 
	{
		RandomAccessFile file = null;
		InputStream stream = null;
		// Open connection to URL and connect
		try 
		{
			HttpURLConnection connection = 
			(HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Range","bytes=" + downloaded + "-");
			connection.connect();

			if (connection.getResponseCode() / 100 != 2)
						error();
      
			// Check for valid content length.
			int contentLength = connection.getContentLength();
			if (contentLength < 1) {
			error();
			}

			if (size == -1) 
			{
				size = contentLength;
				stateChanged();
			}

      // Open file and seek to the end of it.
      file = new RandomAccessFile(getFileName(url, path), "rw");
      file.seek(downloaded);

      stream = connection.getInputStream();
      while (status == DOWNLOADING)
	  {
        byte buffer[];
        if (size - downloaded > MAX_BUFFER_SIZE) 
		          buffer = new byte[MAX_BUFFER_SIZE];
        
		else 
		          buffer = new byte[size - downloaded];
        

        // Read from server into buffer.
        int read = stream.read(buffer);
        if (read == -1)
          break;

        // Write buffer to file.
        file.write(buffer, 0, read);
        downloaded += read;
        stateChanged();
		}

      if (status == DOWNLOADING) 
	  {
			status = COMPLETE;
			stateChanged();
      }
    } 
	catch (Exception e)
	{
      error();
    }
	
	finally 
	{
        if (file != null) 
		{
			try
			{
			file.close();
			}
			catch (Exception e) {}
		}

      // Close connection to server.
		if (stream != null)
		{
			try 
			{
				stream.close();
			} 
			catch (Exception e) {}
		}
    }
  }

  //if download's status has changed.
  private void stateChanged() 
  {
    setChanged();
    notifyObservers();
  }
}