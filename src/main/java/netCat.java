import java.net.*;
import java.text.*;
import java.util.*;
import java.io.*;

public class netCat {

  private static String sensorID = "p100";
  private static String value    = null;
  private static String sensorData = null;

  public static void main(String args[]) {

// ---------------validate input parameters -------------------

    if (args.length <4) {
        System.out.println("Usage: java netCat <Data type: [l|n]> <Sample rate in ms> <Number of Samples> <data port>");
        System.exit(0);
    }

    String sampleType = args[0];
    int sampleRate = Integer.valueOf(args[1]).intValue();
    int sampleCount = Integer.valueOf(args[2]).intValue();
    int dataPort = Integer.valueOf(args[3]).intValue();

    System.out.println("*****************************************");
    if (sampleType.equals("l")) {
       System.out.println("Data sample type: Linear"); } 
    else if (sampleType.equals("n")) {
       System.out.println("Data sample type: Non-linear"); }
    else {
       System.out.println("Invalid data sample type: " + sampleType);
       return;
    }

    if (sampleRate<1 || sampleRate>1000) {
      System.out.println("Invalid data sample rate: " + sampleRate);
      return; } 
    else {
      System.out.println("Data sample rate: " + sampleRate + "ms");
    }

    if (sampleCount<1 || sampleRate>10000) {
      System.out.println("Invalid data sample count: " + sampleCount);
      return; } 
    else {
      System.out.println("Data sample count: " + sampleRate);
    }

    System.out.println("*****************************************");
    System.out.println("Waiting for listener........");

// --------------------------------------------------------------------------------

    try {
      ServerSocket serverSocket = new ServerSocket(dataPort);
      Socket clientSocket = serverSocket.accept();
      PrintWriter out =
      new PrintWriter(clientSocket.getOutputStream(), true);
                        
      int i = 0;
      while(i<=sampleCount){  // number of samples to send
        i++;                       
        
        if (sampleType.equals("l")) {
          value = String.valueOf(i);  // linear data using counter             
        }
        else {
          value = String.valueOf(new Double(Math.random() * i)); // Non-linear data
        }

        sensorData = sensorID + "," + value + System.getProperty("line.separator");
        System.out.println(sensorData);
        out.write(sensorData);
        out.flush();
        Thread.sleep(sampleRate);  // sample rate per ms
      }
    } catch (Throwable e) {
        e.printStackTrace();
      }
  }
}
