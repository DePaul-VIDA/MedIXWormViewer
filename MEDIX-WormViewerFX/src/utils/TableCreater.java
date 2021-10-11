package utils;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import singleton.ConfigurationManager;

/**
 *
 * @author MSHAO1
 */
public class TableCreater {

    static final String[] TABLE_NAMES = new String[]{"Straintype", "ImageInfo", "ExperimentalFeatures", "FeatureLog", "HeadTailInfo", "LogDat", "RawMovementFeatures", "MovementFeaturesBinned", "Occupancy", "PostureFeatures", "ReferenceFeatures", "SegmentCentroid", "SizeandShapeFeatures", "Tracker", "TrajectoryFeatures", "VideoInfo"};

    private ArrayList<String> frameIdList;
    private String strainTypeId;
    private CSVParser masterFileDataRecordParser;
    private TextArea consoleDisplayTextArea;

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

    public TableCreater(TextArea consoleDisplayTextArea) {
        try {
            this.consoleDisplayTextArea = consoleDisplayTextArea;
            new File(ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath()).mkdir();
            strainTypeId = "";
            frameIdList = new ArrayList<>();
            final String masterFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getMasterFilePath();
            consoleDisplayTextArea.setText("====== Start of reading in master file ======\n");
            Reader masterFileIn;
            masterFileIn = new FileReader(masterFilePath);
            masterFileDataRecordParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(masterFileIn);
            consoleDisplayTextArea.setText("done!\n");
            consoleDisplayTextArea.setText("====== End of reading in master file ======\n");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void resetMasterFileDataRecordParser() throws IOException {
        if (!masterFileDataRecordParser.isClosed()) {
            try {
                final String masterFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getMasterFilePath();
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== Start of reseting MF parser ======\n");
                Reader masterFileIn;
                masterFileIn = new FileReader(masterFilePath);
                masterFileDataRecordParser = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(masterFileIn);
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "done!\n");
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "====== End of reseting MF parser ======\n");
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void createAllDBTables() {
        createStraintype();
        createOccupancy();
        createImageInfo();
        createImagePathInfo();
        createLogDat();
        createTablesFromMasterFile();
        creatMovementFeaturesBinned();
        creatOtherTables();

        consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "\n\n!!! All 17 tables completed for " + strainTypeId + " !!!\n");
        Utils.displaySimpleDialog(Alert.AlertType.INFORMATION, "All 17 tables completed for " + strainTypeId);
    }

    private void createStraintype() {
        try {
            final String[] outputHeaders = new String[]{"StrainTypeid", "WormType", "ResolutionType", "FoodCondition", "SIndex"};
            final String outputFileName = "Straintype.csv";
            final String outputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath() + "\\" + outputFileName;
            File outputFile = new File(outputFilePath);
            outputFile.createNewFile();

            final Appendable outputFileWriter = new FileWriter(outputFilePath);
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(outputHeaders).print(outputFileWriter);

            String[] decompositedCName = ConfigurationManager.getConfigurationManager().getGTConfiguration().getCatagoryName().split("_");
            String wormType = "";
            String resolutionType = "";
            String foodCondition = "";
            String sIndex = "";

            if (decompositedCName.length > 0) {
                wormType = decompositedCName[0];

                String fCAndSI = "";
                if (decompositedCName.length == 2) {
                    resolutionType = "LR";
                    fCAndSI = decompositedCName[1];
                } else if (decompositedCName.length == 3) {
                    resolutionType = "HR";
                    fCAndSI = decompositedCName[2];
                }

                if (!fCAndSI.isEmpty()) {
                    foodCondition = fCAndSI.replaceAll("[^A-Za-z]", "");
                    sIndex = fCAndSI.replaceAll("[^0-9]", "");
                }

                strainTypeId = wormType + "_" + resolutionType + "_" + foodCondition + "_" + sIndex;
            } else {
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "CATAGORY_NAME parse fails\n");
                return;
            }

            List<String> outputFileData = new ArrayList<>();
            outputFileData.add(strainTypeId);
            outputFileData.add(wormType);
            outputFileData.add(resolutionType);
            outputFileData.add(foodCondition);
            outputFileData.add(sIndex);
            printer.printRecord(outputFileData);
            printer.flush();
            printer.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= StrainTypeId creat completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createOccupancy() {
        try {
            String[] occupancyHeaders = new String[]{"FrameId", "OTime", "UniqueCellsVisited"};
            String occupancyOutputFileName = "Occupancy.csv";
            CSVPrinter occupancyPrinter = getOutputCSVPrinter(occupancyOutputFileName, occupancyHeaders);

            String occupancyInputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getKylePath() + "\\occupancy.csv";
            Reader occupancyInputFileIn;
            occupancyInputFileIn = new FileReader(occupancyInputFilePath);
            CSVParser occupancyInputFileDataRecordParser = CSVFormat.DEFAULT.parse(occupancyInputFileIn);
            Iterator<CSVRecord> occupancyInputFileDataRecordParserItr = occupancyInputFileDataRecordParser.iterator();

            while (occupancyInputFileDataRecordParserItr.hasNext()) {
                CSVRecord record = occupancyInputFileDataRecordParserItr.next();

                List<String> outputFileData = new ArrayList<>();
                outputFileData.add(strainTypeId);
                outputFileData.add(record.get(0));
                outputFileData.add(record.get(1));
                occupancyPrinter.printRecord(outputFileData);
                occupancyPrinter.flush();
            }

            occupancyPrinter.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= Occupancy creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createImageInfo() {
        try {
            final String[] outputHeaders = new String[]{"FrameId", "StrainTypeId", "ImageNumber", "TimeElapsed", "TimeDelta", "IMinute"};
            final String outputFileName = "ImageInfo.csv";
            final String outputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath() + "\\" + outputFileName;
            File outputFile = new File(outputFilePath);
            outputFile.createNewFile();

            final Appendable outputFileWriter = new FileWriter(outputFilePath);
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(outputHeaders).print(outputFileWriter);

            String frameId = "";
            String imageNumberStr = "";
            String timeElapsedStr = "";
            String timeDelta = "";
            String iMinute = "";

            Iterator<CSVRecord> masterFileDataRecordItr = masterFileDataRecordParser.iterator();

            while (masterFileDataRecordItr.hasNext()) {
                CSVRecord record = masterFileDataRecordItr.next();
                imageNumberStr = record.get(0);
                frameId = strainTypeId + "_" + imageNumberStr;
                frameIdList.add(frameId);
                timeDelta = record.get(2);
                timeElapsedStr = record.get(1);
                double timeElapsed = Double.parseDouble(timeElapsedStr);
                iMinute = Long.toString(Math.round(Math.floor(timeElapsed / 60.0)));

                List<String> outputFileData = new ArrayList<>();
                outputFileData.add(frameId);
                outputFileData.add(strainTypeId);
                outputFileData.add(imageNumberStr);
                outputFileData.add(timeElapsedStr);
                outputFileData.add(timeDelta);
                outputFileData.add(iMinute);
                printer.printRecord(outputFileData);
                printer.flush();

                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + imageNumberStr + "\n");
            }
            printer.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= ImageInfo creation completed ==========\n");

            createVideoInfo(strainTypeId, timeElapsedStr, Integer.parseInt(imageNumberStr) + 1);
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createVideoInfo(String strainTypeId, String endTimeStr, int numberOfFrames) {
        try {
            final String[] outputHeaders = new String[]{"StrainTypeId", "Resolution", "FrameRate", "Length", "TotalFrames", "VideoPath"};
            final String outputFileName = "VideoInfo.csv";
            final String outputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath() + "\\" + outputFileName;
            File outputFile = new File(outputFilePath);
            outputFile.createNewFile();

            final Appendable outputFileWriter = new FileWriter(outputFilePath);
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(outputHeaders).print(outputFileWriter);

            String resolution = "";
            if (strainTypeId.contains("HR")) {
                resolution = "1280*960";
            } else {
                resolution = "640*480";
            }
            int frameRate = Math.round(numberOfFrames / Float.parseFloat(endTimeStr));
            String videoPath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getInputPath() + ".avi";

            List<String> outputFileData = new ArrayList<>();
            outputFileData.add(strainTypeId);
            outputFileData.add(resolution);
            outputFileData.add(Integer.toString(frameRate));
            outputFileData.add(endTimeStr);
            outputFileData.add(Integer.toString(numberOfFrames));
            outputFileData.add(videoPath);
            printer.printRecord(outputFileData);
            printer.flush();
            printer.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= VideoInfo creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createImagePathInfo() {
        try {
            String[] imagePathInfoFeaturesOutputHeaders = new String[]{"FrameId", "Path"};
            String outputFileName = "ImagePathInfo.csv";
            CSVPrinter imagePathInfoFeaturesPrinter = getOutputCSVPrinter(outputFileName, imagePathInfoFeaturesOutputHeaders);

            String imagePathDirStr = ConfigurationManager.getConfigurationManager().getGTConfiguration().getInputPath() + "\\";
            File imagePathDir = new File(imagePathDirStr);

            Iterator<String> frameIdItr = frameIdList.iterator();
            int counter = 0;
            for (final File f : imagePathDir.listFiles(IMAGE_FILTER)) {
                if (counter < ConfigurationManager.getConfigurationManager().getGTConfiguration().getOffset()) {
                    counter++;
                    continue;
                }

                if (frameIdItr.hasNext()) {
                    String frameId = frameIdItr.next();
                    List<String> outputFileData = new ArrayList<>();
                    outputFileData.add(frameId);
                    outputFileData.add(f.getAbsolutePath());
                    imagePathInfoFeaturesPrinter.printRecord(outputFileData);
                    imagePathInfoFeaturesPrinter.flush();
                }
            }

            imagePathInfoFeaturesPrinter.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= ImagePathInfo creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createLogDat() {
        try {
            String[] logDatOutputHeaders = new String[]{"FrameId", "TimeElapsedInLog", "Xpixel", "Ypixel"};
            String outputFileName = "LogDat.csv";
            CSVPrinter logDatPrinter = getOutputCSVPrinter(outputFileName, logDatOutputHeaders);

            String trackerDatPath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getLogPath() + "\\log.dat";
            File trackDat = new File(trackerDatPath);
            if (!trackDat.exists()) {
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "No log.dat file!\n");
                logDatPrinter.close();
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= LogDat creation completed ==========\n");
                return;
            }

            DataInputStream is = new DataInputStream(new FileInputStream(new File(trackerDatPath)));
            Iterator<String> frameIdItr = frameIdList.iterator();

            while (frameIdItr.hasNext()) {
                String frameId = frameIdItr.next();
                is.readInt(); //frame
                Long timeStamp = is.readLong();
                int x = is.readInt();
                int y = is.readInt();
                is.readInt();//isMoving

                System.out.println(frameId);

                List<String> outputFileData = new ArrayList<>();
                outputFileData.add(frameId);
                outputFileData.add(Long.toString(timeStamp));
                outputFileData.add(Integer.toString(x));
                outputFileData.add(Integer.toString(y));
                logDatPrinter.printRecord(outputFileData);
                logDatPrinter.flush();
            }

            is.close();
            logDatPrinter.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= LogDat creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void creatMovementFeaturesBinned() {
        try {
            final String[] outputHeaders = new String[]{"StrainTypeId", "MMinute", "Speed", "Acceleration", "Angle", "AngularVelocity"};
            final String outputFileName = "MovementFeaturesBinned.csv";
            final String outputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath() + "\\" + outputFileName;
            File outputFile = new File(outputFilePath);
            outputFile.createNewFile();

            final Appendable outputFileWriter = new FileWriter(outputFilePath);
            final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(outputHeaders).print(outputFileWriter);

            final String inputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getKylePath() + "\\movementFeaturesBinned.csv";
            Reader inputFileIn;
            inputFileIn = new FileReader(inputFilePath);
            CSVParser inputFileDataRecordParser = CSVFormat.DEFAULT.parse(inputFileIn);
            Iterator<CSVRecord> inputFileDataRecordParserItr = inputFileDataRecordParser.iterator();
            while (inputFileDataRecordParserItr.hasNext()) {
                CSVRecord record = inputFileDataRecordParserItr.next();

                List<String> outputFileData = new ArrayList<>();
                outputFileData.add(strainTypeId);
                for (String s : record) {
                    outputFileData.add(s);
                }
                printer.printRecord(outputFileData);
                printer.flush();
            }
            inputFileDataRecordParser.close();
            printer.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= MovementFeaturesBinned creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void creatOtherTables() {
        try {
            String[] trackerOutputHeaders = new String[]{"FrameId", "Xstep", "Ystep", "IsMoving"};
            String outputFileName = "Tracker.csv";
            CSVPrinter trackerPrinter = getOutputCSVPrinter(outputFileName, trackerOutputHeaders);

            String[] featureLogOutputHeaders = new String[]{"FrameId", "FArea"};
            outputFileName = "FeatureLog.csv";
            CSVPrinter featureLogPrinter = getOutputCSVPrinter(outputFileName, featureLogOutputHeaders);

            String trackerInputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getKylePath() + "\\tracker.csv";
            Reader trackerInputFileIn;
            trackerInputFileIn = new FileReader(trackerInputFilePath);
            CSVParser trackerInputFileDataRecordParser = CSVFormat.DEFAULT.parse(trackerInputFileIn);
            Iterator<CSVRecord> trackerInputFileDataRecordParserItr = trackerInputFileDataRecordParser.iterator();

            String featureLogInputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getLogPath() + "\\feature.log";
            Reader featureLogInputFileIn;
            featureLogInputFileIn = new FileReader(featureLogInputFilePath);
            CSVParser featureLogInputFileDataRecordParser = CSVFormat.DEFAULT.parse(featureLogInputFileIn);
            Iterator<CSVRecord> featureLogInputFileDataRecordParserItr = featureLogInputFileDataRecordParser.iterator();

            for (int i = 0; i < ConfigurationManager.getConfigurationManager().getGTConfiguration().getOffset(); i++) {
                trackerInputFileDataRecordParserItr.next(); // frame offset
                featureLogInputFileDataRecordParserItr.next();
            }

            Iterator<String> frameIdItr = frameIdList.iterator();
            while (frameIdItr.hasNext()) {
                CSVRecord trackerRecord = trackerInputFileDataRecordParserItr.next();
                CSVRecord featureLogRecord = featureLogInputFileDataRecordParserItr.next();
                String frameId = frameIdItr.next();
                consoleDisplayTextArea.setText(frameId + "\n");

                List<String> trackerOutputFileData = new ArrayList<>();
                trackerOutputFileData.add(frameId);
                trackerOutputFileData.add(trackerRecord.get(3));
                trackerOutputFileData.add(trackerRecord.get(4));
                trackerOutputFileData.add(trackerRecord.get(5));
                trackerPrinter.printRecord(trackerOutputFileData);
                trackerPrinter.flush();

                List<String> featureLogOutputFileData = new ArrayList<>();
                featureLogOutputFileData.add(frameId);
                featureLogOutputFileData.add(featureLogRecord.get(3));
                featureLogPrinter.printRecord(featureLogOutputFileData);
                featureLogPrinter.flush();
            }
            trackerInputFileIn.close();
            trackerPrinter.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= Tracker creation completed ==========\n");
            featureLogInputFileIn.close();
            featureLogPrinter.close();
            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= FeatureLog creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void createTablesFromMasterFile() {
        try {
            resetMasterFileDataRecordParser();

            if (frameIdList.isEmpty()) {
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "Empty frameid list found!\n");
                return;
            }

            String[] experimentalFeaturesOutputHeaders = new String[]{"FrameId", "InertiaXX", "InertiaYY", "InertiaXY"};
            String outputFileName = "ExperimentalFeatures.csv";
            CSVPrinter experimentalFeaturesPrinter = getOutputCSVPrinter(outputFileName, experimentalFeaturesOutputHeaders);

            String[] headTailInfoOutputHeaders = new String[]{"FrameId", "IntH", "IntT", "HeadRow", "HeadCol", "TailRow", "TailCol", "HeadCurvPtRow", "HeadCurvPtCol", "TailCurvPtRow", "TailCurvPtCol", "CurvHead", "CurvTail"};
            outputFileName = "HeadTailInfo.csv";
            CSVPrinter headTailInfoPrinter = getOutputCSVPrinter(outputFileName, headTailInfoOutputHeaders);

            String[] postureFeaturesOutputHeaders = new String[]{"FrameId", "MajorAxisLength", "MinorAxisLength", "Heywood", "Hydraulic", "RectBigSide", "RectRatio", "Posture", "SkewerAngle", "IsLoop", "SkelNumPixels", "LengthToPixels", "SktAmpRatio", "SktCmptFactor", "SktElgFactor", "SktIxx", "SktIyy", "SktAglAve", "Xsym", "Ysym", "XYsym", "TrackAmplitude", "TrackPeriod", "SktvAglAve", "SktvDisAveToLength", "SktvDisMaxToLength", "SktvDisMinToLength", "SktvAglMax", "LclCentroidRow", "LclCentroidCol", "Elongation", "ComptFactor"};
            outputFileName = "PostureFeatures.csv";
            CSVPrinter postureFeaturesPrinter = getOutputCSVPrinter(outputFileName, postureFeaturesOutputHeaders);

            String[] rawMovementFeaturesOutputHeaders = new String[]{"FrameId", "Speed", "Acceleration", "Angle", "AngularVelocity"};
            outputFileName = "RawMovementFeatures.csv";
            CSVPrinter rawMovementFeaturesPrinter = getOutputCSVPrinter(outputFileName, rawMovementFeaturesOutputHeaders);

            String[] referenceFeaturesOutputHeaders = new String[]{"FrameId", "FrameNum", "NumRows", "NumCols", "Resol", "CameraStartRow", "CameraStartCol", "CameraStepRows", "CameraStepCols", "CameraOffsetRows", "CameraOffsetCols", "CropOffsetRows", "CropOffsetCols", "TotalOffsetRows", "TotalOffsetCols"};
            outputFileName = "ReferenceFeatures.csv";
            CSVPrinter referenceFeaturesPrinter = getOutputCSVPrinter(outputFileName, referenceFeaturesOutputHeaders);

            String[] segmentCentroidOutputHeaders = new String[]{"FrameId", "CentroidX", "CentroidY"};
            outputFileName = "SegmentCentroid.csv";
            CSVPrinter segmentCentroidPrinter = getOutputCSVPrinter(outputFileName, segmentCentroidOutputHeaders);

            String[] sizeandShapeFeaturesOutputHeaders = new String[]{"FrameId", "Perimeter", "MaxWidth", "Length", "Fatness", "Thickness", "Area"};
            outputFileName = "SizeandShapeFeatures.csv";
            CSVPrinter sizeandShapeFeaturesPrinter = getOutputCSVPrinter(outputFileName, sizeandShapeFeaturesOutputHeaders);

            String[] trajectoryFeaturesOutputHeaders = new String[]{"FrameId", "DirectionCode", "DeltaTime", "DeltaX", "DeltaY", "DeltaDist", "VectorAngle", "InstantVelocity", "InstantAccel", "CumDist", "Range", "GblCentroidRow", "GblCentroidCol", "SktpMovement"};
            outputFileName = "TrajectoryFeatures.csv";
            CSVPrinter trajectoryFeaturesPrinter = getOutputCSVPrinter(outputFileName, trajectoryFeaturesOutputHeaders);

            Iterator<String> frameIdItr = frameIdList.iterator();
            Iterator<CSVRecord> masterFileDataRecordItr = masterFileDataRecordParser.iterator();
            while (masterFileDataRecordItr.hasNext()) {
                CSVRecord record = masterFileDataRecordItr.next();
                String frameId = frameIdItr.next();
                consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + frameId + "\n");

                createExperimentalFeatures(experimentalFeaturesPrinter, record, frameId);
                createGenericTableFromMasterFile(headTailInfoPrinter, record, frameId, headTailInfoOutputHeaders, 1);
                createGenericTableFromMasterFile(postureFeaturesPrinter, record, frameId, postureFeaturesOutputHeaders, 1);
                createGenericTableFromMasterFile(rawMovementFeaturesPrinter, record, frameId, rawMovementFeaturesOutputHeaders, 1);
                createGenericTableFromMasterFile(referenceFeaturesPrinter, record, frameId, referenceFeaturesOutputHeaders, 1);
                createGenericTableFromMasterFile(segmentCentroidPrinter, record, frameId, segmentCentroidOutputHeaders, 1);
                createGenericTableFromMasterFile(sizeandShapeFeaturesPrinter, record, frameId, sizeandShapeFeaturesOutputHeaders, 1);
                createGenericTableFromMasterFile(trajectoryFeaturesPrinter, record, frameId, trajectoryFeaturesOutputHeaders, 1);
            }

            experimentalFeaturesPrinter.close();
            headTailInfoPrinter.close();
            postureFeaturesPrinter.close();
            rawMovementFeaturesPrinter.close();
            referenceFeaturesPrinter.close();
            segmentCentroidPrinter.close();
            sizeandShapeFeaturesPrinter.close();
            trajectoryFeaturesPrinter.close();

            consoleDisplayTextArea.setText(consoleDisplayTextArea.getText() + "========= MF related tables creation completed ==========\n");
        } catch (IOException ex) {
            Logger.getLogger(TableCreater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private CSVPrinter getOutputCSVPrinter(String fileName, String[] headers) throws IOException {
        String outputFilePath = ConfigurationManager.getConfigurationManager().getGTConfiguration().getOutputPath() + "\\" + fileName;
        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();
        Appendable outputFileWriter = new FileWriter(outputFilePath);
        CSVPrinter outputCSVPrinter = CSVFormat.DEFAULT.withHeader(headers).print(outputFileWriter);
        return outputCSVPrinter;
    }

    private void createExperimentalFeatures(CSVPrinter printer, CSVRecord masterFileDataRecord, String frameId) throws IOException {
        List<String> outputFileData = new ArrayList<>();
        outputFileData.add(frameId);
        outputFileData.add(masterFileDataRecord.get("Ixx"));
        outputFileData.add(masterFileDataRecord.get("Iyy"));
        outputFileData.add(masterFileDataRecord.get("Ixy"));
        printer.printRecord(outputFileData);
        printer.flush();
    }

    private void createGenericTableFromMasterFile(CSVPrinter printer, CSVRecord masterFileDataRecord, String frameId, String[] headers, int offest) throws IOException {
        List<String> outputFileData = new ArrayList<>();
        outputFileData.add(frameId);

        for (int i = offest; i < headers.length; i++) {
            if (headers[i].equalsIgnoreCase("DeltaTime")) {
                outputFileData.add(masterFileDataRecord.get(2));
            } else {
                outputFileData.add(masterFileDataRecord.get(headers[i]));
            }
        }
        printer.printRecord(outputFileData);
        printer.flush();
    }

}
