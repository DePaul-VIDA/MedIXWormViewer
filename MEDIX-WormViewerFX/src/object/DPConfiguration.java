package object;

import java.util.HashSet;

/**
 *
 * @author mshao1
 */
public class DPConfiguration extends FilePathConfiguration {
    private static final String[] TABLE_NAMES = new String[]{"Straintype", "ImageInfo", "ExperimentalFeatures", "FeatureLog", "HeadTailInfo", "LogDat", "RawMovementFeatures", "MovementFeaturesBinned", "Occupancy", "PostureFeatures", "ReferenceFeatures", "SegmentCentroid", "SizeandShapeFeatures", "Tracker", "TrajectoryFeatures", "VideoInfo"};
    
    public String[] getTABLE_NAMES() {
        return TABLE_NAMES;
    } 
}