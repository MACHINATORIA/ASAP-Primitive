package asap.primitive.string;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import asap.primitive.pattern.Lambdas;
import asap.primitive.pattern.TreePattern.AbstractTreeItem;
import asap.primitive.pattern.TreePattern.TreeItem;

public class HtmlHelper {

    public static class HtmlChart {

        public static class Header extends AbstractTreeItem< Header > {

            public Header( String text ) {
                super( text );
            }

            public Header( String text,
                           Header... childs ) {
                super( text,
                       childs );
                this.colspan = 0;
            }

            public Header( String text,
                           List< String > childNames ) {
                super( text,
                       childNames );
                this.colspan = 0;
            }

            public Header( String text,
                           String... childNames ) {
                super( text,
                       childNames );
            }

            public Header( TreeItem treeItem ) {
                super( treeItem );
            }

            public Header( List< ? extends TreeItem > treeItems ) {
                super( treeItems );
            }

            @Override
            protected Header construct( String text ) {
                return new Header( text );
            }

            protected int colspan;
        }

        protected int           columnCount;

        protected int           headerDepth;

        protected boolean       distinctBackground;

        protected StringBuilder htmlText;

        public HtmlChart( ) {
            this.htmlText = new StringBuilder( );
        }

        public void setHeaders( List< Header > headers ) {
            this.htmlText.delete( 0,
                                  this.htmlText.length( ) );
            this.columnCount = 0;
            this.headerDepth = 1;
            this.distinctBackground = true;
            for ( Header tmpHeader : headers ) {
                this.countColumns( tmpHeader,
                                   1 );
            }
            for ( int tmpDepth = 0; tmpDepth < this.headerDepth; tmpDepth++ ) {
                List< String > tmpHeaderLine = new ArrayList< String >( );
                this.buildHeader( tmpHeaderLine,
                                  headers,
                                  tmpDepth );
                this.htmlText.append( this.buildLine( this.distinctBackground,
                                                      tmpHeaderLine ) );
            }
        }

        public void setHeaders( Header... headers ) {
            this.setHeaders( Arrays.asList( headers ) );
        }

        public void addDataLine( List< String > lineCells ) {
            List< String > tmpDataLine = new ArrayList< String >( );
            for ( String tmpCell : lineCells ) {
                this.addCell( tmpDataLine,
                              tmpCell,
                              false,
                              0 );
            }
            this.distinctBackground ^= true;
            this.htmlText.append( this.buildLine( this.distinctBackground,
                                                  tmpDataLine ) );
        }

        public void addDataLine( String... lineCells ) {
            this.addDataLine( Arrays.asList( lineCells ) );
        }

        public String getResult( ) {
            return new StringBuilder( ).append( "<table>" ).append( this.htmlText ).append( "</table>" ).toString( );
        }

        protected void buildHeader( List< String > headerLine,
                                    List< Header > headers,
                                    int depth ) {
            if ( depth > 0 ) {
                for ( Header tmpHeader : headers ) {
                    if ( tmpHeader.getChilds( ).size( ) > 0 ) {
                        this.buildHeader( headerLine,
                                          tmpHeader.getChilds( ),
                                          ( depth - 1 ) );
                    }
                    else {
                        this.addCell( headerLine,
                                      "",
                                      true,
                                      0 );
                    }
                }
            }
            else {
                for ( Header tmpHeader : headers ) {
                    this.addCell( headerLine,
                                  tmpHeader.getText( ),
                                  true,
                                  tmpHeader.colspan );
                }
            }
        }

        protected String buildLine( boolean distinctBackground,
                                    List< String > cells ) {
            boolean tmpFirstCell = true;
            List< String > tmpHtmlCells = new ArrayList< String >( );
            HtmlColor tmpBackgroundColor = distinctBackground ? HtmlColor.Gray
                                                              : null;
            for ( String tmpCell : cells ) {
                if ( tmpFirstCell ) {
                    tmpFirstCell = false;
                }
                else {
                    tmpHtmlCells.add( HtmlHelper.formatCell( false,
                                                             1,
                                                             true,
                                                             HtmlColor.White,
                                                             null,
                                                             null,
                                                             null,
                                                             HtmlHelper.formatText( null,
                                                                                    1,
                                                                                    HtmlColor.White,
                                                                                    "-" ) ) );
                }
                tmpHtmlCells.add( tmpCell );
            }
            return HtmlHelper.formatLine( HtmlVerticalAlignment.Middle,
                                          tmpBackgroundColor,
                                          tmpHtmlCells.toArray( new String[ 0 ] ) );
        }

        protected void addCell( List< String > chartLine,
                                String caption,
                                boolean header,
                                int colspan ) {
            chartLine.add( HtmlHelper.formatCell( header,
                                                  null,
                                                  true,
                                                  null,
                                                  HtmlHorizontalAlignment.Center,
                                                  ( colspan > 1 ) ? ( ( colspan * 2 ) - 1 )
                                                                  : 0,
                                                  null,
                                                  HtmlHelper.formatText( false,
                                                                         3,
                                                                         null,
                                                                         caption ) ) );
        }

        protected void countColumns( Header header,
                                     int depth ) {
            header.colspan = header.getChilds( ).size( );
            if ( header.colspan == 0 ) {
                ++this.columnCount;
            }
            else {
                ++depth;
                if ( this.headerDepth < depth ) {
                    this.headerDepth = depth;
                }
                for ( Header tmpChild : header.getChilds( ) ) {
                    this.countColumns( tmpChild,
                                       depth );
                    if ( tmpChild.colspan > 0 ) {
                        header.colspan += ( tmpChild.colspan - 1 );
                    }
                }
            }
        }
    }

    public static enum HtmlColor {
        Black(
            "black",
            0x00000000 ),
        Blue(
            "blue",
            0x000000ff ),
        Cyan(
            "cyan",
            0x0000ffff ),
        Green(
            "green",
            0x00009900 ),
        Red(
            "red",
            0x00ff0000 ),
        Orange(
            "orange",
            0x00ff4500 ),
        Magenta(
            "magenta",
            0x00ff00ff ),
        DarkGray(
            "darkGray",
            0x00a9a9a9 ),
        Gray(
            "gray",
            0x00d3d3d3 ),
        LightGray(
            "lightGray",
            0x00e8e8e8 ),
        White(
            "white",
            0x00ffffff );

        public final String tag;

        public final int    code;

        private HtmlColor( String tag,
                           int code ) {
            this.tag = tag;
            this.code = code;
        }

        public String codeAsString( ) {
            return String.format( "%06X",
                                  this.code );
        }
    }

    public static enum HtmlHorizontalAlignment {
        Left(
            "left" ),
        Center(
            "center" ),
        Right(
            "right" ),
        Justify(
            "justify" );

        public final String attribute;

        private HtmlHorizontalAlignment( String attribute ) {
            this.attribute = attribute;
        }
    }

    public static enum HtmlVerticalAlignment {
        Top(
            "top" ),
        Middle(
            "middle" ),
        Bottom(
            "bottom" ),
        Baseline(
            "baseline" );

        public final String attribute;

        private HtmlVerticalAlignment( String attribute ) {
            this.attribute = attribute;
        }
    }

    /**
     * Traduz tags de texto em tags HTML, substituindo ou não os caracteres reservados
     */
    public static String formatText( Boolean fixedWidth,
                                     Integer fontSize,
                                     HtmlColor foregroundColor,
                                     boolean doHtmlEscapes,
                                     String textString ) {
        String tmpText = doHtmlEscapes ? StringHelper.escapeHtml( textString )
                                       : textString;
        tmpText = tmpText.replaceAll( "\\[/?n\\]",
                                      "" );
        for ( String tmpTag : new String[ ] { "i",
                                              "b",
                                              "ul",
                                              "ol",
                                              "dl",
                                              "li",
                                              "dt",
                                              "dd",
                                              "br" } ) {
            tmpText = tmpText.replaceAll( String.format( "\\[%s\\]",
                                                         tmpTag ),
                                          String.format( "<%s>",
                                                         tmpTag ) );
            tmpText = tmpText.replaceAll( String.format( "\\[/%s\\]",
                                                         tmpTag ),
                                          String.format( "</%s>",
                                                         tmpTag ) );
        }
        for ( HtmlColor tmpColor : HtmlColor.values( ) ) {
            tmpText = tmpText.replaceAll( String.format( "\\[%s\\]",
                                                         tmpColor.tag ),
                                          String.format( "<font color=\"#%s\">",
                                                         tmpColor.codeAsString( ) ) );
            tmpText = tmpText.replaceAll( String.format( "\\[/%s\\]",
                                                         tmpColor.tag ),
                                          "</font>" );
        }
        tmpText = tmpText.replaceAll( "\\[f(\\d)\\]",
                                      "<font size=\"$1\">" );
        tmpText = tmpText.replaceAll( "\\[/f\\d?\\]",
                                      "</font>" );
        boolean tmpHasFontTag = ( ( fixedWidth != null ) || ( fontSize != null ) || ( foregroundColor != null ) );
        return tmpHasFontTag ? String.format( "<font%s%s%s>%s</font>",
                                              ( ( fixedWidth == null ) ? ""
                                                                       : String.format( " face=\"%s\"",
                                                                                        fixedWidth ? "monospace"
                                                                                                   : "helvetica" ) ),
                                              ( fontSize == null ) ? ""
                                                                   : String.format( " size=\"%d\"",
                                                                                    fontSize ),
                                              ( foregroundColor == null ) ? ""
                                                                          : String.format( " color=\"#%s\"",
                                                                                           foregroundColor.codeAsString( ) ),
                                              tmpText )
                             : tmpText;
    }

    /**
     * Traduz tags de texto em tags HTML, substituindo os caracteres reservados
     */
    public static String formatText( Boolean fixedWidth,
                                     Integer fontSize,
                                     HtmlColor foregroundColor,
                                     String textString ) {
        return HtmlHelper.formatText( fixedWidth,
                                      fontSize,
                                      foregroundColor,
                                      true,
                                      textString );
    }

    /**
     * Traduz tags de texto em tags HTML, substituindo ou não os caracteres reservados
     */
    public static String formatText( boolean doHtmlEscapes,
                                     String textString ) {
        return HtmlHelper.formatText( null,
                                      null,
                                      null,
                                      doHtmlEscapes,
                                      textString );
    }

    /**
     * Traduz tags de texto em tags HTML, substituindo os caracteres reservados
     */
    public static String formatText( String textString ) {
        return HtmlHelper.formatText( null,
                                      null,
                                      null,
                                      true,
                                      textString );
    }

    /**
     * Gera uma célula HTML com seus respectivos atributos a partir de uma string HTML
     */
    public static String formatCell( boolean header,
                                     Integer width,
                                     Boolean noWrap,
                                     HtmlColor backgroundColor,
                                     HtmlHorizontalAlignment horizontalAlignment,
                                     Integer colspan,
                                     Integer rowspan,
                                     String htmlString ) {
        String tmpContainer = header ? "th"
                                     : "td";
        return String.format( "<%s%s%s%s%s%s><div%s>%s</div></%s>",
                              tmpContainer,
                              ( ( width == null ) ? ""
                                                  : String.format( " width=\"%d%%\"",
                                                                   width ) ),
                              ( ( noWrap != null ) && noWrap ) ? " nowrap"
                                                               : "",
                              ( backgroundColor != null ) ? String.format( " bgcolor=\"%s\"",
                                                                           backgroundColor.codeAsString( ) )
                                                          : "",
                              ( ( colspan != null ) && ( colspan > 0 ) ) ? String.format( " colspan=\"%d\"",
                                                                                          colspan )
                                                                         : "",
                              ( ( rowspan != null ) && ( rowspan > 0 ) ) ? String.format( " rowspan=\"%d\"",
                                                                                          rowspan )
                                                                         : "",
                              ( horizontalAlignment != null ) ? String.format( " align=\"%s\"",
                                                                               horizontalAlignment.attribute )
                                                              : "",
                              htmlString,
                              tmpContainer );
    }

    /**
     * Gera uma célula HTML a partir de uma string HTML, com apenas o atributo "nowrap"
     */
    public static String formatCell( String htmlString ) {
        return formatCell_colspan( null,
                                   htmlString );
    }

    /**
     * Gera uma célula HTML a partir de uma string HTML, sempre com o atributo "nowrap" e
     * opcionalmente com o atributo "colspan"
     */
    public static String formatCell_colspan( Integer colSpan,
                                             String htmlString ) {
        return formatCell( false,
                           null,
                           true,
                           null,
                           null,
                           colSpan,
                           null,
                           htmlString );
    }

    /**
     * Gera uma célula HTML a partir de uma string HTML, sempre com o atributo "nowrap" e
     * opcionalmente com o atributo "width"
     */
    public static String formatCell_width( Integer width,
                                           String htmlString ) {
        return formatCell( false,
                           width,
                           true,
                           null,
                           null,
                           null,
                           null,
                           htmlString );
    }

    /**
     * Gera uma linha HTML com seus respectivos atributos a partir de um array de céulas HTML
     */
    public static String formatLine( HtmlVerticalAlignment verticalAlignment,
                                     HtmlColor backgroundColor,
                                     String... htmlCells ) {
        StringBuilder tmpResult = new StringBuilder( );
        tmpResult.append( String.format( "<tr%s%s>",
                                         ( verticalAlignment != null ) ? String.format( " valign=\"%s\"",
                                                                                        verticalAlignment.attribute )
                                                                       : "",
                                         ( backgroundColor != null ) ? String.format( " style=\"background-color: #%s;\"",
                                                                                      backgroundColor.codeAsString( ) )
                                                                     : "" ) );
        for ( String tmpHtmlCell : htmlCells ) {
            tmpResult.append( tmpHtmlCell );
        }
        tmpResult.append( "</tr>" );
        return tmpResult.toString( );
    }

    /**
     * Gera uma linha HTML com "divisões" a partir de strings HTML
     */
    public static String formatLine( boolean header,
                                     HtmlVerticalAlignment verticalAlignment,
                                     HtmlHorizontalAlignment horizontalAlignment,
                                     HtmlColor backgroundColor,
                                     String... htmlCells ) {
        List< String > tmpHtmlCells = new ArrayList< String >( );
        boolean tmpFirstCell = true;
        for ( String tmpHtmlCell : htmlCells ) {
            if ( tmpFirstCell ) {
                tmpFirstCell = false;
            }
            else {
                tmpHtmlCells.add( HtmlHelper.formatCell_width( 1,
                                                               formatText( null,
                                                                           1,
                                                                           backgroundColor,
                                                                           "-" ) ) );
            }
            tmpHtmlCells.add( HtmlHelper.formatCell( header,
                                                     null,
                                                     true,
                                                     null,
                                                     horizontalAlignment,
                                                     null,
                                                     null,
                                                     tmpHtmlCell ) );
        }
        return formatLine( verticalAlignment,
                           backgroundColor,
                           tmpHtmlCells.toArray( new String[ 0 ] ) );
    }

    public static List< String > formatStripedLines( Integer width,
                                                     Integer fontSize,
                                                     Boolean fixedWidth,
                                                     Boolean startGrayBackground,
                                                     String separationString,
                                                     String suffixString,
                                                     List< String[ ] > textStringLines ) {
        List< String > tmpHtmlLines = new ArrayList< String >( );
        boolean tmpGrayBackground = ( startGrayBackground == null ) ? true
                                                                    : startGrayBackground;
        Lambdas.StringBuilder< String > tmpSeparatorBuilder = ( separator ) -> {
            return HtmlHelper.formatCell( false,
                                          1,
                                          null,
                                          null,
                                          null,
                                          null,
                                          null,
                                          formatText( fixedWidth,
                                                      fontSize,
                                                      null,
                                                      separator ) );
        };
        Lambdas.StringBuilder< String > tmpCellBuilder = ( text ) -> {
            return HtmlHelper.formatCell( false,
                                          null,
                                          true,
                                          null,
                                          null,
                                          null,
                                          null,
                                          formatText( fixedWidth,
                                                      fontSize,
                                                      null,
                                                      text ) );
        };
        int tmpColumnCount = 0;
        for ( String[ ] tmpTextStringLine : textStringLines ) {
            if ( tmpTextStringLine.length > tmpColumnCount ) {
                tmpColumnCount = tmpTextStringLine.length;
            }
        }
        for ( String[ ] tmpTextStringLine : textStringLines ) {
            boolean tmpFirstCell = true;
            HtmlColor tmpBackgroundColor = tmpGrayBackground ? HtmlColor.Gray
                                                             : null;
            int tmpCellCount = 0;
            List< String > tmpHtmlCells = new ArrayList< String >( );
            for ( String tmpTextString : tmpTextStringLine ) {
                if ( tmpFirstCell ) {
                    tmpFirstCell = false;
                }
                else if ( separationString != null ) {
                    tmpHtmlCells.add( tmpSeparatorBuilder.build( separationString ) );
                }
                tmpHtmlCells.add( tmpCellBuilder.build( tmpTextString ) );
                ++tmpCellCount;
            }
            for ( int tmpPadCount = tmpCellCount; tmpPadCount < tmpColumnCount; ++tmpPadCount ) {
                if ( tmpFirstCell ) {
                    tmpFirstCell = false;
                }
                else if ( separationString != null ) {
                    tmpHtmlCells.add( tmpSeparatorBuilder.build( separationString ) );
                }
                tmpHtmlCells.add( HtmlHelper.formatCell( formatText( fixedWidth,
                                                                     fontSize,
                                                                     null,
                                                                     "" ) ) );
            }
            if ( suffixString != null ) {
                tmpHtmlCells.add( HtmlHelper.formatCell( false,
                                                         100,
                                                         null,
                                                         null,
                                                         null,
                                                         null,
                                                         null,
                                                         formatText( fixedWidth,
                                                                     fontSize,
                                                                     tmpBackgroundColor,
                                                                     suffixString ) ) );
            }
            tmpHtmlLines.add( HtmlHelper.formatLine( null,
                                                     tmpBackgroundColor,
                                                     tmpHtmlCells.toArray( new String[ 0 ] ) ) );
            tmpGrayBackground = !tmpGrayBackground;
        }
        return tmpHtmlLines;
    }

    /**
     * Gera uma tabela HTML a partir de um array de linhas HTML
     */
    public static String formatTable( Integer width,
                                      String... htmlLines ) {
        StringBuilder tmpResult = new StringBuilder( );
        tmpResult.append( String.format( "<table%s>",
                                         ( width == null ) ? ""
                                                           : String.format( " width=\"%d%%\"",
                                                                            width ) ) );
        for ( String tmpHmtlLine : htmlLines ) {
            tmpResult.append( tmpHmtlLine );
        }
        tmpResult.append( "</table>" );
        return tmpResult.toString( );
    }

    /**
     * Gera uma tabela HTML de apenas uma linha a partir de strings de texto
     */
    public static String formatSingleLineTable( Boolean fixedWidth,
                                                Integer fontSize,
                                                Boolean bold,
                                                HtmlColor foregroundColor,
                                                String... textStrings ) {
        List< String > tmpHtmlCells = new ArrayList< String >( );
        for ( String tmpTextString : textStrings ) {
            tmpHtmlCells.add( formatCell( formatText( fixedWidth,
                                                      fontSize,
                                                      foregroundColor,
                                                      ( ( bold == null ) || !bold ) ? tmpTextString
                                                                                    : String.format( "[b]%s[/b]",
                                                                                                     tmpTextString ) ) ) );
        }
        return formatTable( null,
                            formatLine( null,
                                        null,
                                        tmpHtmlCells.toArray( new String[ 0 ] ) ) );
    }

    /**
     * Gera uma tabela HTML de apenas uma linha a partir de strings de texto, sem nenhum atributo
     */
    public static String formatSingleLineTable( String... textStrings ) {
        return formatSingleLineTable( null,
                                      null,
                                      null,
                                      null,
                                      textStrings );
    }

    /**
     * Gera uma tabela HTML de apenas uma coluna a partir de strings de texto
     */
    public static String formatSingleColumnTable( Boolean fixedWidth,
                                                  Integer fontSize,
                                                  Boolean bold,
                                                  Boolean noWrap,
                                                  HtmlHorizontalAlignment horizontalAlignment,
                                                  HtmlColor foregroundColor,
                                                  String... textStrings ) {
        Pattern tmpCenterPattern = Pattern.compile( "\\A<center>(.*)</center>\\z" );
        List< String > tmpHtmlLines = new ArrayList< String >( );
        for ( String tmpTextString : textStrings ) {
            boolean tmpCenterTag = false;
            Matcher tmpCenterMatcher = tmpCenterPattern.matcher( tmpTextString );
            if ( tmpCenterMatcher.matches( ) ) {
                tmpTextString = tmpCenterMatcher.group( 1 );
                tmpCenterTag = true;
            }
            HtmlHorizontalAlignment tmpHorizontalAlignment = tmpCenterTag ? HtmlHorizontalAlignment.Center
                                                                          : horizontalAlignment;
            tmpHtmlLines.add( formatLine( null,
                                          null,
                                          formatCell( false,
                                                      null,
                                                      noWrap,
                                                      null,
                                                      tmpHorizontalAlignment,
                                                      null,
                                                      null,
                                                      formatText( fixedWidth,
                                                                  fontSize,
                                                                  foregroundColor,
                                                                  ( ( bold == null ) || !bold ) ? tmpTextString
                                                                                                : String.format( "[b]%s[/b]",
                                                                                                                 tmpTextString ) ) ) ) );
        }
        return formatTable( null,
                            tmpHtmlLines.toArray( new String[ 0 ] ) );
    }

    /**
     * Gera uma tabela HTML de apenas uma coluna a partir de strings de texto, sem o atributo "bold"
     */
    public static String formatSingleColumnTable( Boolean fixedWidth,
                                                  Integer fontSize,
                                                  Boolean noWrap,
                                                  HtmlHorizontalAlignment horizontalAlignment,
                                                  HtmlColor foregroundColor,
                                                  String... textStrings ) {
        return formatSingleColumnTable( fixedWidth,
                                        fontSize,
                                        null,
                                        noWrap,
                                        horizontalAlignment,
                                        foregroundColor,
                                        textStrings );
    }

    /**
     * Gera uma tabela HTML de apenas uma coluna a partir de strings de texto, sem nenhum atributo
     */
    public static String formatSingleColumnTable( String... textStrings ) {
        return formatSingleColumnTable( null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        textStrings );
    }

    /**
     * Gera uma tabela HTML com "divisões" (células separadoras preenchidas apenas com um hífen) a
     * partir de uma matriz (lista de arrays) de strings de texto
     */
    public static String formatStripedTable( Integer width,
                                             Integer fontSize,
                                             Boolean fixedWidth,
                                             Boolean startGrayBackground,
                                             List< String[ ] > textStringMatrix ) {
        return HtmlHelper.formatTable( width,
                                       formatStripedLines( width,
                                                           fontSize,
                                                           fixedWidth,
                                                           startGrayBackground,
                                                           "",
                                                           "",
                                                           textStringMatrix ).toArray( new String[ 0 ] ) );
    }

    /**
     * Gera uma tabela HTML com "divisões" (células separadoras preenchidas apenas com um hífen) a
     * partir de uma matriz (lista de arrays) de strings de texto
     */
    public static String formatStripedTable( Integer fontSize,
                                             Boolean fixedWidth,
                                             List< String[ ] > textStringMatrix ) {
        return formatStripedTable( 100,
                                   fontSize,
                                   fixedWidth,
                                   null,
                                   textStringMatrix );
    }

    /**
     * Gera uma tabela HTML com "divisões" (células separadoras preenchidas apenas com um hífen) a
     * partir de uma matriz (relação de arrays) de string de texto
     */
    public static String formatStripedTable( Integer fontSize,
                                             Boolean fixedWidth,
                                             Boolean startGrayBackground,
                                             String[ ]... cellsTextsMatrix ) {
        return formatStripedTable( 100,
                                   fontSize,
                                   fixedWidth,
                                   startGrayBackground,
                                   Arrays.asList( cellsTextsMatrix ) );
    }

    /**
     * Gera uma tabela HTML com "divisões" (células separadoras preenchidas apenas com um hífen) a
     * partir de uma matriz (relação de arrays) de string de texto
     */
    public static String formatStripedTable( Integer fontSize,
                                             Boolean fixedWidth,
                                             String[ ]... cellsTextsMatrix ) {
        return formatStripedTable( 100,
                                   fontSize,
                                   fixedWidth,
                                   null,
                                   Arrays.asList( cellsTextsMatrix ) );
    }

    /**
     * Gera uma tabela HTML com "divisões" (células separadoras preenchidas apenas com um hífen) de
     * apenas uma coluna partir de strings de texto
     */
    public static String formatStripedTable( Integer fontSize,
                                             Boolean fixedWidth,
                                             String... cellsTextsMatrix ) {
        List< String[ ] > tmpCellsTextsMatrix = new ArrayList< String[ ] >( );
        for ( String tmpCellLine : cellsTextsMatrix ) {
            tmpCellsTextsMatrix.add( new String[ ] { tmpCellLine } );
        }
        return formatStripedTable( fontSize,
                                   fixedWidth,
                                   tmpCellsTextsMatrix );
    }

    /**
     * Gera uma tabela HTML "irregular" (linhas com quantidades diferentes de colunas) com
     * "divisões" (células separadoras preenchidas apenas com um hífen) a partir de uma matriz
     * (relação de arrays) de strings de texto
     */
    public static String formatIrregularTable( Integer width,
                                               Integer headFontSize,
                                               Integer itemFontSize,
                                               Boolean fixedWidth,
                                               Boolean hasItemCaption,
                                               String[ ]... cellsTextsMatrix ) {
        List< String > tmpHtmlLines = new ArrayList< String >( );
        List< String > tmpHtmlCells = new ArrayList< String >( );
        boolean tmpGrayBackground = true;
        boolean tmpHighlightItem = hasItemCaption;
        int tmpMaxColCount = 0;
        for ( String[ ] tmpLineCells : cellsTextsMatrix ) {
            if ( tmpMaxColCount < tmpLineCells.length ) {
                tmpMaxColCount = tmpLineCells.length;
            }
        }
        for ( String[ ] tmpLineCells : cellsTextsMatrix ) {
            tmpHtmlCells.clear( );
            HtmlColor tmpBackgroundColor = null;
            int tmpColSpan = ( ( ( tmpMaxColCount - tmpLineCells.length ) * 2 ) + 1 );
            if ( tmpLineCells.length == 1 ) {
                tmpHtmlCells.add( formatCell_colspan( tmpColSpan,
                                                      formatText( fixedWidth,
                                                                  headFontSize,
                                                                  null,
                                                                  tmpLineCells[ 0 ] ) ) );
                tmpGrayBackground = true;
                tmpHighlightItem = hasItemCaption;
            }
            else {
                tmpBackgroundColor = tmpGrayBackground ? HtmlColor.DarkGray
                                                       : null;
                tmpGrayBackground = !tmpGrayBackground;
                int tmpSpanCount = tmpLineCells.length;
                boolean tmpFirstCell = true;
                for ( String tmpCellText : tmpLineCells ) {
                    if ( tmpFirstCell ) {
                        tmpFirstCell = false;
                    }
                    else {
                        tmpHtmlCells.add( HtmlHelper.formatCell_width( 1,
                                                                       formatText( fixedWidth,
                                                                                   itemFontSize,
                                                                                   tmpBackgroundColor,
                                                                                   "  " ) ) );
                    }
                    tmpHtmlCells.add( HtmlHelper.formatCell_colspan( ( --tmpSpanCount > 0 ) ? null
                                                                                            : tmpColSpan,
                                                                     HtmlHelper.formatText( fixedWidth,
                                                                                            itemFontSize,
                                                                                            null,
                                                                                            tmpHighlightItem ? String.format( "[b]%s[/b]",
                                                                                                                              tmpCellText )
                                                                                                             : tmpCellText ) ) );
                }
                tmpHtmlCells.add( HtmlHelper.formatCell_width( 100,
                                                               formatText( fixedWidth,
                                                                           itemFontSize,
                                                                           tmpBackgroundColor,
                                                                           "  " ) ) );
                tmpHighlightItem = false;
            }
            tmpHtmlLines.add( HtmlHelper.formatLine( HtmlVerticalAlignment.Middle,
                                                     tmpBackgroundColor,
                                                     tmpHtmlCells.toArray( new String[ 0 ] ) ) );
        }
        return HtmlHelper.formatTable( width,
                                       tmpHtmlLines.toArray( new String[ 0 ] ) );
    }

    /**
     * Gera uma tabela HTML "irregular" (linhas com quantidades diferentes de colunas) com
     * "divisões" (células separadoras preenchidas apenas com um hífen) a partir de uma matriz
     * (relação de arrays) de strings de texto, com largura de 100%
     */
    public static String formatIrregularTable( Integer headFontSize,
                                               Integer itemFontSize,
                                               Boolean fixedWidth,
                                               Boolean hasItemCaption,
                                               String[ ]... cellsTextsMatrix ) {
        return HtmlHelper.formatIrregularTable( 100,
                                                headFontSize,
                                                itemFontSize,
                                                fixedWidth,
                                                hasItemCaption,
                                                cellsTextsMatrix );
    }

    public static String wrapTables( HtmlHorizontalAlignment horizontalAlignment,
                                     String... htmlTables ) {
        List< String > tmpLines = new ArrayList< String >( );
        for ( String tmpHtmlTable : htmlTables ) {
            if ( tmpHtmlTable != null ) {
                tmpLines.add( formatLine( null,
                                          null,
                                          formatCell( false,
                                                      null,
                                                      null,
                                                      null,
                                                      horizontalAlignment,
                                                      null,
                                                      null,
                                                      tmpHtmlTable ) ) );
            }
        }
        return formatTable( 100,
                            tmpLines.toArray( new String[ 0 ] ) );
    }

    public static String wrapTables( HtmlVerticalAlignment verticalAlignment,
                                     String... htmlTables ) {
        List< String > tmpColumns = new ArrayList< String >( );
        for ( String tmpHtmlTable : htmlTables ) {
            tmpColumns.add( formatCell( false,
                                        null,
                                        null,
                                        null,
                                        HtmlHorizontalAlignment.Center,
                                        null,
                                        null,
                                        tmpHtmlTable ) );
        }
        return formatTable( 100,
                            formatLine( verticalAlignment,
                                        null,
                                        tmpColumns.toArray( new String[ 0 ] ) ) );
    }

    /**
     * Concatena "horizontalmente" as matrizes (arrays de arrays) de strings de texto de duas ou
     * mais tabelas, gerando células vazias para preencher as diferenças de "largura" entre as
     * linhas de uma mesma tabela, e linhas vazias para preencher as diferenças de "altura" entre as
     * tabelas distintas
     */
    public static List< String[ ] > mergeTablesHorizontally( String separator,
                                                             String[ ][ ]... textTables ) {
        List< String[ ] > tmpResult = new ArrayList< String[ ] >( );
        List< Integer > tmpCollumnCounts = new ArrayList< Integer >( );
        int tmpLineCount = 0;
        for ( String[ ][ ] tmpTextTable : textTables ) {
            if ( tmpTextTable.length > tmpLineCount ) {
                tmpLineCount = tmpTextTable.length;
            }
            int tmpMaxColumnCount = 0;
            for ( String[ ] tmpTextLine : tmpTextTable ) {
                if ( ( tmpTextLine != null ) && ( tmpTextLine.length > tmpMaxColumnCount ) ) {
                    tmpMaxColumnCount = tmpTextLine.length;
                }
            }
            tmpCollumnCounts.add( tmpMaxColumnCount );
        }
        //
        for ( int tmpLineIndex = 0; tmpLineIndex < tmpLineCount; tmpLineIndex++ ) {
            List< String > tmpColumns = new ArrayList< String >( );
            for ( int tmpTableIndex = 0; tmpTableIndex < textTables.length; tmpTableIndex++ ) {
                if ( ( separator != null ) && ( tmpTableIndex != 0 ) ) {
                    tmpColumns.add( separator );
                }
                int tmpCollumnCount = tmpCollumnCounts.get( tmpTableIndex );
                if ( tmpLineIndex < textTables[ tmpTableIndex ].length ) {
                    String[ ] tmpColumnsTexts = textTables[ tmpTableIndex ][ tmpLineIndex ];
                    tmpColumns.addAll( Arrays.asList( tmpColumnsTexts ) );
                    int tmpTableLinePadding = ( tmpCollumnCount - tmpColumnsTexts.length );
                    if ( tmpTableLinePadding > 0 ) {
                        tmpColumns.addAll( StringHelper.emptyList( tmpTableLinePadding ) );
                    }
                }
                else {
                    tmpColumns.addAll( StringHelper.emptyList( tmpCollumnCount ) );
                }
            }
            tmpResult.add( tmpColumns.toArray( new String[ 0 ] ) );
        }
        return tmpResult;
    }

    public static List< String[ ] > mergeTablesHorizontally( String[ ][ ]... textTables ) {
        return mergeTablesHorizontally( null,
                                        textTables );
    }
}
