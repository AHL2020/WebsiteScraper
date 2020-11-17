package websiteScraper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateManager {

    public static void main(String[] args) {

        test();
    }

    private static void test() {
        String[] testDate = {
                "La Liga Classics – Barcelona vs Valladolid -16-05-2010"
                , "La Liga Classics – Barcelona v Osasuna – 26th April 2017"
                , "David Beckham Special – 9th May 2020"
                , "World Cup Classics – Argentina v England – 1st June 2002"
                , "World Cup Classics – Argentina vs England – 2nd June 2002"
                , "World Cup Classics – Argentina V England – 3rd June 2002"
                , "World Cup Classics – Argentina VS England – 21st Nov 2002"
                , "World Cup Classics – Argentina v England – 22nd June 2002"
                , "World Cup Classics – Argentina vs England – 23rd June 2002"
                , "World Cup Classics – Argentina vs England – Wed, 27 Nov 2019"
                , "World Cup Classics – Argentina vs England – Thu, 1 Nov 2019"
                , "World Cup Classics – Argentina vs England – Wed, Nov 2019"
                , "World Cup Classics – Argentina vs England – 16-05-2010"
                , "World Cup Classics – Argentina vs England – 16-5-10"
                , "World Cup Classics – Argentina vs England – 16-5-010"
                , "World Cup Classics – Argentina vs England – 15 January 2020"
                , "World Cup Classics – Argentina vs England – 15 Jan 2020"
                , "World Cup Classics – Argentina vs England – Apr 30, 2020 - xxxx"
                , "World Cup Classics – Argentina vs England – Apr 1, 2020"
        };
        for(int i = 0; i < testDate.length; i++) {
            Map<String, String> dateParts = extractDate(testDate[i]);
            System.out.println("Text: " + testDate[i]);
            System.out.println("Date - day: " + dateParts.get("day"));
            System.out.println("Date - month: " + dateParts.get("month"));
            System.out.println("Date - year: " + dateParts.get("year"));
            System.out.println("date parts: " + dateParts);
            System.out.println("Formatted: " + formatDate(dateParts));

            String formatted = formatDate(toInstant(formatDate(dateParts)), "yyyy-MM-dd hh:mm:ss");
            System.out.println("Formatted: " + formatted);
            formatted = formatDate(toInstant(formatDate(dateParts)), "EEEE, dd MMMM yyyy");
            System.out.println("Formatted: " + formatted);
        }
    }

    public static String formatDate(Instant instant, String pattern) {
        if(instant == null) return "";
        // "yyyy-MM-dd hh:mm:ss"
        //System.out.println("Instant: " + instant);
        LocalDateTime datetime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        String formatted = DateTimeFormatter.ofPattern(pattern).format(datetime);
        return formatted;
    }

    /**
     * Input formats supported:
     * Wed, 27 Nov 2019   DD MMM YYYY
     * 26th April 2017
     * 26 April 2017
     * 26 Apr 2017
     * 16-05-2010         DD-MM-YYYY
     *
     * Output format: hashmap with 'day', 'month', 'year' as keys
     */
    public static Map<String, String> extractDate(String text) {
        Map<String, String> dateParts = new HashMap<>();
        String dateString = "";
        String regex = "";
        Matcher m = null;
        boolean found = false;
        // some cleaning first
        //System.out.println("Before: " + text);
        text = text.replaceAll("(?<=\\d)(st|nd|rd|th)", "");
        //System.out.println("After: " + text);
        // format: 27 Nov 2019 or 27th Nov 2019
        if(!found) {
            regex = "[0-9]{1,2} [a-zA-Z]{3} [0-9]{4}";
            m = Pattern.compile(regex).matcher(text);
            if(m.find()) {
                found = true;
                dateString = m.group();
                //System.out.println("parsed: " + dateString);
                String[] parts = dateString.split(" ");
                if(parts.length==3) {
                    dateParts.put("day", parts[0]);
                    dateParts.put("month", parts[1]);
                    dateParts.put("year", parts[2]);
                }
            }
        }
        // format: 27 November 2019 or 27th November 2019
        if(!found) {
            regex = "[0-9]{1,2} (January|February|March|April|May|June|July|August|September|October|November|December) [0-9]{4}";
            m = Pattern.compile(regex).matcher(text);
            if(m.find()) {
                found = true;
                dateString = m.group();
                //System.out.println("parsed: " + dateString);
                String[] parts = dateString.split(" ");
                if(parts.length==3) {
                    dateParts.put("day", parts[0]);
                    dateParts.put("month", parts[1]);
                    dateParts.put("year", parts[2]);
                }
            }
        }
        // format:  Apr 30, 2020
        if(!found) {
            regex = "[a-zA-Z]{3} [0-9]{1,2}, [0-9]{4}";
            m = Pattern.compile(regex).matcher(text);
            if(m.find()) {
                found = true;
                dateString = m.group();
                //System.out.println("parsed: " + dateString);
                String[] parts = dateString.split(" ");
                if(parts.length==3) {
                    dateParts.put("day", parts[1].replace(",", ""));
                    dateParts.put("month", parts[0]);
                    dateParts.put("year", parts[2]);
                }
            }
        }
        // format:
        if(!found) {
            regex = "[0-9]{4}-[0-9]{2}";
            m = Pattern.compile(regex).matcher(text);
            if(m.find()) {
                found = true;
                dateString = m.group();
                //System.out.println("parsed: " + dateString);
                String[] parts = dateString.split("-");
                if(parts.length==2) {
                    dateParts.put("day", "");
                    dateParts.put("month", "");
                    dateParts.put("year", parts[0]);
                }
            }
        }
        if(!found) {
            // for future use
        }
        // format: 16-05-2010         DD-MM-YYYY
        if(!found) {
            regex = "[0-9]{1,2}-[0-9]{1,2}-[0-9]{2,4}";
            m = Pattern.compile(regex).matcher(text);
            if(m.find()) {
                found = true;
                dateString = m.group();
                //System.out.println("parsed: " + dateString);
                String[] parts = dateString.split("-");
                if(parts.length==3) {
                    dateParts.put("day", parts[0]);
                    dateParts.put("month", parts[1]);
                    dateParts.put("year", parts[2]);
                }
            }
        }
        return dateParts;
    }

    // returns "2019-12-19T00:00:00.000Z"
    public static String formatDate(Map<String, String> dateParts) {
        String month = dateParts.get("month");
        if(!isInt(month)) month = monthNameToNumber(month);
        return formatDate(dateParts.get("day"), month, dateParts.get("year"));
    }

    public static boolean isInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }

    /**
     *
     * @param day: 19
     * @param month: 12
     * @param year: 2019
     * @return "2019-12-19T00:00:00.000Z"
     */
    public static String formatDate(String day, String month, String year) {
        if(day == null || month == null || year == null) return "";
        if(!isInt(month)) month = monthNameToNumber(month);
        if(day.length() < 1 || day.length() > 2) return "";
        if(month.length() < 1 || month.length() > 2) return "";
        if(year.length() != 2 && year.length() != 4) return "";
        if(day.length()==1) day = "0" + day;
        if(month.length()==1) month = "0" + month;
        if(year.length()==2 && Integer.parseInt(year) > 50) year = "19" + year;
        if(year.length()==2 && Integer.parseInt(year) < 50) year = "20" + year;
        return "" + year + "-" + month + "-" + day + "T00:00:00.000Z";
    }

    public static String monthNameToNumber(String monthName) {
        if(monthName == null) return "";
        String monthNumber = "";
        switch(monthName) {
            case "Jan"       : monthNumber = "01"; break;
            case "January"   : monthNumber = "01"; break;
            case "Feb"       : monthNumber = "02"; break;
            case "February"  : monthNumber = "02"; break;
            case "Mar"       : monthNumber = "03"; break;
            case "March"     : monthNumber = "03"; break;
            case "Apr"       : monthNumber = "04"; break;
            case "April"     : monthNumber = "04"; break;
            case "May"       : monthNumber = "05"; break;
            case "Jun"       : monthNumber = "06"; break;
            case "June"      : monthNumber = "06"; break;
            case "Jul"       : monthNumber = "07"; break;
            case "July"      : monthNumber = "07"; break;
            case "Aug"       : monthNumber = "08"; break;
            case "August"    : monthNumber = "08"; break;
            case "Sep"       : monthNumber = "09"; break;
            case "September" : monthNumber = "09"; break;
            case "Oct"       : monthNumber = "10"; break;
            case "October"   : monthNumber = "10"; break;
            case "Nov"       : monthNumber = "11"; break;
            case "November"  : monthNumber = "11"; break;
            case "Dec"       : monthNumber = "12"; break;
            case "December"  : monthNumber = "12"; break;
            default: monthNumber = "";
        }
        return monthNumber;
    }

    public static Instant toInstant(String str) {
        if(str.equalsIgnoreCase("")) return null;
        Instant instant = null;
        try {
            int idx = str.indexOf("+");
            if(idx != -1) {
                str = str.substring(0, idx) + ".000Z";
            }
            //System.out.println("str: " + str);
            instant = Instant.parse(str);
            //System.out.println("instant: " + instant);
            //System.exit(0);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return instant;
    }

}
