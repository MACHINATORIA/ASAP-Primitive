package asap.primitive.dateTime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeHelper {

    public static final Date             DEFAULT_REFERENCE_DATE          = new GregorianCalendar( 2000,
                                                                                                  0,
                                                                                                  1 ).getTime( );

    /*
     */
    public static final SimpleDateFormat FORMAT_NUMERIC_DAY              = new SimpleDateFormat( "dd/MM/yyyy" );

    public static final SimpleDateFormat FORMAT_SHORT_MONTH_DAY          = new SimpleDateFormat( "dd/MMM/yyyy" );

    public static final SimpleDateFormat FORMAT_NAMED_MONTH_DAY          = new SimpleDateFormat( "dd/MMMMM/yyyy" );

    /*
     */
    public static final SimpleDateFormat FORMAT_NUMERIC_MILI             = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss.SSS" );

    public static final SimpleDateFormat FORMAT_NUMERIC_SECOND           = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );

    public static final SimpleDateFormat FORMAT_NUMERIC_MINUTE           = new SimpleDateFormat( "dd/MM/yyyy HH:mm" );

    /*
     */
    public static final SimpleDateFormat FORMAT_SHORT_MONTH_MILI         = new SimpleDateFormat( "dd/MMM/yyyy HH:mm:ss.SSS" );

    public static final SimpleDateFormat FORMAT_SHORT_MONTH_SECOND       = new SimpleDateFormat( "dd/MMM/yyyy HH:mm:ss" );

    public static final SimpleDateFormat FORMAT_SHORT_MONTH_MINUTE       = new SimpleDateFormat( "dd/MMM/yyyy HH:mm" );

    /*
     */
    public static final SimpleDateFormat FORMAT_NAMED_MONTH_MILI         = new SimpleDateFormat( "dd/MMMMM/yyyy HH:mm:ss.SSS" );

    public static final SimpleDateFormat FORMAT_NAMED_MONTH_SECOND       = new SimpleDateFormat( "dd/MMMMM/yyyy HH:mm:ss" );

    public static final SimpleDateFormat FORMAT_NAMED_MONTH_MINUTE       = new SimpleDateFormat( "dd/MMMMM/yyyy HH:mm" );

    /*
     */
    public static final SimpleDateFormat FORMAT_ORDERED_DATE_MONTH       = new SimpleDateFormat( "yyyyMM" );

    public static final SimpleDateFormat FORMAT_ORDERED_DATE_DAY         = new SimpleDateFormat( "yyyyMMdd" );

    public static final SimpleDateFormat FORMAT_ORDERED_HORARY_MINUTE    = new SimpleDateFormat( "HHmm" );

    public static final SimpleDateFormat FORMAT_ORDERED_HORARY_SECOND    = new SimpleDateFormat( "HHmmss" );

    public static final SimpleDateFormat FORMAT_ORDERED_TIMESTAMP_MINUTE = new SimpleDateFormat( "yyyyMMddHHmm" );

    public static final SimpleDateFormat FORMAT_ORDERED_TIMESTAMP_SECOND = new SimpleDateFormat( "yyyyMMddHHmmss" );

    /*
     */
    public static enum MonthFormat {
        Numeric,
        ShortNamed,
        FullNamed;
    }

    public static enum HoraryFormat {
        Miliseconds,
        Seconds,
        Minutes;
    }

    public static String formatDatetime( MonthFormat monthFormat,
                                         HoraryFormat horaryFormat,
                                         Date datetime ) {
        final String tmpInvalidHoraryFormat = "Invalid month format";
        switch ( monthFormat ) {
            case Numeric:
                switch ( horaryFormat ) {
                    case Miliseconds:
                        return FORMAT_NUMERIC_MILI.format( datetime );
                    case Minutes:
                        return FORMAT_NUMERIC_SECOND.format( datetime );
                    case Seconds:
                        return FORMAT_NUMERIC_MINUTE.format( datetime );
                    default:
                        throw new Error( tmpInvalidHoraryFormat );
                }
            case ShortNamed:
                switch ( horaryFormat ) {
                    case Miliseconds:
                        return FORMAT_SHORT_MONTH_MILI.format( datetime );
                    case Minutes:
                        return FORMAT_SHORT_MONTH_SECOND.format( datetime );
                    case Seconds:
                        return FORMAT_SHORT_MONTH_MINUTE.format( datetime );
                    default:
                        throw new Error( tmpInvalidHoraryFormat );
                }
            case FullNamed:
                switch ( horaryFormat ) {
                    case Miliseconds:
                        return FORMAT_NAMED_MONTH_MILI.format( datetime );
                    case Minutes:
                        return FORMAT_NAMED_MONTH_SECOND.format( datetime );
                    case Seconds:
                        return FORMAT_NAMED_MONTH_MINUTE.format( datetime );
                    default:
                        throw new Error( tmpInvalidHoraryFormat );
                }
            default:
                throw new Error( "Invalid date format" );
        }
    }

    /*
     */
    public static String formatTimestamp( Date timestamp ) {
        return FORMAT_NAMED_MONTH_MILI.format( timestamp );
    }

    public static String formatElapsedTime( long elapsedTimeMilis,
                                            boolean supressLeftZeroes,
                                            boolean supressRigthZeroes,
                                            boolean supressLeftPadding,
                                            String separator ) {
        StringBuilder tmpResult = new StringBuilder( );
        //
        int tmpElapsedMilis = (int) ( elapsedTimeMilis % 1000L );
        int tmpElapsedSeconds = (int) ( elapsedTimeMilis / 1000 );
        int tmpElapsedMinutes = ( tmpElapsedSeconds / 60 );
        tmpElapsedSeconds %= 60;
        int tmpElapsedHours = ( tmpElapsedMinutes / 60 );
        tmpElapsedMinutes %= 60;
        int tmpElapsedDays = ( tmpElapsedHours / 24 );
        tmpElapsedHours %= 24;
        //
        boolean tmpSuppressLeftDays = ( supressLeftZeroes && ( tmpElapsedDays == 0 ) );
        boolean tmpSuppressLeftHours = ( tmpSuppressLeftDays && ( tmpElapsedHours == 0 ) );
        boolean tmpSuppressLeftMinutes = ( tmpSuppressLeftHours && ( tmpElapsedMinutes == 0 ) );
        boolean tmpSuppressLeftSeconds = ( tmpSuppressLeftMinutes && ( tmpElapsedSeconds == 0 ) );
        boolean tmpSuppressLeftMilis = false; // ( tmpSuppressLeftSeconds && ( tmpElapsedMilis == 0 ) );
        //
        boolean tmpSupressRigthMilis = ( supressRigthZeroes && ( tmpElapsedMilis == 0 ) );
        boolean tmpSupressRigthSeconds = ( tmpSupressRigthMilis && ( tmpElapsedSeconds == 0 ) );
        boolean tmpSupressRigthMinutes = ( tmpSupressRigthSeconds && ( tmpElapsedMinutes == 0 ) );
        boolean tmpSupressRigthHours = ( tmpSupressRigthMinutes && ( tmpElapsedHours == 0 ) );
        boolean tmpSupressRigthDays = false; // ( tmpSupressRigthHours && ( tmpElapsedDays == 0 ) );
        //
        if ( !( tmpSuppressLeftDays || tmpSupressRigthDays ) ) {
            tmpResult.append( String.format( "%dd%s",
                                             tmpElapsedDays,
                                             separator ) );
            supressLeftPadding = false;
        }
        if ( !( tmpSuppressLeftHours || tmpSupressRigthHours ) ) {
            tmpResult.append( String.format( supressLeftPadding ? "%dh%s"
                                                                : "%02dh%s",
                                             tmpElapsedHours,
                                             separator ) );
            supressLeftPadding = false;
        }
        if ( !( tmpSupressRigthMinutes || tmpSuppressLeftMinutes ) ) {
            tmpResult.append( String.format( supressLeftPadding ? "%dm%s"
                                                                : "%02dm%s",
                                             tmpElapsedMinutes,
                                             separator ) );
            supressLeftPadding = false;
        }
        if ( !( tmpSuppressLeftSeconds || tmpSupressRigthSeconds ) ) {
            tmpResult.append( String.format( supressLeftPadding ? "%ds%s"
                                                                : "%02ds%s",
                                             tmpElapsedSeconds,
                                             separator ) );
            supressLeftPadding = false;
        }
        if ( !( tmpSuppressLeftMilis || tmpSupressRigthMilis ) ) {
            tmpResult.append( String.format( supressLeftPadding ? "%dms"
                                                                : "%03dms",
                                             tmpElapsedMilis,
                                             separator ) );
        }
        return tmpResult.toString( ).trim( );
    }

    public static String formatElapsedTime( long elapsedTimeMilis ) {
        return formatElapsedTime( elapsedTimeMilis,
                                  true );
    }

    public static String formatElapsedTime( long elapsedTimeMilis,
                                            boolean suppressLeft ) {
        return formatElapsedTime( elapsedTimeMilis,
                                  suppressLeft,
                                  !suppressLeft,
                                  true,
                                  "" );
    }

    /**
     * Cria um data ( o número do mês começa em "1" )
     * 
     * @param yearNum
     *            Número do ano
     * @param monthNum
     *            Número do mês começando em "1" ( Janeiro = 1 )
     * @param dayNum
     *            Número do dia
     * @return
     */
    public static Date create( Integer yearNum,
                               Integer monthNum,
                               Integer dayNum ) {
        return create( yearNum,
                       monthNum,
                       dayNum,
                       0,
                       0,
                       0,
                       0 );
    }

    /**
     * Cria um data ( o número do mês começa em "1" )
     * 
     * @param yearNum
     *            Número do ano
     * @param monthNum
     *            Número do mês começando em "1" ( Janeiro = 1 )
     * @param dayNum
     *            Número do dia
     * @param hour
     *            Número da hora
     * @param minute
     *            Número do minuto
     * @return
     */
    public static Date create( Integer yearNum,
                               Integer monthNum,
                               Integer dayNum,
                               Integer hour,
                               Integer minute ) {
        return create( yearNum,
                       monthNum,
                       dayNum,
                       hour,
                       minute,
                       0,
                       0 );
    }

    /**
     * Cria um data ( o número do mês começa em "1" )
     * 
     * @param yearNum
     *            Número do ano
     * @param monthNum
     *            Número do mês começando em "1" ( Janeiro = 1 )
     * @param dayNum
     *            Número do dia
     * @param hour
     *            Número da hora
     * @param minute
     *            Número do minuto
     * @param second
     *            Número do segundo
     * @return
     */
    public static Date create( Integer yearNum,
                               Integer monthNum,
                               Integer dayNum,
                               Integer hour,
                               Integer minute,
                               Integer second ) {
        return create( yearNum,
                       monthNum,
                       dayNum,
                       hour,
                       minute,
                       second,
                       0 );
    }

    /**
     * Cria um data ( o número do mês começa em "1" )
     * 
     * @param yearNum
     *            Número do ano
     * @param monthNum
     *            Número do mês começando em "1" ( Janeiro = 1 )
     * @param dayNum
     *            Número do dia
     * @param hour
     *            Número da hora
     * @param minute
     *            Número do minuto
     * @param second
     *            Número do segundo
     * @param mili
     *            Número do milissegundo
     * @return
     */
    public static Date create( Integer yearNum,
                               Integer monthNum,
                               Integer dayNum,
                               Integer hour,
                               Integer minute,
                               Integer second,
                               Integer mili ) {
        GregorianCalendar tmpResult = new GregorianCalendar( );
        tmpResult.setLenient( false );
        tmpResult.setTime( new Date( ) );
        //
        if ( yearNum != null ) {
            tmpResult.set( Calendar.YEAR,
                           yearNum );
        }
        if ( monthNum != null ) {
            tmpResult.set( Calendar.MONTH,
                           ( monthNum - 1 ) );
        }
        if ( dayNum != null ) {
            tmpResult.set( Calendar.DAY_OF_MONTH,
                           dayNum );
        }
        if ( hour != null ) {
            tmpResult.set( Calendar.HOUR_OF_DAY,
                           hour );
        }
        if ( minute != null ) {
            tmpResult.set( Calendar.MINUTE,
                           minute );
        }
        if ( second != null ) {
            tmpResult.set( Calendar.SECOND,
                           second );
        }
        if ( mili != null ) {
            tmpResult.set( Calendar.MILLISECOND,
                           mili );
        }
        tmpResult.get( Calendar.MILLISECOND );
        return tmpResult.getTime( );
    }

    private static Pattern DMY_DATE_STRING_PATTERN  = Pattern.compile( "(\\d{1,2})[\\\\/](\\d{1,2})[\\\\/](\\d{4})" );

    private static String  DMY_DATE_STRING_MISMATCH = "Data não corresponde ao formato esperado (d/m/yyyy)";

    public static Date parseDMYString( String dmyString )
        throws ParseException {
        Matcher tmpDateMatcher = DMY_DATE_STRING_PATTERN.matcher( dmyString.trim( ) );
        if ( !tmpDateMatcher.matches( ) ) {
            throw new ParseException( DMY_DATE_STRING_MISMATCH,
                                      ( -1 ) );
        }
        return new GregorianCalendar( Integer.parseInt( tmpDateMatcher.group( 3 ) ),
                                      ( Integer.parseInt( tmpDateMatcher.group( 2 ) ) - 1 ),
                                      Integer.parseInt( tmpDateMatcher.group( 1 ) ) ).getTime( );
    }

    public static String normalizeDMYString( String dmyString ) {
        Matcher tmpDateMatcher = DMY_DATE_STRING_PATTERN.matcher( dmyString.trim( ) );
        if ( !tmpDateMatcher.matches( ) ) {
            return dmyString;
        }
        return String.format( "%02d/%02d/%04d",
                              Integer.parseInt( tmpDateMatcher.group( 1 ) ),
                              Integer.parseInt( tmpDateMatcher.group( 2 ) ),
                              Integer.parseInt( tmpDateMatcher.group( 3 ) ) );
    }

    public static int compareDMYString( String date1,
                                        String date2 )
        throws ParseException {
        Date tmpDate1 = DateTimeHelper.parseDMYString( date1 );
        Date tmpDate2 = DateTimeHelper.parseDMYString( date2 );
        if ( tmpDate1.before( tmpDate2 ) ) {
            return ( -1 );
        }
        else if ( tmpDate1.after( tmpDate2 ) ) {
            return 1;
        }
        return 0;
    }

    public static List< String > sortDMYStringList( Collection< String > dmyList ) {
        List< String > tmpResult = new ArrayList< String >( dmyList );
        Collections.sort( tmpResult,
                          new Comparator< String >( ) {

                              @Override
                              public int compare( String date1,
                                                  String date2 ) {
                                  try {
                                      return DateTimeHelper.compareDMYString( date1,
                                                                              date2 );
                                  }
                                  catch ( ParseException e ) {
                                      return date1.compareTo( date2 );
                                  }
                              }
                          } );
        return tmpResult;
    }

    public static List< Date > sort( Collection< Date > dateList ) {
        List< Date > tmpResult = new ArrayList< Date >( dateList );
        Collections.sort( tmpResult );
        return tmpResult;
    }

    public static int computeElapsedDays( Date minuendDate ) {
        return computeElapsedDays( minuendDate,
                                   DEFAULT_REFERENCE_DATE );
    }

    public static int computeElapsedDays( Date minuendDate,
                                          Date subtrahendDate ) {
        GregorianCalendar tmpMinuendCalendar = new GregorianCalendar( );
        tmpMinuendCalendar.setTime( minuendDate );
        GregorianCalendar tmpSubtrahendCalendar = new GregorianCalendar( );
        tmpSubtrahendCalendar.setTime( subtrahendDate );
        int tmpMinuendDayCount = tmpMinuendCalendar.get( Calendar.DAY_OF_YEAR );
        int tmpSubtrahendDayCount = tmpSubtrahendCalendar.get( Calendar.DAY_OF_YEAR );
        int tmpMinuendYear = tmpMinuendCalendar.get( Calendar.YEAR );
        int tmpSubtrahendYear = tmpSubtrahendCalendar.get( Calendar.YEAR );
        if ( tmpMinuendYear > tmpSubtrahendYear ) {
            GregorianCalendar tmpDraftCalendar = (GregorianCalendar) tmpSubtrahendCalendar.clone( );
            for ( int tmpYear = tmpSubtrahendYear; tmpYear < tmpMinuendYear; tmpYear++ ) {
                tmpMinuendDayCount += tmpDraftCalendar.getActualMaximum( Calendar.DAY_OF_YEAR );
                tmpDraftCalendar.add( Calendar.YEAR,
                                      1 );
            }
        }
        else if ( tmpMinuendYear < tmpSubtrahendYear ) {
            GregorianCalendar tmpDraftCalendar = (GregorianCalendar) tmpMinuendCalendar.clone( );
            for ( int tmpYear = tmpMinuendYear; tmpYear < tmpSubtrahendYear; tmpYear++ ) {
                tmpSubtrahendDayCount += tmpDraftCalendar.getActualMaximum( Calendar.DAY_OF_YEAR );
                tmpDraftCalendar.add( Calendar.YEAR,
                                      1 );
            }
        }
        return ( tmpMinuendDayCount - tmpSubtrahendDayCount );
    }

    public static long computeElapsedSeconds( Date minuendTimestamp,
                                              Date subtrahendTimestamp ) {
        return ( ( minuendTimestamp.getTime( ) - subtrahendTimestamp.getTime( ) ) / 1000 );
    }

    public static Date computeElapsedDate( Date initialDate,
                                           long elapsedSeconds ) {
        return new Date( initialDate.getTime( ) + ( elapsedSeconds * 1000 ) );
    }

    public static Date computeElapsedDate( Date initialDate,
                                           int dayOffset,
                                           int hourOffset,
                                           int minuteOffset,
                                           int secondOffset ) {
        return new Date( initialDate.getTime( )
                         + ( (long) dayOffset * 24 * 60 * 60 * 1000 )
                         + ( (long) hourOffset * 60 * 60 * 1000 )
                         + ( (long) minuteOffset * 60 * 1000 )
                         + ( (long) secondOffset * 1000 ) );
    }

    public static long getHorarySeconds( Date date ) {
        return getHorarySeconds( date,
                                 true );
    }

    public static long getHorarySeconds( Date date,
                                         boolean roundMilis ) {
        return ( ( getHoraryMilis( date ) + ( roundMilis ? 500
                                                         : 0 ) )
                 / 1000 );
    }

    public static long getHoraryMilis( Date date ) {
        GregorianCalendar tmpCalendar = new GregorianCalendar( );
        tmpCalendar.setTime( date );
        return ( ( tmpCalendar.get( Calendar.HOUR_OF_DAY ) * 60 * 60 * 1000 )
                 + ( tmpCalendar.get( Calendar.MINUTE ) * 60 * 1000 )
                 + ( tmpCalendar.get( Calendar.SECOND ) * 1000 )
                 + ( tmpCalendar.get( Calendar.MILLISECOND ) ) );
    }

    public static class TimeoutCheck {

        public TimeoutCheck( ) {
            this.setTimeout( 0 );
        }

        public TimeoutCheck( long timeoutMilis ) {
            this.setTimeout( timeoutMilis );
        }

        public void setTimeout( long timeoutMilis ) {
            this.startTime = new Date( ).getTime( );
            this.timeoutMilis = timeoutMilis;
        }

        public void reset( ) {
            this.startTime = new Date( ).getTime( );
        }

        public boolean expired( ) {
            return ( new Date( ).getTime( ) > ( this.startTime + this.timeoutMilis ) );
        }

        protected long startTime;

        protected long timeoutMilis;
    }

    public static class DateLimits {

        protected Date oldest;

        protected Date newest;

        protected Date defaultLimit;

        public DateLimits( Date defaultLimit ) {
            this.oldest = null;
            this.newest = null;
            this.defaultLimit = defaultLimit;
        }

        public DateLimits( ) {
            this( new Date( ) );
        }

        public void update( Date date ) {
            if ( ( this.oldest == null ) || date.before( this.oldest ) ) {
                this.oldest = date;
            }
            if ( ( this.newest == null ) || date.after( this.newest ) ) {
                this.newest = date;
            }
        }

        public Date getOldest( ) {
            return ( this.oldest != null ) ? this.oldest
                                           : this.defaultLimit;
        }

        public Date getNewest( ) {
            return ( this.newest != null ) ? this.newest
                                           : this.defaultLimit;
        }
    }
}
