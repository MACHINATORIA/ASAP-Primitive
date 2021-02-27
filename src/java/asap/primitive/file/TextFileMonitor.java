package asap.primitive.file;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TextFileMonitor {

    public static enum LineBreakType {
        NoBreak,
        EmptyBreak, //  CR+CR or LF+LF
        SingleBreak, // LF+char or CR+char
        DoubleBreak; // CR+LF or LF+CR
    }

    public static interface LineListener {

        public abstract void newLine( long lineOffset,
                                      LineBreakType breakStyle,
                                      String lineText );
    }

    public TextFileMonitor( LineListener lineListener ) {
        //
        this.lineListener = lineListener;
        this.fileStream = null;
        this.bufferedStream = null;
        this.readBuffer = new byte[ (int) READ_BURST_LENGTH ];
        //
        this.lineBuffer = new ByteArrayOutputStream( this.readBuffer.length );
        this.linePosition = 0;
        this.lastChar = 0;
    }

    protected static final long     READ_BURST_LENGTH = 4096;

    protected final LineListener    lineListener;

    // File state
    protected FileInputStream       fileStream;

    protected BufferedInputStream   bufferedStream;

    protected byte[ ]               readBuffer;

    // Text state
    protected ByteArrayOutputStream lineBuffer;

    protected long                  linePosition;

    protected byte                  lastChar;

    public void open( String fileName )
        throws IOException {
        this.open( fileName,
                   -1 );
    }

    public void open( String fileName,
                      long position )
        throws IOException {
        this.close( );
        this.fileStream = new FileInputStream( new File( fileName ) );
        if ( position > 0 ) {
            this.fileStream.getChannel( ).position( position );
        }
        this.bufferedStream = new BufferedInputStream( this.fileStream );
    }

    public void close( )
        throws IOException {
        if ( this.fileStream != null ) {
            if ( this.bufferedStream != null ) {
                this.bufferedStream.close( );
                this.bufferedStream = null;
            }
            this.fileStream.close( );
            this.fileStream = null;
        }
    }

    public long getSize( )
        throws IOException {
        return ( this.fileStream == null ) ? 0
                                           : this.fileStream.getChannel( ).size( );
    }

    public long getPosition( )
        throws IOException {
        //    return ( this.fileStream == null )
        //                                      ? 0
        //                                      : this.fileStream.getChannel( ).position( );
        return this.linePosition;
    }

    public void poll( )
        throws IOException {
        this.poll( -1 );
    }

    public void poll( long maxBytesToRead )
        throws IOException {
        if ( this.fileStream != null ) {
            long tmpFileSize = this.fileStream.getChannel( ).size( );
            long tmpFileOffset = this.fileStream.getChannel( ).position( );
            long tmpBytesReady = ( tmpFileSize - tmpFileOffset );
            if ( ( maxBytesToRead > 0 ) && ( maxBytesToRead < tmpBytesReady ) ) {
                tmpBytesReady = maxBytesToRead;
            }
            while ( tmpBytesReady > 0 ) {
                //
                int tmpBytesToRead = (int) ( ( tmpBytesReady > this.readBuffer.length ) ? this.readBuffer.length
                                                                                        : tmpBytesReady );
                int tmpReadBytes = this.bufferedStream.read( this.readBuffer,
                                                             0,
                                                             tmpBytesToRead );
                tmpBytesReady -= tmpReadBytes;
                //
                int tmpByteIndex = 0;
                LineBreakType tmpBreakType = LineBreakType.NoBreak;
                while ( tmpByteIndex < tmpReadBytes ) {
                    //
                    byte tmpByte = this.readBuffer[ tmpByteIndex ];
                    //
                    if ( ( tmpByte == 0x0a ) || ( tmpByte == 0x0d ) ) {
                        if ( ( this.lastChar == 0x0a ) || ( this.lastChar == 0x0d ) ) {
                            if ( tmpByte == this.lastChar ) {
                                // Break at CR+CR or LF+LF (empty break)
                                tmpBreakType = LineBreakType.EmptyBreak;
                            }
                            else {
                                // Break at CR+LF or LF+CR (double break)
                                tmpBreakType = LineBreakType.DoubleBreak;
                            }
                        }
                    }
                    else {
                        if ( ( this.lastChar == 0x0a ) || ( this.lastChar == 0x0d ) ) {
                            // Break at LF+char or CR+char (single break)
                            tmpBreakType = LineBreakType.SingleBreak;
                        }
                        else {
                            // Append character and continue
                            this.lineBuffer.write( tmpByte );
                        }
                    }
                    //
                    if ( tmpBreakType != LineBreakType.NoBreak ) {
                        //
                        String tmpLineText = new String( this.lineBuffer.toByteArray( ) );
                        this.lineBuffer.reset( );
                        //
                        try {
                            this.lineListener.newLine( this.linePosition,
                                                       tmpBreakType,
                                                       tmpLineText );
                        }
                        catch ( Throwable e ) {
                            e.printStackTrace( );
                        }
                        //
                        this.linePosition = tmpFileOffset;
                        //
                        if ( tmpBreakType == LineBreakType.SingleBreak ) {
                            // Append character (single break)
                            this.lineBuffer.write( tmpByte );
                        }
                        else if ( tmpBreakType == LineBreakType.DoubleBreak ) {
                            tmpByte = 0;
                            this.linePosition += 1;
                        }
                        //
                        tmpBreakType = LineBreakType.NoBreak;
                        //
                    }
                    this.lastChar = tmpByte;
                    tmpFileOffset += 1;
                    tmpByteIndex += 1;
                }
            }
        }
    }

    public static List< String > readFile( String fileName )
        throws IOException {
        final List< String > tmpResult = new ArrayList< String >( );
        TextFileMonitor tmpMonitor = new TextFileMonitor( new LineListener( ) {

            @Override
            public void newLine( long lineOffset,
                                 LineBreakType breakStyle,
                                 String lineText ) {
                tmpResult.add( lineText );
            }
        } );
        tmpMonitor.poll( );
        return tmpResult;
    }
}
