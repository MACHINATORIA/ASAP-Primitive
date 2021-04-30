package asap.primitive.log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import asap.primitive.exception.ExceptionTemplate;
import asap.primitive.file.FileHelper;
import asap.primitive.pattern.ValuePattern.BasicValue;
import asap.primitive.pattern.ValuePattern.Value;
import asap.primitive.process.ProcessHelper;
import asap.primitive.string.StringHelper;

public class LogService {

    @XmlType
    @XmlEnum( String.class )
    @XmlAccessorType( XmlAccessType.NONE )
    public static enum LogLevel {
        Off(
            "OFF" ),
        Fatal(
            "FATAL" ),
        Error(
            "ERROR" ),
        Warn(
            "WARN" ),
        Info(
            "INFO" ),
        Exception(
            "EXCEP" ),
        Debug(
            "DEBUG" ),
        Trace(
            "TRACE" ),
        All(
            "ALL" );

        public final String tag;

        public boolean isLesser( LogLevel anotherLevel ) {
            return ( ( anotherLevel != null ) && ( this.ordinal( ) < anotherLevel.ordinal( ) ) );
        }

        public boolean isGreater( LogLevel anotherLevel ) {
            return ( ( anotherLevel != null ) && ( this.ordinal( ) > anotherLevel.ordinal( ) ) );
        }

        public static LogLevel minimum( ) {
            return LogLevel.Off;
        }

        public static LogLevel maximum( ) {
            return LogLevel.All;
        }

        private LogLevel( String tag ) {
            this.tag = tag;
        }
    }

    public static final class Logger {

        public String getName( ) {
            synchronized ( this ) {
                return this.name;
            }
        }

        public void changeName( String newName ) {
            synchronized ( this ) {
                this.context.changeLoggerName( this,
                                               newName );
            }
        }

        public LogProfile getProfile( ) {
            synchronized ( this ) {
                return this.profile;
            }
        }

        public Logger setMinLevel( LogLevel minLevel ) {
            synchronized ( this ) {
                this.localMinLevel = minLevel;
            }
            return this;
        }

        public Logger setMaxLevel( LogLevel maxLevel ) {
            synchronized ( this ) {
                this.localMaxLevel = maxLevel;
            }
            return this;
        }

        protected boolean isLocalEnabled( LogLevel level ) {
            return ( !level.isLesser( this.localMinLevel ) && !level.isGreater( this.localMaxLevel ) );
        }

        public boolean isEnabled( LogLevel level ) {
            synchronized ( this ) {
                return ( this.isLocalEnabled( level ) && this.profile.isEnabled( level ) );
            }
        }

        public boolean isTraceEnabled( ) {
            return this.isEnabled( LogLevel.Trace );
        }

        public boolean isDebugEnabled( ) {
            return this.isEnabled( LogLevel.Debug );
        }

        public boolean isExceptionEnabled( ) {
            return this.isEnabled( LogLevel.Exception );
        }

        public boolean isInfoEnabled( ) {
            return this.isEnabled( LogLevel.Info );
        }

        public boolean isWarnEnabled( ) {
            return this.isEnabled( LogLevel.Warn );
        }

        public boolean isErrorEnabled( ) {
            return this.isEnabled( LogLevel.Error );
        }

        public boolean isFatalEnabled( ) {
            return this.isEnabled( LogLevel.Fatal );
        }

        public Logger resetIndentation( ) {
            synchronized ( this ) {
                this.indentation = 0;
                return this;
            }
        }

        public Logger increaseIndentation( ) {
            synchronized ( this ) {
                if ( this.indentation < MAXIMUM_INDENTATION ) {
                    ++this.indentation;
                }
                return this;
            }
        }

        public Logger decreaseIndentation( ) {
            synchronized ( this ) {
                if ( this.indentation > 0 ) {
                    --this.indentation;
                }
                return this;
            }
        }

        public Logger message( LogLevel level,
                               String format,
                               Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( level ) ) {
                    this.profile.message( this.indentation,
                                          level,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger message( int indentation,
                               LogLevel level,
                               String format,
                               Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( level ) && this.profile.isEnabled( level ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          level,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger trace( String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Trace ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Trace,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger trace( int indentation,
                             String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Trace ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Trace,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger debug( String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Debug ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Debug,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger debug( int indentation,
                             String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Debug ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Debug,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger exception( Throwable e ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Exception ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Exception,
                                          "%s: %s",
                                          e.getClass( ).getSimpleName( ),
                                          e.getLocalizedMessage( ) );
                }
            }
            return this;
        }

        public Logger exception( int indentation,
                                 Throwable e ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Exception ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Exception,
                                          "%s: %s",
                                          e.getClass( ).getSimpleName( ),
                                          e.getLocalizedMessage( ) );
                }
            }
            return this;
        }

        public Logger exception( String format,
                                 Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Exception ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Exception,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger exception( int indentation,
                                 String format,
                                 Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Exception ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Exception,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger info( String format,
                            Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Info ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Info,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger info( int indentation,
                            String format,
                            Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Info ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Info,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger warn( String format,
                            Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Warn ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Warn,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger warn( int indentation,
                            String format,
                            Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Warn ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Warn,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger error( String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Error ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Error,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger error( int indentation,
                             String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Error ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Error,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger fatal( String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Fatal ) ) {
                    this.profile.message( this.indentation,
                                          LogLevel.Fatal,
                                          format,
                                          args );
                }
            }
            return this;
        }

        public Logger fatal( int indentation,
                             String format,
                             Object... args ) {
            synchronized ( this ) {
                if ( this.isLocalEnabled( LogLevel.Fatal ) ) {
                    this.profile.message( ( this.indentation + indentation ),
                                          LogLevel.Fatal,
                                          format,
                                          args );
                }
            }
            return this;
        }

        protected Logger( String name,
                          LogContext context,
                          boolean inheritParent,
                          boolean defaultToNull,
                          LogProfile profile ) {
            this.name = name;
            this.context = context;
            this.inheritParent = inheritParent;
            this.defaultToNull = defaultToNull;
            this.profile = profile;
            this.localMinLevel = null;
            this.localMaxLevel = null;
            this.indentation = 0;
        }

        protected LogProfile setProfile( LogProfile profile ) {
            synchronized ( this ) {
                LogProfile tmpPrevious = this.profile;
                this.profile = profile;
                return tmpPrevious;
            }
        }

        protected String setName( String name ) {
            synchronized ( this ) {
                String tmpPrevious = this.name;
                this.name = name;
                return tmpPrevious;
            }
        }

        protected static final int MAXIMUM_INDENTATION = 10;

        protected String           name;

        protected LogContext       context;

        protected boolean          inheritParent;

        protected boolean          defaultToNull;

        protected LogProfile       profile;

        protected LogLevel         localMinLevel;

        protected LogLevel         localMaxLevel;

        protected int              indentation;
    }

    @SuppressWarnings( "serial" )
    public static final class LogException extends ExceptionTemplate {

        public LogException( Throwable cause ) {
            super( cause );
        }

        public LogException( String messageFormat,
                             Object... messageArgs ) {
            super( messageFormat,
                   messageArgs );
        }

        public LogException( Throwable cause,
                             String messageFormat,
                             Object... messageArgs ) {
            super( cause,
                   messageFormat,
                   messageArgs );
        }
    }

    @XmlType
    @XmlEnum( String.class )
    @XmlAccessorType( XmlAccessType.NONE )
    public static enum LogDetail {
        CalendarDate,
        TimeStamp,
        LevelTag,
        ThreadId,
        InnermostClassName,
        OutermostClassName,
        SimpleClassName,
        FullClassName,
        MethodName,
        LineNumber;
    }

    @XmlType( name = "layout",
              propOrder = { "name",
                            "width",
                            "details" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static final class LogLayout {

        @XmlAttribute( required = true )
        protected String                  name;

        @XmlAttribute( required = false )
        protected Integer                 width;

        @XmlAttribute( required = false )
        protected Boolean                 noWrap;

        @XmlElement( required = false,
                     name = "detail" )
        protected List< LogDetail >       details;

        protected static SimpleDateFormat dateFormat                         = new SimpleDateFormat( "dd/MM/yyyy " );

        protected static SimpleDateFormat timeFormat                         = new SimpleDateFormat( "HH:mm:ss.SSS " );

        protected static final int        INDENTATION_WIDTH                  = 2;

        protected static final int        MINIMUM_OUTPUT_WIDTH               = 80;

        protected static final int        MAXIMUM_OUTPUT_WIDTH               = 999;

        protected static final int        DEFAULT_OUTPUT_WIDTH               = MAXIMUM_OUTPUT_WIDTH;

        protected static Pattern          SIMPLE_CLASS_NAME_SPLIT_PATTERN    = Pattern.compile( "\\." );

        protected static Pattern          OUTERMOST_CLASS_NAME_SPLIT_PATTERN = Pattern.compile( "\\.|\\$.*" );

        protected static Pattern          INNERMOST_CLASS_NAME_SPLIT_PATTERN = Pattern.compile( "\\.|\\$(?!\\d+)" );

        protected boolean                 includeCalendarDate;

        protected boolean                 includeTimeStamp;

        protected boolean                 includeLevelTag;

        protected boolean                 includeThreadId;

        protected LogDetail               includeClassName;

        protected boolean                 includeMethodName;

        protected boolean                 includeLineNumber;

        public String getName( ) {
            return this.name;
        }

        @XmlTransient
        public Integer getWidth( ) {
            synchronized ( this ) {
                return this.width;
            }
        }

        public LogLayout setWidth( Integer width ) {
            synchronized ( this ) {
                this.width = width;
            }
            return this;
        }

        @XmlTransient
        public List< LogDetail > getDetails( ) {
            synchronized ( this ) {
                return new ArrayList< LogDetail >( this.details );
            }
        }

        public LogLayout setDetails( List< LogDetail > details ) {
            synchronized ( this ) {
                this.details = new ArrayList< LogDetail >( details );
                this.parseDetails( );
            }
            return this;
        }

        public LogLayout setDetails( LogDetail... details ) {
            return this.setDetails( Arrays.asList( details ) );
        }

        protected void parseDetails( ) {
            this.includeCalendarDate = this.details.contains( LogDetail.CalendarDate );
            this.includeTimeStamp = this.details.contains( LogDetail.TimeStamp );
            this.includeLevelTag = this.details.contains( LogDetail.LevelTag );
            this.includeThreadId = this.details.contains( LogDetail.ThreadId );
            if ( this.details.contains( LogDetail.FullClassName ) ) {
                this.includeClassName = LogDetail.FullClassName;
            }
            else if ( this.details.contains( LogDetail.SimpleClassName ) ) {
                this.includeClassName = LogDetail.SimpleClassName;
            }
            else if ( this.details.contains( LogDetail.OutermostClassName ) ) {
                this.includeClassName = LogDetail.OutermostClassName;
            }
            else if ( this.details.contains( LogDetail.InnermostClassName ) ) {
                this.includeClassName = LogDetail.InnermostClassName;
            }
            else {
                this.includeClassName = null;
            }
            this.includeMethodName = this.details.contains( LogDetail.MethodName );
            this.includeLineNumber = this.details.contains( LogDetail.LineNumber );
        }

        protected String format( Calendar timestamp,
                                 LogLevel level,
                                 long threadId,
                                 StackTraceElement stackElement,
                                 Value< Integer > messageShift,
                                 int indentation,
                                 String message ) {
            int tmpWidth;
            synchronized ( this ) {
                tmpWidth = this.width;
            }
            StringBuilder tmpLogMessage = new StringBuilder( );
            /*
             * 
             */
            // MessageTime
            Date tmpCurrentTime = new Date( );
            if ( this.includeCalendarDate ) {
                tmpLogMessage.append( LogLayout.dateFormat.format( tmpCurrentTime ) );
            }
            if ( this.includeTimeStamp ) {
                tmpLogMessage.append( LogLayout.timeFormat.format( tmpCurrentTime ) );
            }
            // MessageLevel
            if ( this.includeLevelTag ) {
                tmpLogMessage.append( String.format( "[%-5s] ",
                                                     level.tag ) );
            }
            // Indentation
            if ( indentation > 0 ) {
                tmpLogMessage.append( StringHelper.spaces( indentation * INDENTATION_WIDTH ) );
            }
            // MessageOrigin
            if ( this.includeThreadId ) {
                tmpLogMessage.append( String.format( "0x%08X ",
                                                     threadId ) );
            }
            if ( ( this.includeClassName != null ) && ( stackElement != null ) ) {
                String tmpClassName = null;
                switch ( this.includeClassName ) {
                    case FullClassName:
                        tmpClassName = stackElement.getClassName( );
                        break;
                    case SimpleClassName: {
                        String[ ] tmpClassNamePieces = SIMPLE_CLASS_NAME_SPLIT_PATTERN.split( stackElement.getClassName( ) );
                        tmpClassName = tmpClassNamePieces[ tmpClassNamePieces.length - 1 ];
                        break;
                    }
                    case OutermostClassName: {
                        String[ ] tmpClassNamePieces = OUTERMOST_CLASS_NAME_SPLIT_PATTERN.split( stackElement.getClassName( ) );
                        tmpClassName = tmpClassNamePieces[ tmpClassNamePieces.length - 1 ];
                        break;
                    }
                    case InnermostClassName: {
                        String[ ] tmpClassNamePieces = INNERMOST_CLASS_NAME_SPLIT_PATTERN.split( stackElement.getClassName( ),
                                                                                                 -1 );
                        tmpClassName = tmpClassNamePieces[ tmpClassNamePieces.length - 1 ];
                        break;
                    }
                    default:
                        break;
                }
                if ( tmpClassName != null ) {
                    tmpLogMessage.append( tmpClassName );
                    if ( this.includeMethodName ) {
                        boolean tmpBlockMethod = false;
                        String tmpMethodName = stackElement.getMethodName( );
                        if ( tmpMethodName.compareTo( "<init>" ) == 0 ) {
                            tmpMethodName = "constructor";
                        }
                        else if ( tmpMethodName.compareTo( "<clinit>" ) == 0 ) {
                            tmpMethodName = "static";
                            tmpBlockMethod = true;
                        }
                        tmpLogMessage.append( String.format( tmpBlockMethod ? ".%s{}"
                                                                            : ".%s()",
                                                             tmpMethodName ) );
                    }
                    if ( this.includeLineNumber ) {
                        tmpLogMessage.append( String.format( ":%s",
                                                             stackElement.getLineNumber( ) ) );
                    }
                    tmpLogMessage.append( " - " );
                }
            }
            // MessageShift
            if ( messageShift.get( ) < tmpLogMessage.length( ) ) {
                int tmoNewShift = ( ( ( tmpLogMessage.length( ) + INDENTATION_WIDTH - 1 ) / INDENTATION_WIDTH )
                                    * INDENTATION_WIDTH );
                tmoNewShift = tmpLogMessage.length( );
                messageShift.set( tmoNewShift );
            }
            tmpLogMessage.append( StringHelper.spaces( messageShift.get( ) - tmpLogMessage.length( ) ) );
            int tmpMessageWrapColumn = ( INDENTATION_WIDTH * 2 ); //tmpLogMessage.length( );
            // MessageText
            tmpLogMessage.append( message );
            return ( this.noWrap ) ? tmpLogMessage.toString( )
                                   : StringHelper.wrap( tmpLogMessage.toString( ),
                                                        tmpWidth,
                                                        0,
                                                        tmpMessageWrapColumn );
        }

        protected LogLayout( ) {
            this.noWrap = false;
            this.details = new ArrayList< LogDetail >( );
        }

        public LogLayout( String name,
                          Integer width,
                          Boolean noWrap,
                          LogDetail... details ) {
            this.name = name;
            this.width = width;
            this.noWrap = noWrap;
            this.details = new ArrayList< LogDetail >( Arrays.asList( details ) );
        }

        public LogLayout( String name,
                          Integer width,
                          LogDetail... details ) {
            this( name,
                  width,
                  false,
                  details );
        }

        public LogLayout( String name,
                          LogDetail... details ) {
            this( name,
                  DEFAULT_OUTPUT_WIDTH,
                  false,
                  details );
        }

        protected void engage( LogConfig config )
            throws LogException {
            if ( ( this.name == null ) || ( this.name.length( ) == 0 ) ) {
                throw new LogException( "Layout name is %s",
                                        ( this.name == null ) ? "null"
                                                              : "empty" );
            }
            this.parseDetails( );
        }
    }

    @XmlTransient
    @XmlAccessorType( XmlAccessType.NONE )
    public static abstract class LogOutput {

        @XmlAttribute( required = true )
        protected String           name;

        @XmlAttribute( required = false )
        protected LogLevel         minLevel;

        @XmlAttribute( required = false )
        protected LogLevel         maxLevel;

        @XmlAttribute( required = false,
                       name = "layout" )
        protected String           layoutName;

        @XmlTransient
        protected LogLayout        layout;

        @XmlTransient
        protected Value< Integer > messageShift;

        public String getName( ) {
            return this.name;
        }

        public LogLevel getMinLevel( ) {
            synchronized ( this ) {
                return ( this.minLevel != null ) ? this.minLevel
                                                 : LogLevel.minimum( );
            }
        }

        public LogOutput setMinLevel( LogLevel minLevel ) {
            synchronized ( this ) {
                this.minLevel = minLevel;
            }
            return this;
        }

        public LogLevel getMaxLevel( ) {
            synchronized ( this ) {
                return ( this.maxLevel != null ) ? this.maxLevel
                                                 : LogLevel.maximum( );
            }
        }

        public LogOutput setMaxLevel( LogLevel maxLevel ) {
            synchronized ( this ) {
                this.maxLevel = maxLevel;
            }
            return this;
        }

        public LogLayout getLayout( ) {
            synchronized ( this ) {
                return this.layout;
            }
        }

        public void setLayout( LogLayout layout ) {
            synchronized ( this ) {
                if ( ( layout != null ) && !layout.equals( this.layout ) ) {
                    this.layout = layout;
                    this.messageShift.set( 0 );
                }
            }
        }

        protected LogOutput( ) {
            this.messageShift = new BasicValue< Integer >( 0 );
        }

        protected LogOutput( String name,
                             LogLevel minLevel,
                             LogLevel maxLevel,
                             String layoutName ) {
            this.name = name;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.layout = null;
            this.layoutName = layoutName;
            this.messageShift = new BasicValue< Integer >( 0 );
        }

        protected boolean isEnabled( LogLevel level ) {
            synchronized ( this ) {
                return ( !level.isLesser( this.minLevel ) && !level.isGreater( this.maxLevel ) );
            }
        }

        protected void message( Calendar timestamp,
                                LogLevel level,
                                long threadId,
                                StackTraceElement stackElement,
                                int indentation,
                                String message ) {
            synchronized ( this ) {
                this.write( timestamp,
                            this.layout.format( timestamp,
                                                level,
                                                threadId,
                                                stackElement,
                                                this.messageShift,
                                                indentation,
                                                message ) );
            }
        }

        protected void engage( LogConfig config )
            throws LogException {
            if ( ( this.name == null ) || ( this.name.length( ) == 0 ) ) {
                throw new LogException( "Output name is %s",
                                        ( this.name == null ) ? "null"
                                                              : "empty" );
            }
            if ( this.layoutName == null ) {
                this.layout = config.defaultLayout;
            }
            else {
                for ( LogLayout tmpLayout : config.layouts ) {
                    if ( tmpLayout.name.compareTo( this.layoutName ) == 0 ) {
                        this.layout = tmpLayout;
                        return;
                    }
                }
                throw new LogException( "Layout '%s' on output '%s' not found in configuration",
                                        this.layoutName,
                                        this.name );
            }
        }

        protected abstract void write( Calendar timestamp,
                                       String message );
    }

    @XmlTransient
    @XmlAccessorType( XmlAccessType.NONE )
    protected static abstract class LogOutputConsole extends LogOutput {

        @XmlTransient
        protected PrintStream stream;

        protected LogOutputConsole( PrintStream stream ) {
            super( );
            this.stream = stream;
        }

        protected LogOutputConsole( String name,
                                    LogLevel minLevel,
                                    LogLevel maxLevel,
                                    String layoutName,
                                    PrintStream stream ) {
            super( name,
                   minLevel,
                   maxLevel,
                   layoutName );
            this.stream = stream;
        }

        @Override
        protected void write( Calendar timestamp,
                              String message ) {
            synchronized ( this ) {
                this.stream.println( message );
                this.stream.flush( );
                // ThreadHelper.sleep( 2 );
            }
        }
    }

    @XmlType( name = "stdout",
              propOrder = { "name",
                            "minLevel",
                            "maxLevel",
                            "layoutName" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class LogOutputStdout extends LogOutputConsole {

        protected LogOutputStdout( ) {
            super( System.out );
        }

        public LogOutputStdout( String name,
                                LogLevel minLevel,
                                LogLevel maxLevel,
                                String layoutName ) {
            super( name,
                   minLevel,
                   maxLevel,
                   layoutName,
                   System.out );
        }
    }

    @XmlType( name = "stderr",
              propOrder = { "name",
                            "minLevel",
                            "maxLevel",
                            "layoutName" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static class LogOutputStderr extends LogOutputConsole {

        protected LogOutputStderr( ) {
            super( System.err );
        }

        public LogOutputStderr( String name,
                                LogLevel minLevel,
                                LogLevel maxLevel,
                                String layoutName ) {
            super( name,
                   minLevel,
                   maxLevel,
                   layoutName,
                   System.err );
        }
    }

    @XmlType( name = "file",
              propOrder = { "name",
                            "minLevel",
                            "maxLevel",
                            "layoutName",
                            "dirPath",
                            "fileName" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static final class LogOutputFile extends LogOutput {

        @XmlAttribute( required = false )
        protected String   dirPath;

        @XmlAttribute( required = false )
        protected String   fileName;

        @XmlTransient
        protected File     dir;

        @XmlTransient
        protected File     currentFile;

        @XmlTransient
        protected Calendar currentTimestamp;

        public String getDirPath( ) {
            synchronized ( this ) {
                return this.dirPath;
            }
        }

        public LogOutputFile setDirPath( String dirPath ) {
            synchronized ( this ) {
                if ( ( dirPath == null ) || ( dirPath.length( ) == 0 ) ) {
                    dirPath = System.getProperty( "user.dir" );
                }
                if ( ( this.dirPath == null ) || ( this.dirPath.compareTo( dirPath ) != 0 ) ) {
                    this.dirPath = dirPath;
                    this.reset( );
                }
            }
            return this;
        }

        public String getFileName( ) {
            synchronized ( this ) {
                return this.fileName;
            }
        }

        public LogOutputFile setFileName( String fileName ) {
            synchronized ( this ) {
                if ( ( fileName == null ) || ( fileName.length( ) == 0 ) ) {
                    fileName = this.name;
                }
                if ( ( this.fileName == null ) || ( this.fileName.compareTo( fileName ) != 0 ) ) {
                    this.fileName = fileName;
                    this.reset( );
                }
            }
            return this;
        }

        protected void reset( ) {
            this.dir = null;
            this.currentFile = null;
            this.currentTimestamp = null;
        }

        protected LogOutputFile( ) {
            super( );
            this.dirPath = null;
            this.fileName = null;
            this.reset( );
        }

        public LogOutputFile( String name,
                              LogLevel minLevel,
                              LogLevel maxLevel,
                              String layoutName,
                              String dirPath,
                              String fileName ) {
            super( name,
                   minLevel,
                   maxLevel,
                   layoutName );
            this.dirPath = dirPath;
            this.fileName = fileName;
            this.reset( );
        }

        @Override
        protected void engage( LogConfig config )
            throws LogException {
            super.engage( config );
            this.setDirPath( this.dirPath );
            this.setFileName( this.fileName );
        }

        protected File selectApropriateFile( Calendar timestamp ) {
            //
            //    if ( this.lastTimestamp != null ) {
            //        timestamp = new GregorianCalendar( );
            //        timestamp.setTime( this.lastTimestamp.getTime( ) );
            //        timestamp.add( Calendar.DAY_OF_MONTH,
            //                       1 );
            //    }
            //
            if ( this.currentTimestamp != null ) {
                if ( ( this.currentTimestamp.get( Calendar.YEAR ) != timestamp.get( Calendar.YEAR ) )
                     || ( this.currentTimestamp.get( Calendar.MONTH ) != timestamp.get( Calendar.MONTH ) )
                     || ( this.currentTimestamp.get( Calendar.DAY_OF_MONTH ) != timestamp.get( Calendar.DAY_OF_MONTH ) ) ) {
                    this.currentTimestamp = null;
                    this.currentFile = null;
                }
            }
            if ( this.currentFile == null ) {
                if ( this.dir == null ) {
                    File tmpDir = new File( this.dirPath );
                    boolean tmpDirExists = tmpDir.exists( );
                    if ( !tmpDirExists ) {
                        tmpDirExists = FileHelper.ensureDirectoryExists( tmpDir );
                    }
                    if ( tmpDirExists ) {
                        this.dir = tmpDir;
                    }
                }
                if ( this.dir != null ) {
                    String tmpFileName = String.format( "%s-%04d-%02d-%02d.%s",
                                                        FileHelper.getFileName( this.fileName ),
                                                        timestamp.get( Calendar.YEAR ),
                                                        ( timestamp.get( Calendar.MONTH ) + 1 ),
                                                        timestamp.get( Calendar.DAY_OF_MONTH ),
                                                        FileHelper.getFileExtension( this.fileName ) );
                    File tmpFile = new File( this.dir,
                                             tmpFileName );
                    if ( !tmpFile.exists( ) || tmpFile.canWrite( ) ) {
                        this.currentFile = tmpFile;
                        this.currentTimestamp = timestamp;
                    }
                }
            }
            return this.currentFile;
        }

        @Override
        protected void write( Calendar timestamp,
                              String message ) {
            File tmpFile = this.selectApropriateFile( timestamp );
            if ( tmpFile != null ) {
                try {
                    FileHelper.ensureDirectoryExists( tmpFile.getParentFile( ) );
                    FileOutputStream tmpFileStream = new FileOutputStream( tmpFile,
                                                                           true );
                    try {
                        BufferedOutputStream tmpBufferedStream = new BufferedOutputStream( tmpFileStream );
                        try {
                            tmpBufferedStream.write( message.getBytes( ) );
                            tmpBufferedStream.write( "\n".getBytes( ) );
                        }
                        finally {
                            tmpBufferedStream.close( );
                        }
                    }
                    finally {
                        tmpFileStream.close( );
                    }
                }
                catch ( IOException e ) {
                    this.currentFile = null;
                    e.printStackTrace( );
                }
            }
        }
    }

    @XmlType( name = "profile",
              propOrder = { "name",
                            "minLevel",
                            "maxLevel",
                            "forwardParent",
                            "outputNames" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static final class LogProfile {

        @XmlAttribute( required = true )
        protected String            name;

        @XmlAttribute( required = false )
        protected LogLevel          minLevel;

        @XmlAttribute( required = false )
        protected LogLevel          maxLevel;

        @XmlAttribute( required = false )
        protected Boolean           forwardParent;

        @XmlElement( required = false,
                     name = "output" )
        protected List< String >    outputNames;

        @XmlTransient
        protected LogProfile        parentProfile;

        @XmlTransient
        protected List< LogOutput > outputs;
        
        protected static int        STACK_DEPTH = 3;
        //
        static {
            StackTraceElement[ ] tmpStackTrace = Thread.currentThread( ).getStackTrace( );
            for ( StackTraceElement tmpElement : tmpStackTrace ) {
                if ( tmpElement.getMethodName( ).compareTo( "getStackTrace" ) == 0 ) {
                    break;
                }
                ++STACK_DEPTH;
            }
        }

        public String getName( ) {
            return this.name;
        }

        @XmlTransient
        public LogLevel getMinLevel( ) {
            synchronized ( this ) {
                return ( this.minLevel != null ) ? this.minLevel
                                                 : LogLevel.minimum( );
            }
        }

        public LogProfile setMinLevel( LogLevel minLevel ) {
            synchronized ( this ) {
                this.minLevel = minLevel;
            }
            return this;
        }

        @XmlTransient
        public LogLevel getMaxLevel( ) {
            synchronized ( this ) {
                return ( this.maxLevel != null ) ? this.maxLevel
                                                 : LogLevel.maximum( );
            }
        }

        public LogProfile setMaxLevel( LogLevel maxLevel ) {
            synchronized ( this ) {
                this.maxLevel = maxLevel;
            }
            return this;
        }

        @XmlTransient
        public boolean getForwardParent( ) {
            synchronized ( this ) {
                return ( ( this.forwardParent != null ) && this.forwardParent.booleanValue( ) );
            }
        }

        public LogProfile setForwardParent( boolean forwardParent ) {
            synchronized ( this ) {
                this.forwardParent = forwardParent;
            }
            return this;
        }

        public List< LogOutput > getOutputs( ) {
            synchronized ( this ) {
                return new ArrayList< LogOutput >( this.outputs );
            }
        }

        protected LogProfile( ) {
            this.outputs = new ArrayList< LogOutput >( );
            this.outputNames = new ArrayList< String >( );
        }

        public LogProfile( String name,
                           LogLevel minLevel,
                           LogLevel maxLevel,
                           Boolean forwardParent,
                           String... outputNames ) {
            this.name = name;
            this.minLevel = minLevel;
            this.maxLevel = maxLevel;
            this.forwardParent = forwardParent;
            this.parentProfile = null;
            this.outputs = new ArrayList< LogOutput >( );
            this.outputNames = new ArrayList< String >( Arrays.asList( outputNames ) );
        }

        public LogProfile( String name,
                           LogLevel minLevel,
                           LogLevel maxLevel,
                           String... outputNames ) {
            this( name,
                  minLevel,
                  maxLevel,
                  null,
                  outputNames );
        }

        protected boolean isParentForwarding( ) {
            return ( ( this.parentProfile != null )
                     && !this.parentProfile.equals( this )
                     && ( this.forwardParent != null )
                     && this.forwardParent.booleanValue( ) );
        }

        protected boolean isEnabled( LogLevel level ) {
            synchronized ( this ) {
                boolean tmpIsEnabled = false;
                for ( LogOutput tmpOutput : this.outputs ) {
                    if ( tmpOutput.isEnabled( level ) ) {
                        tmpIsEnabled = ( !level.isLesser( this.minLevel ) && !level.isGreater( this.maxLevel ) );
                        break;
                    }
                }
                if ( !tmpIsEnabled && isParentForwarding( ) ) {
                    tmpIsEnabled = this.parentProfile.isEnabled( level );
                }
                return tmpIsEnabled;
            }
        }

        protected void message( int indentation,
                                LogLevel level,
                                String format,
                                Object... args ) {
            synchronized ( this ) {
                Long tmpThreadId = null;
                StackTraceElement tmpStackElement = null;
                String tmpMessage = null;
                Calendar tmpTimestamp = new GregorianCalendar( );
                if ( this.outputs.size( ) > 0 ) {
                    if ( this.isEnabled( level ) ) {
                        for ( LogOutput tmpOutput : this.outputs ) {
                            if ( tmpOutput.isEnabled( level ) ) {
                                if ( tmpThreadId == null ) {
                                    tmpThreadId = Thread.currentThread( ).getId( );
                                    StackTraceElement[ ] tmpStackTrace = Thread.currentThread( ).getStackTrace( );
                                    if ( tmpStackTrace.length >= LogProfile.STACK_DEPTH ) {
                                        tmpStackElement = tmpStackTrace[ LogProfile.STACK_DEPTH ];
                                    }
                                    tmpMessage = StringHelper.flawlessFormat( format,
                                                                              args );
                                }
                                tmpOutput.message( tmpTimestamp,
                                                   level,
                                                   tmpThreadId,
                                                   tmpStackElement,
                                                   indentation,
                                                   tmpMessage );
                            }
                        }
                    }
                }
                if ( this.isParentForwarding( ) ) {
                    if ( tmpThreadId == null ) {
                        tmpThreadId = Thread.currentThread( ).getId( );
                        StackTraceElement[ ] tmpStackTrace = Thread.currentThread( ).getStackTrace( );
                        if ( tmpStackTrace.length >= LogProfile.STACK_DEPTH ) {
                            tmpStackElement = tmpStackTrace[ LogProfile.STACK_DEPTH ];
                        }
                        tmpMessage = StringHelper.flawlessFormat( format,
                                                                  args );
                    }
                    this.parentProfile.forwardParent( tmpTimestamp,
                                                      level,
                                                      tmpThreadId,
                                                      tmpStackElement,
                                                      indentation,
                                                      tmpMessage );
                }
            }
        }

        protected void forwardParent( Calendar timestamp,
                                      LogLevel level,
                                      long threadId,
                                      StackTraceElement stackElement,
                                      int indentation,
                                      String message ) {
            if ( this.isEnabled( level ) ) {
                for ( LogOutput tmpOutput : this.outputs ) {
                    if ( tmpOutput.isEnabled( level ) ) {
                        tmpOutput.message( timestamp,
                                           level,
                                           threadId,
                                           stackElement,
                                           indentation,
                                           message );
                    }
                }
            }
            if ( this.isParentForwarding( ) ) {
                this.parentProfile.forwardParent( timestamp,
                                                  level,
                                                  threadId,
                                                  stackElement,
                                                  indentation,
                                                  message );
            }
        }

        protected void engage( LogConfig config )
            throws LogException {
            if ( ( this.name == null ) || ( this.name.length( ) == 0 ) ) {
                throw new LogException( "Profile name is %s",
                                        ( this.name == null ) ? "null"
                                                              : "empty" );
            }
            if ( this.outputNames.size( ) == 0 ) {
                this.outputs.add( config.defaultOutput );
            }
            else {
                for ( String tmpOutputName : this.outputNames ) {
                    LogOutput tmpOutputObject = null;
                    for ( LogOutput tmpOutput : config.outputs ) {
                        if ( tmpOutput.name.compareTo( tmpOutputName ) == 0 ) {
                            tmpOutputObject = tmpOutput;
                            break;
                        }
                    }
                    if ( tmpOutputObject != null ) {
                        this.outputs.add( tmpOutputObject );
                    }
                    else {
                        throw new LogException( "Output '%s' on profile '%s' not found in configuration",
                                                tmpOutputName,
                                                this.name );
                    }
                }
            }
            //
        }

        protected void setParent( LogConfig config ) {
            String tmpParentName = this.name;
            do {
                int tmpDotPosition = tmpParentName.lastIndexOf( '$' );
                if ( tmpDotPosition < 0 ) {
                    tmpDotPosition = tmpParentName.lastIndexOf( '.' );
                }
                if ( tmpDotPosition < 0 ) {
                    break;
                }
                tmpParentName = tmpParentName.substring( 0,
                                                         tmpDotPosition );
            }
            while ( ( this.parentProfile = config.profileRegistry.get( tmpParentName ) ) == null );
            if ( this.parentProfile == null ) {
                this.parentProfile = config.defaultProfile;
            }
        }
    }

    @XmlRootElement( name = "logConfig",
                     namespace = "##default" )
    @XmlType( name = "config",
              propOrder = { "defaultLayoutName",
                            "defaultOutputName",
                            "defaultProfileName",
                            "layouts",
                            "outputs",
                            "profiles" } )
    @XmlAccessorType( XmlAccessType.NONE )
    public static final class LogConfig {

        @XmlElement( required = true,
                     name = "layout" )
        protected List< LogLayout >         layouts;

        @XmlElements( { @XmlElement( required = true,
                                     name = "consoleStdout",
                                     type = LogOutputStdout.class ),
                        @XmlElement( required = true,
                                     name = "consoleStderr",
                                     type = LogOutputStderr.class ),
                        @XmlElement( required = true,
                                     name = "rollingFile",
                                     type = LogOutputFile.class ) } )
        protected List< LogOutput >         outputs;

        @XmlElement( required = true,
                     name = "profile" )
        protected List< LogProfile >        profiles;

        @XmlAttribute( required = true,
                       name = "defaultProfile" )
        protected String                    defaultProfileName;

        @XmlAttribute( required = true,
                       name = "defaultOutput" )
        protected String                    defaultOutputName;

        @XmlAttribute( required = true,
                       name = "defaultLayout" )
        protected String                    defaultLayoutName;

        @XmlTransient
        protected LogProfile                defaultProfile;

        @XmlTransient
        protected LogProfile                nullProfile;

        @XmlTransient
        protected LogOutput                 defaultOutput;

        @XmlTransient
        protected LogLayout                 defaultLayout;

        @XmlTransient
        protected Map< String, LogLayout >  layoutRegistry;

        @XmlTransient
        protected Map< String, LogOutput >  outputRegistry;

        @XmlTransient
        protected Map< String, LogProfile > profileRegistry;

        public List< LogLayout > getLayouts( ) {
            synchronized ( this ) {
                return new ArrayList< LogLayout >( this.layouts );
            }
        }

        public LogLayout getLayout( String layoutName ) {
            for ( LogLayout tmpLayout : this.layouts ) {
                if ( tmpLayout.name.compareTo( layoutName ) == 0 ) {
                    return tmpLayout;
                }
            }
            return null;
        }

        public List< LogOutput > getOutputs( ) {
            synchronized ( this ) {
                return new ArrayList< LogOutput >( this.outputs );
            }
        }

        public LogOutput getOutput( String outputName ) {
            for ( LogOutput tmpOutput : this.outputs ) {
                if ( tmpOutput.name.compareTo( outputName ) == 0 ) {
                    return tmpOutput;
                }
            }
            return null;
        }

        public List< LogProfile > getProfiles( ) {
            synchronized ( this ) {
                return new ArrayList< LogProfile >( this.profiles );
            }
        }

        public LogProfile getProfile( String loggerName,
                                      boolean inheritParent,
                                      boolean defaultToNull ) {
            LogProfile tmpProfile = null;
            while ( ( ( tmpProfile = this.profileRegistry.get( loggerName ) ) == null ) && inheritParent ) {
                int tmpDotPosition = loggerName.lastIndexOf( '$' );
                if ( tmpDotPosition < 0 ) {
                    tmpDotPosition = loggerName.lastIndexOf( '.' );
                }
                if ( tmpDotPosition < 0 ) {
                    break;
                }
                loggerName = loggerName.substring( 0,
                                                   tmpDotPosition );
            }
            if ( tmpProfile == null ) {
                tmpProfile = defaultToNull ? this.nullProfile
                                           : this.defaultProfile;
            }
            return tmpProfile;
        }

        public LogLayout getDefaultLayout( ) {
            synchronized ( this ) {
                return this.defaultLayout;
            }
        }

        public LogOutput getDefaultOutput( ) {
            synchronized ( this ) {
                return this.defaultOutput;
            }
        }

        public LogProfile getDefaultProfile( ) {
            synchronized ( this ) {
                return this.defaultProfile;
            }
        }

        public String toXml( )
            throws LogException {
            try {
                JAXBContext tmpJaxbContext = JAXBContext.newInstance( LogConfig.class );
                Marshaller tmpMarshaller = tmpJaxbContext.createMarshaller( );
                tmpMarshaller.setProperty( "jaxb.encoding",
                                           System.getProperty( "file.encoding" ) );
                ByteArrayOutputStream tmpBytes = new ByteArrayOutputStream( );
                tmpMarshaller.marshal( this,
                                       tmpBytes );
                return new String( tmpBytes.toByteArray( ) );
            }
            catch ( Throwable e ) {
                throw new LogException( e,
                                        "Erro gerando XML de configuração do log - %s: %s",
                                        e.getClass( ).getSimpleName( ),
                                        e.getMessage( ) );
            }
        }

        public static LogConfig fromXml( String xmlString )
            throws LogException {
            try {
                JAXBContext tmpJaxbContext = JAXBContext.newInstance( LogConfig.class );
                Unmarshaller tmpUnmarshaller = tmpJaxbContext.createUnmarshaller( );
                //
                ByteArrayInputStream tmpInputStream = new ByteArrayInputStream( xmlString.getBytes( ) );
                /*
                 * Forja referência aos construtores 'default',
                 * para que não sejam removidos em alguma otimização
                 */
                LogConfig tmpDefaultConfig = new LogConfig( );
                tmpDefaultConfig.getDefaultProfile( );
                LogProfile tmpDefaultProfile = new LogProfile( );
                tmpDefaultProfile.getName( );
                LogOutput tmpDefaultOutput = new LogOutputStdout( );
                tmpDefaultOutput.getName( );
                tmpDefaultOutput = new LogOutputStderr( );
                tmpDefaultOutput.getName( );
                tmpDefaultOutput = new LogOutputFile( );
                tmpDefaultOutput.getName( );
                LogLayout tmpDefaultLayout = new LogLayout( );
                tmpDefaultLayout.getName( );
                //
                LogConfig tmpConfig = (LogConfig) tmpUnmarshaller.unmarshal( tmpInputStream );
                tmpConfig.engage( );
                return tmpConfig;
            }
            catch ( JAXBException e ) {
                //catch ( Throwable e ) {
                throw new LogException( e,
                                        "Erro ao interpretar/construir mapa a partir da visao XML: %s",
                                        ProcessHelper.stackTrace( e ) );
            }
        }

        protected LogConfig( ) {
            //
            this.layoutRegistry = new HashMap< String, LogLayout >( );
            this.outputRegistry = new HashMap< String, LogOutput >( );
            this.profileRegistry = new HashMap< String, LogProfile >( );
        }

        public LogConfig( LogLayout[ ] layouts,
                          LogOutput[ ] outputs,
                          LogProfile[ ] profiles,
                          String defaultProfileName,
                          String defaultOutputName,
                          String defaultLayoutName )
            throws LogException {
            this( );
            this.layouts = new ArrayList< LogLayout >( Arrays.asList( layouts ) );
            this.outputs = new ArrayList< LogOutput >( Arrays.asList( outputs ) );
            this.profiles = new ArrayList< LogProfile >( Arrays.asList( profiles ) );
            //
            this.defaultProfileName = defaultProfileName;
            this.defaultOutputName = defaultOutputName;
            this.defaultLayoutName = defaultLayoutName;
            //
            this.engage( );
        }

        protected void engage( )
            throws LogException {
            for ( LogLayout tmpLayout : this.layouts ) {
                if ( this.layoutRegistry.get( tmpLayout.name ) != null ) {
                    throw new LogException( "Duplicated layout: %s",
                                            tmpLayout.name );
                }
                this.layoutRegistry.put( tmpLayout.name,
                                         tmpLayout );
                tmpLayout.engage( this );
                if ( this.defaultLayoutName.compareTo( tmpLayout.name ) == 0 ) {
                    this.defaultLayout = tmpLayout;
                }
            }
            if ( this.defaultLayout == null ) {
                throw new LogException( "Default layout '%s' not found",
                                        this.defaultLayoutName );
            }
            //
            for ( LogOutput tmpOutput : this.outputs ) {
                if ( this.outputRegistry.get( tmpOutput.name ) != null ) {
                    throw new LogException( "Duplicated output '%s'",
                                            tmpOutput.name );
                }
                this.outputRegistry.put( tmpOutput.name,
                                         tmpOutput );
                tmpOutput.engage( this );
                if ( this.defaultOutputName.compareTo( tmpOutput.name ) == 0 ) {
                    this.defaultOutput = tmpOutput;
                }
            }
            if ( this.defaultOutput == null ) {
                throw new LogException( "Default output '%s' not found",
                                        this.defaultProfileName );
            }
            //
            for ( LogProfile tmpProfile : this.profiles ) {
                if ( this.profileRegistry.get( tmpProfile.name ) != null ) {
                    throw new LogException( "Duplicated profile '%s'",
                                            tmpProfile.name );
                }
                this.profileRegistry.put( tmpProfile.name,
                                          tmpProfile );
                tmpProfile.engage( this );
                if ( this.defaultProfileName.compareTo( tmpProfile.name ) == 0 ) {
                    this.defaultProfile = tmpProfile;
                }
            }
            if ( this.defaultProfile == null ) {
                throw new LogException( "Default profile '%s' not found",
                                        this.defaultProfileName );
            }
            //
            for ( LogProfile tmpProfile : this.profiles ) {
                tmpProfile.setParent( this );
            }
            //
            this.nullProfile = new LogProfile( "null",
                                               LogLevel.Off,
                                               LogLevel.Off,
                                               false );
        }
    }

    public static class LogContext {

        public LogConfig getConfig( ) {
            synchronized ( this ) {
                return this.config;
            }
        }

        public LogContext setConfig( LogConfig config ) {
            synchronized ( this ) {
                this.config = config;
                for ( Map.Entry< String, Logger > tmpLoggerEntry : this.loggerRegistry.entrySet( ) ) {
                    Logger tmpLogger = tmpLoggerEntry.getValue( );
                    tmpLogger.setProfile( this.config.getProfile( tmpLoggerEntry.getKey( ),
                                                                  tmpLogger.inheritParent,
                                                                  tmpLogger.defaultToNull ) );
                }
            }
            return this;
        }

        public LogContext setLevels( LogLevel minLevel,
                                     LogLevel maxLevel ) {
            synchronized ( this ) {
                for ( LogOutput tmpOutput : this.config.outputs ) {
                    if ( minLevel != null ) {
                        tmpOutput.setMinLevel( minLevel );
                    }
                    if ( maxLevel != null ) {
                        tmpOutput.setMaxLevel( maxLevel );
                    }
                }
                for ( LogProfile tmpProfile : this.config.profiles ) {
                    if ( minLevel != null ) {
                        tmpProfile.setMinLevel( minLevel );
                    }
                    if ( maxLevel != null ) {
                        tmpProfile.setMaxLevel( maxLevel );
                    }
                }
            }
            return this;
        }

        public LogContext setMinLevel( LogLevel minLevel ) {
            return this.setLevels( minLevel,
                                   null );
        }

        public LogContext setMaxLevel( LogLevel maxLevel ) {
            return this.setLevels( null,
                                   maxLevel );
        }

        public LogContext setDetails( LogDetail... details ) {
            synchronized ( this ) {
                for ( LogLayout tmpLayout : this.config.layouts ) {
                    tmpLayout.setDetails( details );
                }
            }
            return this;
        }

        public Logger getLogger( String loggerName,
                                 boolean inheritParent,
                                 boolean defaultToNull ) {
            synchronized ( this ) {
                Logger tmpResult = this.loggerRegistry.get( loggerName );
                if ( tmpResult == null ) {
                    tmpResult = new Logger( loggerName,
                                            this,
                                            inheritParent,
                                            defaultToNull,
                                            this.config.getProfile( loggerName,
                                                                    inheritParent,
                                                                    defaultToNull ) );
                    this.loggerRegistry.put( loggerName,
                                             tmpResult );
                }
                return tmpResult;
            }
        }

        public LogContext changeLoggerName( Logger logger,
                                            String newName ) {
            synchronized ( this ) {
                if ( logger.name.compareTo( newName ) != 0 ) {
                    Logger tmpLogger = this.loggerRegistry.get( logger.name );
                    if ( ( tmpLogger != null ) && tmpLogger.equals( logger ) ) {
                        this.loggerRegistry.remove( logger.name );
                    }
                    logger.setName( newName );
                    logger.setProfile( this.config.getProfile( newName,
                                                               logger.inheritParent,
                                                               logger.defaultToNull ) );
                    this.loggerRegistry.put( newName,
                                             logger );
                }
                return this;
            }
        }

        protected LogConfig             config;

        protected Map< String, Logger > loggerRegistry;

        protected LogContext( ) {
            this.loggerRegistry = new HashMap< String, Logger >( );
            //
            try {
                this.config = this.createDefaultConfig( );
            }
            catch ( LogException e ) {
                e.printStackTrace( );
            }
        }

        protected LogConfig createDefaultConfig( )
            throws LogException {
            return new LogConfig( new LogLayout[ ] { new LogLayout( "defaultLayout",
                                                                    999,
                                                                    LogDetail.TimeStamp,
                                                                    LogDetail.LevelTag,
                                                                    LogDetail.ThreadId,
                                                                    LogDetail.OutermostClassName,
                                                                    LogDetail.LineNumber ) },
                                  new LogOutput[ ] { new LogOutputStderr( "defaultOutput",
                                                                          null,
                                                                          LogLevel.Exception,
                                                                          "defaultLayout" ) },
                                  new LogProfile[ ] { new LogProfile( "defaultProfile",
                                                                      null,
                                                                      null,
                                                                      "defaultOutput" ) },
                                  "defaultProfile",
                                  "defaultOutput",
                                  "defaultLayout" );
        }
    }

    public static class LogManager {

        protected static LogContext CONTEXT_SINGLETON = new LogContext( );

        public static LogConfig configFromXml( String xmlConfig )
            throws LogException {
            LogConfig tmpConfig = LogConfig.fromXml( xmlConfig );
            CONTEXT_SINGLETON.setConfig( tmpConfig );
            return tmpConfig;
        }

        public static LogConfig configFromStream( InputStream inputStream )
            throws LogException {
            try {
                return configFromXml( FileHelper.loadTextStream( inputStream ) );
            }
            catch ( IOException e ) {
                throw new LogException( e );
            }
        }

        public static LogConfig configFromResource( Class< ? > clazz,
                                                    String resourceName )
            throws LogException {
            try {
                return configFromXml( FileHelper.loadTextResource( clazz,
                                                                   resourceName ) );
            }
            catch ( IOException e ) {
                throw new LogException( e );
            }
        }

        public static LogConfig configFromFile( String filePath )
            throws LogException {
            try {
                return configFromXml( FileHelper.loadTextFile( filePath ) );
            }
            catch ( IOException e ) {
                throw new LogException( e );
            }
        }

        public static LogContext getContext( ) {
            return CONTEXT_SINGLETON;
        }

        public static LogConfig getConfig( ) {
            return CONTEXT_SINGLETON.getConfig( );
        }

        public static Logger getLogger( String loggerName ) {
            return CONTEXT_SINGLETON.getLogger( loggerName,
                                                true,
                                                false );
        }

        public static Logger getLogger( String loggerName,
                                        boolean inheritParent,
                                        boolean defaultToNull ) {
            return CONTEXT_SINGLETON.getLogger( loggerName,
                                                inheritParent,
                                                defaultToNull );
        }

        public static Logger getLogger( Class< ? > ownerClass ) {
            return getLogger( ownerClass.getName( ) );
        }

        public static Logger getLogger( Class< ? > ownerClass,
                                        boolean inheritParent,
                                        boolean defaultToNull ) {
            return getLogger( ownerClass.getName( ),
                              inheritParent,
                              defaultToNull );
        }

        public static Logger getDesenvLogger( String loggerName ) {
            return CONTEXT_SINGLETON.getLogger( loggerName,
                                                false,
                                                true );
        }

        public static Logger getDesenvLogger( Class< ? > ownerClass ) {
            return getDesenvLogger( ownerClass.getName( ) );
        }
        
        public static Logger getSystemLogger( ) {
            return CONTEXT_SINGLETON.getLogger( "",
                                                false,
                                                false );
        }

        public static void setMaxLevel( LogLevel maxLevel ) {
            CONTEXT_SINGLETON.setMaxLevel( maxLevel );
        }

        public static void setMinLevel( LogLevel minLevel ) {
            CONTEXT_SINGLETON.setMinLevel( minLevel );
        }

        public static void setDetails( LogDetail... details ) {
            CONTEXT_SINGLETON.setDetails( details );
        }
    }
}
