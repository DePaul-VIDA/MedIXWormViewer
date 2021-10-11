
package utils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import singleton.ConfigurationManager;

/**
 *
 * @author MSHAO1
 */


public class MasterFileCreater {
    private final TextArea consoleDisplayTextArea;
    
    private enum MasterFileHeaders {
        FrameNum, ElapsedTimeInLogFile, DeltaTimeInLogFile, CentroidX, CentroidY, Speed, Acceleration, Angle, AngularVelocity, ElapsedTimeInVideo, NumRows, NumCols, Resol, CameraStartRow, CameraStartCol, CameraStepRows, CameraStepCols, CameraOffsetRows, CameraOffsetCols, CropOffsetRows, CropOffsetCols, TotalOffsetRows, TotalOffsetCols, LclCentroidRow, LclCentroidCol, GblCentroidRow, GblCentroidCol, Area, MajorAxisLength, MinorAxisLength, Elongation, ComptFactor, Heywood, Hydraulic, RectBigSide, RectRatio, Perimeter, Ixx, Iyy, Ixy, MaxWidth, Posture, SkewerAngle, IsLoop, Length, HeadRow, HeadCol, TailRow, TailCol, HeadCurvPtRow, HeadCurvPtCol, TailCurvPtRow, TailCurvPtCol, CurvHead, CurvTail, IntH, IntT, SkelNumPixels, LengthToPixels, Fatness, Thickness, SegStatus, SktAmpRatio, SktCmptFactor, SktElgFactor, SktIxx, SktIyy, SktAglAve, Xsym, Ysym, XYsym, TrackAmplitude, TrackPeriod, SktvAglAve, SktvDisAveToLength, SktvDisMaxToLength, SktvDisMinToLength, SktvAglMax, SktpMovement, DirectionCode, GblCentroidColNew, GblCentroidRowNew, DeltaTimeInVideo, DeltaX, DeltaY, DeltaDist, VectorAngle, InstantVelocity, InstantAccel, CumDist, Range
    }
    
    public MasterFileCreater(TextArea consoleDisplayTextArea){
        this.consoleDisplayTextArea = consoleDisplayTextArea;
    }
    
    public void createMasterFile(){
        String pssDataPath = ConfigurationManager.getConfigurationManager().getMFConfiguration().getRonPath()+ "\\AllFeatures.csv";
        String trajectoryDataPath = ConfigurationManager.getConfigurationManager().getMFConfiguration().getKylePath() + "\\movementFeatures.csv";
        String masterFilePath = ConfigurationManager.getConfigurationManager().getMFConfiguration().getMasterFilePath();
        
        try {
            boolean isFirstTime =  true;
            final Appendable masterFileWriter = new FileWriter(masterFilePath);
            final CSVPrinter printer;
            printer = CSVFormat.DEFAULT.withHeader(MasterFileHeaders.class).print(masterFileWriter);
            
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== Start of parsing PSS file ======\n");
            Reader pssDataIn;
            pssDataIn = new FileReader(pssDataPath);
            Iterable<CSVRecord> pssDataRecords = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(pssDataIn);
            Iterator<CSVRecord> pssDataRecordsItr = pssDataRecords.iterator();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "done!\n");
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== End of parsing PSS file ======\n");
            
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== Start of parsing trajectory file ======\n");
            Reader trajectoryDataIn;
            trajectoryDataIn = new FileReader(trajectoryDataPath);
            Iterable<CSVRecord> trajectoryDataRecords = CSVFormat.DEFAULT.parse(trajectoryDataIn);
            Iterator<CSVRecord> trajectoryDataRecordsItr = trajectoryDataRecords.iterator();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "done!\n");
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== End of parsing trajectory file ======\n");
            
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== Start of creating master file ======\n");
            while(pssDataRecordsItr.hasNext() && trajectoryDataRecordsItr.hasNext()) {
                CSVRecord pssDataRecord = pssDataRecordsItr.next();
                if(isFirstTime && (pssDataRecord.size() != 84)) {
                    consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "Old version PSS file found! No good!\n");
                    consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "The length of the PSS is " + pssDataRecord.size() + "\n");
                    return;
                }
                isFirstTime = false;
                CSVRecord trajectoryDataRecord = trajectoryDataRecordsItr.next();
                
                int pssDataRecordFrameID = Integer.parseInt(pssDataRecord.get(0)) - 1;
                int trajectoryDataRecordFrameID = Integer.parseInt(trajectoryDataRecord.get(0));
                
                while (pssDataRecordFrameID != trajectoryDataRecordFrameID) {
                    if (pssDataRecordFrameID < trajectoryDataRecordFrameID) {
                        pssDataRecord = pssDataRecordsItr.next();
                        pssDataRecordFrameID = Integer.parseInt(pssDataRecord.get(0)) - 1;
                    } else if (pssDataRecordFrameID > trajectoryDataRecordFrameID) {
                        trajectoryDataRecord = trajectoryDataRecordsItr.next();
                        trajectoryDataRecordFrameID = Integer.parseInt(trajectoryDataRecord.get(0));
                    }
                }
                
                List<String> masterFileData = new ArrayList<>();
                Iterator<String> trajectoryDataEntryItr = trajectoryDataRecord.iterator();
                while (trajectoryDataEntryItr.hasNext()) {
                    masterFileData.add(trajectoryDataEntryItr.next());
                }
                
                Iterator<String> pssDataEntryItr = pssDataRecord.iterator();
                pssDataEntryItr.next(); // skip SeqNum in Ron's file
                pssDataEntryItr.next(); // skip FrameNum in Ron's file
                while (pssDataEntryItr.hasNext()) {
                    masterFileData.add(pssDataEntryItr.next());
                }
                
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + masterFileData.get(0) + "\n");
                printer.printRecord(masterFileData);
                printer.flush();
            }
            
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "done!\n");
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== End of creating master file ======\n");
            Utils.displaySimpleDialog(Alert.AlertType.INFORMATION, "Finished creating MasterFile!\n");
            pssDataIn.close();
            trajectoryDataIn.close();
            printer.close();
            
        } catch (FileNotFoundException ex) {
            Utils.displaySimpleDialog(Alert.AlertType.ERROR, ex.getLocalizedMessage());
        } catch (IOException ex) {
            Utils.displaySimpleDialog(Alert.AlertType.ERROR, ex.getLocalizedMessage());
        } 
    }
}
