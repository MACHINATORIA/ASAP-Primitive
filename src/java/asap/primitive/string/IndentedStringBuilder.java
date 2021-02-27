package asap.primitive.string;

public class IndentedStringBuilder {

    protected String           lastAppend;

    protected StringBuilder    result;

    protected int              level;

    protected String           indentation;

    protected final static int INDENTATION_LENGTH = 4;

    public IndentedStringBuilder( ) {
        this.clear( );
    }

    public IndentedStringBuilder( int range ) {
        this( );
        this.increaseIndent( range );
    }

    public IndentedStringBuilder( String stringFormat,
                                  Object... stringArgs ) {
        this( );
        this.append( stringFormat,
                     stringArgs );
    }

    public IndentedStringBuilder( int range,
                                  String stringFormat,
                                  Object... stringArgs ) {
        this( );
        this.increaseIndent( range );
        this.append( stringFormat,
                     stringArgs );
    }

    public IndentedStringBuilder clear( ) {
        this.lastAppend = "";
        this.result = new StringBuilder( );
        this.level = 0;
        this.indentation = "";
        return this;
    }

    protected IndentedStringBuilder updateIndentation( ) {
        this.indentation = StringHelper.spaces( this.level );
        return this;
    }

    public IndentedStringBuilder resetIndent( ) {
        this.level = 0;
        this.updateIndentation( );
        return this;
    }

    public IndentedStringBuilder increaseIndent( ) {
        this.increaseIndent( 1 );
        return this;
    }

    public IndentedStringBuilder increaseIndent( int range ) {
        this.level += ( INDENTATION_LENGTH * range );
        this.updateIndentation( );
        return this;
    }

    public IndentedStringBuilder decreaseIndent( ) {
        this.decreaseIndent( 1 );
        return this;
    }

    public IndentedStringBuilder decreaseIndent( int range ) {
        this.level -= ( INDENTATION_LENGTH * range );
        if ( this.level < 0 ) {
            this.level = 0;
        }
        this.updateIndentation( );
        return this;
    }

    public IndentedStringBuilder append( String format,
                                         Object... args ) {
        if ( format != null ) {
            this.lastAppend = StringHelper.flawlessFormat( format,
                                                           args );
            String tmpLines[] = this.lastAppend.split( "[\r\n]+",
                                                       -1 );
            for ( String tmpLine : tmpLines ) {
                this.result.append( ( this.result.length( ) > 0 ) ? "\n"
                                                                  : "" );
                this.result.append( this.indentation );
                this.result.append( tmpLine );
            }
        }
        return this;
    }

    public IndentedStringBuilder append( IndentedStringBuilder anotherIndentedStringBuffer ) {
        return this.append( anotherIndentedStringBuffer.getResult( ) );
    }

    public IndentedStringBuilder appendIndented( int range,
                                                 String format,
                                                 Object... args ) {
        this.increaseIndent( range );
        this.append( format,
                     args );
        return this.decreaseIndent( range );
    }

    public IndentedStringBuilder appendIndented( String format,
                                                 Object... args ) {
        return this.appendIndented( 1,
                                    format,
                                    args );
    }

    public String getLastAppend( ) {
        return this.lastAppend;
    }

    public int getLength( ) {
        return this.result.length( );
    }

    public String getResult( ) {
        return this.result.toString( );
    }

    @Override
    public String toString( ) {
        return this.getResult( );
    }
}
