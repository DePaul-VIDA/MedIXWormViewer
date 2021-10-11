/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annotationtoolfx.db;

import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import annotationtoolfx.object.FileNameInfo;

/**
 *
 * @author jpiane
 */
public class Utilities {
    
    private Utilities(){

    }
    
    static String sixJPEG = "000035.jpeg";
    static String sevenJPEG = "0000035.jpeg";
    static String sixJPG = "000035.jpg";
    static String sevenJPG = "0000035.jpg";

    public static FileNameInfo getFileComponents(String strainId) {

        String partialUrl = annotationtoolfx.object.Constants.WormUrl + strainId + "/";

        String surl;
        try {
            surl = partialUrl + sixJPEG;

            URL website;
            website = new URL(surl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            rbc.close();
        }
        catch(Exception e){
           
            surl = partialUrl + sevenJPEG;

            URL website;
            try{
                website = new URL(surl);
                ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                rbc.close();
                return new FileNameInfo(annotationtoolfx.object.Constants.SevenZeros, annotationtoolfx.object.Constants.JPEG);
            }
            catch(Exception e1){
            
                surl = partialUrl + sevenJPG;

                try{
                    website = new URL(surl);
                    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                    rbc.close();
                    return new FileNameInfo(annotationtoolfx.object.Constants.SevenZeros, annotationtoolfx.object.Constants.JPG);
                }
                catch(Exception e2){
                    surl = partialUrl + sixJPG;

                    try{
                        website = new URL(surl);
                        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
                        rbc.close();
                        return new FileNameInfo(annotationtoolfx.object.Constants.SixZeros, annotationtoolfx.object.Constants.JPG);
                    }
                    catch(Exception e3){

                    } 
                }
            }
        }
        
        return new FileNameInfo(annotationtoolfx.object.Constants.SixZeros, annotationtoolfx.object.Constants.JPEG);

    }
}
