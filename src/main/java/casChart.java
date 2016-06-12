import org.knowm.xchart.*;

import com.datastax.driver.core.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.math.BigDecimal;

import org.knowm.xchart.internal.chartpart.AxisPair;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.chartpart.Legend_AxesChart;
import org.knowm.xchart.internal.chartpart.Plot_XY;
import org.knowm.xchart.internal.style.SeriesColorMarkerLineStyle;
import org.knowm.xchart.internal.style.SeriesColorMarkerLineStyleCycler;
import org.knowm.xchart.style.Styler.ChartTheme;
import org.knowm.xchart.style.Theme;
import org.knowm.xchart.style.XYStyler;

/**
 * Creates a simple chart using QuickChart
 */
public class casChart {
 
  public static void main(String[] args) throws Exception {

    Cluster cluster;
    Session session;
    ResultSet results;
    Row rows;

    // Connect to the cluster and keyspace "demo"`
    cluster = Cluster
      .builder()
	     .addContactPoint("localhost")
//       .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
//	     .withLoadBalancingPolicy(new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
      .build();
    session = cluster.connect("sparksensordata");

    List<Date> xData = new ArrayList<Date>();
    List<Double> yData = new ArrayList<Double>();

    // Get record count
    results = session.execute("SELECT COUNT(*) FROM sensordata");

    for (Row row : results) {
      Long rowCount = row.getLong("Count");
    }

    Date sdate = null;

    results = session.execute("SELECT time, value FROM sensordata");

    // Build result set

    for (Row row : results) {
      System.out.println("Adding " + sdate + ", " + row.getDecimal("Value"));
      sdate=row.getDate("Time");
      xData.add(sdate);
      yData.add(row.getDecimal("Value").doubleValue());
    }


    // Create Chart
    XYChart chart = new XYChartBuilder().width(800).height(600).title("Spark Data").xAxisTitle("Time").yAxisTitle("Sensor Data Value").build();
    XYSeries series = chart.addSeries("Sensor Data", xData, yData);


    // Show it
    new SwingWrapper(chart).displayChart();
 
    // Clean up the connection by closing it
    cluster.close();
  }
}
