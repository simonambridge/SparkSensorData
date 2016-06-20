import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class netCat {

  private static String sensorID = "p100";
  private static String value    = null;
  private static String sensorData = null;

  public static void main(String args[]) {

// --------------- check input parameters provided-------------------

    if (args.length <4) {
      System.out.println("Usage: java netCat <Data type: [l|n]> <Sample rate in ms> <Number of Samples> <data port>");
      System.exit(0);
    }

    String sampleType = args[0];
    int sampleRate = Integer.valueOf(args[1]).intValue();
    int sampleCount = Integer.valueOf(args[2]).intValue();
    int dataPort = Integer.valueOf(args[3]).intValue();

    System.out.println("*****************************************");

// --------------- check input parameters -------------------

    if (sampleType.equals("l")) {
      System.out.println("Data sample type: Linear"); }
    else if (sampleType.equals("n")) {
      System.out.println("Data sample type: Non-linear"); }
    else {
      System.out.println("Sample type: " + sampleType + " not l or n");
      return;    }

    if (sampleRate<1 || sampleRate>1000) {
      System.out.println("Data sample rate " + sampleRate + " not between 1 and 1000ms");
      return; }

    if (sampleCount<1 || sampleCount>10000) {
      System.out.println("Sample count " + sampleCount + " not between 1 and 10000 samples");
      return; }

    System.out.println("*****************************************");
    System.out.println("Waiting for listener on port " + dataPort + "........");

// ------------------- send data -----------------------------

    try {
      ServerSocket serverSocket = new ServerSocket(dataPort);
      Socket clientSocket = serverSocket.accept();
      PrintWriter out =
              new PrintWriter(clientSocket.getOutputStream(), true);

      String value = "";
      int i = 0;
      Double valueDouble = 0.0;

      while(i<=sampleCount){  // number of samples to send cx c
        i++;

        if (sampleType.equals("l")) {
          valueDouble = (double) i;  // linear data using counter - convert to Double from Int
        }
        else {
          if (i==0)
          { valueDouble = Math.random() * 2; } // if its the first iteration
          else { valueDouble = valueDouble + Math.random() - .5; };
        }
        value = String.valueOf(valueDouble);

        sensorData = sensorID + "," + value + System.getProperty("line.separator");
        System.out.println(sensorData);
        out.write(sensorData);
        out.flush();

        Thread.sleep(sampleRate);  // sample rate per ms
      }
    }

    catch (Throwable e) {
      e.printStackTrace();
    }
  }

}

