import com.xeiam.xchart.Chart;
import com.xeiam.xchart.QuickChart;
import com.xeiam.xchart.SwingWrapper;
import com.xeiam.xchart.*;
import com.datastax.driver.core.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.math.*;
 
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
//                     .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
//	             .withLoadBalancingPolicy(
//			new TokenAwarePolicy(new DCAwareRoundRobinPolicy()))
	             .build();
    session = cluster.connect("demo");

    Collection<Date> xData = new ArrayList<Date>();
    Collection<BigDecimal> yData = new ArrayList<BigDecimal>();

    // Get record count
    results = session.execute("SELECT COUNT(*) FROM sensor_data");

    for (Row row : results) {
      Long rowCount = row.getLong("Count");
    }

    Date sdate = null;

    results = session.execute("SELECT time, value FROM sensor_data");

    // Build result set

    for (Row row : results) {
      sdate=row.getDate("Time");
      xData.add(sdate);
System.out.println(sdate + ", " + row.getDecimal("Value"));
      yData.add(row.getDecimal("Value"));
    }


    // Create Chart
    Chart chart = new Chart(800, 600);
    Series series = chart.addSeries("Sensor Data", xData, yData);
 
    // Show it
    new SwingWrapper(chart).displayChart();
 
    // Clean up the connection by closing it
    cluster.close();
  }
}

