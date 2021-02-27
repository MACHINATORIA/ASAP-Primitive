package asap.primitive.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import asap.primitive.string.StringHelper;
import asap.primitive.string.RegexHelper.RegexList;

public class FileHelper {

    protected static String              RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE_FORMAT = "Recurso '%s' não encontrado";

    protected static int                 DEFAULT_FILE_NAME_SHRINK_LENGTH             = 50;

    protected static int                 DEFAULT_READ_WRITE_BUFFER_LENGTH            = 1024;

    public static final Pattern          PATH_SEPARATOR_PATTERN                      = Pattern.compile( "[\\\\/]+" );

    public static final String           NORMALIZED_PATH_SEPARATOR                   = "/";

    public static final String           TIME_STAMP_REGEX                            = "\\d{8}-\\d{6}";

    public static final SimpleDateFormat TIME_STAMP_FORMATER                         = new SimpleDateFormat( "yyyyMMdd-HHmmss" );

    public static String normalizePath( String path ) {
        Matcher tmpMatcher = PATH_SEPARATOR_PATTERN.matcher( path );
        return tmpMatcher.replaceAll( NORMALIZED_PATH_SEPARATOR );
    }

    public static File testDirectoryPath( String directoryPath )
        throws IOException {
        File tmpDirectoryPath = new File( directoryPath );
        if ( !tmpDirectoryPath.exists( ) || !tmpDirectoryPath.isDirectory( ) ) {
            throw new IOException( String.format( "O caminho '%s' não existe ou não é um diretório",
                                                  StringHelper.nullText( directoryPath ) ) );
        }
        return tmpDirectoryPath;
    }

    public static List< String > getFileListNames( List< File > fileList ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( File tmpFile : fileList ) {
            tmpResult.add( tmpFile.getName( ) );
        }
        return tmpResult;
    }

    public static List< String > getFileListPaths( List< File > fileList ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( File tmpFile : fileList ) {
            tmpResult.add( tmpFile.getAbsolutePath( ) );
        }
        return tmpResult;
    }

    public static List< String > getFileNames( String baseDirectoryPath,
                                               String... fileNameRegexes ) {
        return getSubitemTexts( false,
                                false,
                                baseDirectoryPath,
                                fileNameRegexes );
    }

    public static List< String > getFileNames( File baseDirectory,
                                               String... fileNameRegexes ) {
        return getSubitemTexts( false,
                                false,
                                baseDirectory,
                                fileNameRegexes );
    }

    public static List< String > getFilePaths( String baseDirectoryPath,
                                               String... fileNameRegexes ) {
        return getSubitemTexts( true,
                                false,
                                baseDirectoryPath,
                                fileNameRegexes );
    }

    public static List< String > getFilePaths( File baseDirectory,
                                               String... fileNameRegexes ) {
        return getSubitemTexts( true,
                                false,
                                baseDirectory,
                                fileNameRegexes );
    }

    public static List< String > getDirNames( String baseDirectoryPath,
                                              String... subDirNameRegexes ) {
        return getSubitemTexts( false,
                                true,
                                baseDirectoryPath,
                                subDirNameRegexes );
    }

    public static List< String > getDirNames( File baseDirectory,
                                              String... subDirNameRegexes ) {
        return getSubitemTexts( false,
                                true,
                                baseDirectory,
                                subDirNameRegexes );
    }

    public static List< String > getDirPaths( String baseDirectoryPath,
                                              String... subDirNameRegexes ) {
        return getSubitemTexts( true,
                                true,
                                baseDirectoryPath,
                                subDirNameRegexes );
    }

    public static List< String > getDirPaths( File baseDirectory,
                                              String... subDirNameRegexes ) {
        return getSubitemTexts( true,
                                true,
                                baseDirectory,
                                subDirNameRegexes );
    }

    public static List< String > getSubitemTexts( boolean absolutePath,
                                                  boolean enumDirectories,
                                                  String baseDirectoryPath,
                                                  String... subitemNameRegexes ) {
        return getSubitemTexts( absolutePath,
                                enumDirectories,
                                new File( baseDirectoryPath ),
                                subitemNameRegexes );
    }

    public static List< String > getSubitemTexts( boolean absolutePath,
                                                  boolean enumDirectories,
                                                  File baseDirectory,
                                                  String... subitemNameRegexes ) {
        List< String > tmpResult = new ArrayList< String >( );
        for ( File tmpSubitem : enumerateSubitens( baseDirectory,
                                                   0,
                                                   enumDirectories,
                                                   false,
                                                   null,
                                                   subitemNameRegexes ) ) {
            tmpResult.add( absolutePath ? tmpSubitem.getAbsolutePath( )
                                        : tmpSubitem.getName( ) );
        }
        return tmpResult;
    }

    public static List< File > getFiles( String baseDirectoryPath,
                                         String... fileNameRegexes ) {
        return enumerateSubitens( new File( baseDirectoryPath ),
                                  0,
                                  false,
                                  false,
                                  null,
                                  fileNameRegexes );
    }

    public static List< File > getFiles( File baseDirectory,
                                         String... fileNameRegexes ) {
        return enumerateSubitens( baseDirectory,
                                  0,
                                  false,
                                  false,
                                  null,
                                  fileNameRegexes );
    }

    public static List< File > getDirs( String baseDirectoryPath,
                                        String... subDirNameRegexes ) {
        return enumerateSubitens( new File( baseDirectoryPath ),
                                  0,
                                  true,
                                  false,
                                  null,
                                  subDirNameRegexes );
    }

    public static List< File > getDirs( File baseDirectory,
                                        String... subDirNameRegexes ) {
        return enumerateSubitens( baseDirectory,
                                  0,
                                  true,
                                  false,
                                  null,
                                  subDirNameRegexes );
    }

    public static List< String > getRecursiveFilePaths( String baseDirectoryPath,
                                                        String... fileNamePatternRegexes ) {
        return getRecursiveFilePaths( new File( baseDirectoryPath ),
                                      fileNamePatternRegexes );
    }

    public static List< String > getRecursiveFilePaths( File baseDirectory,
                                                        String... fileNamePatternRegexes ) {
        return getFileListPaths( getRecursiveFiles( baseDirectory,
                                                    fileNamePatternRegexes ) );
    }

    public static List< File > getRecursiveFiles( String baseDirectoryPath,
                                                  String... fileNamePatternRegexes ) {
        return getRecursiveFiles( new File( baseDirectoryPath ),
                                  fileNamePatternRegexes );
    }

    public static List< File > getRecursiveFiles( File baseDirectory,
                                                  String... fileNamePatternRegexes ) {
        return enumerateSubitens( baseDirectory,
                                  -1,
                                  false,
                                  true,
                                  null,
                                  fileNamePatternRegexes );
    }

    private static void enumerateSubitens_recursion( List< File > resultList,
                                                     String relativePath,
                                                     File directory,
                                                     int recursionCount,
                                                     int maxRecursion,
                                                     boolean enumDirectories,
                                                     boolean searchNested,
                                                     RegexList excludeRegexes,
                                                     RegexList includeRegexes ) {
        if ( directory.isDirectory( ) && directory.canRead( ) ) {
            for ( File tmpSubitem : directory.listFiles( ) ) {
                String tmpSubitemPath = StringHelper.concatenate( relativePath,
                                                                  NORMALIZED_PATH_SEPARATOR,
                                                                  tmpSubitem.getName( ) );
                if ( tmpSubitem.isDirectory( ) ) {
                    boolean tmpMatches = ( enumDirectories
                                           && ( ( excludeRegexes == null )
                                                || !excludeRegexes.matches( tmpSubitemPath ) )
                                           && ( ( includeRegexes == null )
                                                || includeRegexes.matches( tmpSubitemPath ) ) );
                    if ( tmpMatches ) {
                        resultList.add( tmpSubitem );
                    }
                    if ( ( !tmpMatches || searchNested )
                         && ( ( maxRecursion < 0 ) || ( recursionCount < maxRecursion ) ) ) {
                        enumerateSubitens_recursion( resultList,
                                                     tmpSubitemPath,
                                                     tmpSubitem,
                                                     ( recursionCount + 1 ),
                                                     maxRecursion,
                                                     enumDirectories,
                                                     searchNested,
                                                     excludeRegexes,
                                                     includeRegexes );
                    }
                }
                else if ( !enumDirectories
                          && ( ( excludeRegexes == null ) || !excludeRegexes.matches( tmpSubitemPath ) )
                          && ( ( includeRegexes == null ) || includeRegexes.matches( tmpSubitemPath ) ) ) {
                    resultList.add( tmpSubitem );
                }
            }
        }
    }

    public static List< File > enumerateSubitens( File baseDirectory,
                                                  int maxRecursion,
                                                  boolean enumDirectories,
                                                  boolean searchNested,
                                                  String[ ] excludeRegexes,
                                                  String... includeRegexes ) {
        List< File > tmpResult = new ArrayList< File >( );
        RegexList tmpExcludeRegexes = ( ( excludeRegexes == null ) || ( excludeRegexes.length == 0 ) ) ? null
                                                                                                       : new RegexList( excludeRegexes );
        RegexList tmpIncludeRegexes = ( includeRegexes.length == 0 ) ? null
                                                                     : new RegexList( includeRegexes );
        enumerateSubitens_recursion( tmpResult,
                                     "",
                                     baseDirectory,
                                     0,
                                     maxRecursion,
                                     enumDirectories,
                                     searchNested,
                                     tmpExcludeRegexes,
                                     tmpIncludeRegexes );
        return tmpResult;
    }

    public static List< File > filterByDir( List< File > fileList,
                                            String... dirNameRegexes )
        throws IOException {
        return filterByDirAndName( fileList,
                                   new RegexList( dirNameRegexes ),
                                   null );
    }

    public static List< File > filterByName( List< File > fileList,
                                             String... fileNameRegexes )
        throws IOException {
        return filterByDirAndName( fileList,
                                   null,
                                   new RegexList( fileNameRegexes ) );
    }

    public static List< File > filterByDirAndName( List< File > fileList,
                                                   RegexList dirPathRegexList,
                                                   RegexList fileNameRegexList )
        throws IOException {
        List< File > tmpResult = new ArrayList< File >( );
        for ( File tmpFile : fileList ) {
            if ( dirPathRegexList != null ) {
                String tmpDirPath = normalizePath( tmpFile.getParentFile( ).getCanonicalPath( ) );
                if ( !dirPathRegexList.matches( tmpDirPath ) ) {
                    continue;
                }
            }
            if ( fileNameRegexList != null ) {
                if ( !fileNameRegexList.matches( tmpFile.getName( ) ) ) {
                    continue;
                }
            }
            tmpResult.add( tmpFile );
        }
        return tmpResult;
    }

    public static List< File > filterDuplicated( List< File > fileList,
                                                 File duplicatesPath ) {
        List< File > tmpResult = new ArrayList< File >( );
        List< String > tmpDuplicatesNames = getFileNames( duplicatesPath );
        for ( File tmpFile : fileList ) {
            if ( tmpDuplicatesNames.contains( tmpFile.getName( ) ) ) {
                tmpResult.add( tmpFile );
            }
        }
        return tmpResult;
    }

    public static int getMaxSubdirNumber( String directoryPath ) {
        return getHighestSubdirNumber( 0,
                                       directoryPath,
                                       null,
                                       null );
    }

    public static int getHighestSubdirNumber( int startingNumber,
                                              String directoryPath ) {
        return getHighestSubdirNumber( startingNumber,
                                       directoryPath,
                                       null,
                                       null );
    }

    public static int getHighestSubdirNumber( String directoryPath,
                                              String subDirNameRegex ) {
        return getHighestSubdirNumber( 0,
                                       directoryPath,
                                       subDirNameRegex,
                                       null );
    }

    public static int getHighestSubdirNumber( int startingNumber,
                                              String directoryPath,
                                              String subDirNameRegex,
                                              Integer subDirNumberGroup ) {
        int tmpResult = startingNumber;
        String tmpNameRegex = ( ( subDirNameRegex == null ) ? "(\\d+)"
                                                            : subDirNameRegex );
        List< String > tmpSubDirs = FileHelper.getDirNames( directoryPath,
                                                            tmpNameRegex );
        for ( String tmpSubDir : tmpSubDirs ) {
            int tmpNumber = Integer.parseInt( tmpSubDir.replace( tmpNameRegex,
                                                                 ( ( subDirNameRegex == null )
                                                                   || ( subDirNumberGroup == null ) ) ? "$1"
                                                                                                      : String.format( "$%d",
                                                                                                                       subDirNumberGroup ) ) );
            if ( tmpNumber > tmpResult ) {
                tmpResult = tmpNumber;
            }
        }
        return tmpResult;
    }

    public static void copyFile( String sourcePath,
                                 String targetPath )
        throws IOException {
        copyFile( new File( sourcePath ),
                  new File( targetPath ) );
    }

    public static void copyFile( File sourceFile,
                                 File targetFile )
        throws IOException {
        FileInputStream tmpSourceFileStream = new FileInputStream( sourceFile );
        try {
            FileOutputStream tmpTargetFileStream = new FileOutputStream( targetFile );
            try {
                int tmpReadedBytes;
                byte[ ] tmpReadBuffer = new byte[ DEFAULT_READ_WRITE_BUFFER_LENGTH ];
                while ( tmpSourceFileStream.available( ) > 0 ) {
                    tmpReadedBytes = tmpSourceFileStream.read( tmpReadBuffer );
                    tmpTargetFileStream.write( tmpReadBuffer,
                                               0,
                                               tmpReadedBytes );
                }
            }
            finally {
                tmpTargetFileStream.close( );
            }
        }
        finally {
            tmpSourceFileStream.close( );
        }
    }

    public static void moveFile( String sourcePath,
                                 String targetPath )
        throws IOException {
        moveFile( new File( sourcePath ),
                  new File( targetPath ) );
    }

    public static void moveFile( File sourceFile,
                                 File targetFile )
        throws IOException {
        moveFile( false,
                  sourceFile,
                  targetFile );
    }

    public static void moveFile( boolean copyAndDelete,
                                 File sourceFile,
                                 File targetFile )
        throws IOException {
        if ( copyAndDelete ) {
            File tmpTargetFile = targetFile;
            if ( targetFile.exists( ) && targetFile.isDirectory( ) ) {
                tmpTargetFile = new File( targetFile,
                                          sourceFile.getName( ) );
            }
            FileInputStream tmpInputStream = new FileInputStream( sourceFile );
            try {
                FileChannel tmpInputChannel = tmpInputStream.getChannel( );
                try {
                    FileOutputStream tmpOutputStream = new FileOutputStream( tmpTargetFile );
                    try {
                        FileChannel tmpOutputChannel = tmpOutputStream.getChannel( );
                        try {
                            tmpInputChannel.transferTo( 0,
                                                        tmpInputChannel.size( ),
                                                        tmpOutputChannel );
                            if ( sourceFile.exists( ) ) {
                                sourceFile.delete( );
                            }
                        }
                        finally {
                            tmpOutputChannel.close( );
                        }
                    }
                    finally {
                        tmpOutputStream.close( );
                    }
                }
                finally {
                    tmpInputChannel.close( );
                }
            }
            finally {
                tmpInputStream.close( );
            }
        }
        else {
            Path tmpSourceFilePath = sourceFile.getAbsoluteFile( ).toPath( );
            Path tmpTargetDirPath = targetFile.getAbsoluteFile( ).toPath( );
            Files.move( tmpSourceFilePath,
                        tmpTargetDirPath );
        }
    }

    public static void copyResourceFile( Class< ? > contextClass,
                                         String resourcePath,
                                         String targetPath )
        throws IOException {
        ClassLoader tmpClassLoader = ( contextClass != null ) ? contextClass.getClassLoader( )
                                                              : Thread.currentThread( ).getContextClassLoader( );
        InputStream tmpSourceStream = tmpClassLoader.getResourceAsStream( resourcePath );
        if ( tmpSourceStream != null ) {
            try {
                FileOutputStream tmpTargetStream = new FileOutputStream( new File( targetPath ) );
                try {
                    int tmpReadedBytes;
                    byte[ ] tmpReadBuffer = new byte[ DEFAULT_READ_WRITE_BUFFER_LENGTH ];
                    while ( tmpSourceStream.available( ) > 0 ) {
                        tmpReadedBytes = tmpSourceStream.read( tmpReadBuffer );
                        tmpTargetStream.write( tmpReadBuffer,
                                               0,
                                               tmpReadedBytes );
                    }
                }
                finally {
                    tmpTargetStream.close( );
                }
            }
            finally {
                tmpSourceStream.close( );
            }
        }
    }

    public static byte[ ] getStreamHash( InputStream inputStream )
        throws IOException,
            NoSuchAlgorithmException {
        MessageDigest tmpSha1Digest = MessageDigest.getInstance( "SHA-1" );
        byte[ ] tmpReadBuffer = new byte[ DEFAULT_READ_WRITE_BUFFER_LENGTH ];
        BufferedInputStream tmpBufferedStream = ( inputStream instanceof BufferedInputStream ) ? (BufferedInputStream) inputStream
                                                                                               : new BufferedInputStream( inputStream );
        while ( tmpBufferedStream.available( ) > 0 ) {
            int tmpReadCount = tmpBufferedStream.read( tmpReadBuffer );
            if ( tmpReadCount > 0 ) {
                tmpSha1Digest.update( tmpReadBuffer,
                                      0,
                                      tmpReadCount );
            }
        }
        return tmpSha1Digest.digest( );
    }

    public static byte[ ] getFileHash( String filePath )
        throws IOException,
            NoSuchAlgorithmException {
        return getFileHash( new File( filePath ) );
    }

    public static byte[ ] getFileHash( File file )
        throws IOException,
            NoSuchAlgorithmException {
        FileInputStream tmpFileStream = new FileInputStream( file );
        try {
            return getStreamHash( tmpFileStream );
        }
        finally {
            tmpFileStream.close( );
        }
    }

    public static String getResourcePath( Class< ? > clazz ) {
        return clazz.getPackage( ).getName( ).replace( ".",
                                                       NORMALIZED_PATH_SEPARATOR );
    }

    public static String getResourcePath( Class< ? > clazz,
                                          String resourceName ) {
        return StringHelper.concatenate( getResourcePath( clazz ),
                                         NORMALIZED_PATH_SEPARATOR,
                                         resourceName );
    }

    public static byte[ ] getResourceHash( String resourcePath )
        throws IOException,
            NoSuchAlgorithmException {
        InputStream tmpResourceStream = Thread.currentThread( ).getContextClassLoader( ).getResourceAsStream( resourcePath );
        if ( tmpResourceStream == null ) {
            throw new FileNotFoundException( String.format( RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE_FORMAT,
                                                            resourcePath ) );
        }
        try {
            return getStreamHash( tmpResourceStream );
        }
        finally {
            tmpResourceStream.close( );
        }
    }

    public static byte[ ] loadBinStream( InputStream inputStream )
        throws IOException {
        //
        int tmpResultSize = ( ( ( inputStream.available( ) / DEFAULT_READ_WRITE_BUFFER_LENGTH ) + 1 )
                              * DEFAULT_READ_WRITE_BUFFER_LENGTH );
        ByteArrayOutputStream tmpBytes = new ByteArrayOutputStream( tmpResultSize );
        byte[ ] tmpReadBuffer = new byte[ DEFAULT_READ_WRITE_BUFFER_LENGTH ];
        BufferedInputStream tmpBufferedStream = ( inputStream instanceof BufferedInputStream ) ? (BufferedInputStream) inputStream
                                                                                               : new BufferedInputStream( inputStream );
        while ( tmpBufferedStream.available( ) > 0 ) {
            int tmpReadCount = tmpBufferedStream.read( tmpReadBuffer );
            if ( tmpReadCount > 0 ) {
                tmpBytes.write( tmpReadBuffer,
                                0,
                                tmpReadCount );
            }
        }
        return tmpBytes.toByteArray( );
    }

    public static byte[ ] loadBinResource( Class< ? > clazz,
                                           String resourceName )
        throws IOException {
        return loadBinResource( getResourcePath( clazz,
                                                 resourceName ) );
    }

    public static byte[ ] loadBinResource( String resourcePath )
        throws IOException {
        InputStream tmpResourceStream = Thread.currentThread( ).getContextClassLoader( ).getResourceAsStream( resourcePath );
        if ( tmpResourceStream == null ) {
            throw new FileNotFoundException( String.format( RESOURCE_NOT_FOUND_EXCEPTION_MESSAGE_FORMAT,
                                                            resourcePath ) );
        }
        try {
            return loadBinStream( tmpResourceStream );
        }
        finally {
            tmpResourceStream.close( );
        }
    }

    public static byte[ ] loadBinFile( String filePath )
        throws IOException {
        return loadBinFile( new File( filePath ) );
    }

    public static byte[ ] loadBinFile( File file )
        throws IOException {
        FileInputStream tmpFileStream = new FileInputStream( file );
        try {
            return loadBinStream( tmpFileStream );
        }
        finally {
            tmpFileStream.close( );
        }
    }

    public static String loadTextStream( InputStream inputStream )
        throws IOException {
        return new String( loadBinStream( inputStream ) );
    }

    public static String loadTextResource( String resourcePath )
        throws IOException {
        return new String( loadBinResource( resourcePath ) );
    }

    public static String loadTextResource( Class< ? > clazz,
                                           String resourceName )
        throws IOException {
        return new String( loadBinResource( clazz,
                                            resourceName ) );
    }

    public static String loadTextFile( String filePath )
        throws IOException {
        return new String( loadBinFile( filePath ) );
    }

    public static String loadTextFile( File file )
        throws IOException {
        return new String( loadBinFile( file ) );
    }

    public static class StreamFinder {

        public InputStream stream;

        public String      path;

        public String      name;

        public StreamFinder( File file )
            throws IOException {
            this( (Class< ? >) null,
                  file.getParentFile( ),
                  file.getName( ) );
        }

        public StreamFinder( String fileNameFormat,
                             Object... fileNameArgs )
            throws IOException {
            this( null,
                  null,
                  fileNameFormat,
                  fileNameArgs );
        }

        public StreamFinder( File directory,
                             String fileNameFormat,
                             Object... fileNameArgs )
            throws IOException {
            this( null,
                  directory,
                  fileNameFormat,
                  fileNameArgs );
        }

        public StreamFinder( Class< ? > resourceClass,
                             String fileNameFormat,
                             Object... fileNameArgs )
            throws IOException {
            this( resourceClass,
                  null,
                  fileNameFormat,
                  fileNameArgs );
        }

        public StreamFinder( Class< ? > resourceClass,
                             File directory,
                             String fileNameFormat,
                             Object... fileNameArgs )
            throws IOException {
            this.stream = null;
            this.path = null;
            this.name = String.format( fileNameFormat,
                                       fileNameArgs );
            File tmpFile = new File( ( directory != null ) ? directory
                                                           : new File( System.getProperty( "user.dir" ) ),
                                     this.name );
            if ( ( tmpFile != null ) && tmpFile.exists( ) ) {
                this.stream = new FileInputStream( tmpFile );
                this.path = tmpFile.getAbsolutePath( );
            }
            else {
                String tmpResourcePath = ( resourceClass == null ) ? this.name
                                                                   : getResourcePath( resourceClass,
                                                                                      this.name );
                this.stream = ( ( resourceClass != null ) ? resourceClass.getClassLoader( )
                                                          : Thread.currentThread( ).getContextClassLoader( ) ).getResourceAsStream( tmpResourcePath );
                if ( this.stream != null ) {
                    this.path = StringHelper.concatenate( "res://",
                                                          tmpResourcePath );
                }
                else {
                    throw new IOException( String.format( "No file or resource found with name '%s'",
                                                          this.name ) );
                }
            }
        }
    }

    public static void saveBinFile( String filePath,
                                    byte[ ] fileContent )
        throws IOException {
        saveBinFile( new File( filePath ),
                     fileContent );
    }

    public static void saveBinFile( File file,
                                    byte[ ] fileContent )
        throws IOException {
        FileOutputStream tmpFileStream = new FileOutputStream( file );
        int tmpWriteOffset = 0;
        int tmpWriteLength;
        try {
            while ( tmpWriteOffset < fileContent.length ) {
                if ( ( tmpWriteOffset + DEFAULT_READ_WRITE_BUFFER_LENGTH ) <= fileContent.length ) {
                    tmpWriteLength = DEFAULT_READ_WRITE_BUFFER_LENGTH;
                }
                else {
                    tmpWriteLength = ( fileContent.length - tmpWriteOffset );
                }
                tmpFileStream.write( fileContent,
                                     tmpWriteOffset,
                                     tmpWriteLength );
                tmpWriteOffset += tmpWriteLength;
            }
        }
        finally {
            tmpFileStream.close( );
        }
    }

    public static void saveTextFile( String filePath,
                                     String fileContent )
        throws IOException {
        saveBinFile( filePath,
                     fileContent.getBytes( ) );
    }

    public static void saveTextFile( File file,
                                     String fileContent )
        throws IOException {
        saveBinFile( file,
                     fileContent.getBytes( ) );
    }

    public static void appendBinFile( String filePath,
                                      byte[ ] fileContent )
        throws IOException {
        FileOutputStream tmpFileStream = new FileOutputStream( filePath,
                                                               true );
        try {
            BufferedOutputStream tmpBufferedStream = new BufferedOutputStream( tmpFileStream );
            try {
                tmpBufferedStream.write( fileContent );
            }
            finally {
                tmpBufferedStream.close( );
            }
        }
        finally {
            tmpFileStream.close( );
        }
    }

    public static void appendTextFile( String filePath,
                                       String fileContent )
        throws IOException {
        appendBinFile( filePath,
                       fileContent.getBytes( ) );
    }

    public static String getFileFolder( File file ) {
        return getFileFolder( file.getAbsolutePath( ) );
    }

    public static String getFileFolder( String filePath ) {
        String tmpFilePath = null;
        //    try {
        //        tmpFilePath = new File( filePath ).getCanonicalPath( );
        //    }
        //    catch ( IOException e ) {
        //    }
        if ( tmpFilePath == null ) {
            tmpFilePath = normalizePath( filePath );
        }
        int tmpLastSlashPos = tmpFilePath.lastIndexOf( File.separator );
        if ( tmpLastSlashPos < 0 ) {
            tmpLastSlashPos = 0;
        }
        return tmpFilePath.substring( 0,
                                      tmpLastSlashPos );
    }

    public static String getFileName( File file ) {
        return getFileName( file.getName( ) );
    }

    public static String getFileName( String filePath ) {
        String tmpFilePath = normalizePath( filePath );
        int tmpLastSlashPos = tmpFilePath.lastIndexOf( File.separator );
        if ( tmpLastSlashPos < 0 ) {
            tmpLastSlashPos = 0;
        }
        else {
            ++tmpLastSlashPos;
        }
        int tmpLastDotPos = tmpFilePath.lastIndexOf( "." );
        if ( ( tmpLastDotPos < 0 ) || ( tmpLastDotPos < tmpLastSlashPos ) ) {
            tmpLastDotPos = tmpFilePath.length( );
        }
        return tmpFilePath.substring( tmpLastSlashPos,
                                      tmpLastDotPos );
    }

    public static String getFileExtension( File file ) {
        return getFileExtension( file.getName( ) );
    }

    public static String getFileExtension( String filePath ) {
        int tmpLastSlashPos = filePath.lastIndexOf( File.separator );
        int tmpLastDotPos = filePath.lastIndexOf( "." );
        return ( tmpLastSlashPos >= tmpLastDotPos ) ? ""
                                                    : filePath.substring( tmpLastDotPos + 1 );
    }

    public static String removeFileExtension( String filePath ) {
        int tmpLastSlashPos = filePath.lastIndexOf( File.separator );
        int tmpLastDotPos = filePath.lastIndexOf( "." );
        return ( tmpLastSlashPos >= tmpLastDotPos ) ? filePath
                                                    : filePath.substring( 0,
                                                                          tmpLastDotPos );
    }

    public static String changeFileExtension( String originalFilePath,
                                              String newFileExtension ) {
        StringBuilder tmpResult = new StringBuilder( removeFileExtension( originalFilePath ) );
        tmpResult.append( "." );
        tmpResult.append( newFileExtension );
        return tmpResult.toString( );
    }

    public static String shrinkFilePath( String filePath ) {
        return shrinkFilePath( filePath,
                               true,
                               DEFAULT_FILE_NAME_SHRINK_LENGTH );
    }

    public static String shrinkFilePath( String filePath,
                                         boolean hideLeft ) {
        return shrinkFilePath( filePath,
                               hideLeft,
                               DEFAULT_FILE_NAME_SHRINK_LENGTH );
    }

    public static String shrinkFilePath( String filePath,
                                         int maxStringLength ) {
        return shrinkFilePath( filePath,
                               true,
                               maxStringLength );
    }

    public static String shrinkFilePath( String filePath,
                                         boolean hideLeft,
                                         int maxStringLength ) {
        if ( maxStringLength < 1 ) {
            return "";
        }
        else {
            String tmpFilePath;
            tmpFilePath = normalizePath( filePath );
            if ( tmpFilePath.length( ) < maxStringLength ) {
                return filePath;
            }
            switch ( maxStringLength ) {
                case 1:
                    return ".";
                case 2:
                    return "..";
                case 3:
                    return "...";
                default:
                    break;
            }
            maxStringLength -= 3;
            StringBuilder tmpResult = new StringBuilder( );
            int tmpLastSlashPos = tmpFilePath.lastIndexOf( File.separator );
            if ( tmpLastSlashPos < 0 ) {
                tmpLastSlashPos = 0;
            }
            else {
                ++tmpLastSlashPos;
            }
            int tmpNameLength = ( tmpFilePath.length( ) - tmpLastSlashPos );
            if ( tmpNameLength >= maxStringLength ) {
                tmpResult.append( filePath.substring( tmpLastSlashPos,
                                                      ( tmpLastSlashPos + maxStringLength ) ) );
                tmpResult.append( "..." );
            }
            else {
                if ( hideLeft ) {
                    tmpResult.append( "..." );
                    tmpResult.append( filePath.substring( ( tmpFilePath.length( ) - maxStringLength ),
                                                          tmpFilePath.length( ) ) );
                }
                else {
                    tmpResult.append( filePath.subSequence( 0,
                                                            ( maxStringLength - tmpNameLength - 1 ) ) );
                    tmpResult.append( "..." );
                    tmpResult.append( File.separator );
                    tmpResult.append( filePath.substring( ( tmpFilePath.length( ) - tmpNameLength ),
                                                          tmpFilePath.length( ) ) );
                }
            }
            return tmpResult.toString( );
        }
    }

    private static String TIME_STAMP_PREPEND_MATCH_REGEX   = String.format( "\\A%s-.+\\z",
                                                                            TIME_STAMP_REGEX );

    private static String TIME_STAMP_PREPEND_REPLACE_REGEX = String.format( "\\A%s-",
                                                                            TIME_STAMP_REGEX );

    private static String TIME_STAMP_APPEND_MATCH_REGEX    = String.format( "\\A.+-%s\\z",
                                                                            TIME_STAMP_REGEX );

    private static String TIME_STAMP_APPEND_REPLACE_REGEX  = String.format( "-%s\\z",
                                                                            TIME_STAMP_REGEX );

    public static String prependFileNameTimeStamp( String originalFilePath ) {
        return prependFileNameTimeStamp( originalFilePath,
                                         new Date( ) );
    }

    public static String prependFileNameTimeStamp( String originalFilePath,
                                                   Date timeStamp ) {
        return insertFileNameTimeStamp( originalFilePath,
                                        timeStamp,
                                        true );
    }

    public static String appendFileNameTimeStamp( String originalFilePath ) {
        return appendFileNameTimeStamp( originalFilePath,
                                        new Date( ) );
    }

    public static String appendFileNameTimeStamp( String originalFilePath,
                                                  Date timeStamp ) {
        return insertFileNameTimeStamp( originalFilePath,
                                        timeStamp,
                                        false );
    }

    public static String insertFileNameTimeStamp( String originalFilePath,
                                                  Date timeStamp,
                                                  boolean prepend ) {
        String tmpFileFolder = getFileFolder( originalFilePath );
        String tmpFileName = getFileName( originalFilePath );
        String tmpFileExtension = getFileExtension( originalFilePath );
        if ( tmpFileName.matches( prepend ? TIME_STAMP_PREPEND_MATCH_REGEX
                                          : TIME_STAMP_APPEND_MATCH_REGEX ) ) {
            tmpFileName = tmpFileName.replaceAll( prepend ? TIME_STAMP_PREPEND_REPLACE_REGEX
                                                          : TIME_STAMP_APPEND_REPLACE_REGEX,
                                                  "" );
        }
        String tmpTimeStamp = TIME_STAMP_FORMATER.format( timeStamp );
        StringBuilder tmpResult = new StringBuilder( );
        if ( tmpFileFolder.length( ) > 0 ) {
            tmpResult.append( tmpFileFolder );
            tmpResult.append( File.separator );
        }
        tmpResult.append( prepend ? tmpTimeStamp
                                  : tmpFileName );
        tmpResult.append( "-" );
        tmpResult.append( prepend ? tmpFileName
                                  : tmpTimeStamp );
        if ( tmpFileExtension.length( ) > 0 ) {
            tmpResult.append( "." );
            tmpResult.append( tmpFileExtension );
        }
        return tmpResult.toString( );
    }

    public static final String DEFAULT_BACKUP_EXTENSION = "bkp";

    public static String makeBackupFileName( String originalFilePath ) {
        return makeBackupFileName( originalFilePath,
                                   null,
                                   null );
    }

    public static String makeBackupFileName( String originalFilePath,
                                             Date timeStamp ) {
        return makeBackupFileName( originalFilePath,
                                   null,
                                   timeStamp );
    }

    public static String makeBackupFileName( String originalFilePath,
                                             String newFileExtension ) {
        return makeBackupFileName( originalFilePath,
                                   newFileExtension,
                                   null );
    }

    public static String makeBackupFileName( String originalFilePath,
                                             String newFileExtension,
                                             Date timeStamp ) {
        StringBuilder tmpResult = new StringBuilder( originalFilePath );
        if ( timeStamp == null ) {
            timeStamp = new Date( );
        }
        tmpResult.append( "." );
        String tmpTimeStamp = TIME_STAMP_FORMATER.format( timeStamp );
        tmpResult.append( tmpTimeStamp );
        if ( newFileExtension == null ) {
            newFileExtension = DEFAULT_BACKUP_EXTENSION;
        }
        tmpResult.append( "." );
        tmpResult.append( newFileExtension );
        return tmpResult.toString( );
    }

    public static boolean ensureDirectoryExists( File directory ) {
        boolean tmpResult = false;
        File tmpParent = directory.getParentFile( );
        if ( tmpParent != null ) {
            tmpResult = ensureDirectoryExists( tmpParent );
        }
        if ( directory.exists( ) ) {
            if ( directory.isDirectory( ) ) {
                tmpResult = true;
            }
            else {
                tmpResult = ( directory.delete( ) && directory.mkdir( ) );
            }
        }
        else {
            tmpResult = directory.mkdir( );
        }
        return tmpResult;
    }
}
