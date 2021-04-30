package asap.primitive.string;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class StringHelper {

    public static final String EMPTY                          = "";

    public static final String NULL                           = "<null>";

    public static final char   TAB_CHAR                       = '\t';

    public static final String TAB                            = new String( new char[ ] { TAB_CHAR } );

    public static final char   CR_CHAR                        = '\n';

    public static final String CR                             = new String( new char[ ] { CR_CHAR } );

    public static final char   LF_CHAR                        = '\r';

    public static final String LF                             = new String( new char[ ] { LF_CHAR } );

    public static final char   SPACE_CHAR                     = ' ';

    public static final String SPACE                          = new String( new char[ ] { SPACE_CHAR } );

    public static final char   FIRST_PRINTABLE_CHAR           = SPACE_CHAR;

    public static final char   LAST_PRINTABLE_CHAR            = '~';

    public static final char   NON_PRINTABLE_CHAR_REPLACEMENT = '.';

    public static final String LINE_BREAK                     = System.getProperty( "line.separator" );

    public static final String YES                            = "Sim";

    public static final String NO                             = "Não";

    public static boolean isSpace( char character ) {
        return ( ( character == SPACE_CHAR ) //
                 || ( character == TAB_CHAR ) //
                 || ( character == CR_CHAR ) //
                 || ( character == LF_CHAR ) );
    }

    public static char printable( char tmpChar ) {
        return ( ( ( tmpChar >= FIRST_PRINTABLE_CHAR ) //
                   && ( tmpChar <= LAST_PRINTABLE_CHAR ) ) ? tmpChar
                                                           : NON_PRINTABLE_CHAR_REPLACEMENT );
    }

    public static String nullDefault( String defaultString,
                                      String... inputStrings ) {
        for ( String tmpString : inputStrings ) {
            if ( tmpString != null ) {
                return tmpString;
            }
        }
        return defaultString;
    }

    public static String nullText( String... inputStrings ) {
        return nullDefault( StringHelper.NULL,
                            inputStrings );
    }

    public static String nullEmpty( String... inputStrings ) {
        return nullDefault( StringHelper.EMPTY,
                            inputStrings );
    }

    public static String yesOrNo( Boolean yesOrNo ) {
        return ( yesOrNo == null ) ? NULL
                                   : yesOrNo ? YES
                                             : NO;
    }

    public static int[ ] lengths( String... strings ) {
        int[ ] tmpResult = new int[ strings.length ];
        for ( int tmpStringIndex = 0; tmpStringIndex < strings.length; tmpStringIndex++ ) {
            String tmpString = strings[ tmpStringIndex ];
            tmpResult[ tmpStringIndex ] = ( tmpString == null ) ? ( -1 )
                                                                : tmpString.length( );
        }
        return tmpResult;
    }

    public static char[ ][ ] charArrays( String... strings ) {
        char[ ][ ] tmpResult = new char[ strings.length ][ ];
        for ( int tmpStringIndex = 0; tmpStringIndex < strings.length; tmpStringIndex++ ) {
            String tmpString = strings[ tmpStringIndex ];
            tmpResult[ tmpStringIndex ] = ( tmpString == null ) ? null
                                                                : tmpString.toCharArray( );
        }
        return tmpResult;
    }

    public static int[ ] countEveryChar( String string,
                                         char... chars ) {
        int[ ] tmpResult = new int[ chars.length ];
        for ( char tmpStringChar : string.toCharArray( ) ) {
            for ( int tmpCharIndex = 0; tmpCharIndex < chars.length; tmpCharIndex++ ) {
                if ( chars[ tmpCharIndex ] == tmpStringChar ) {
                    tmpResult[ tmpCharIndex ] += 1;
                }
            }
        }
        return tmpResult;
    }

    public static int countAllChars( String string,
                                     char... charGroup ) {
        int tmpResult = 0;
        for ( char tmpStringChar : string.toCharArray( ) ) {
            for ( char tmpGroupChar : charGroup ) {
                if ( tmpStringChar == tmpGroupChar ) {
                    tmpResult += 1;
                }
            }
        }
        return tmpResult;
    }

    public static int[ ] countCharGroups( String string,
                                          char[ ]... charGroups ) {
        int[ ] tmpResult = new int[ charGroups.length ];
        for ( char tmpStringChar : string.toCharArray( ) ) {
            for ( int tmpGroupIndex = 0; tmpGroupIndex < charGroups.length; tmpGroupIndex++ ) {
                for ( char tmpGroupChar : charGroups[ tmpGroupIndex ] ) {
                    if ( tmpStringChar == tmpGroupChar ) {
                        tmpResult[ tmpGroupIndex ] += 1;
                    }
                }
            }
        }
        return tmpResult;
    }

    public static int[ ] countCharGroups( String string,
                                          String... charGroups ) {
        return countCharGroups( string,
                                charArrays( charGroups ) );
    }

    public static String repeatedChar( int timesToRepeat,
                                       char charToRepeat ) {
        char[ ] tmpByteBuffer = new char[ timesToRepeat ];
        Arrays.fill( tmpByteBuffer,
                     charToRepeat );
        return new String( tmpByteBuffer );
    }

    public static String spaces( int timesToRepeat ) {
        return repeatedChar( timesToRepeat,
                             SPACE_CHAR );
    }

    public static String leftSub( String string,
                                  int charCount ) {
        int tmpStringLength = string.length( );
        if ( charCount < 0 ) {
            charCount = 0;
        }
        else if ( charCount >= tmpStringLength ) {
            charCount = tmpStringLength;
        }
        return string.substring( 0,
                                 charCount );
    }

    public static String rightSub( String string,
                                   int charCount ) {
        int tmpStringLength = string.length( );
        if ( charCount < 0 ) {
            charCount = 0;
        }
        else if ( charCount >= tmpStringLength ) {
            charCount = tmpStringLength;
        }
        return string.substring( ( tmpStringLength - charCount ),
                                 tmpStringLength );
    }

    public static String padLeft( String original,
                                  int padCount ) {
        return new StringBuilder( spaces( padCount ) ).append( original ).toString( );
    }

    public static String padRight( String original,
                                   int padCount ) {
        return new StringBuilder( original ).append( spaces( padCount ) ).toString( );
    }

    public static List< String > sort( Collection< String > strings ) {
        List< String > tmpResult = new ArrayList< String >( strings );
        Collections.sort( tmpResult );
        return tmpResult;
    }

    public static String shift( int shift,
                                String inputString ) {
        String tmpResult = inputString;
        int tmpShift = ( Math.abs( shift ) % inputString.length( ) );
        int tmpLength = inputString.length( );
        if ( shift < 0 ) {
            tmpResult = inputString.substring( tmpShift,
                                               tmpLength ).concat( inputString.substring( 0,
                                                                                          tmpShift ) );
        }
        else {
            tmpResult = inputString.substring( ( tmpLength - tmpShift ),
                                               tmpLength ).concat( inputString.substring( 0,
                                                                                          ( tmpLength - tmpShift ) ) );
        }
        return tmpResult;
    }

    public static String truncate( int desiredSize,
                                   String string ) {
        return truncate( '.',
                         3,
                         true,
                         desiredSize,
                         string );
    }

    public static String truncate( char endingChar,
                                   int endingLength,
                                   boolean endingSpace,
                                   int desiredSize,
                                   String string ) {
        if ( string.length( ) <= desiredSize ) {
            return string;
        }
        int tmpTruncatePosition = ( desiredSize - endingLength - ( endingSpace ? 1
                                                                               : 0 ) );
        StringBuilder tmpResult = new StringBuilder( );
        tmpResult.append( string.substring( 0,
                                            tmpTruncatePosition ) );
        if ( endingSpace ) {
            tmpResult.append( " " );
        }
        tmpResult.append( StringHelper.repeatedChar( endingLength,
                                                     endingChar ) );
        return tmpResult.toString( );
    }

    public static String shrink( int desiredSize,
                                 String string ) {
        return shrink( '.',
                       3,
                       true,
                       desiredSize,
                       string );
    }

    public static String shrink( char gapChar,
                                 int gapLength,
                                 boolean gapSpaces,
                                 int desiredSize,
                                 String string ) {
        return shrink( gapChar,
                       gapLength,
                       gapLength,
                       gapSpaces,
                       gapSpaces,
                       desiredSize,
                       string );
    }

    public static String shrink( char gapChar,
                                 int minGapLength,
                                 int maxGapLength,
                                 boolean leftGapSpace,
                                 boolean rightGapSpace,
                                 int desiredSize,
                                 String string ) {
        /*
         *  Shrinked format:
         *    - At least the leftmost character of 'string'
         *    - An optional left white gap space
         *    - A gap string built repeating 'gapChar'
         *    - An optional rigth white gap space
         *    - At least the rigthmost character of 'string'
         *   
         *  Minimum string length:       5
         *  Minimum gap length:          1
         *  
         */
        if ( minGapLength < 1 ) {
            throw new IllegalArgumentException( "minGapLength < 1" );
        }
        if ( minGapLength > maxGapLength ) {
            throw new IllegalArgumentException( "minGapLength > maxGapLength" );
        }
        int tmpEfectiveGapLength = ( minGapLength
                                     + ( leftGapSpace ? 1
                                                      : 0 )
                                     + ( rightGapSpace ? 1
                                                       : 0 ) );
        int tmpMinimumResultLength = ( 2 + tmpEfectiveGapLength );
        if ( desiredSize < tmpMinimumResultLength ) {
            throw new IllegalArgumentException( "desiredSize < minimumResultLength" );
        }
        int tmpOriginalLength = string.length( );
        if ( tmpOriginalLength <= desiredSize ) {
            return string;
        }
        int tmpLeftLength = ( ( desiredSize - tmpEfectiveGapLength ) / 2 );
        int tmpRigthLength = ( desiredSize - tmpEfectiveGapLength - tmpLeftLength );
        StringBuilder tmpResult = new StringBuilder( );
        tmpResult.append( string.substring( 0,
                                            tmpLeftLength ) );
        if ( leftGapSpace ) {
            tmpResult.append( " " );
        }
        tmpResult.append( repeatedChar( minGapLength,
                                        gapChar ) );
        if ( rightGapSpace ) {
            tmpResult.append( " " );
        }
        tmpResult.append( string.substring( ( tmpOriginalLength - tmpRigthLength ),
                                            tmpOriginalLength ) );
        return tmpResult.toString( );
    }

    public static String[ ] buildArray( String... strings ) {
        return strings;
    }

    public static String[ ] buildArray( String string,
                                        int count ) {
        String[ ] tmpResult = new String[ count ];
        for ( int tmpIndex = 0; tmpIndex < count; tmpIndex++ ) {
            tmpResult[ tmpIndex ] = string;
        }
        return tmpResult;
    }

    public static List< String > buildList( String... strings ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( String tmpString : strings ) {
            tmpResult.add( tmpString );
        }
        return tmpResult;
    }

    public static List< String > buildList( String string,
                                            int count ) {
        List< String > tmpStringList = new ArrayList< String >( );
        for ( int tmpIndex = 0; tmpIndex < count; tmpIndex++ ) {
            tmpStringList.add( string );
        }
        return tmpStringList;
    }

    public static List< String > emptyList( int count ) {
        return buildList( EMPTY,
                          count );
    }

    public static String[ ] emptyArray( int count ) {
        return buildArray( EMPTY,
                           count );
    }

    public static String[ ] concatenate( String[ ]... arrays ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( String[ ] tmpArray : arrays ) {
            tmpResult.addAll( Arrays.asList( tmpArray ) );
        }
        return tmpResult.toArray( new String[ 0 ] );
    }

    @SuppressWarnings( "unchecked" )
    public static String[ ] concatenate( String[ ] array,
                                         List< String >... lists ) {
        List< String > tmpResult = new ArrayList< String >( );
        tmpResult.addAll( Arrays.asList( array ) );
        for ( List< String > tmpList : lists ) {
            tmpResult.addAll( tmpList );
        }
        return tmpResult.toArray( new String[ 0 ] );
    }

    @SafeVarargs
    public static List< String > concatenate( List< String >... lists ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( List< String > tmpList : lists ) {
            tmpResult.addAll( tmpList );
        }
        return tmpResult;
    }

    public static List< String > concatenate( List< String > list,
                                              String[ ]... arrays ) {
        List< String > tmpResult = new ArrayList< String >( );
        tmpResult.addAll( list );
        for ( String[ ] tmpArray : arrays ) {
            tmpResult.addAll( Arrays.asList( tmpArray ) );
        }
        return tmpResult;
    }

    public static String concatenate( String... stringList ) {
        return concatenate( Arrays.asList( stringList ) );
    }

    public static String concatenate( List< String > stringList ) {
        StringBuilder tmpResult = new StringBuilder( );
        for ( String tmpString : stringList ) {
            tmpResult.append( tmpString );
        }
        return tmpResult.toString( );
    }

    public static String escapePath( String path ) {
        String tmpResult = path.replaceAll( "[\\\\/:\\*\\?\"<>\\|]",
                                            "-" );
        return tmpResult.replaceAll( "\\A-*|-*\\Z",
                                     "" );
    }

    public static String escapeFormat( String string ) {
        String tmpFormat = string.replaceAll( "\\\\",
                                              "\\\\" );
        tmpFormat = tmpFormat.replaceAll( "%",
                                          "%%" );
        return tmpFormat;
    }

    public static String escapeHtml( String string ) {
        StringBuilder tmpResult = new StringBuilder( string.length( ) );
        boolean tmpLeftSpace = true;
        int tmpTextLength = string.length( );
        char tmpTextChar;
        for ( int tmpIndex = 0; tmpIndex < tmpTextLength; tmpIndex++ ) {
            tmpTextChar = string.charAt( tmpIndex );
            if ( tmpTextChar == SPACE_CHAR ) {
                tmpResult.append( tmpLeftSpace ? "&nbsp;"
                                               : SPACE );
                tmpLeftSpace = true;
            }
            else {
                tmpLeftSpace = false;
                if ( tmpTextChar == '"' ) {
                    tmpResult.append( "&quot;" );
                }
                else if ( tmpTextChar == '&' ) {
                    tmpResult.append( "&amp;" );
                }
                else if ( tmpTextChar == '<' ) {
                    tmpResult.append( "&lt;" );
                }
                else if ( tmpTextChar == '>' ) {
                    tmpResult.append( "&gt;" );
                }
                else if ( tmpTextChar == '\n' ) {
                    tmpResult.append( "<br/>" );
                    tmpLeftSpace = true;
                }
                else {
                    int tmpUnicode = 0xffff & tmpTextChar;
                    if ( tmpUnicode < 160 )
                        tmpResult.append( tmpTextChar );
                    else {
                        tmpResult.append( "&#" );
                        tmpResult.append( Integer.valueOf( tmpUnicode ).toString( ) );
                        tmpResult.append( ';' );
                    }
                }
            }
        }
        return tmpResult.toString( );
    }

    public static String escapeRegex( String string ) {
        //    String tmpRegex = string.replaceAll( "\\\\",
        //                                         Matcher.quoteReplacement( "\\\\" ) );
        //    tmpRegex = tmpRegex.replaceAll( "\\.",
        //                                    Matcher.quoteReplacement( "\\." ) );
        //    tmpRegex = tmpRegex.replaceAll( "\\*",
        //                                    Matcher.quoteReplacement( "\\*" ) );
        //    tmpRegex = tmpRegex.replaceAll( "\\?",
        //                                    Matcher.quoteReplacement( "\\?" ) );
        //    tmpRegex = tmpRegex.replaceAll( "(\\$|\\{|\\})",
        //                                    "\\\\$1" );
        //    return tmpRegex;
        return string.replaceAll( "(\\\\|\\.|\\*|\\?|\\$|\\{|\\}|\\[|\\]|\\|)",
                                  "\\\\$1" );
    }

    public static String camelCaseToUpperLined( String camelCase ) {
        List< String > tmpBlockList = new ArrayList< String >( );
        StringBuilder tmpBlockBuilder = new StringBuilder( );
        char tmpLastchar = '\0';
        for ( char tmpChar : camelCase.toCharArray( ) ) {
            if ( ( ( Character.isUpperCase( tmpChar ) && !Character.isUpperCase( tmpLastchar ) )
                   || ( Character.isDigit( tmpChar ) && !Character.isDigit( tmpLastchar ) )
                   || ( tmpChar == '_' ) )
                 && ( tmpBlockBuilder.length( ) > 0 ) ) {
                tmpBlockList.add( tmpBlockBuilder.toString( ) );
                tmpBlockBuilder = new StringBuilder( );
            }
            if ( tmpChar != '_' ) {
                tmpBlockBuilder.append( Character.toUpperCase( tmpChar ) );
            }
            tmpLastchar = tmpChar;
        }
        if ( tmpBlockBuilder.length( ) > 0 ) {
            tmpBlockList.add( tmpBlockBuilder.toString( ) );
        }
        StringBuilder tmpResult = new StringBuilder( );
        boolean tmpPreviousSingle = false;
        for ( int tmpBlockIndex = 0; tmpBlockIndex < tmpBlockList.size( ); tmpBlockIndex++ ) {
            String tmpBlock = tmpBlockList.get( tmpBlockIndex );
            if ( ( tmpBlockIndex > 0 ) && ( ( tmpBlock.length( ) > 1 ) || !tmpPreviousSingle ) ) {
                tmpResult.append( '_' );
            }
            tmpPreviousSingle = ( tmpBlock.length( ) == 1 );
            tmpResult.append( tmpBlock );
        }
        return tmpResult.toString( );
    }

    public static String flawlessFormat( String format,
                                         Object... args ) {
        String tmpNull = "<null>";
        String tmpMessage;
        try {
            if ( format == null ) {
                tmpMessage = tmpNull;
            }
            else {
                if ( ( args == null ) || ( args.length == 0 ) ) {
                    tmpMessage = format;
                }
                else {
                    tmpMessage = String.format( format,
                                                args );
                }
            }
        }
        catch ( Throwable e ) {
            StringBuilder tmpArgs = new StringBuilder( );
            if ( args != null ) {
                for ( Object tmpArg : args ) {
                    tmpArgs.append( ", " );
                    tmpArgs.append( ( tmpArg == null ) ? tmpNull
                                                       : String.format( "%s: \"%s\"",
                                                                        tmpArg.getClass( ).getSimpleName( ),
                                                                        tmpArg.toString( ).replaceAll( "\"",
                                                                                                       "\\\"" ) ) );
                }
            }
            String tmpExceptionMessage = e.getLocalizedMessage( );
            tmpMessage = String.format( "%s em String.format( \"%s\"%s ): %s",
                                        e.getClass( ).getSimpleName( ),
                                        format,
                                        tmpArgs.toString( ),
                                        ( tmpExceptionMessage != null ) ? tmpExceptionMessage
                                                                        : tmpNull );
        }
        return tmpMessage;
    }

    public static String wrap( String text,
                               int outputWidth ) {
        return wrap( text,
                     outputWidth,
                     0,
                     0,
                     LINE_BREAK,
                     false );
    }

    public static String wrap( String text,
                               int outputWidth,
                               int indentation ) {
        return wrap( text,
                     outputWidth,
                     indentation,
                     indentation,
                     LINE_BREAK,
                     false );
    }

    public static String wrap( String text,
                               int outputWidth,
                               int firstIndentation,
                               int indentation ) {
        return wrap( text,
                     outputWidth,
                     firstIndentation,
                     indentation,
                     LINE_BREAK,
                     false );
    }

    public static String wrap( String text,
                               int outputWidth,
                               int firstIndentation,
                               int indentation,
                               String lineBreak ) {
        return wrap( text,
                     outputWidth,
                     firstIndentation,
                     indentation,
                     lineBreak,
                     false );
    }

    public static String wrap( String text,
                               int outputWidth,
                               int firstIndentation,
                               int indentation,
                               String lineBreak,
                               boolean traceMode ) {
        return wrap( new StringBuilder( text ),
                     outputWidth,
                     firstIndentation,
                     indentation,
                     lineBreak,
                     traceMode ).toString( );
    }

    public static String wrap( String text,
                               int outputWidth,
                               int indentation,
                               String lineBreak ) {
        return wrap( text,
                     outputWidth,
                     indentation,
                     indentation,
                     lineBreak,
                     false );
    }

    public static String wrap( String text,
                               int outputWidth,
                               String lineBreak ) {
        return wrap( text,
                     outputWidth,
                     0,
                     0,
                     lineBreak,
                     false );
    }

    public static StringBuilder wrap( StringBuilder text,
                                      int outputWidth,
                                      int firstIndentation,
                                      int indentation ) {
        return wrap( text,
                     outputWidth,
                     firstIndentation,
                     indentation,
                     LINE_BREAK,
                     false );
    }

    public static String wrapTrace( String text,
                                    int outputWidth ) {
        return wrap( text,
                     outputWidth,
                     0,
                     0,
                     LINE_BREAK,
                     true );
    }

    protected static StringBuilder wrap( StringBuilder text,
                                         int outputWidth,
                                         int firstIndentation,
                                         int indentation,
                                         String lineBreak,
                                         boolean traceMode ) {
        if ( ( firstIndentation < 0 ) || ( indentation < 0 ) || ( outputWidth < 0 ) ) {
            throw new IllegalArgumentException( "Negative dimension" );
        }
        int tmpAllowedCols = ( outputWidth - 1 );
        if ( ( ( tmpAllowedCols - indentation ) < 2 ) || ( ( tmpAllowedCols - firstIndentation ) < 2 ) ) {
            throw new IllegalArgumentException( "Usable columns < 2" );
        }
        int tmpTextLength = text.length( );
        int tmpDefaultNextLeft = ( tmpAllowedCols - indentation );
        int tmpBeginIndex = 0;
        int tmpEndIndex = 0;
        StringBuilder tmpResult = new StringBuilder( (int) ( tmpTextLength * 1.2 ) );
        int tmpLeftIndex = ( tmpAllowedCols - firstIndentation );
        tmpResult.append( spaces( firstIndentation ) );
        String tmpDefaultBreakAndIndent = ( lineBreak + spaces( indentation ) );
        boolean tmpFirstSectOfSrcLine = true;
        boolean tmpFirstWordOfSrcLine = true;
        int tmpTraceLineState = 0;
        int tmpNextLeft = tmpDefaultNextLeft;
        String tmpBreakAndIndent = tmpDefaultBreakAndIndent;
        char tmpLastChar1;
        char tmpLastChar2;
        do {
            word: //
            while ( tmpEndIndex <= tmpTextLength ) {
                if ( tmpEndIndex != tmpTextLength ) {
                    tmpLastChar1 = text.charAt( tmpEndIndex );
                }
                else {
                    tmpLastChar1 = SPACE_CHAR;
                }
                if ( ( tmpTraceLineState > 0 ) && ( tmpEndIndex > tmpBeginIndex ) ) {
                    if ( ( tmpLastChar1 == '.' ) && ( tmpTraceLineState == 1 ) ) {
                        tmpLastChar1 = SPACE_CHAR;
                    }
                    else {
                        tmpLastChar2 = text.charAt( tmpEndIndex - 1 );
                        if ( tmpLastChar2 == ':' ) {
                            tmpLastChar1 = SPACE_CHAR;
                        }
                        else if ( tmpLastChar2 == '(' ) {
                            tmpTraceLineState = 2;
                            tmpLastChar1 = SPACE_CHAR;
                        }
                    }
                }
                if ( !isSpace( tmpLastChar1 ) ) {
                    tmpEndIndex++;
                }
                else {
                    int tmpWordLength = ( tmpEndIndex - tmpBeginIndex );
                    if ( tmpLeftIndex >= tmpWordLength ) {
                        tmpResult.append( text.substring( tmpBeginIndex,
                                                          tmpEndIndex ) );
                        tmpLeftIndex -= tmpWordLength;
                        tmpBeginIndex = tmpEndIndex;
                    }
                    else {
                        tmpWordLength = ( tmpEndIndex - tmpBeginIndex );
                        if ( ( tmpWordLength > tmpNextLeft ) || tmpFirstWordOfSrcLine ) {
                            int tmpInitialIndex = tmpBeginIndex;
                            while ( tmpWordLength > tmpLeftIndex ) {
                                if ( ( tmpLeftIndex > 2 )
                                     || ( ( tmpLeftIndex == 2 )
                                          && ( tmpFirstWordOfSrcLine
                                               || !( ( tmpBeginIndex == tmpInitialIndex )
                                                     && ( tmpNextLeft > 2 ) ) ) ) ) {
                                    tmpResult.append( text.substring( tmpBeginIndex,
                                                                      ( tmpBeginIndex + tmpLeftIndex - 1 ) ) );
                                    tmpResult.append( "-" );
                                    tmpResult.append( tmpBreakAndIndent );
                                    tmpWordLength -= ( tmpLeftIndex - 1 );
                                    tmpBeginIndex += ( tmpLeftIndex - 1 );
                                    tmpLeftIndex = tmpNextLeft;
                                }
                                else {
                                    int tmpIndex = ( tmpResult.length( ) - 1 );
                                    if ( ( tmpIndex >= 0 ) && ( tmpResult.charAt( tmpIndex ) == SPACE_CHAR ) ) {
                                        tmpResult.delete( tmpIndex,
                                                          ( tmpIndex + 1 ) );
                                    }
                                    tmpResult.append( tmpBreakAndIndent );
                                    tmpLeftIndex = tmpNextLeft;
                                }
                            }
                            tmpResult.append( text.substring( tmpBeginIndex,
                                                              ( tmpBeginIndex + tmpWordLength ) ) );
                            tmpBeginIndex += tmpWordLength;
                            tmpLeftIndex -= tmpWordLength;
                        }
                        else {
                            int tmpIndex = ( tmpResult.length( ) - 1 );
                            if ( ( tmpIndex >= 0 ) && ( tmpResult.charAt( tmpIndex ) == SPACE_CHAR ) ) {
                                tmpResult.delete( tmpIndex,
                                                  ( tmpIndex + 1 ) );
                            }
                            tmpResult.append( tmpBreakAndIndent );
                            tmpResult.append( text.substring( tmpBeginIndex,
                                                              tmpEndIndex ) );
                            tmpLeftIndex = ( tmpNextLeft - tmpWordLength );
                            tmpBeginIndex = tmpEndIndex;
                        }
                    }
                    tmpFirstSectOfSrcLine = false;
                    tmpFirstWordOfSrcLine = false;
                    break word;
                }
            }
            int tmpExtra = 0;
            space: //
            while ( tmpEndIndex < tmpTextLength ) {
                tmpLastChar1 = text.charAt( tmpEndIndex );
                if ( tmpLastChar1 == SPACE_CHAR ) {
                    tmpEndIndex++;
                }
                else if ( tmpLastChar1 == TAB_CHAR ) {
                    tmpEndIndex++;
                    tmpExtra += 7;
                }
                else if ( ( tmpLastChar1 == CR_CHAR ) || ( tmpLastChar1 == LF_CHAR ) ) {
                    tmpNextLeft = tmpDefaultNextLeft;
                    tmpBreakAndIndent = tmpDefaultBreakAndIndent;
                    tmpResult.append( tmpBreakAndIndent );
                    tmpEndIndex++;
                    if ( tmpEndIndex < tmpTextLength ) {
                        tmpLastChar2 = text.charAt( tmpEndIndex );
                        if ( ( ( tmpLastChar2 == CR_CHAR ) || ( tmpLastChar2 == LF_CHAR ) )
                             && ( tmpLastChar1 != tmpLastChar2 ) ) {
                            tmpEndIndex++;
                        }
                    }
                    tmpLeftIndex = tmpNextLeft;
                    tmpBeginIndex = tmpEndIndex;
                    tmpFirstSectOfSrcLine = true;
                    tmpFirstWordOfSrcLine = true;
                    tmpTraceLineState = 0;
                }
                else {
                    int tmpWordLength = ( tmpEndIndex - tmpBeginIndex + tmpExtra );
                    if ( tmpFirstSectOfSrcLine ) {
                        int tmpRemainingChars = ( tmpAllowedCols - indentation - tmpWordLength );
                        if ( traceMode
                             && ( tmpTextLength > ( tmpEndIndex + 2 ) )
                             && ( text.charAt( tmpEndIndex ) == 'a' )
                             && ( text.charAt( tmpEndIndex + 1 ) == 't' )
                             && ( text.charAt( tmpEndIndex + 2 ) == ' ' ) ) {
                            if ( tmpRemainingChars > ( 5 + 3 ) ) {
                                tmpRemainingChars -= 3;
                            }
                            tmpTraceLineState = 1;
                        }
                        if ( tmpRemainingChars > 5 ) {
                            tmpRemainingChars = ( tmpAllowedCols - tmpRemainingChars );
                            tmpNextLeft = ( tmpAllowedCols - tmpRemainingChars );
                            tmpBreakAndIndent = ( lineBreak + spaces( tmpRemainingChars ) );
                        }
                    }
                    if ( tmpWordLength <= tmpLeftIndex ) {
                        tmpResult.append( text.substring( tmpBeginIndex,
                                                          tmpEndIndex ) );
                        tmpLeftIndex -= tmpWordLength;
                        tmpBeginIndex = tmpEndIndex;
                    }
                    else {
                        tmpResult.append( tmpBreakAndIndent );
                        tmpLeftIndex = tmpNextLeft;
                        tmpBeginIndex = tmpEndIndex;
                    }
                    tmpFirstSectOfSrcLine = false;
                    break space;
                }
            }
        }
        while ( tmpEndIndex < tmpTextLength );
        return tmpResult;
    }

    public static String arrayToLines( String... stringArray ) {
        return arrayToLines( 0,
                             stringArray );
    }

    public static String arrayToLines( int stringIndex,
                                       String... stringArray ) {
        return listToLines( stringIndex,
                            Arrays.asList( stringArray ) );
    }

    public static String listToLines( int stringIndex,
                                      List< String > stringList ) {
        StringBuilder tmpResult = new StringBuilder( listToSeparated( "\n",
                                                                      stringIndex,
                                                                      stringList ) );
        if ( tmpResult.length( ) > 0 ) {
            tmpResult.append( "\n" );
        }
        return tmpResult.toString( );
    }

    public static String listToLines( List< String > stringList ) {
        return listToLines( 0,
                            stringList );
    }

    private static enum LineBreakStyle {
        NoBreak,
        EmptyBreak, //  CR+CR or LF+LF
        SingleBreak, // LF+char or CR+char
        DoubleBreak; // CR+LF or LF+CR
    }

    public static List< String > linesToList( String textString ) {
        return linesToList( textString,
                            0,
                            0 );
    }

    public static List< String > linesToList( String textString,
                                              int initialLineIndex,
                                              int maxLineCount ) {
        List< String > tmpResult = new ArrayList< String >( );
        int tmpLineCount = 0;
        char tmpLastChar = 0;
        ByteArrayOutputStream tmpLineBuffer = new ByteArrayOutputStream( );
        LineBreakStyle tmpBreakType = LineBreakStyle.NoBreak;
        for ( char tmpChar : textString.toCharArray( ) ) {
            if ( ( tmpChar == CR_CHAR ) || ( tmpChar == LF_CHAR ) ) {
                if ( ( tmpLastChar == CR_CHAR ) || ( tmpLastChar == LF_CHAR ) ) {
                    if ( tmpChar == tmpLastChar ) {
                        // Break at CR+CR or LF+LF (empty break)
                        tmpBreakType = LineBreakStyle.EmptyBreak;
                    }
                    else {
                        // Break at CR+LF or LF+CR (double break)
                        tmpBreakType = LineBreakStyle.DoubleBreak;
                    }
                }
            }
            else {
                if ( ( tmpLastChar == CR_CHAR ) || ( tmpLastChar == LF_CHAR ) ) {
                    // Break at LF+char or CR+char (single break)
                    tmpBreakType = LineBreakStyle.SingleBreak;
                }
                else {
                    // Append character and continue
                    tmpLineBuffer.write( tmpChar );
                }
            }
            if ( tmpBreakType != LineBreakStyle.NoBreak ) {
                //
                tmpLineCount += 1;
                if ( tmpLineCount >= initialLineIndex ) {
                    tmpResult.add( new String( tmpLineBuffer.toByteArray( ) ) );
                    if ( ( maxLineCount > 0 ) && ( tmpResult.size( ) >= maxLineCount ) ) {
                        tmpLineBuffer.reset( );
                        break;
                    }
                }
                tmpLineBuffer.reset( );
                //
                if ( tmpBreakType == LineBreakStyle.SingleBreak ) {
                    // Append character (single break)
                    tmpLineBuffer.write( tmpChar );
                }
                else if ( tmpBreakType == LineBreakStyle.DoubleBreak ) {
                    tmpChar = 0;
                }
                //
                tmpBreakType = LineBreakStyle.NoBreak;
                //
            }
            tmpLastChar = tmpChar;
        }
        if ( tmpLineBuffer.size( ) > 0 ) {
            tmpLineCount += 1;
            if ( ( tmpLineCount >= initialLineIndex )
                 && ( ( maxLineCount <= 0 ) || ( tmpResult.size( ) < maxLineCount ) ) ) {
                tmpResult.add( new String( tmpLineBuffer.toByteArray( ) ) );
            }
        }
        return tmpResult;
    }

    public static String arrayToSeparated( String separator,
                                           String... stringArray ) {
        return listToSeparated( separator,
                                0,
                                Arrays.asList( stringArray ) );
    }

    public static String listToSeparated( String prefix,
                                          String sufix,
                                          String separator,
                                          String lastSeparator,
                                          int stringIndex,
                                          int stringCount,
                                          List< String > stringList ) {
        StringBuilder tmpResult = new StringBuilder( );
        int tmpListSize = stringList.size( );
        if ( ( tmpListSize > 0 ) && ( stringCount > 0 ) ) {
            int tmpBeginIndex = ( stringIndex < 0 ) ? 0
                                                    : stringIndex;
            int tmpEndIndex = ( stringIndex + stringCount );
            if ( tmpEndIndex > stringList.size( ) ) {
                tmpEndIndex = stringList.size( );
            }
            for ( int tmpIndex = tmpBeginIndex; tmpIndex < tmpEndIndex; tmpIndex++ ) {
                if ( tmpIndex > tmpBeginIndex ) {
                    if ( tmpIndex < ( tmpEndIndex - 1 ) ) {
                        tmpResult.append( separator );
                    }
                    else {
                        tmpResult.append( lastSeparator );
                    }
                }
                if ( prefix != null ) {
                    tmpResult.append( prefix );
                }
                tmpResult.append( stringList.get( tmpIndex ) );
                if ( sufix != null ) {
                    tmpResult.append( sufix );
                }
            }
        }
        return tmpResult.toString( );
    }

    public static String listToSeparated( String separator,
                                          int stringIndex,
                                          List< String > stringList ) {
        return listToSeparated( null,
                                null,
                                separator,
                                separator,
                                stringIndex,
                                ( Integer.MAX_VALUE - stringIndex ),
                                stringList );
    }

    public static String listToSeparated( String prefix,
                                          String sufix,
                                          String separator,
                                          List< String > stringList ) {
        return listToSeparated( prefix,
                                sufix,
                                separator,
                                separator,
                                0,
                                Integer.MAX_VALUE,
                                stringList );
    }

    public static String listToSeparated( String separator,
                                          List< String > stringList ) {
        return listToSeparated( null,
                                null,
                                separator,
                                separator,
                                0,
                                Integer.MAX_VALUE,
                                stringList );
    }

    public static String listToSeparated( String separator,
                                          String lastSeparator,
                                          List< String > stringList ) {
        return listToSeparated( null,
                                null,
                                separator,
                                lastSeparator,
                                0,
                                Integer.MAX_VALUE,
                                stringList );
    }

    public static String listToQuotedSeparated( String quotes,
                                                String separator,
                                                List< String > strings ) {
        return listToSeparated( quotes,
                                quotes,
                                separator,
                                separator,
                                0,
                                Integer.MAX_VALUE,
                                strings );
    }

    public static String listToQuotedSeparated( String quotes,
                                                String separator,
                                                String... strings ) {
        return listToQuotedSeparated( quotes,
                                      separator,
                                      Arrays.asList( strings ) );
    }

    public static Map< String, String > parseStringMap( List< String > mappings ) {
        Map< String, String > tmpResult = new HashMap< String, String >( );
        for ( String tmpMapping : mappings ) {
            if ( tmpMapping.length( ) > 0 ) {
                String[ ] tmpKeyAndValue = tmpMapping.split( "=" );
                tmpResult.put( tmpKeyAndValue[ 0 ],
                               ( tmpKeyAndValue.length > 1 ) ? tmpKeyAndValue[ 1 ]
                                                             : null );
            }
        }
        return tmpResult;
    }

    public static Map< String, String > removeMapDuplicates( boolean ignoreValueDifferences,
                                                             Map< String, String > targetMap,
                                                             Map< String, String > sourceMap ) {
        Map< String, String > tmpDifferences = new HashMap< String, String >( );
        Iterator< Map.Entry< String, String > > tmpTargetIterator = targetMap.entrySet( ).iterator( );
        while ( tmpTargetIterator.hasNext( ) ) {
            Map.Entry< String, String > tmpTargetEntry = tmpTargetIterator.next( );
            String tmpSourceValue = sourceMap.get( tmpTargetEntry.getKey( ) );
            if ( ( tmpSourceValue != null ) //
                 && ( ignoreValueDifferences || ( tmpTargetEntry.getValue( ).compareTo( tmpSourceValue ) != 0 ) ) ) {
                tmpDifferences.put( tmpTargetEntry.getKey( ),
                                    tmpTargetEntry.getValue( ) );
                tmpTargetIterator.remove( );
            }
        }
        return tmpDifferences;
    }

    public static String dumpStringList( List< String > list ) {
        return StringHelper.dumpStringList( list,
                                            CR );
    }

    public static String dumpStringList( List< String > list,
                                         String separator ) {
        StringBuilder tmpResult = new StringBuilder( );
        for ( String tmpDetail : list ) {
            if ( tmpResult.length( ) > 0 ) {
                tmpResult.append( separator );
            }
            tmpResult.append( tmpDetail );
        }
        return tmpResult.toString( );
    }

    public static List< String > dumpStringMap( Map< String, String > map ) {
        return StringHelper.dumpStringMap( map,
                                           ": " );
    }

    public static List< String > dumpStringMap( Map< String, String > map,
                                                String separator ) {
        List< String > tmpResult = new ArrayList< String >( );
        StringBuilder tmpStringBuilder = new StringBuilder( 100 );
        for ( Map.Entry< String, String > tmpFieldEntry : map.entrySet( ) ) {
            tmpStringBuilder.delete( 0,
                                     tmpStringBuilder.length( ) );
            tmpStringBuilder.append( tmpFieldEntry.getKey( ) );
            tmpStringBuilder.append( separator );
            tmpStringBuilder.append( tmpFieldEntry.getValue( ) );
            tmpResult.add( tmpStringBuilder.toString( ) );
        }
        return tmpResult;
    }

    public static String zenitPOLAR( String input ) {
        Map< Character, Character > tmpZenitMap = new HashMap< Character, Character >( );
        String tmpZenitCode = "ZpEoNlIaTr1836547290";
        Character tmpZenitChar = null;
        int tmpZenitIndex = 0;
        for ( Character tmpPolarChar : tmpZenitCode.toCharArray( ) ) {
            if ( ( tmpZenitIndex % 2 ) == 0 ) {
                tmpZenitChar = tmpPolarChar;
            }
            else {
                tmpZenitMap.put( Character.toUpperCase( tmpZenitChar ),
                                 Character.toLowerCase( tmpPolarChar ) );
                tmpZenitMap.put( Character.toUpperCase( tmpPolarChar ),
                                 Character.toLowerCase( tmpZenitChar ) );
            }
            ++tmpZenitIndex;
        }
        StringBuilder tmpResult = new StringBuilder( );
        for ( Character tmpInput : input.toCharArray( ) ) {
            Character tmpOutput = tmpZenitMap.get( Character.toUpperCase( tmpInput ) );
            if ( tmpOutput == null ) {
                tmpOutput = tmpInput;
            }
            else {
                tmpOutput = Character.isLowerCase( tmpInput ) ? tmpOutput
                                                              : Character.toUpperCase( tmpOutput );
            }
            tmpResult.append( tmpOutput );
        }
        return tmpResult.toString( );
    }
}
