package com.ycs.screenshot;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Calendar;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.*;

import com.android.ddmlib.IDevice;
import com.ycs.screenshot.PhoneFrame.LoadScreenThread;

/**
 * This program shows how to build a table from a table model.
 * 
 * @version 1.02 2007-08-01
 * @author Cay Horstmann
 */
public class MainView {
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new InvestmentTableFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		});
	}
}

/**
 * This frame contains the investment table.
 */
class InvestmentTableFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InvestmentTableFrame() {
		setTitle("Android手机录制屏幕工具");
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
		setLocationRelativeTo(null);
	
		table = new JTable();
		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(
							ListSelectionEvent listselectionevent) {
						// TODO Auto-generated method stub

					}

				});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				selectedRow = table.getSelectedRow();
			}
		});

		add(new JScrollPane(table));
		JButton btn = new JButton("录制");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {
				if (selectedRow != -1) {
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							JFrame frame = new PhoneFrame(
									DeviceTableModel.devices[selectedRow]);
							// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
							frame.setVisible(true);
						}
					});

				}
			}
		});

		// set up a button panel
		JPanel panel = new JPanel();
		panel.add(btn);
		add(panel, BorderLayout.NORTH);
		
		Thread thread = new Thread(new LoadDeviceThread());
		thread.start();
	}
	
	class LoadDeviceThread implements Runnable {

		@Override
		public void run() {
			TableModel model = new DeviceTableModel();
			table.setModel(model);
			table.validate();
		}
	};

	JTable table;
	int selectedRow = -1;
	private static final int DEFAULT_WIDTH = 600;
	private static final int DEFAULT_HEIGHT = 400;
}



/**
 * This table model computes the cell entries each time they are requested. The
 * table contents shows the growth of an investment for a number of years under
 * different interest rates.
 */
class DeviceTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructs an investment table model.
	 * 
	 * @param y
	 *            the number of years
	 * @param r1
	 *            the lowest interest rate to tabulate
	 * @param r2
	 *            the highest interest rate to tabulate
	 */
	public static IDevice[] devices;
	public static ScreenShot shot;

	public DeviceTableModel() {
		shot = new ScreenShot();
		devices = shot.getDevice();
		for (IDevice d : devices) {
			System.out.println(d.getSerialNumber());
		}
	}

	public int getRowCount() {
		return devices.length;
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int row, int column) {

		return devices[row].getName();
	}

	public String getColumnName(int c) {
		return "Phone Name";
	}

}
