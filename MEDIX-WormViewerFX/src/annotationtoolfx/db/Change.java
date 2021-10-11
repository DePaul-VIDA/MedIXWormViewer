/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package annotationtoolfx.db;

import annotationtoolfx.object.AnnotationShortValue;
/**
 *
 * @author jenny
 */
public class Change {
	public AnnotationShortValue Annotation = AnnotationShortValue.Unknown;
	public int StartEarlierBy = 0; 
	public int StartLaterBy = 0; 
	public int EndEarlierBy = 0;
	public int EndLaterBy = 0;
	public boolean MoreThan30PercentChange = false;
        public int TotalNeedsReview = 0;
        public int OrigLength = 0;
        
        public Change(int origLen)
        {
            OrigLength = origLen;
        }
        
        public int FindShift()
        {
            if((StartEarlierBy > 0 && EndEarlierBy > 0))
            {
                if(StartEarlierBy < EndEarlierBy)
                {
                    return StartEarlierBy;
                }
                else 
                {
                    return EndEarlierBy;
                }
            }
            else if(StartLaterBy > 0 &&EndLaterBy > 0)
            {
                    if(StartLaterBy < EndLaterBy)
                {
                    return StartLaterBy;
                }
                else 
                {
                    return EndLaterBy;
                }
            }
            return 0;
        }
        
        public boolean IsShifted()
        {
            if((StartEarlierBy > 0 && EndEarlierBy > 0) ||
                (StartLaterBy > 0 &&EndLaterBy > 0))
            {
                return true;
            }
            return false;
        }
        
        public int FindShiftPlus()
          {
            if((StartEarlierBy > 0 && EndEarlierBy > 0))
            {
                if(StartEarlierBy < EndEarlierBy)
                {
                    return EndEarlierBy;
                }
                else 
                {
                    return StartEarlierBy;
                }
            }
            else if(StartLaterBy > 0 &&EndLaterBy > 0)
            {
                if(StartLaterBy < EndLaterBy)
                {
                    return EndLaterBy;
                }
                else 
                {
                    return StartLaterBy;
                }
            }
            return 0;
        }

        public int ContractedBy()
        {
            if(IsContracted())
                return -(StartLaterBy + EndEarlierBy);
            return 0;
        }

        
        public boolean IsContracted()
        {
            return StartLaterBy > 0 && EndEarlierBy > 0;
        }

        public boolean IsExpanded()
        {
            return StartEarlierBy > 0 && EndLaterBy > 0;
        }
        
        public int ExpandedBy()
        {
            if(IsExpanded())
                return StartEarlierBy + EndLaterBy;
            return 0;
        }
        
        public int GetTotalRevised()
        {
            return StartEarlierBy + StartLaterBy + EndEarlierBy + EndLaterBy;
        }
        
        public int OneSidedBy()
        {
            if(IsOneSided())
            {
                return StartEarlierBy + EndLaterBy - StartLaterBy - EndEarlierBy;
            }
            return 0;
        }

        public boolean IsOneSided()
        {
            return (StartEarlierBy != 0 && StartLaterBy == 0 && EndEarlierBy == 0 && EndLaterBy ==  0) ||
                    (StartEarlierBy == 0 && StartLaterBy != 0 && EndEarlierBy == 0 && EndLaterBy ==  0) ||
                    (StartEarlierBy == 0 && StartLaterBy == 0 && EndEarlierBy != 0 && EndLaterBy ==  0) ||
                    (StartEarlierBy == 0 && StartLaterBy == 0 && EndEarlierBy == 0 && EndLaterBy !=  0);
        }
      
}
