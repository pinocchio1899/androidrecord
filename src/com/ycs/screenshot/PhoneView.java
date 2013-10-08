package com.ycs.screenshot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.android.ddmlib.IDevice;
import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

/**
 * This frame displays the loaded images. The menu has items for loading and
 * saving files.
 */
public class PhoneView {

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new PhoneFrame(null);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}

class PhoneFrame extends JFrame {

	private static IRational FRAME_RATE = IRational.make(3, 1);
	private static final int SECONDS_TO_RUN_FOR = 5;

	public PhoneFrame(IDevice device) {
		this.device = device;
		setTitle(device.getName());
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setLocationRelativeTo(null);
		JButton startBtn = new JButton("开始录制");
		startBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				captureScreen = new CaptureScreenThread();
				captureScreen.setCapture(true);
				Thread thread = new Thread(captureScreen);
				thread.start();

			}
		});

		JButton stopBtn = new JButton("停止录制");
		stopBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				if(captureScreen != null && captureScreen.isCapture){
					captureScreen.setCapture(false);
				}
			}
		});

		JButton refreshBtn = new JButton("刷新");
		refreshBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				refresh();
			}
		});
		JButton shotBtn = new JButton("截图");
		shotBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				saveFile();
			}
		});

		// set up a button panel
		JPanel panel = new JPanel();
		panel.add(startBtn);
		panel.add(stopBtn);
		panel.add(refreshBtn);
		panel.add(shotBtn);
		add(panel, BorderLayout.NORTH);
		
		javax.swing.filechooser.FileSystemView fsv = javax.swing.filechooser.FileSystemView
				.getFileSystemView();
		homeDir = fsv.getHomeDirectory().toString();

		Thread thread = new Thread(new LoadScreenThread());
		thread.start();
	}

	class LoadScreenThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			LoadScreen();
		}
	};

	class CaptureScreenThread implements Runnable {

		private Dimension screenBounds;
		private boolean isCapture = false;

		public CaptureScreenThread() {
			screenBounds = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		}

		@Override
		public void run() {
			capture();
		}

		public void setCapture(boolean isCapture) {
			this.isCapture = isCapture;
		}

		public boolean isCapture() {
			return isCapture;
		}

		public void capture() {
			try {
				final String outFile;
				outFile = homeDir + "\\android_movie_" + getTimeStr() + ".mp4";
				final IMediaWriter writer = ToolFactory.makeWriter(outFile);
				int width = (int) screenBounds.getWidth();
				int height = (int) screenBounds.getHeight();
				if (image != null) {
					if (image.getWidth() < width) {
						width = image.getWidth();
					}
					if (image.getHeight() < width) {
						height = image.getHeight();
					}
				}
				writer.addVideoStream(0, 0, FRAME_RATE, width, height);
				long startTime = System.nanoTime();
				while (isCapture) {
					LoadScreen();
					BufferedImage bgrScreen = CaptureScreen.convertToType(
							image, BufferedImage.TYPE_3BYTE_BGR);
					writer.encodeVideo(0, bgrScreen, System.nanoTime()
							- startTime, TimeUnit.NANOSECONDS);

					System.out.println("encoded image");
				}
				writer.close();
				System.out.println("complete...");
			} catch (Throwable e) {
				System.err.println("an error occurred: " + e.getMessage());
			}
		}

	}

	private void refresh() {

		if (captureScreen == null || !captureScreen.isCapture) {
			LoadScreen();
		}
	}

	private void LoadScreen() {
		image = DeviceTableModel.shot.getScreenShot(this.device);
		add(new JComponent() {
			public void paintComponent(Graphics g) {
				if (image != null)
					g.drawImage(image, 0, 0, null);
			}
		});
		validate();
	}
	
	private String getTimeStr(){
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
				"yyyy-MM-dd-HH-mm-ss");
		return format.format(now);
	}

	public void saveFile() {
		refresh();
		if (image == null)
			return;
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("PNG");
		ImageWriter writer = iter.next();
		String imgName = homeDir + "\\android_screenshot_" + getTimeStr() + ".png";
		File f = new File(imgName);
		try {
			f.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(f);
			writer.setOutput(imageOut);

			writer.write(new IIOImage(image, null, null));
			imageOut.close();

		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, e);
		}
	}

	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 600;

	private IDevice device;
	private BufferedImage image;

	private CaptureScreenThread captureScreen;

	private String homeDir;

}
