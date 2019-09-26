package meatbol;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
public class Date 
{
    /**
     * Empty Constructor
     */
    public Date()
    {
        
    }
        /**
     * Checks whether the string is a valid date
     * Date must be in the format yyyy-mm-dd
     * Date must be separated by '-' as a delimiter
     * @param date - date in string format, could be valid or invalid
     * @return true if valid date, otherwise false
     */
    public boolean isValidDate(String date)
    {
        // Date must be 10 characters long
        if (date.length() != 10)
            return false;
        String dateM[] = date.split("-");
        // date must be separated by '-' from format yyyy-mm-dd
        if (dateM.length != 3)
            return false;
        try
        {
            int[] maxDays = {31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            // year must be at least 4 chars
            if (dateM[0].length() != 4)
                return false;
            int year = Integer.parseInt(dateM[0]);
            // month and day must be at least 2 chars
            if (dateM[1].length() != 2 || dateM[2].length() != 2)
                return false;
            int month = Integer.parseInt(dateM[1]);
            // validate month
            if (month < 0 || month > 12)
                return false;
            int day = Integer.parseInt(dateM[2]);
            // validate day 
            if (day < 1 || day > maxDays[month - 1])
                return false;
            // if the 29th of Feb, check for leap year
            if (day == 29 && month == 2)
            {
                // it is a leap year
                if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0))
                    return true;    
                // not a leap year, so the day is invalid
                else 
                    return false;   
            }
        }
        // If either the year, month, or day contained a non-numeric character
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }
    /**
     * returns an Int representing the difference in days
     * @param date1 - the 1st date
     * @param date2 - the 2nd date
     * @return the difference in 2 dates by days
     */
    public ResultValue dateDiff(ResultValue date1, ResultValue date2) throws Exception
    {
        // Check if the dates are valid
        if (date1.type != SubClassif.DATE && date1.type != SubClassif.STRING)
            throw new Exception("Expected " + date1.value + " to be of type DATE, instead found type " + date1.type);
        if (date2.type != SubClassif.DATE && date2.type != SubClassif.STRING)
            throw new Exception("Expected " + date2.value + " to be of type DATE, instead found type " + date2.type);
        if (!isValidDate(date1.value))
            throw new Exception(date1.value + " is not a valid date");
        if (!isValidDate(date2.value))
            throw new Exception(date2.value + " is not a valid date");
        int dateDiff = dateToJulian(date1.value) - dateToJulian(date2.value);
        return new ResultValue(SubClassif.INTEGER, String.valueOf(dateDiff));
    }
    /**
     * returns an Int representing the years between the dates
     * @param date1 - the 1st date
     * @param date2 - the 2nd date
     * @return year difference between 2 dates
     * @throws Exception 
     */
    public ResultValue dateAge(ResultValue date1, ResultValue date2) throws Exception
    {
        // Check if the dates are valid
        if (date1.type != SubClassif.DATE && date1.type != SubClassif.STRING)
            throw new Exception("Expected " + date1.value + " to be of type DATE, instead found type " + date1.type);
        if (date2.type != SubClassif.DATE && date2.type != SubClassif.STRING)
            throw new Exception("Expected " + date2.value + " to be of type DATE, instead found type " + date2.type);
        if (!isValidDate(date1.value))
            throw new Exception(date1.value + " is not a valid date");
        if (!isValidDate(date2.value))
            throw new Exception(date2.value + " is not a valid date");
        int d1Days = dateToJulian(date1.value);
        int d2Days = dateToJulian(date2.value);
        int dateAge = (d1Days - d2Days) / 365;
        return new ResultValue(SubClassif.INTEGER, String.valueOf(dateAge));
    }
    /**
     * returns a date that has been adjusted by day
     * @param date - the date
     * @param days - how many days to add to the date
     * @return - the new date that has been adjusted by the days added
     * @throws Exception 
     */
    public ResultValue dateAdj(ResultValue date, ResultValue days) throws Exception
    {
        // Check for valid date
        if (date.type != SubClassif.DATE && date.type != SubClassif.STRING)
            throw new Exception("Expected " + date.value + " to be of type DATE, instead found type " + date.type);
        if (!isValidDate(date.value))
            throw new Exception(date.value + " is not a valid date");        
        // Check if days is an integer
        if (days.type != SubClassif.INTEGER)
            throw new Exception("Expected " + days.value + " to be an integer, instead found type " + days.type);
        // Extract year, month and day from the date
        String dateM[] = date.value.split("-");
        try
        {
            int year = Integer.valueOf(dateM[0]);
            int month = Integer.valueOf(dateM[1]) - 1;
            int day = Integer.valueOf(dateM[2]);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            // GregorianCalendar constructor's month is relative to zero not 01 (Feb is 01)
            Calendar calendar = new GregorianCalendar(year, month, day);
            // add days
            calendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(days.value));
            return new ResultValue(SubClassif.DATE, sdf.format(calendar.getTime()));
        }
        catch (NumberFormatException e)
        {
            throw new Exception("Failed to convert the date into years, months and days");
        }
    }
    /**
     * Converts a date into days
     * The date is in yyyy-mm-dd format
     * Assumptions: The date is valid
     * @param date - the date
     * @return a date converted into days
     */
    public int dateToJulian(String date) throws Exception
    {
        String dateM[] = date.split("-");
        // Calculate number of days since 0000-03-01
        int iCountDays = 0;
        try
        {
            int year = Integer.valueOf(dateM[0]);
            int month = Integer.valueOf(dateM[1]);
            int day = Integer.valueOf(dateM[2]);
            
            // If month is March or greater, decrease it by 3
            if (month > 2)
                month -= 3;
            else
            {
                month += 9;
                year--;
            }
            iCountDays = 365 * year
                       + year / 4 - year / 100 + year / 400
                       + (month & 306 + 5) / 10
                       + (day);
        }
        catch( NumberFormatException e)
        {
            throw new Exception(date + " is an invalid date --> in date to julian function");
        }
        return iCountDays;
    }
}
