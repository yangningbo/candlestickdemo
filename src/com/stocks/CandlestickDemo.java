package com.stocks;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultHighLowDataset;


/*
 * Используем в разработке бесплатную профильную библиотеку для построения графиков JFreeChart
 * 
 * */	

public class CandlestickDemo  {

	private static final String title = "USD/EUR rate";
	private ChartPanel chartPanel = createChart();
	private JFrame f;
	private JPanel panel;
	private String s;
	private static Connection c;
	private int monthsCount =1;
	public CandlestickDemo(){
		f = new JFrame(title);
        f.setTitle(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout(0, 5));
        f.add(chartPanel, BorderLayout.CENTER);

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.add(selectPeriod());//добавляем на панель отображения графика компонент отвечающий за выбор периода отображения
        f.add(panel, BorderLayout.SOUTH);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setVisible(true);
	}
	private JComboBox selectPeriod() {
		// реализовываем обработчик выбора периода отображения из выпадающего списка
		final JComboBox period = new JComboBox();
		final String[] periodValues = {"Год","Месяц"};//задаем возможные значения пунктов выпадающего списка
		period.setModel(new DefaultComboBoxModel(periodValues));//создаем компонент выпадающий список
		period.addActionListener(new ActionListener() {//
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// добавляем реакцию на событие "выбор элемента списка"
				if(periodValues[1].equals(period.getSelectedItem())){
					monthsCount=1;
					panel.add(showPreviousMonthData());
					panel.add(showNextMonthData());
					chartPanel.setChart(createMonthlyChart(monthsCount));
					f.add(panel,BorderLayout.SOUTH);
					f.pack();
			        f.setLocationRelativeTo(null);
			        f.setVisible(true);
					chartPanel.repaint();//отрисовываем заново график
				}else {
					if(periodValues[0].equals(period.getSelectedItem())){
						f.remove(panel);
						chartPanel.setChart(createMonthlyChart(13));
						chartPanel.repaint();//отрисовываем заново график
					}
				}
			}
		});
		return period;
	}
	private JButton showPreviousMonthData() {
		final JButton show = new JButton(new AbstractAction("Назад") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(monthsCount == 1){
					JOptionPane.showMessageDialog(f.getComponent(0), "Данных нет!");
				}else{
					chartPanel.setChart(createMonthlyChart(--monthsCount));
					
					chartPanel.repaint();
					
				}
			}
		});
		return show;
	}
	private JButton showNextMonthData() {
		// TODO Auto-generated method stub
		final JButton show = new JButton(new AbstractAction("Вперед") {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(monthsCount==12){
					JOptionPane.showMessageDialog(f.getComponent(0), "Данных нет!");
				}else{
					chartPanel.setChart(createMonthlyChart(++monthsCount));
					chartPanel.repaint();
					
				}
			}
		});
		return show;
	}
	private ChartPanel createChart() {
		// TODO Auto-generated method stub
		DefaultHighLowDataset dataset = createDataset();
		JFreeChart chart = ChartFactory.createCandlestickChart("USD to EUR exchange rate for 2012 year", 
				"Date", "Rate", dataset, true);
		chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
		DecimalFormat df =  new DecimalFormat("0.0000");//настраиваем количество отображаемых цифр после запятой
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);//здесь в дальнейшем значения нижней и верхней границы возьмем из бд
		rangeAxis.setNumberFormatOverride(df);
		//DateAxis domain = (DateAxis) chart.getXYPlot().getDomainAxis();
		//domain.setDateFormatOverride(DateFormat.getDateInstance());
		return new ChartPanel(chart);
	}
	
	private static Connection openConnection() {
		Properties properties = new Properties();
		  properties.put("user", "root");//имя пользователя базы данных
		  properties.put("password", "");//пароль для подключения к бд
		  properties.put("characterEncoding", "ISO-8859-1");//выставляем кодировку
		  properties.put("useUnicode", "true");//включаем поддержку юникода
		  String url = "jdbc:mysql://localhost/test";//указываем имя бд, к которой будем подключаться

		  try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();//указываем класс драйвера для работы с бд
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  
		try {
			c = DriverManager.getConnection(url, properties);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		  return c;
		
	}
	private DefaultHighLowDataset createDataset() {
		Comparable<String> seriesKey = "EUR/USD exchange rate";
		List<Date> date = new ArrayList<Date>();
		List<Double> high = new ArrayList<Double>();
		List<Double> low = new ArrayList<Double>();
		List<Double> open = new ArrayList<Double>();
		List<Double> close = new ArrayList<Double>();
		List<Double> volume = new ArrayList<Double>();
		Connection c;

		
		try{
			c = openConnection();//устанавливаем соединение с бд
			Statement stmt = c.createStatement();
			s = "SELECT * FROM valuta";//составляем запрос
			ResultSet rs = stmt.executeQuery(s);//выполняем запрос к нашей таблице
			while(rs.next()){//заполняем наш набор данными из бд
				date.add(rs.getDate("date"));
				open.add(rs.getDouble("open"));
				high.add(rs.getDouble("max"));
				low.add(rs.getDouble("min"));
				close.add(rs.getDouble("close"));
				volume.add(Double.parseDouble("10.00"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		DefaultHighLowDataset result = new DefaultHighLowDataset(seriesKey, 
				date.toArray(new Date[date.size()]), convertDoubles(high), 
				convertDoubles(low), convertDoubles(open), convertDoubles(close), convertDoubles(volume));
		return result;
	}
	private double[] convertDoubles(List<Double> doubles) {
		double[] ret = new double[doubles.size()];
		Iterator<Double> iterator = doubles.iterator();
		for(int i = 0; i < ret.length; i++){
			ret[i] = iterator.next().doubleValue();
		}
		return ret;
	}

	
	public static void main(String[] args){
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				CandlestickDemo csd = new CandlestickDemo();
			}
		});
	}
	
	private JFreeChart createMonthlyChart(int month){
		
		JFreeChart chart = ChartFactory.createCandlestickChart("USD to EUR exchange rate for 2012 year", 
				"Date", "Rate", createDataSetForMonth(month), true);
		chart.getXYPlot().setOrientation(PlotOrientation.VERTICAL);
		DecimalFormat df =  new DecimalFormat("0.0000");//настраиваем количество отображаемых цифр после запятой
		NumberAxis rangeAxis = (NumberAxis) chart.getXYPlot().getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);//здесь в дальнейшем значения нижней и верхней границы возьмем из бд
		rangeAxis.setNumberFormatOverride(df);
		return chart;
		
	} 
	private DefaultHighLowDataset createDataSetForMonth(int month){
		Comparable<String> seriesKey = "EUR/USD exchange rate";
		List<Date> date = new ArrayList<Date>();
		List<Double> high = new ArrayList<Double>();
		List<Double> low = new ArrayList<Double>();
		List<Double> open = new ArrayList<Double>();
		List<Double> close = new ArrayList<Double>();
		List<Double> volume = new ArrayList<Double>();
		ResultSet rs = null;
		try{
			//c = openConnection();//устанавливаем соединение с бд
			
			if(month == 13){
				Statement stmt = c.createStatement();
				String str = "SELECT * FROM valuta";//составляем запрос
				rs = stmt.executeQuery(str);
			}else{
				PreparedStatement pst = c.prepareStatement("select * from valuta where month(date) = ?");
				pst.setString(1,Integer.toString(month));

				rs = pst.executeQuery();
			}
			//выполняем запрос к нашей таблице
			while(rs.next()){//заполняем наш набор данными из бд
				date.add(rs.getDate("date"));
				open.add(rs.getDouble("open"));
				high.add(rs.getDouble("max"));
				low.add(rs.getDouble("min"));
				close.add(rs.getDouble("close"));
				volume.add(Double.parseDouble("10.00"));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		DefaultHighLowDataset result = new DefaultHighLowDataset(seriesKey, 
				date.toArray(new Date[date.size()]), convertDoubles(high), 
				convertDoubles(low), convertDoubles(open), convertDoubles(close), convertDoubles(volume));
		return result;
	}
}
