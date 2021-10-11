/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utils;

import object.FiveNumberSummary;
import java.io.File;
import java.io.FilenameFilter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author mingfeishao
 */
public class Utils {
    static final String[] EXTENSIONS = new String[]{
        "jpeg", "jpg"
    };

    static final FilenameFilter IMAGE_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            for (final String ext : EXTENSIONS) {
                if (name.endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };
    
    public static ObservableList<String> generateDataRowFromFiveNumberSummaryList (String label, ArrayList<FiveNumberSummary> fnsList) {
        DecimalFormat df = new DecimalFormat("#.##");
        ObservableList<String> oList = FXCollections.observableArrayList();
        oList.add(label);
        for (FiveNumberSummary fns : fnsList) {
            switch (label) {
                case "Min":
                    oList.add(df.format(fns.getMin()));
                    break;
                case "1st Quartile":
                    oList.add(df.format(fns.getFirstQuartile()));
                    break;
                case "Median":
                    oList.add(df.format(fns.getMedian()));
                    break;
                case "3rd Quartile":
                    oList.add(df.format(fns.getThirdQuartile()));
                    break;
                case "Max":
                    oList.add(df.format(fns.getMax()));
                    break;
                default:
                    break;
            }           
        }
        return oList;
    }
    
    public static String convertStarinTypeIdToDatasetName(String StrainTypeId) {
        String[] decompositedSTId = StrainTypeId.split("_");
        boolean isHR = false;
        
        if (decompositedSTId[1].equals("HR")) {
            isHR = true;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(decompositedSTId[0]);
        if(isHR){
            sb.append("_");
            sb.append(decompositedSTId[1]);
        }
        sb.append("_");
        sb.append(decompositedSTId[2].toLowerCase());
        sb.append(decompositedSTId[3]);
        return sb.toString();
    }
    
    public static LinkedList<String> loadImagePathByStrainTypeId (String StrainTypeId, int offset, int end){
        LinkedList<String> imagePathList = new LinkedList<>();
//        String dataSetName = convertStarinTypeIdToDatasetName(StrainTypeId);
        String dataSetName = StrainTypeId.toUpperCase();
        String inputHost = "http://140.192.247.25:8585/images/*****/";
        inputHost = inputHost.replace("*****", dataSetName);
        System.out.println(inputHost);
        String format = "%07d.jpeg";
        if (!urlExists(inputHost+String.format(format, offset))) {
            format = "%07d.jpg";
            if(!urlExists(inputHost+String.format(format, offset))){
                format = "%06d.jpeg";
                if(!urlExists(inputHost+String.format(format, offset))){
                    return imagePathList;
                }
            }
        }
        
        for (int i = offset; i <= end; i++) {
            imagePathList.add(inputHost+String.format(format, i));
        }
        
        return imagePathList;
    }
    
    public static void displaySimpleDialog(AlertType type, String context){
        Alert alert = new Alert(type);
        alert.setTitle(type.toString());
        alert.setHeaderText(null);
        alert.setContentText(context);
        alert.showAndWait();
    }
    
   public static boolean urlExists(String URLName){
    try {
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection con =
         (HttpURLConnection) new URL(URLName).openConnection();
      con.setRequestMethod("HEAD");
      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    }
    catch (Exception e) {
       e.printStackTrace();
       return false;
    }
  } 
}
