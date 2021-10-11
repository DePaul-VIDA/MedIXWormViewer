/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annotationtoolfx.db;

import annotationtoolfx.object.AnnotationLongValue;
import annotationtoolfx.object.AnnotationShortValue;
import annotationtoolfx.object.FrameAnnotationInfo;
import java.util.ArrayList;
import java.io.*;

/**
 *
 * @author jenny
 */
public class AutoRevise {
    
    ArrayList<FrameAnnotationInfo> list;
    ArrayList<FindAnnResults> blocksNRCurrent;
    ArrayList<FindAnnResults> blocksNRNext;
    ArrayList<Change> changelist;
    FileWriter slopewriter;
    FileWriter accelwriter;
    FileWriter freqwriter;
    FileWriter perwriter;
    double[] Accel;
    double[] Speed;
	int arcount_internal = 0;
	int needsreviewcount = 0;
	int changedcount = 0;
        
        int turnCount = 0;
        int reversalCount = 0;
        int stopCount = 0;
        int turnCountNR = 0;
        int reversalCountNR = 0;
        int stopCountNR = 0;

    double RMSE_THRESHOLD = 30.0f;
    double LOW_ACCEL_THRES = 1.0f;
    double SLOPE_HIGH_THRES = 1.4f;
    double SLOPE_LOW_THRES = 0.7f;
    double thresholdAdjustment = 0.0f;
    /* minimum # of consecutive annotations.  If there are less than the minimum,
      meaning the worm was only performing this movement for MIN_ANNOTATIONS frames,
      the user is asked to revise.  It could be an indication that the annotation was 
      selected or overwritten in error.  */
      final int MIN_ANNOTATIONS = 4; 
        /*Search before and after start and endpoints by this amount*/
      final int ADJUST_RANGE = 6;
      final int SLOPE_RANGE = 11;
      static double THRESHOLD_FORWARD_SLOPE;
      

    public boolean Revise(ArrayList<FrameAnnotationInfo> frameList, String setName)
    {
    	list = frameList;
        SetupAccelAndSpeed();
        THRESHOLD_FORWARD_SLOPE = FindThresholdForwordSlope() *0.4;
        return doAutoRevise(frameList, setName);
    }
    
    boolean doAutoRevise(ArrayList<FrameAnnotationInfo> frameList, String setName)
    {
    	list = frameList;
        changelist = new ArrayList<Change>();
//        blocksNRCurrent = blocksNRNext;
        blocksNRNext = new ArrayList<FindAnnResults>();
        SetAllPredicted();
        boolean result = false;
        try {
            slopewriter = new FileWriter("c:\\out\\slopecomp.csv");
            perwriter = new FileWriter("c:\\out\\perchange.csv");
            accelwriter = new FileWriter("c:\\out\\accel.csv");
            freqwriter = new FileWriter("c:\\out\\freq.csv");
            

        result =  ReviseReservals() && ReviseStop() && ReviseSharpTurn();
            slopewriter.flush();
            slopewriter.close();
            perwriter.flush();
            perwriter.close();
            accelwriter.flush();
            accelwriter.close();
            freqwriter.flush();
            freqwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int lastAnn = FindLastAnn();
        try {
            FileWriter writer = new FileWriter("c:\\out\\MyFile.txt");
            WriteChangeList(AnnotationShortValue.Backward, writer, lastAnn);
            WriteChangeList(AnnotationShortValue.Stop, writer, lastAnn);
            WriteChangeList(AnnotationShortValue.Turn, writer, lastAnn);
            WriteChangeList(AnnotationShortValue.Unknown, writer, lastAnn);
            writer.flush();
            writer.close();
 
        } catch (IOException e) {
            e.printStackTrace();
        }
       // result = result & WalkList();
        
       WalkList(setName);
        
        return result;
    }
    
    	
    
    
    public boolean WalkList(String setName)
    {

    	int one = 0;
    	int priornrc = needsreviewcount;
    	needsreviewcount = 0;
    	int priorcc = changedcount;
    	changedcount = 0;

    	arcount_internal++;
    	int temp = 0;
        try {
            String imnum = this.list.get(3).getdbFrameId();
            int endhere = imnum.lastIndexOf("_");
            String setnum = imnum.substring(0, endhere);
//            FileWriter writer = new FileWriter(String.format("c:\\out\\%s_AR_%d.csv", setName, arcount_internal));
            FileWriter writer = new FileWriter(String.format("c:\\out\\myfile%d.csv", arcount_internal));

    	
	    	for(int i=0; i<list.size()-1; i++)
		    	{
	    		try
	    		{
		    		if(list.get(i).getPredictedAnnotation() == null ||
		    				list.get(i).getHumanAnnotation() == null)
		    			break;
	  
	    		
	    		writer.write(list.get(i).getdbFrameId());
	            writer.write(", ");
	    		if(list.get(i).getHumanAnnotation() != null)
	    			writer.write(list.get(i).getHumanAnnotation());
	            writer.write(", ");
	    		if(list.get(i).getPredictedAnnotation() != null)
	    			writer.write(list.get(i).getPredictedAnnotation());
	            writer.write(", ");
	    		
	    		if(list.get(i).getPredictedAnnotation().contains("NeedsReview")) {
    	            writer.write("1");
	    			needsreviewcount++;
	    			changedcount++;
	    		}
	    		else if(list.get(i).getHumanAnnotation().replace("-", "").compareTo(list.get(i).getPredictedAnnotation().replace("-", "")) != 0)
	    		{
    	            writer.write("1");
	    			changedcount++;
	    		}
	    		else
	    		{
    	            writer.write("0");
	    		}
	            writer.write(", ");
	            writer.write(String.format("%f",list.get(i).getSpeed()));
	            writer.write("\n");
	    		}
	    		catch(Exception e)
	    		{
	    			e.printStackTrace();
	    			    			
	    		}
    			
		    	}
	    	writer.close();
    	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if(needsreviewcount > 0)
        {
            	for(int i=0; i<list.size(); i++)
	    	{
	    		list.get(i).setSpeed(Speed[i]);
	    		if(list.get(i).getPredictedAnnotation() != null)
	    		{
		    		if(list.get(i).getPredictedAnnotation().contains("NeedsReview"))
		    		{
		    			int index = list.get(i).getPredictedAnnotation().lastIndexOf("_");
		    			String ann = list.get(i).getPredictedAnnotation().substring(index +1);
		    			list.get(i).setPredictedAnnotation(ann);
		    		}
		    		list.get(i).setHumanAnnotation(list.get(i).getPredictedAnnotation());
	    		}
	    	}
        	
	    	
	    	if(priornrc == needsreviewcount && priorcc == changedcount)
	    		return false;
	    	
                if(arcount_internal > 20)
                    return true;
                
                THRESHOLD_FORWARD_SLOPE += THRESHOLD_FORWARD_SLOPE*0.05;
                RMSE_THRESHOLD += RMSE_THRESHOLD*0.05;
                LOW_ACCEL_THRES += LOW_ACCEL_THRES*0.05;
                SLOPE_LOW_THRES -= SLOPE_LOW_THRES*0.05;
                SLOPE_HIGH_THRES += SLOPE_HIGH_THRES*0.05;
                thresholdAdjustment +=  0.05;
        	return doAutoRevise(list, setName);
        }
        return true;
    	
    	/*Look for singles and figure out how --
    	 *Write list with needs review _1, include original and revised annotation
    	 *If needs review > 0 then prep for redo
    	 *		** Apply smoothing filter 
    	 *		** Add to all thresholds by 5%
    	 *		** 
    	 * */
    	
    	
    }

    
    public int FindLastAnn()
    {
        for(int i=0; i<list.size(); i++)
        {
            if(list.get(i).getHumanAnnotation() == null ||
                    list.get(i).getHumanAnnotation().trim().compareTo("") == 0)
                return i -1;
        }
        return list.size();
    }
    
    public double FindThresholdForwordSlope()
    {
        int index = 0;
        int count = 0;
        double slope = 0;
        while(index < list.size())
        {
           FindAnnResults fBlock = FindAnnBlock(index, AnnotationShortValue.Forward);
           if(fBlock.endAnn < list.size() && 
                   list.get(fBlock.startAnn).getHumanAnnotation() != null && 
                   list.get(fBlock.startAnn).getHumanAnnotation().contains("NTD"))
           {
               for(int i = fBlock.startAnn; i<fBlock.endAnn-SLOPE_RANGE; i++)
               {
                   slope += Math.abs(getSlope(i));
                   count++;
               }
           }
           index = fBlock.endAnn + 1;
           
        }
        return slope/count;
    }
    
    public void WriteChangeList(AnnotationShortValue val, FileWriter writer, int lastAnn) throws IOException
    {
          
        if(val == AnnotationShortValue.Unknown)
        {
            writer.write("Totals for all changes \n");
        }
        else
        {
            writer.write("Totals for ");
            writer.write(val.toString());
            writer.write(" all changes \n");
        }   

        int review = 0, totalchanges = 0, shift = 0, shiftPlus = 0;
        int expandedBy = 0, contractedby = 0, onesided = 0;
        int patternNotFound = 0;
        int Change30 = 0;
        int countBlockType = 0;
        int countFrameType = 0;

        for(Change c : changelist)
        {
            if(c.Annotation == val || val == AnnotationShortValue.Unknown)
            {
                countBlockType++;
                countFrameType += c.OrigLength;
                review += c.TotalNeedsReview;
                if(c.TotalNeedsReview > 0)
                {
                    if(c.MoreThan30PercentChange)
                        Change30+=  c.TotalNeedsReview;
                    else
                        patternNotFound += c.TotalNeedsReview;
                }

                totalchanges += c.GetTotalRevised();
                shift += c.FindShift();
                shiftPlus += c.FindShiftPlus();
                expandedBy += c.ExpandedBy();
                contractedby += c.ContractedBy();
                onesided += c.OneSidedBy();
            }
        }
        
        writer.write(list.get(0).getdbFrameId());
      writer.write("\n");
/*        writer.write(new Integer(list.).toString());
        writer.write("\n");
        writer.write(new Integer(review).toString());
        writer.write("\n");
        writer.write(new Integer(patternNotFound).toString());
        writer.write("\n");
        writer.write(new Integer(Change30).toString());
        writer.write("\n");
        writer.write(new Integer(totalchanges).toString());
        writer.write("\n");
        writer.write(new Integer(shift).toString());
        writer.write("\n");
        writer.write(new Integer(shiftPlus).toString());
        writer.write("\n");
        writer.write(new Integer(expandedBy).toString());
        writer.write("\n");
        writer.write(new Integer(contractedby).toString());
        writer.write("\n");
        writer.write(new Integer(onesided).toString());
        writer.write("\n");
        writer.write(new Integer(countBlockType).toString());
        writer.write("\n");
        writer.write(new Integer(countFrameType).toString());
        writer.write("\n");
 */


        writer.write(new Integer(list.size()).toString());
        writer.write("\n");
        writer.write(new Integer(countBlockType).toString());
        writer.write("\n");
        writer.write(new Integer(countFrameType).toString());
        writer.write("\n");
        writer.write(new Integer(Change30 + patternNotFound).toString());
        writer.write("\n");
        writer.write(new Integer(patternNotFound).toString());
        writer.write("\n");
        writer.write(new Integer(Change30).toString());
        writer.write("\n");
        writer.write(new Integer(totalchanges).toString());
        writer.write("\n");
        writer.write(new Integer(shift).toString());
        writer.write("\n");
        writer.write(new Integer(shiftPlus).toString());
        writer.write("\n");
        writer.write(new Integer(expandedBy).toString());
        writer.write("\n");
        writer.write(new Integer(contractedby).toString());
        writer.write("\n");
        writer.write(new Integer(onesided).toString());
        writer.write("\n");
    
        writer.write(" ----------------- \n\n");
    }
    

    public boolean ReviseSharpTurn()
    {
        int index = 0;
        while(index < list.size())
        {
           FindAnnResults turnBlock = FindAnnBlock(index, AnnotationShortValue.Turn);
           if(turnBlock == null)
               return true;
           AnnotationShortValue nextAnn = FindNextAnn(turnBlock);
           turnCount++;
           if(list.size() > turnBlock.endAnn + 1 && 
        		   list.get(turnBlock.endAnn+1).getHumanShortAnnotation() == AnnotationShortValue.Turn)
        	   turnBlock.endAnn++;
           Change c;
           if(turnBlock.endAnn - turnBlock.startAnn < MIN_ANNOTATIONS)
           {
               c = new Change(turnBlock.endAnn - turnBlock.startAnn);
               SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn, turnBlock.endAnn, c, "TooSmall", "Forward-Sharp", turnBlock);
               
                changelist.add(c);
                index = turnBlock.endAnn + 1;
                continue;
            }

           if(list.get(turnBlock.endAnn).getHumanAnnotation() == null)
               return true;
           if(nextAnn == AnnotationShortValue.Backward)
               c = DoReversalPatternMatch(turnBlock, AnnotationShortValue.Turn, "Forward-Sharp");
           else
               c = DoSharpTurnPatternMatch(turnBlock);
           index = turnBlock.endAnn + c.EndLaterBy + 1;
           c.Annotation = AnnotationShortValue.Turn;
           
           if(c.TotalNeedsReview == 0)
        	   setWholeBlock(turnBlock.startAnn - c.StartEarlierBy + c.StartLaterBy, turnBlock.endAnn + c.EndLaterBy - c.EndEarlierBy, AnnotationLongValue.ForwardSharp);
           
           
        }
        return true;
    }
    
    public AnnotationShortValue FindNextAnn(FindAnnResults turnBlock)
    {
        int index = turnBlock.endAnn + 1;
        if(list.size() > index)
            return list.get(index).getHumanShortAnnotation();
        return AnnotationShortValue.Forward;
    }
    
    public void WriteMatchPercent(double per, AnnotationShortValue annVal, FileWriter writer)
    {
        try
        {
        writer.write(new Double(per).toString());
        writer.write(", ");
        writer.write(annVal.toString());
        writer.write("\n");
        }
        catch(Exception e)
        {
        }

    }

    
    public Change DoReversalPatternMatch(FindAnnResults annBlock, AnnotationShortValue annVal, String ann)
    {
        Change c = new Change(annBlock.endAnn - annBlock.startAnn);
        changelist.add(c);                                                                         
        c.Annotation = annVal;
        int startLowPoint = annBlock.startAnn, highpoint = annBlock.startAnn, endLowPoint = annBlock.endAnn;
            
        int endAnn = annBlock.endAnn;
        int startAnn = annBlock.startAnn;
        if(endAnn - startAnn < MIN_ANNOTATIONS)
        {
            if(endAnn < list.size())
                SetToNeedsReview(annBlock.startAnn, annBlock.endAnn, annBlock.startAnn, annBlock.endAnn, c, "TooSmall", ann, annBlock);
          return c;
        }
            //lowest near start t
            int minIndexBeg = findMinSpeedAdj(startAnn, false);
            int minIndexEnd = findMinSpeedAdj(endAnn, false);

            if(minIndexBeg > minIndexEnd)
            {
                //for debugging
                findMinSpeedAdj(startAnn, false);
                findMinSpeedAdj(endAnn, false);
                SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "RevPeaksNotFound", ann, annBlock);
                return c;
            }
            else if(minIndexBeg == minIndexEnd)
            {
                int attempt = 1;
                boolean OK = false;
                while(attempt <=3)
                {
                    minIndexBeg = findMinSpeedAdj(startAnn - ADJUST_RANGE*2*attempt, false);
                    minIndexEnd = findMinSpeedAdj(endAnn+ ADJUST_RANGE*2*attempt, false);
                    if(minIndexBeg < minIndexEnd)
                    {
                        OK = true;
                        break;
                    }
                    attempt++;
                }                
                if(!OK)
                {
                    SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "RevPeaksNotFound", ann, annBlock);
                    return c;
                }
            }
            int max = findMaxSpeed(minIndexBeg, minIndexEnd);
            if(minIndexBeg >= max || minIndexEnd <= max)
            {
                findMaxSpeed(minIndexBeg, minIndexEnd);
                    SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "RevPeaksOutOfPlace", ann, annBlock);
                return c;

            }

            /*
            which is higher start or adjusted start
            divide directions into fifths look for differences
            */
            int checkA, checkB;
            if(startAnn < minIndexBeg)
            {
                checkA = minIndexBeg;
                checkB = startAnn;
                c.StartLaterBy = minIndexBeg - startAnn;
            }
            else
            {
                checkA = startAnn;
                checkB = minIndexBeg;
                c.StartEarlierBy = startAnn - minIndexBeg;
            }

            int val = checkA - checkB;
            if(val > 5)
            {
                int inc = val/2;

                //find slope between 

                double fullway = (list.get(checkA).getSpeed() - list.get(checkB).getSpeed())
                        /(Double.parseDouble(list.get(checkA).getElapsedTime()) - Double.parseDouble(list.get(checkB).getElapsedTime()));

                double halfway = (list.get(checkA).getSpeed() - list.get(checkA-inc).getSpeed())
                        /(Double.parseDouble(list.get(checkA).getElapsedTime()) - Double.parseDouble(list.get(checkA-inc).getElapsedTime()));
                
                double comp = fullway / halfway ;
                WriteMatchPercent(comp, AnnotationShortValue.Backward, slopewriter);
                if(comp> SLOPE_HIGH_THRES)
                {
                    checkA = startAnn;
                    SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "SlopeChangeHigh", ann, annBlock);
                return c;

                }
                if(comp < SLOPE_LOW_THRES)
                {
                    checkA = startAnn;
                    SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "SlopeChangeLow", ann, annBlock);
                    return c;

                }
            }

            double per = matchPercent(minIndexBeg, minIndexEnd, annVal);
            WriteMatchPercent(per, AnnotationShortValue.Backward, perwriter);
            if(per < 0.7)
            {
                //For Debugging
                matchPercent(minIndexBeg, minIndexEnd,annVal);
                 //   c.MoreThan30PercentChange = true;
               //     SetToNeedsReview(startAnn, endAnn, minIndexBeg, minIndexEnd, c, "MatchPercent");
             //   return c;
            }


            String str = list.get(startAnn).getHumanAnnotation();
            for(int i= minIndexBeg; i< minIndexEnd; i++)
            {
                list.get(i).setPredictedAnnotation(str);
            }

            str = list.get(startAnn - 1).getHumanAnnotation();
            for(int i= startAnn; i< startAnn + c.StartLaterBy; i++)
            {
                list.get(i).setPredictedAnnotation(str);
            }
            
            if(minIndexEnd > endAnn)
            {
                c.EndLaterBy = minIndexEnd- endAnn;
            }
            else
            {
                c.EndEarlierBy = endAnn - minIndexEnd;
            }

            if(minIndexEnd + 1 < list.size())
            {
                str = list.get(endAnn + 1).getHumanAnnotation();
                for(int i= endAnn - c.EndEarlierBy; i<=endAnn; i++)
                {
                    list.get(i).setPredictedAnnotation(str);
                }        
            }

         return c;
    }
    
    public Change DoSharpTurnPatternMatch(FindAnnResults turnBlock)
    {
        Change c = new Change(turnBlock.endAnn - turnBlock.startAnn);
        c.Annotation = AnnotationShortValue.Turn;
        changelist.add(c);                                                                         


        int min1Index = findMinSpeedAdj(turnBlock.startAnn, false);
        int maxIndex = findMaxSpeedAdj(min1Index, true);
        int min2Index = findMinSpeedAdj(maxIndex, true);
        if(min1Index == 0)
        {
            SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn, turnBlock.endAnn, c, "StartNotFound", "Forward-Sharp", turnBlock);
            c.MoreThan30PercentChange = false;
            return c;
       
        }
        
        int start = 0;
        
        if(!(min1Index < maxIndex && maxIndex < min2Index))
        {        
          SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn, turnBlock.endAnn, c, "PeaksOutOfOrder", "Forward-Sharp", turnBlock);
          c.MoreThan30PercentChange = false;
          return c;
        }

        int midIndex = 0, mid2Index=0;
        double yMid = (Speed[maxIndex] - Speed[min1Index])/2 + Speed[min1Index]; 
      //  int midIndex = (min2Index-maxIndex)/2 + maxIndex;
        for(midIndex = min1Index +1; midIndex< maxIndex; midIndex++)
            if(yMid < Speed[midIndex])
                break;

        yMid = (Speed[maxIndex] - Speed[min2Index])/2 + Speed[min2Index]; 
        for(mid2Index = maxIndex +1; mid2Index< min2Index; mid2Index++)
            if(yMid > Speed[mid2Index])
                break;

        //double m = (Speed[midIndex]- Speed[min1Index])/(midIndex-min1Index);
        double m = (Speed[mid2Index]- Speed[midIndex])/(mid2Index-midIndex);
      //  double b = Speed[midIndex] -m*(midIndex-min1Index);
        double b = Speed[midIndex] -m*(midIndex);

//        int subSigEnd = (midIndex-min1Index)*2+min1Index;	
          int subSigEnd = 0;
          double y;
          for(subSigEnd = min2Index+1; subSigEnd< Speed.length; subSigEnd++)
          {
              y = m*subSigEnd+b;
              if(y <= Speed[subSigEnd] || y >= Speed[maxIndex])
                  break;
          }              

        int xLineCrossing = min1Index -1;
        int xheightBehind = min1Index -1;
        
        for(xLineCrossing=min1Index-1; xLineCrossing>0; xLineCrossing--)        
        {
            y =xLineCrossing*m+b; 
            if(y<=Speed[xLineCrossing])
            {
                break;
            }
        }
        
        double m2 = (Speed[xLineCrossing]- Speed[min1Index])/(xLineCrossing-min1Index);
        if(m2 < -0.50)
        {
            
            for(xheightBehind=min1Index; xheightBehind>0; xheightBehind--)        
            {
                if(Speed[xheightBehind]>=Speed[maxIndex])
                {
                    break;
                }
            }
            start = xLineCrossing;
        }
        else
        {
            start = min1Index;
        }
        int subsiglen = subSigEnd-midIndex;
        double[] newaxis = new double[subsiglen];
        double[] newspeed = new double[subsiglen];
        double[] fitSin = new double[subsiglen];

        int nsMinI = 500; 
        int nsMaxI = 500; 
        double nsMax = Double.MIN_VALUE;
        double nsMin = Double.MAX_VALUE;
        b=Speed[midIndex];
        for(int x=0; x<subsiglen; x++)
        {
            newaxis[x]= m*x+b;
            newspeed[x]=Speed[midIndex+x]-newaxis[x];
            if(newspeed[x] < nsMin)
            {
                nsMin = newspeed[x];
                nsMinI = x;
            }
            else if(newspeed[x] > nsMax)
            {
                nsMax = newspeed[x];
                nsMaxI = x;
            }
        }

        if(nsMin > 0 || nsMax < 0)
        {
            SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn, turnBlock.endAnn, c, "RotatedPeaks", "Forward-Sharp", turnBlock);
            c.MoreThan30PercentChange = false;
            return c;
        }
        double amp = (nsMax - nsMin)/2;

        double currSSE;
        double lowSSE = Double.MAX_VALUE;
        int lowSSEFA = 0;
        for(int freqAdj =1; freqAdj<=32; freqAdj++)
        {
            currSSE = 0;
            for(int x =1; x<subsiglen; x++)
            {

                fitSin[x]=  Math.sin(x/(double)freqAdj)*amp;
                currSSE = currSSE + (fitSin[x] - newspeed[x])*(fitSin[x] - newspeed[x]);
            }
            if(lowSSE > currSSE)
            {
                lowSSEFA = freqAdj;
                lowSSE = currSSE;
            }
        }
        
        WriteMatchPercent(lowSSEFA, AnnotationShortValue.Turn, freqwriter);
        
       double MSE = lowSSE/subsiglen;
       double RMSE = Math.sqrt(MSE);
        if(RMSE > RMSE_THRESHOLD)
        {
           SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn, turnBlock.endAnn, c, "RMSE", "Forward-Sharp", turnBlock);
            c.MoreThan30PercentChange = false;
            return c;
        }
           
        double slope = Double.MAX_VALUE;
        int index = subSigEnd + 1;
        
        do
        {
            slope = getSlope(index);
            index++;
        }
        while(slope > THRESHOLD_FORWARD_SLOPE);
//Removed
        int count = 0;
        do
        {
            
            slope = getSlope(index);
            if(slope < THRESHOLD_FORWARD_SLOPE)
                count++;
            index++;
        }
        while(count < 35 - (Speed[index]/10));
//Removed
        
        

        double frames = index - min1Index;
        double percentsimilar = matchPercent(turnBlock.startAnn, turnBlock.endAnn, AnnotationShortValue.Turn);
        WriteMatchPercent(percentsimilar, AnnotationShortValue.Turn, perwriter);
        
      
        if(percentsimilar < .7)
        {
          c.MoreThan30PercentChange = true;
         // SetToNeedsReview(turnBlock.startAnn, turnBlock.endAnn, turnBlock.startAnn - ADJUST_RANGE, turnBlock.endAnn, c, "PercentMatch");
         // return c;
        }              
        String str;
        if(start <turnBlock.startAnn )
        {
            str = "Forward-Sharp";
            c.StartEarlierBy = turnBlock.startAnn - start;
            for(int i=min1Index; i<turnBlock.startAnn; i++)
                list.get(i).setPredictedAnnotation(str);
        }
        else
        {
            str = list.get(turnBlock.startAnn-1).getPredictedAnnotation();
            c.StartLaterBy = start - turnBlock.startAnn;
            for(int i=turnBlock.startAnn; i<start; i++)
                list.get(i).setPredictedAnnotation(str);
        }
        
        if(index >turnBlock.endAnn )
        {
            str = "Forward-Sharp";
            c.EndLaterBy = index - turnBlock.endAnn;
            for(int i=turnBlock.endAnn; i<index; i++)
                list.get(i).setPredictedAnnotation(str);
        }
        else
        {
            str = list.get(turnBlock.endAnn+1).getPredictedAnnotation();
            c.EndEarlierBy = turnBlock.endAnn - index;
            for(int i=index; i<=turnBlock.endAnn; i++)
                list.get(i).setPredictedAnnotation(str);
        }
        return c;
        
    }
    
    public double getSlope(int start)
    {
        if(list.size() > start + SLOPE_RANGE)
        {
            double a = Speed[start];
            double b = Speed[start+ SLOPE_RANGE];
         
            return (b-a)/SLOPE_RANGE;
        }
            
        return Double.MAX_VALUE;
    }
    
    public boolean ReviseStop(){
       
        int index = 0;
        boolean stopped = false;
        boolean done = false;
        boolean minHit = false;
        while(index < list.size())
        {
            FindAnnResults stopBlock = FindAnnBlock(index, AnnotationShortValue.Stop);
            if(stopBlock == null)
                return true;
            stopCount++;
            if(stopBlock.endAnn == stopBlock.startAnn)
                break;
            done = false;
            stopped = false;
            int maxSpeedBeg = stopBlock.startAnn - ADJUST_RANGE * 3;
            if(maxSpeedBeg <= 0)
                maxSpeedBeg = 0;
            int maxSpeedEnd = stopBlock.startAnn + ADJUST_RANGE * 3;
            if(maxSpeedEnd >= list.size())
                maxSpeedEnd = list.size() -1;

            index = findMaxSpeedAdj(stopBlock.startAnn, false);
            int start = index;
            double min = findMinSpeed(maxSpeedBeg,  stopBlock.endAnn);
            Change c = new Change(stopBlock.endAnn - stopBlock.startAnn);
            changelist.add(c);
            c.Annotation = AnnotationShortValue.Stop;
            int newStart = adjustBeginning(stopBlock.startAnn, index, AnnotationLongValue.Stop, c);
            double startingAccel = Accel[index];
            boolean stopping = false;
            double priorSpeed = 0;
            double currentSpeed = 0;
            int newEnd = stopBlock.endAnn;
            do
            {
                currentSpeed = Math.abs(Speed[index]);
                WriteMatchPercent(Accel[index], AnnotationShortValue.Stop, accelwriter);

                if(stopping && ((currentSpeed < min + 3) || Accel[index] < LOW_ACCEL_THRES))
                {
                    minHit = false;
                    while(!minHit && (priorSpeed > currentSpeed || Accel[index] < LOW_ACCEL_THRES) && Speed.length > index+1)
                    {
                        try
                        {
                        priorSpeed = currentSpeed;
                        index++;
                        currentSpeed = Math.abs(Speed[index]);
                        if(currentSpeed <= min + 0.05)
                            minHit = true;
                        }
                        catch(Exception e)
                        {
                        }
                                    
                    }
                    
                    if(!minHit)
                    {
                        SetToNeedsReview(stopBlock.startAnn, stopBlock.endAnn, stopBlock.startAnn - ADJUST_RANGE, maxSpeedEnd, c, "MinNeverHit", "Stop", stopBlock);//Never got to stopped but should have
                        index = stopBlock.endAnn + 1;
                       break;
                        
                    }
                    
                    while(currentSpeed < min + 3 && index < list.size()-1 && Speed.length > index)
                    {
                        
                        priorSpeed = currentSpeed;
                        index++;
                        currentSpeed = Math.abs(Speed[index]);
                    }
                    done = true;
                    break;
                }  //still not stopped
                else if((Accel[index] < LOW_ACCEL_THRES || priorSpeed > currentSpeed) )
                {
                    stopping = true; //stopping
                }
                else
                {
                    
                    SetToNeedsReview(stopBlock.startAnn, stopBlock.endAnn, stopBlock.startAnn - ADJUST_RANGE, maxSpeedEnd, c, "SpeedNotLow", "Stop", stopBlock);//Never got to stopped but should have
                    while(index < stopBlock.endAnn)
                    {
                        index++;
                    }
                    break;
                }
                index++;
                priorSpeed = currentSpeed;
                
            }while(!done);

            if(done) //finished without breaking
            {
                         
                newEnd = adjustEnd(stopBlock.endAnn, index, AnnotationLongValue.Stop, c);
                if(stopBlock.endAnn > index)
                    index = stopBlock.endAnn;
            }
            if(stopBlock.startAnn < start)
                start = stopBlock.startAnn;
            if(stopBlock.endAnn < index)
                 stopBlock.endAnn = index;
                        
            
            double per = matchPercent(start, stopBlock.endAnn,AnnotationShortValue.Stop);
             WriteMatchPercent(per, AnnotationShortValue.Stop, perwriter);
            if(per < 0.7)
            {
              //  c.MoreThan30PercentChange = true;
            //    SetToNeedsReview(start, stopBlock.endAnn, stopBlock.startAnn - ADJUST_RANGE, maxSpeedEnd, c, "MatchPercent");
            //return c;
            }
            index++;
            if(c.TotalNeedsReview == 0)
            setWholeBlock(newStart, newEnd, AnnotationLongValue.Stop);
        }
        
        
        
        return true;
    }
    
    private int adjustEnd(int humanEnd, int revisedEnd,  AnnotationLongValue value, Change c){
        String setValue = value.toString();
        int res = revisedEnd;
         if(humanEnd < revisedEnd)
         {
             c.EndLaterBy = revisedEnd-humanEnd;
              for(; humanEnd<revisedEnd; humanEnd++)
                list.get(humanEnd).setPredictedAnnotation(setValue);
         }
         else
         {
             c.EndEarlierBy = humanEnd - revisedEnd;
             try
             {
                 if(humanEnd+1<list.size())
                 {
                    setValue = list.get(humanEnd+1).getPredictedAnnotation();
                    for(; revisedEnd<=humanEnd; revisedEnd++)
                        list.get(revisedEnd).setPredictedAnnotation(setValue);
                 }
             }
             catch(Exception e)
            {
            }
         }
         return res;
        
    }
    
    private int adjustBeginning(int humanBeginning, int revisedBeginning, AnnotationLongValue value, Change record)
    {
    	int res = revisedBeginning;
         String setValue = value.toString();
         if(revisedBeginning < humanBeginning)
         {
             record.StartEarlierBy = humanBeginning - revisedBeginning;
              for(; revisedBeginning<humanBeginning; revisedBeginning++)
                list.get(revisedBeginning).setPredictedAnnotation(setValue);
         }
         else
         {
             
             record.StartLaterBy = revisedBeginning - humanBeginning;
            setValue = list.get(humanBeginning).getPredictedAnnotation();
            for(; humanBeginning<=revisedBeginning; humanBeginning++)
                list.get(humanBeginning).setPredictedAnnotation(setValue);

         }
         return res;
    }
    
    private void setWholeBlock(int beg, int end, AnnotationLongValue value)
    {
        String setValue = value.toString();
    	for(int i=beg; i<=end; i++)
            list.get(i).setPredictedAnnotation(setValue);
    }
    
    
private void convolve(double[] Signal, int SignalLen, double[] Kernel, int KernelLen, double[] Result)
{
    int n;

    for (n = 0; n < SignalLen + KernelLen - 1; n++)
    {
        int kmin, kmax, k;

        Result[n] = 0;

        kmin = (n >= KernelLen - 1) ? n - (KernelLen - 1) : 0;
        kmax = (n < SignalLen - 1) ? n : SignalLen - 1;

        for (k = kmin; k <= kmax; k++)
        {
          Result[n] += Signal[k] * Kernel[n - k];
        }
    }
}
    
    private void SetupAccelAndSpeed()
    {
       
       double[] Kernel = new double[] { 0.0049, 0.0033, 0.0018, 0.0005, -0.0006, -0.0016, //6
            -0.0024, -0.0031, -0.0036, -0.0041, -0.0044, -0.0046, -0.0047, -0.0047, -0.0046, //9
           -0.0044, -0.0042, -0.0039, -0.0036, -0.0031, -0.0027, -0.0022, -0.0017, -0.0011, //9
            -0.0006, 0.0000, 0.0006, 0.0011, 0.0017, 0.0022, 0.0027, 0.0031, 0.0036, 0.0039, //10
            0.0042, 0.0044, 0.0046, 0.0047, 0.0047, 0.0046, 0.0044, 0.0041, 0.0036, 0.0031, //10
             0.0024, 0.0016, 0.0006, -0.0005, -0.0018, -0.0033, -0.0049};  //7

    double[] Smooth = new double[] { -0.0266, -0.0211, -0.0158, -0.0107, -0.0058, -0.0012, //6
           0.0033, 0.0075, 0.0114, 0.0152, 0.0187, 0.0219, 0.0250, 0.0278, 0.0304,  //9
           0.0328, 0.0350, 0.0369, 0.0386, 0.0401, 0.0413, 0.0423, 0.0431, 0.0437, //9  
           0.0440, 0.0441, 0.0440, 0.0437, 0.0431, 0.0423, 0.0413, 0.0401, 0.0386, 0.0369,  //10
            0.0350, 0.0328, 0.0304, 0.0278, 0.0250, 0.0219, 0.0187, 0.0152, 0.0114, 0.0075, //10
            0.0033, -0.0012, -0.0058, -0.0107, -0.0158, -0.0211, -0.0266}; //7
    
        
        double[] Smooth2 = new double[] { 0.0278, 0.0116, -0.0009, -0.0099, -0.0160, -0.0194,
            -0.0205, -0.0196, -0.0170, -0.0130, -0.0079, -0.0019, 0.0047, 0.0118, 0.0190, 
            0.0262, 0.0333, 0.0401, 0.0464, 0.0521, 0.0571, 0.0613, 0.0646, 0.0671, 0.0686,
            0.0691, 0.0686, 0.0671, 0.0646, 0.0613, 0.0571, 0.0521, 0.0464, 0.0401, 0.0333,
            0.0262, 0.0190, 0.0118, 0.0047, -0.0019, -0.0079, -0.0130, -0.0170, -0.0196,
            -0.0205, -0.0194, -0.0160, -0.0099, -0.0009, 0.0116, 0.0278};
        
        double[] Kernel2 = new double[] { -0.0069, -0.0015, 0.0022,0.0043,0.0051,0.0051,0.0043,
            0.0029,0.0013,-0.0005,-0.0024,-0.0041,-0.0057,-0.0071,-0.0082,-0.0089,-0.0093,-0.0094,
            -0.0091,-0.0085,-0.0075,-0.0064,-0.0050,-0.0034,-0.0017,0.0000,0.0017,0.0034,0.0050,
            0.0064,0.0075,0.0085,0.0091,0.0094,0.0093,0.0089,0.0082,0.0071,0.0057,0.0041,0.0024,
            0.0005,-0.0013,-0.0029,-0.0043,-0.0051,-0.0051,-0.0043,-0.0022,0.0015,0.0069};
        
        double[] Smooth3 = new double[] { -0.025079347,0.003134918,0.016979185,0.020600892,0.01744011,0.010298789,0.001406923,-0.007514358,-0.015192774,-0.020747964,-0.023637624,-0.02360673,-0.020639835,-0.014916444,-0.006769468,0.003353248,0.014924316,0.027369117,0.040095042,0.05251765,0.064083746,0.074291391,0.082706826,0.08897832,0.092846942,0.094154258,0.092846942,0.08897832,0.082706826,0.074291391,0.064083746,0.05251765,0.040095042,0.027369117,0.014924316,0.003353248,-0.006769468,-0.014916444,-0.020639835,-0.02360673,-0.023637624,-0.020747964,-0.015192774,-0.007514358,0.001406923,0.010298789,0.01744011,0.020600892,0.016979185,0.003134918,-0.025079347};
        double[] Kernel3 = new double[] { 0.007732997,-0.003215124,-0.006966447,-0.006333373,-0.003424907,0.000239575,0.003627652,0.006101765,0.007337221,0.007249756,0.005932192,0.003599648,0.000542831,-0.00291112,-0.006430727,-0.009705719,-0.012466515,-0.014498176,-0.015649307,-0.015836424,-0.015044287,-0.013322708,-0.010780324,-0.007575862,-0.003907382,2.77E-17,0.003907382,0.007575862,0.010780324,0.013322708,0.015044287,0.015836424,0.015649307,0.014498176,0.012466515,0.009705719,0.006430727,0.00291112,-0.000542831,-0.003599648,-0.005932192,-0.007249756,-0.007337221,-0.006101765,-0.003627652,-0.000239575,0.003424907,0.006333373,0.006966447,0.003215124,-0.007732997};

        
        double[] Smooth4 = new double[] {0.00302886,-0.01615393,0.02549673,0.00014610,-0.02136818,-0.01105701,0.01175577,0.02172830,0.01137638,-0.00894310,-0.02310143,-0.02107808,-0.00413816,0.01734368,0.03045120,0.02668222,0.00614617,-0.02236113,-0.04494717,-0.04814927,-0.02421504,0.02572234,0.09156580,0.15719041,0.20536166,0.22303374,0.20536166,0.15719041,0.09156580,0.02572234,-0.02421504,-0.04814927,-0.04494717,-0.02236113,0.00614617,0.02668222,0.03045120,0.01734368,-0.00413816,-0.02107808,-0.02310143,-0.00894310,0.01137638,0.02172830,0.01175577,-0.01105701,-0.02136818,0.00014610,0.02549673,-0.01615393,0.00302886};
        double[] Kernel4 = new double[] {-0.00162421,0.00981664,-0.01916250,0.00631231,0.01581098,0.00000397,-0.01488515,-0.01221123,0.00310718,0.01585868,0.01534691,0.00229156,-0.01363580,-0.02128672,-0.01503780,0.00227615,0.02121376,0.03073183,0.02363920,0.00004740,-0.03247593,-0.06188766,-0.07642495,-0.06921635,-0.04100296,0.00000000,0.04100296,0.06921635,0.07642495,0.06188766,0.03247593,-0.00004740,-0.02363920,-0.03073183,-0.02121376,-0.00227615,0.01503780,0.02128672,0.01363580,-0.00229156,-0.01534691,-0.01585868,-0.00310718,0.01221123,0.01488515,-0.00000397,-0.01581098,-0.00631231,0.01916250,-0.00981664,0.00162421};
        
        int sigLen = list.size();
        double[] Signal = new double[sigLen];
        for(int i=0; i<sigLen; i++)
        {
            Signal[i] = list.get(i).getSpeed();
        }
        int KernelLen = Kernel.length;
        double[] Result = new double[list.size() + KernelLen - 1];
        convolve(Signal, sigLen, Smooth, 51, Result);
        Speed = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Speed[n] = Result[n+ (KernelLen - 1)/2];
        }  
        convolve(Signal, sigLen, Kernel, 51, Result);
        Accel = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Accel[n] = Result[n+ (KernelLen - 1)/2];
        }  
    

       try {
            java.io.FileWriter writer = new java.io.FileWriter("out1.csv", false);
            writer.write("Index, Orig, Speed, Accel\n");
        for (int n = 0; n < list.size(); n++)
        {
            writer.write(String.format("%d, %f, %f, %f,\n", n, list.get(n).getSpeed(),Speed[n], Accel[n]));
        }  
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


            convolve(Signal, sigLen, Smooth2, 51, Result);
        Speed = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Speed[n] = Result[n+ (KernelLen - 1)/2];
        }  
        convolve(Signal, sigLen, Kernel2, 51, Result);
        Accel = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Accel[n] = Result[n+ (KernelLen - 1)/2];
        }  
    

       try {
            java.io.FileWriter writer = new java.io.FileWriter("out2.csv", false);
            writer.write("Index, Orig, Speed, Accel\n");
        for (int n = 0; n < list.size(); n++)
        {
            writer.write(String.format("%d, %f, %f, %f,\n", n, list.get(n).getSpeed(),Speed[n], Accel[n]));
        }  
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    
        convolve(Signal, sigLen, Smooth4, 51, Result);
        Speed = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Speed[n] = Result[n+ (KernelLen - 1)/2];
        }  
        convolve(Signal, sigLen, Kernel4, 51, Result);
        Accel = new double[list.size()];
        for (int n = 0; n < list.size(); n++)
        {
            Accel[n] = Result[n+ (KernelLen - 1)/2];
        }  
    
               try {
            java.io.FileWriter writer = new java.io.FileWriter("out4.csv", false);
            writer.write("Index, Orig, Speed, Accel\n");
        for (int n = 0; n < list.size(); n++)
        {
            writer.write(String.format("%d, %f, %f, %f,\n", n, list.get(n).getSpeed(),Speed[n], Accel[n]));
        }  
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    

    }
    
    private void SetAllPredicted(){
        for(int index = 0; index<list.size(); index++)
            list.get(index).setPredictedAnnotation(list.get(index).getHumanAnnotation());
    }

    private FindAnnResults FindAnnBlock(int index, AnnotationShortValue value)
    {
        if(blocksNRCurrent != null)
        {
            for(int i=0; i<blocksNRCurrent.size(); i++)
            {
                if(blocksNRCurrent.get(i).startAnn >= index && blocksNRCurrent.get(i).ann == value)
                    return blocksNRCurrent.get(i);
            }
            return null;
            
        }
        
        FindAnnResults results = new FindAnnResults();
        results.ann = value; 
        for(; index<list.size(); index++){
              
            if(index >= list.size())
                break;

            results.startAnn = index;
            while(list.get(index).getHumanShortAnnotation() == value){
                index++;
                if(index >= list.size()){
                    break;
                }
            }
            results.endAnn = index;
            if(results.startAnn != results.endAnn)
                break;
        }
        if(results.endAnn != list.size() -1)
            results.endAnn--;
        
        
        if(results.startAnn == results.endAnn && results.startAnn < list.size() -1)
        {
            if(list.get(results.startAnn - 1).getHumanShortAnnotation()==
               list.get(results.startAnn + 1).getHumanShortAnnotation())
            {
                list.get(results.startAnn - 1).setHumanAnnotation(list.get(results.startAnn - 1).getHumanAnnotation());
                return FindAnnBlock(results.startAnn + 1, value);
            }
        }
        return results;
    }

    public boolean ReviseReservals(){
      
        int index = 0;
        while(index < list.size())
        {
            FindAnnResults annBlock = FindAnnBlock(index, AnnotationShortValue.Backward);
            if(annBlock == null)
                return true;
            reversalCount++;
         //   annBlock.endAnn++;
            String ann = list.get(annBlock.startAnn).getHumanAnnotation();
            AnnotationLongValue lv = list.get(annBlock.startAnn).getHumanLongAnnotation();
            Change c = DoReversalPatternMatch(annBlock,  AnnotationShortValue.Backward, ann);

            //Fill in
            
            index = annBlock.endAnn + c.EndLaterBy + 1;
            if(c.TotalNeedsReview == 0)
          	   setWholeBlock(annBlock.startAnn - c.StartEarlierBy + c.StartLaterBy, annBlock.endAnn + c.EndLaterBy - c.EndEarlierBy, lv);
        }


        return true;
    }
    
    

    private void SetToNeedsReview(int start, int end, int startRev, int endRev, Change c, String description, String ann, FindAnnResults block)
    {
        if(ann != null)
        {
         if(ann.contains("Sharp"))
            turnCountNR++;
        else if(ann.contains("Reverse"))
            reversalCountNR++;
        else if(ann.contains("Stop"))
            stopCountNR++;
        }
        
        if(!description.contains("TooSmall") && !description.contains("MinNever")) //These will never change
            blocksNRNext.add(block);
        if(startRev < start)
            start = startRev;
        if(endRev > end)
            end = endRev;


        int count = 0;
      //  for(int i = start; i<=end; i++)
       // {
//            if(!list.get(i).getReviewed() || force)
    //                count++;
  //      }
  //      if(count == 1)
   //         return;
        
     //   count = 0;
        

        for(int i = start; i<=end; i++)
        {
         //   if(!list.get(i).getReviewed() || force)
            if(i < list.size())
                if(list.get(i).setPredictedAnnotation(description + "_NeedsReview_" + ann))
                    count++;
        }
        block.startAnn = start;
        block.endAnn = end;
        c.TotalNeedsReview = count;
        c.EndEarlierBy = c.StartLaterBy = c.EndLaterBy = c.StartEarlierBy = 0;
    }
        
    private double matchPercent(int start, int end, AnnotationShortValue value)
    {
        int count = 0;

        for(int i = start; i< end; i++)
        {
            if(list.get(i).getHumanShortAnnotation() == value)
                count++;

        }

        double per = (double)count/(double)(end-start);
        return per;
    }

    private int findMaxSpeed(int start, int end)
    {
        double maxStartSpeed = Speed[start];
        int maxIndex = start;

        for(int i = start; i< end; i++)
        {
            if(maxStartSpeed < Speed[i])
            {
                maxStartSpeed = Speed[i];
                maxIndex = i;
            }

        }

        return maxIndex;
    }
    
    private double findMinSpeed(int start, int end)
    {
        double minStartSpeed = Speed[start];
        int index = 0;

        for(int i = start; i<= end; i++)
        {
            if(minStartSpeed > Speed[i])
            {
                minStartSpeed = Speed[i];
                index = i;
            }
        }
        if(index == end)
        {
            index = findMinSpeedAdj(end, true);
            return Speed[index];
        }
        return minStartSpeed;
    }

    private int findMinSpeedAdj(int humanEndPoint, boolean forwardOnly)
    {
        double minStartSpeed = Speed[humanEndPoint];
        int minIndex = humanEndPoint;
        int begAdj = 0;
        if(!forwardOnly)
            begAdj = ADJUST_RANGE + (int)( thresholdAdjustment * 20);

        int start = humanEndPoint - begAdj;
        int end = humanEndPoint + ADJUST_RANGE + (int)( thresholdAdjustment * 20);

        boolean done = false;
        if(start < 0)
            start = 0;
        while(!done)
        {             
            for(int i= start; i<=end; i++)
            {
                 if(minStartSpeed > Speed[i])
                 {
                    minStartSpeed = Speed[i];
                    minIndex = i;
                }
            }

            if(minIndex == start)
             {
                if(forwardOnly)
                    done = true;
                start = start - begAdj;
                if(start < 0)
                   done = true;
            }
            else if(minIndex == end)
            {
                end = end + ADJUST_RANGE;//+ (int)( thresholdAdjustment * 20);
            }
            else
                done = true;

        }   
        return minIndex;
    }



    private int findMaxSpeedAdj(int humanEndPoint, boolean forwardOnly)
    {
        double maxStartSpeed = Speed[humanEndPoint];
        int maxIndex = humanEndPoint;
        int begAdj = 0;
        if(!forwardOnly)
            begAdj = ADJUST_RANGE;//+ (int)( thresholdAdjustment * 20);
        int start = humanEndPoint - begAdj;
        int end = humanEndPoint + ADJUST_RANGE;//+ (int)( thresholdAdjustment * 20);
        if(start < 0)
            start = 0;

        boolean done = false;

        while(!done)
        {             
            for(int i= start; i<=end; i++)
            {
                if(maxStartSpeed < Speed[i])
                {
                    maxStartSpeed = Speed[i];
                    maxIndex = i;
                }
            }

            if(maxIndex == start)
            {
                if(forwardOnly)
                    done = true;
                start = start - begAdj;
                if(start < 0)
                   done = true;

            }
            else if(maxIndex == end)
            {
                end = end + ADJUST_RANGE;//+ (int)( thresholdAdjustment * 20);
            }
            else
                done = true;

        }   
        return maxIndex;
    }

    
}
