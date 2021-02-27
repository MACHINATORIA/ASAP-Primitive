package asap.primitive.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColumnsStringBuffer {

    protected int[ ]              columnWidths;

    protected int[ ]              maxColumnWidths;

    protected List< String[ ] >   lineColumns;

    protected String[ ]           columnHeaders         = null;

    protected int                 autoHeaderCounter     = 0;

    protected int                 autoHeaderInterval    = ( -1 );

    protected static char         DEFAULT_SEPARATOR     = '-';

    protected static final String DEFAULT_NULL_STRING   = "<null>";

    protected static Pattern      HEADER_TAG_PATTERN    = Pattern.compile( "<<<\\((.*)\\)>>>",
                                                                           Pattern.DOTALL );

    protected static Pattern      SEPARATOR_TAG_PATTERN = Pattern.compile( ">>>\\((.)\\)<<<" );

    //    public static class ColumnSpec {
    //    
    //        public final String name;
    //    
    //        public final int    width;
    //    
    //        public ColumnSpec( String name,
    //                           int width ) {
    //            this.name = name;
    //            this.width = width;
    //        }
    //    }
    //    
    //    public ColumnsStringBuffer( ColumnSpec... columnSpecs ) {
    //        this( columnSpecs.length );
    //        List< String > tmpColumnHeaders = new ArrayList< String >( );
    //        for ( ColumnSpec tmpColumnSpec : columnSpecs ) {
    //            this.columnWidths[ tmpColumnHeaders.size( ) ] = tmpColumnSpec.width;
    //            tmpColumnHeaders.add( tmpColumnSpec.name );
    //        }
    //        this.addLine( tmpColumnHeaders );
    //        this.addSeparator( '-' );
    //    }
    //    
    public ColumnsStringBuffer( int... columnWidths ) {
        this.columnWidths = Arrays.copyOf( columnWidths,
                                           columnWidths.length );
        this.maxColumnWidths = new int[ this.columnWidths.length ];
        this.lineColumns = new ArrayList< String[ ] >( );
        //
    }

    public ColumnsStringBuffer( int columnCount ) {
        this( new int[ columnCount ] );
    }

    public ColumnsStringBuffer( List< String > columnHeaders ) {
        this( columnHeaders.size( ) );
        List< String > tmpColumnHeaders = new ArrayList< String >( );
        for ( String tmpColumnHeader : columnHeaders ) {
            if ( tmpColumnHeader.startsWith( "-" ) ) {
                this.columnWidths[ tmpColumnHeaders.size( ) ] = ( -1 );
                tmpColumnHeader = tmpColumnHeader.replaceAll( "^-",
                                                              "" );
            }
            tmpColumnHeaders.add( tmpColumnHeader.replaceAll( "^\\\\-",
                                                              "-" ) );
        }
        this.columnHeaders = tmpColumnHeaders.toArray( new String[ 0 ] );
        this.addHeader( this.columnHeaders ).addSeparator( DEFAULT_SEPARATOR );
    }

    public ColumnsStringBuffer( String... columnHeaders ) {
        this( Arrays.asList( columnHeaders ) );
    }

    public static String horizontalSheet( int columnCount,
                                          int columnWidth,
                                          List< String > cells ) {
        return horizontalSheet( columnCount,
                                columnWidth,
                                1,
                                1,
                                false,
                                cells );
    }

    public static String horizontalSheet( int columnCount,
                                          int columnWidth,
                                          boolean newLineAtTheEnd,
                                          List< String > cells ) {
        return horizontalSheet( columnCount,
                                columnWidth,
                                1,
                                1,
                                newLineAtTheEnd,
                                cells );
    }

    public static String horizontalSheet( int columnCount,
                                          int columnWidth,
                                          int columnWidthModulo,
                                          int columnGapWidth,
                                          List< String > cells ) {
        return horizontalSheet( columnCount,
                                columnWidth,
                                columnWidthModulo,
                                columnGapWidth,
                                false,
                                cells );
    }

    public static String horizontalSheet( int columnCount,
                                          int columnWidth,
                                          int columnWidthModulo,
                                          int columnGapWidth,
                                          boolean newLineAtTheEnd,
                                          List< String > cells ) {
        return simpleSheet( columnCount,
                            columnWidth,
                            false,
                            columnWidthModulo,
                            columnGapWidth,
                            newLineAtTheEnd,
                            cells );
    }

    public static String verticalSheet( int columnCount,
                                        int columnWidth,
                                        List< String > cells ) {
        return verticalSheet( columnCount,
                              columnWidth,
                              1,
                              1,
                              false,
                              cells );
    }

    public static String verticalSheet( int columnCount,
                                        int columnWidth,
                                        boolean newLineAtTheEnd,
                                        List< String > cells ) {
        return verticalSheet( columnCount,
                              columnWidth,
                              1,
                              1,
                              newLineAtTheEnd,
                              cells );
    }

    public static String verticalSheet( int columnCount,
                                        int columnWidth,
                                        int columnWidthModulo,
                                        int columnGapWidth,
                                        List< String > cells ) {
        return verticalSheet( columnCount,
                              columnWidth,
                              columnWidthModulo,
                              columnGapWidth,
                              false,
                              cells );
    }

    public static String verticalSheet( int columnCount,
                                        int columnWidth,
                                        int columnWidthModulo,
                                        int columnGapWidth,
                                        boolean newLineAtTheEnd,
                                        List< String > cells ) {
        return simpleSheet( columnCount,
                            columnWidth,
                            true,
                            columnWidthModulo,
                            columnGapWidth,
                            newLineAtTheEnd,
                            cells );
    }

    public static String simpleSheet( int columnCount,
                                      int columnWidth,
                                      boolean vertical,
                                      int columnWidthModulo,
                                      int columnGapWidth,
                                      boolean newLineAtTheEnd,
                                      List< String > cells ) {
        int[ ] tmpColumnWidths = new int[ columnCount ];
        Arrays.fill( tmpColumnWidths,
                     columnWidth );
        ColumnsStringBuffer tmpResult = new ColumnsStringBuffer( tmpColumnWidths );
        String[ ] tmpLineStrings = new String[ columnCount ];
        int tmpLineCount = ( ( cells.size( ) + columnCount - 1 ) / columnCount );
        int tmpCellCount = ( tmpLineCount * columnCount );
        int tmpBlankCount = ( columnCount - ( tmpCellCount - cells.size( ) ) );
        for ( int tmpLineIndex = 0; tmpLineIndex < tmpLineCount; tmpLineIndex++ ) {
            for ( int tmpColumnIndex = 0; tmpColumnIndex < columnCount; tmpColumnIndex++ ) {
                int tmpCellIndex = ( ( tmpLineIndex * columnCount ) + tmpColumnIndex );
                if ( tmpCellIndex < cells.size( ) ) {
                    if ( vertical ) {
                        int tmpBlankShift = ( tmpColumnIndex < tmpBlankCount ) ? 0
                                                                               : ( tmpColumnIndex - tmpBlankCount );
                        tmpCellIndex = ( ( tmpColumnIndex * tmpLineCount ) + tmpLineIndex - tmpBlankShift );
                    }
                    tmpLineStrings[ tmpColumnIndex ] = cells.get( tmpCellIndex );
                }
                else {
                    tmpLineStrings[ tmpColumnIndex ] = "";
                }
            }
            tmpResult.addLine( tmpLineStrings );
        }
        return tmpResult.getResult( columnWidthModulo,
                                    columnGapWidth,
                                    StringHelper.SPACE_CHAR,
                                    newLineAtTheEnd );
    }

    public void setAutoheaderInterval( int autoHeaderInterval ) {
        this.autoHeaderInterval = autoHeaderInterval;
    }

    public ColumnsStringBuffer addHeader( String... headerColumns ) {
        String[ ] tmpHeaderColumns = Arrays.copyOf( headerColumns,
                                                    headerColumns.length );
        if ( tmpHeaderColumns.length > 0 ) {
            String tmpFirstHeader = tmpHeaderColumns[ 0 ];
            tmpHeaderColumns[ 0 ] = String.format( "<<<(%s)>>>",
                                                   tmpFirstHeader );
        }
        this.addLine_Common( tmpHeaderColumns );
        this.autoHeaderCounter = 0;
        return this;
    }

    public ColumnsStringBuffer addHeader( List< String > headerColumns ) {
        return addHeader( headerColumns.toArray( new String[ 0 ] ) );
    }

    public ColumnsStringBuffer addLine( List< String > lineColumns ) {
        return this.checkAutoHeader( ).addLine_Common( lineColumns.toArray( new String[ 0 ] ) );
    }

    public ColumnsStringBuffer addLine( String... lineColumns ) {
        return this.checkAutoHeader( ).addLine_Common( Arrays.copyOf( lineColumns,
                                                                      lineColumns.length ) );
    }

    private ColumnsStringBuffer checkAutoHeader( ) {
        if ( ( this.columnHeaders != null )
             && ( this.autoHeaderInterval > 0 )
             && ( this.autoHeaderCounter >= this.autoHeaderInterval ) ) {
            this.addSeparator( DEFAULT_SEPARATOR ).addHeader( this.columnHeaders ).addSeparator( DEFAULT_SEPARATOR );
        }
        return this;
    }

    private ColumnsStringBuffer addLine_Common( String... lineColumns ) {
        if ( lineColumns.length > this.columnWidths.length ) {
            throw new ArrayIndexOutOfBoundsException( String.format( "Invalid line of %d column(s) for %d column(s) formatter",
                                                                     lineColumns.length,
                                                                     this.columnWidths.length ) );
        }
        this.lineColumns.add( lineColumns );
        for ( int tmpColumnIndex = 0; tmpColumnIndex < lineColumns.length; tmpColumnIndex++ ) {
            String tmpColumnText = ( ( lineColumns[ tmpColumnIndex ] == null ) ? DEFAULT_NULL_STRING
                                                                               : lineColumns[ tmpColumnIndex ] );
            if ( tmpColumnIndex == 0 ) {
                Matcher tmpHeaderMatcher = HEADER_TAG_PATTERN.matcher( tmpColumnText );
                if ( tmpHeaderMatcher.matches( ) ) {
                    tmpColumnText = tmpHeaderMatcher.group( 1 );
                }
            }
            List< String > tmpLineColumnsWrap = StringHelper.linesToList( tmpColumnText );
            for ( String tmpLineColumn : tmpLineColumnsWrap ) {
                if ( tmpLineColumn.length( ) > this.maxColumnWidths[ tmpColumnIndex ] ) {
                    this.maxColumnWidths[ tmpColumnIndex ] = tmpLineColumn.length( );
                }
            }
        }
        this.autoHeaderCounter += 1;
        return this;
    }

    public ColumnsStringBuffer addSeparator( char separatorChar ) {
        this.lineColumns.add( new String[ ] { String.format( ">>>(%c)<<<",
                                                             separatorChar ) } );
        return this;
    }

    public String getResult( ) {
        return getResult( 1,
                          1,
                          StringHelper.SPACE_CHAR,
                          true );
    }

    public String getResult( int columnGapWidth ) {
        return getResult( 1,
                          columnGapWidth,
                          StringHelper.SPACE_CHAR,
                          true );
    }

    public String getResult( char columnGapChar ) {
        return getResult( 1,
                          1,
                          columnGapChar,
                          true );
    }

    public String getResult( boolean newLineTermination ) {
        return getResult( 1,
                          1,
                          StringHelper.SPACE_CHAR,
                          newLineTermination );
    }

    public String getResult( int columnWidthModulo,
                             int columnGapWidth ) {
        return this.getResult( columnWidthModulo,
                               columnGapWidth,
                               StringHelper.SPACE_CHAR,
                               false );
    }

    public String getResult( int columnWidthModulo,
                             int columnGapWidth,
                             boolean newLineAtTheEnd ) {
        return this.getResult( columnWidthModulo,
                               columnGapWidth,
                               StringHelper.SPACE_CHAR,
                               newLineAtTheEnd );
    }

    public String getResult( int columnWidthModulo,
                             int columnGapWidth,
                             char columnGapChar,
                             boolean newLineAtTheEnd ) {
        StringBuilder tmpResult = new StringBuilder( );
        //
        int[ ] tmpColumnWidths = new int[ this.columnWidths.length ];
        for ( int tmpColumnIndex = 0; tmpColumnIndex < tmpColumnWidths.length; tmpColumnIndex++ ) {
            int tmpColumnWitdth = 0;
            if ( this.columnWidths[ tmpColumnIndex ] > 0 ) {
                tmpColumnWitdth = this.columnWidths[ tmpColumnIndex ];
            }
            else {
                tmpColumnWitdth = this.maxColumnWidths[ tmpColumnIndex ];
            }
            if ( tmpColumnWitdth < 2 ) {
                tmpColumnWitdth = 2;
            }
            tmpColumnWidths[ tmpColumnIndex ] = ( ( ( tmpColumnWitdth - 1 + columnWidthModulo ) / columnWidthModulo )
                                                  * columnWidthModulo );
        }
        //
        boolean tmpFirstLine = true;
        String tmpGapString = StringHelper.repeatedChar( columnGapWidth,
                                                         columnGapChar );
        for ( String[ ] tmpLineColumns : this.lineColumns ) {
            String tmpHeaderColumn = null;
            if ( ( tmpLineColumns.length > 0 ) && ( tmpLineColumns[ 0 ] != null ) ) {
                Matcher tmpSeparatorMatcher = SEPARATOR_TAG_PATTERN.matcher( tmpLineColumns[ 0 ] );
                if ( tmpSeparatorMatcher.matches( ) ) {
                    // Append separator columns
                    if ( tmpFirstLine ) {
                        tmpFirstLine = false;
                    }
                    else {
                        tmpResult.append( "\n" );
                    }
                    char tmpSeparatorChar = tmpSeparatorMatcher.group( 1 ).charAt( 0 );
                    for ( int tmpColumnIndex = 0; tmpColumnIndex < tmpColumnWidths.length; tmpColumnIndex++ ) {
                        tmpResult.append( StringHelper.repeatedChar( tmpColumnWidths[ tmpColumnIndex ],
                                                                     tmpSeparatorChar ) );
                        if ( tmpColumnIndex < ( tmpColumnWidths.length - 1 ) ) {
                            tmpResult.append( tmpGapString );
                        }
                    }
                    continue;
                }
                Matcher tmpHeaderMatcher = HEADER_TAG_PATTERN.matcher( tmpLineColumns[ 0 ] );
                if ( tmpHeaderMatcher.matches( ) ) {
                    tmpHeaderColumn = tmpHeaderMatcher.group( 1 );
                }
            }
            // Add content columns
            int tmpMaxColumnWraps = 0;
            String[ ][ ] tmpLineColumnsWraps = new String[ this.columnWidths.length ][ ];
            for ( int tmpColumnIndex = 0; tmpColumnIndex < tmpLineColumns.length; tmpColumnIndex++ ) {
                String tmpColumnText;
                if ( ( tmpHeaderColumn != null ) && ( tmpColumnIndex == 0 ) ) {
                    tmpColumnText = tmpHeaderColumn;
                }
                else {
                    tmpColumnText = ( tmpLineColumns[ tmpColumnIndex ] == null ) ? DEFAULT_NULL_STRING
                                                                                 : tmpLineColumns[ tmpColumnIndex ];
                }
                String tmpWrap = StringHelper.wrap( tmpColumnText,
                                                    ( tmpColumnWidths[ tmpColumnIndex ] + 1 ) );
                tmpLineColumnsWraps[ tmpColumnIndex ] = tmpWrap.split( "[\r\n]+" );
                if ( tmpLineColumnsWraps[ tmpColumnIndex ].length > tmpMaxColumnWraps ) {
                    tmpMaxColumnWraps = tmpLineColumnsWraps[ tmpColumnIndex ].length;
                }
            }
            for ( int tmpWrapIndex = 0; tmpWrapIndex < tmpMaxColumnWraps; tmpWrapIndex++ ) {
                if ( tmpFirstLine ) {
                    tmpFirstLine = false;
                }
                else {
                    tmpResult.append( "\n" );
                }
                for ( int tmpColumnIndex = 0; tmpColumnIndex < tmpLineColumns.length; tmpColumnIndex++ ) {
                    boolean tmpRightMostColumn = ( tmpColumnIndex >= ( tmpLineColumns.length - 1 ) );
                    boolean tmpRightAlignedColumn = ( ( tmpHeaderColumn == null )
                                                      && ( this.columnWidths[ tmpColumnIndex ] < 0 ) );
                    String tmpColumnFormat = ( tmpRightMostColumn && !tmpRightAlignedColumn ) ? "%s"
                                                                                              : String.format( "%%%s%ds%%s",
                                                                                                               tmpRightAlignedColumn ? ""
                                                                                                                                     : "-",
                                                                                                               tmpColumnWidths[ tmpColumnIndex ] );
                    tmpResult.append( String.format( tmpColumnFormat,
                                                     ( tmpLineColumnsWraps[ tmpColumnIndex ].length > tmpWrapIndex ) ? tmpLineColumnsWraps[ tmpColumnIndex ][ tmpWrapIndex ]
                                                                                                                     : "",
                                                     tmpGapString ) );
                }
            }
        }
        if ( newLineAtTheEnd ) {
            tmpResult.append( "\n" );
        }
        return tmpResult.toString( );
    }

    public ColumnsStringBuffer reset( ) {
        clear( );
        for ( int tmpColumnIndex = 0; tmpColumnIndex < this.maxColumnWidths.length; tmpColumnIndex++ ) {
            this.maxColumnWidths[ tmpColumnIndex ] = 0;
        }
        return this;
    }

    public ColumnsStringBuffer clear( ) {
        this.lineColumns.clear( );
        this.maxColumnWidths = new int[ this.columnWidths.length ];
        return this;
    }

    @Override
    public String toString( ) {
        return getResult( );
    }
}
